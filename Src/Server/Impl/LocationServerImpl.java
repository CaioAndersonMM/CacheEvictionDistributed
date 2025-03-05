package Src.Server.Impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class LocationServerImpl {
    private String proxyHost;
    private int porta, proxyPort;
    private Socket socketProxy;
    private ServerSocket serverLocation;
    private PrintWriter out;
    private Scanner in;
    private boolean proxyAtivo;

    public LocationServerImpl(int porta, String proxyHost, int proxyPort) {
        this.porta = porta;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        rodar();
    }

    public void rodar() {
        
        try {
            serverLocation = new ServerSocket(porta);
            System.out.println("Servidor de localização rodando " + serverLocation.getInetAddress().getHostAddress() + ":" + porta);

            while (socketProxy == null) {
                try {
                    socketProxy = new Socket(proxyHost, proxyPort);
                    out = new PrintWriter(socketProxy.getOutputStream(), true);
                    in = new Scanner(socketProxy.getInputStream());
    
                    if (in.nextLine().equals("Conexão estabelecida com o Proxy")) {
                        System.out.println("Servidor de Localização conectado ao Proxy - esperando clientes");
                        proxyAtivo = true;
                    }
                } catch (IOException e) {
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

                // Multiplos clientes
                new Thread(new ClienteThread(cliente)).start();
            }

            fecharConexaoProxy();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Servidor de localização conectado ao Proxy " + proxyHost + ":" + proxyPort);
    }

    public void enviarMensagemAoProxy(String message) {
        out.println(message);
    }

    public String receberMensagemDoProxy() {
        return in.nextLine();
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
            try (Scanner inCliente = new Scanner(cliente.getInputStream());
                 PrintWriter outCliente = new PrintWriter(cliente.getOutputStream(), true)) {

                // Envia mensagem do cliente para o Proxy
                enviarMensagemAoProxy("Novo cliente querendo conexão, envie localização");
                String resposta = receberMensagemDoProxy();
                System.out.println("Localização recebida do Proxy: " + resposta);

                String[] localizacao = resposta.split(":");
                String host = localizacao[0];
                int porta = Integer.parseInt(localizacao[1]);

                // Envia localização do Proxy para o cliente
                outCliente.println(host + ":" + porta);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    cliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}