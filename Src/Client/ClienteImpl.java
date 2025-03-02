package Src.Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteImpl {
    private int port;
    private String locationHost;
    private int locationPort;

    public ClienteImpl(int port, String locationHost, int locationPort) {
        this.port = port;
        this.locationHost = locationHost;
        this.locationPort = locationPort;
        rodar();
    }

    public void rodar() {
        System.out.println("Cliente rodando na porta " + port);

        Socket locationSocket = null;

        while (locationSocket == null) {
            try {
                locationSocket = new Socket(locationHost, locationPort);
            } catch (IOException e) {
                System.out.println("Servidor de localização não disponível, tentando novamente...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        try (PrintWriter outLocation = new PrintWriter(locationSocket.getOutputStream(), true);
             Scanner inLocation = new Scanner(locationSocket.getInputStream())) {

            outLocation.println("Novo cliente querendo conexão, envie localização");

            String proxyLocation = inLocation.nextLine();
            System.out.println("Localização do Proxy recebida: " + proxyLocation);

            // O proxy vai responder host:porta
            String[] proxyInfo = proxyLocation.split(":");
            String proxyHost = proxyInfo[0];
            int proxyPort = Integer.parseInt(proxyInfo[1]);

            try (Socket proxySocket = new Socket(proxyHost, proxyPort);
                 PrintWriter outProxy = new PrintWriter(proxySocket.getOutputStream(), true);
                 Scanner inProxy = new Scanner(proxySocket.getInputStream())) {

                System.out.println("Conectado ao Proxy " + proxyHost + ":" + proxyPort);
                Scanner sc = new Scanner(System.in);

                // Recebe resposta do Proxy
                String resposta = inProxy.nextLine();
                System.out.println("Resposta do Proxy: " + resposta);

                System.out.println("Qual seu email? ");
                String email = sc.nextLine();
                System.out.println("Qual sua senha? ");
                String senha = sc.nextLine();

                String mensagem = email + ";" + senha;
                outProxy.println(mensagem);
                
                while (true) {
                    // Envia e recebe mensagens do Proxy
                    System.out.println(inProxy.nextLine());
                    String funcionalidade = sc.nextLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}