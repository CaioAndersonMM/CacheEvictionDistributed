package Src.Server.Impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Src.MenuLogger;

public class LocationServerImpl extends UnicastRemoteObject implements LocationServerInterface {
    private int porta;
    private String host;
    private ServerSocket serverLocation;
    private List<String> proxies;

    public LocationServerImpl(int porta, String host) throws RemoteException {
        this.porta = porta;
        this.host = host;
        this.proxies = new ArrayList<>();
        rodar();
    }

    @Override
    public synchronized void registerProxy(String proxyName) throws RemoteException {
        if (!proxies.contains(proxyName)) {
            proxies.add(proxyName);
            System.out.println("Proxy registrado: " + proxyName);
            MenuLogger.escreverLog("Proxy registrado: " + proxyName);
            notificarProxies(proxyName, true);
        }
    }

    @Override
    public synchronized void removerProxy(String proxyName) throws RemoteException {
        if (proxies.remove(proxyName)) {
            System.out.println("Proxy removida: " + proxyName);
            MenuLogger.escreverLog("Proxy removida: " + proxyName);
            notificarProxies(proxyName, false);
        } else {
            System.out.println("Proxy não encontrada: " + proxyName);
        }
    }

    @Override
    public synchronized List<String> getProxies() throws RemoteException {
        return new ArrayList<>(proxies);
    }

    private void notificarProxies(String proxyName, boolean adicionar) {
        List<String> proxiesInativas = new ArrayList<>();
        for (String proxy : proxies) {
            if (!proxy.equals(proxyName)) { // Evita notificar a proxy de sua própria remoção
                try {
                    ProxyRMI proxyRMI = (ProxyRMI) Naming.lookup(proxy);
                    if (adicionar) {
                        proxyRMI.notificarNovoProxy(proxyName);
                    } else {
                        proxyRMI.removerProxy(proxyName);
                        System.out.println("Proxy removida da lista");
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao notificar proxy: " + proxy);
                    e.printStackTrace();
                    proxiesInativas.add(proxy);
                }
            }
        }
        for (String proxyInativa : proxiesInativas) {
            proxies.remove(proxyInativa);
            System.out.println("Proxy removida da lista");
            MenuLogger.escreverLog("Proxy inativa removida! ");
        }
    }

    private boolean verificarProxyAtiva(String proxyName) {
        try {
            ProxyRMI proxyRMI = (ProxyRMI) Naming.lookup(proxyName);
            proxyRMI.verificarStatus();
            return true;
        } catch (Exception e) {
            System.err.println("Proxy inativa: " + proxyName);
            return false;
        }
    }

    public void rodar() {
        try {
            Registry registry = LocateRegistry.createRegistry(porta + 1);
            registry.rebind("LocationServer", this);
            System.out.println("Servidor de Localização registrado na porta " + (porta + 1));

            serverLocation = new ServerSocket(porta, 50, InetAddress.getByName(host));
            System.out.println("Servidor de Localização rodando " + serverLocation.getInetAddress().getHostAddress()
                    + ":" + porta);
            MenuLogger.escreverLog("Servidor de Localização rodando " + serverLocation.getInetAddress().getHostAddress()
                    + ":" + porta);

            while (true) {
                Socket cliente = serverLocation.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
                MenuLogger.escreverLog("Cliente conectado ao Servidor de Localização: " + cliente.getInetAddress().getHostAddress());
                new Thread(new ClienteThread(cliente)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClienteThread implements Runnable {
        private Socket cliente;

        public ClienteThread(Socket cliente) {
            this.cliente = cliente;
        }

        @Override
        public void run() {
            try (ObjectOutputStream outCliente = new ObjectOutputStream(cliente.getOutputStream());
                ObjectInputStream inCliente = new ObjectInputStream(cliente.getInputStream())) {

                String mensagem = (String) inCliente.readObject();
                System.out.println("Mensagem recebida do cliente: " + mensagem);
                if (mensagem.equals("Novo cliente querendo conexão, envie localização")) {
                    boolean proxyEncontrada = false;
                    Random random = new Random();
                    List<String> proxiesAtivas = new ArrayList<>(proxies);
                    while (!proxyEncontrada && !proxiesAtivas.isEmpty()) {
                        int index = random.nextInt(proxiesAtivas.size());
                        String proxyName = proxiesAtivas.get(index);
    
                        if (verificarProxyAtiva(proxyName)) {
                            ProxyRMI proxyRMI = (ProxyRMI) Naming.lookup(proxyName);
                            String resposta = proxyRMI.receberMensagem("Novo cliente querendo conexão, envie localização");
                            System.out.println("Localização recebida do Proxy: " + resposta);
    
                            // Envia localização do Proxy para o cliente
                            outCliente.writeObject(resposta);
                            outCliente.flush();
                            System.out.println("Localização do Proxy enviada ao cliente: " + resposta);
                            MenuLogger.escreverLog("Location: Localização do Proxy enviada ao cliente: " + resposta);
                            proxyEncontrada = true;
                        } else {
                            System.out.println("Proxy escolhida está inativa: " + proxyName);
                            MenuLogger.escreverLog("OPS! Uma Proxy ficou inativa! " + proxyName);
                            proxiesAtivas.remove(proxyName);
                            proxies.remove(proxyName);
                            notificarProxies(proxyName, false);
                        }
                    }
    
                    if (!proxyEncontrada) {
                        outCliente.writeObject("Nenhum proxy disponível no momento.");
                        outCliente.flush();
                    }
                } else {
                    System.out.println("Mensagem inesperada recebida do cliente: " + mensagem);
                }
    
            } catch (IOException | ClassNotFoundException | NotBoundException e) {
                e.printStackTrace();
            }
        }
    }
}