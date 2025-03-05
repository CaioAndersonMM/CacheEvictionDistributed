package Src.Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import Src.Menu;

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
                    Scanner inProxy = new Scanner(proxySocket.getInputStream());
                    Scanner sc = new Scanner(System.in);) {

                System.out.println("Conectado ao Proxy " + proxyHost + ":" + proxyPort);

                String resposta = inProxy.nextLine();
                System.out.println("Resposta do Proxy: " + resposta);

                System.out.println("Qual seu email? ");
                String email = sc.nextLine();
                System.out.println("Qual sua senha? ");
                String senha = sc.nextLine();

                String mensagem = email + ";" + senha;
                outProxy.println(mensagem);

                resposta = inProxy.nextLine();
                System.out.println(resposta);

                if (resposta.equals("Cliente autenticado")) {
                    while (true) {
                        System.out.println(Menu.exibirMenu());
                        resposta = inProxy.nextLine();

                        String opcao = sc.nextLine();
                        switch(opcao){
                            case "1":
                                System.out.println("digite o nome da ordem de serviço: ");
                                String nome = sc.nextLine();
                                System.out.println("digite a descrição da ordem de serviço: ");
                                String descricao = sc.nextLine();
                                outProxy.println(opcao+";"+nome + ";" + descricao);
                                break;
                                case "2":
                                outProxy.println(opcao);
                                String respostaCache;
                                while (!(respostaCache = inProxy.nextLine()).isEmpty()) {
                                    System.out.println(respostaCache);
                                }
                                break;                            
                            case "3":
                                System.out.println("digite o código da ordem de serviço que deseja alterar: ");
                                int codigo = Integer.parseInt(sc.nextLine().trim());
                                 System.out.println("digite o novo nome da ordem de serviço: ");
                                nome = sc.nextLine();
                                System.out.println("digite a nova descrição da ordem de serviço: ");
                                descricao = sc.nextLine();
                                outProxy.println(opcao+";"+codigo+";"+nome + ";" + descricao);
                                break;
                            case "4":
                                System.out.println("digite o código da ordem de serviço que deseja excluir: ");
                                codigo = Integer.parseInt(sc.nextLine().trim());
                                outProxy.println(opcao+";"+codigo);
                                String rp;
                                while (!(rp = inProxy.nextLine()).isEmpty()) {
                                    System.out.println(rp);
                                }
                                break;
                            case "5":
                                //ignorar por enquanto
                                outProxy.println("5");
                                break;
                            case "6":
                            //ignorar por enquanto
                                outProxy.println("6");
                                break;
                            case "0":
                                outProxy.println("0");
                                break;
                            default:
                                System.out.println("Opção inválida");}

                    }
                } else {
                    System.out.println("Autenticação falhou");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}