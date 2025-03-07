package Src.Server.Impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import Src.MenuLogger;

public class LocationServerImpl {
    private String proxyHost;
    private int porta, proxyPort;
    private Socket socketProxy;
    private ServerSocket serverLocation;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean proxyAtivo;

    public LocationServerImpl(int porta, String proxyHost, int proxyPort) {
        this.porta = porta;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        rodar();
    }

    public void rodar() {
        try {
            String localHostAddress = InetAddress.getLocalHost().getHostAddress();
            serverLocation = new ServerSocket(porta, 50, InetAddress.getByName(localHostAddress));
            System.out.println("Servidor de Localização rodando " + serverLocation.getInetAddress().getHostAddress() + ":" + porta);
            MenuLogger.escreverLog("Servidor de Localização rodando " + serverLocation.getInetAddress().getHostAddress() + ":" + porta);

            while (socketProxy == null) {
                try {
                    System.out.println("Tentando conectar ao Proxy " + proxyHost + ":" + proxyPort);
                    socketProxy = new Socket(proxyHost, proxyPort);
                    out = new ObjectOutputStream(socketProxy.getOutputStream());
                    in = new ObjectInputStream(socketProxy.getInputStream());

                    Object resposta = in.readObject();
                    if (resposta instanceof String && resposta.equals("Conexão estabelecida com o Proxy")) {
                        System.out.println("Servidor de Localização conectado ao Proxy - esperando clientes");
                        MenuLogger.escreverLog("Servidor de Localização conectado ao Proxy - esperando clientes");
                        proxyAtivo = true;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Proxy não disponível, tentando novamente...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }

            while (proxyAtivo) {
                Socket cliente = serverLocation.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
                MenuLogger.escreverLog("Cliente conectado ao Servidor de Localização: " + cliente.getInetAddress().getHostAddress());

                // Multiplos clientes
                new Thread(new ClienteThread(cliente)).start();
            }

            fecharConexaoProxy();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Servidor de localização conectado ao Proxy " + proxyHost + ":" + proxyPort);
        MenuLogger.escreverLog("Servidor de localização conectado ao Proxy " + proxyHost + ":" + proxyPort);
    }

    public void enviarMensagemAoProxy(Object message) {
        try {
            out.writeObject(message);
            out.flush();
            MenuLogger.escreverLog("Location: Mensagem enviada ao Proxy: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object receberMensagemDoProxy() {
        try {
            Object resposta = in.readObject();
            MenuLogger.escreverLog("Location: Mensagem recebida do Proxy: " + resposta);
            return resposta;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void fecharConexaoProxy() throws IOException {
        in.close();
        out.close();
        socketProxy.close();
        System.out.println("Conexão com o Proxy fechada");
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

                // Envia mensagem do cliente para o Proxy
                enviarMensagemAoProxy("Novo cliente querendo conexão, envie localização");
                Object resposta = receberMensagemDoProxy();
                System.out.println("Localização recebida do Proxy: " + resposta);

                if (resposta instanceof String) {
                    String[] localizacao = ((String) resposta).split(":");
                    String host = localizacao[0];
                    int porta = Integer.parseInt(localizacao[1]);

                    // Envia localização do Proxy para o cliente
                    outCliente.writeObject(host + ":" + porta);
                    MenuLogger.escreverLog("Location: Localização do Proxy enviada ao cliente: " + host + ":" + porta);
                }

            } catch (IOException e) {
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