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
            notificarProxies(proxyName);
        }
    }

    @Override
    public synchronized List<String> getProxies() throws RemoteException {
        return new ArrayList<>(proxies);
    }

    private void notificarProxies(String novoProxy) {
        for (String proxy : proxies) {
            try {
                ProxyRMI proxyRMI = (ProxyRMI) Naming.lookup(proxy);
                proxyRMI.notificarNovoProxy(novoProxy);
            } catch (Exception e) {
                System.err.println("Erro ao notificar proxy: " + proxy);
                e.printStackTrace();
            }
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

                if (!proxies.isEmpty()) {
                    Random random = new Random();
                    String proxyName = proxies.get(random.nextInt(proxies.size()));
                    ProxyRMI proxyRMI = (ProxyRMI) Naming.lookup(proxyName);

                    proxyRMI.receberMensagem("Novo cliente querendo conexão, envie localização");
                    Object resposta = (Object) proxyRMI.receberMensagem("Novo cliente querendo conexão, envie localização");
                    System.out.println("Localização recebida do Proxy: " + resposta);
                    
                    if (resposta instanceof String) {
                        String[] localizacao = ((String) resposta).split(":");
                        String host = localizacao[0];
                        int porta = Integer.parseInt(localizacao[1]);

                        // Envia localização do Proxy para o cliente
                        outCliente.writeObject(host + ":" + porta);
                        MenuLogger.escreverLog("Location: Localização do Proxy enviada ao cliente: " + host + ":" + porta);
                    }
                } else {
                    System.out.println("Nenhum proxy registrado para enviar a mensagem.");
                }

            } catch (IOException | NotBoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    cliente.close();
                    MenuLogger.escreverLog("Location: Conexão com o cliente fechada: " + cliente.getInetAddress().getHostAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}