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

        try (Socket locationSocket = new Socket(locationHost, locationPort);
             PrintWriter outLocation = new PrintWriter(locationSocket.getOutputStream(), true);
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

                // Envia e recebe mensagens do Proxy
                outProxy.println("Mensagem do cliente para o Proxy");
                String resposta = inProxy.nextLine();
                System.out.println("Resposta do Proxy: " + resposta);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 12346; // Porta do cliente
        String locationHost = "localhost"; // Host do servidor de localização
        int locationPort = 12345; // Porta do servidor de localização

        new ClienteImpl(port, locationHost, locationPort);
    }
}