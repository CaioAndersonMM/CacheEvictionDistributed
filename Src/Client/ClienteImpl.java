package Src.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import Src.Menu;
import Src.Comando;

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
                System.out.println("Conectado ao servidor de localização " + locationHost + ":" + locationPort);
            } catch (IOException e) {
                System.out.println("Servidor de localização não disponível, tentando novamente...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        try (ObjectOutputStream outLocation = new ObjectOutputStream(locationSocket.getOutputStream());
             ObjectInputStream inLocation = new ObjectInputStream(locationSocket.getInputStream())) {

            outLocation.writeObject("Novo cliente querendo conexão, envie localização");
            outLocation.flush();
            System.out.println("Pedido de localização enviado");

            Object proxyLocationObj = inLocation.readObject();
            if (proxyLocationObj instanceof String) {
                String proxyLocation = (String) proxyLocationObj;
                System.out.println("Localização do Proxy recebida: " + proxyLocation);

                // O proxy vai responder host:porta
                String[] proxyInfo = proxyLocation.split(":");
                String proxyHost = proxyInfo[0];
                int proxyPort = Integer.parseInt(proxyInfo[1]);

                try (Socket proxySocket = new Socket(proxyHost, proxyPort);
                     ObjectOutputStream outProxy = new ObjectOutputStream(proxySocket.getOutputStream());
                     ObjectInputStream inProxy = new ObjectInputStream(proxySocket.getInputStream());
                     Scanner sc = new Scanner(System.in)) {

                    System.out.println("Conectado ao Proxy " + proxyHost + ":" + proxyPort);

                    Object respostaObj = inProxy.readObject();
                    if (respostaObj instanceof String) {
                        String resposta = (String) respostaObj;
                        System.out.println("Resposta do Proxy: " + resposta);

                        System.out.println("Qual seu email? ");
                        String email = sc.nextLine();
                        System.out.println("Qual sua senha? ");
                        String senha = sc.nextLine();

                        String mensagem = email + ";" + senha;
                        outProxy.writeObject(mensagem);
                        outProxy.flush();

                        respostaObj = inProxy.readObject();
                        if (respostaObj instanceof String) {
                            resposta = (String) respostaObj;
                            System.out.println(resposta);

                            if (resposta.equals("Cliente autenticado")) {
                                while (true) {
                                    System.out.println(Menu.exibirMenu());

                                    String opcao = sc.nextLine();
                                    switch (opcao) {
                                        case "1":
                                            System.out.println("Digite o nome da ordem de serviço: ");
                                            String nome = sc.nextLine();
                                            System.out.println("Digite a descrição da ordem de serviço: ");
                                            String descricao = sc.nextLine();
                                            Comando comandoAdicionar = new Comando("adicionar", nome, descricao);
                                            outProxy.writeObject(comandoAdicionar);
                                            outProxy.flush();
                                            break;
                                        case "2":
                                            Comando comandoListar = new Comando("listar");
                                            outProxy.writeObject(comandoListar);
                                            outProxy.flush();
                                            String respostaCache;
                                            while (inProxy.readObject() instanceof String && !(respostaCache = (String) inProxy.readObject()).isEmpty()) {
                                                System.out.println(respostaCache);
                                            }
                                            break;
                                        case "3":
                                            System.out.println("Digite o código da ordem de serviço que deseja alterar: ");
                                            int codigo = Integer.parseInt(sc.nextLine().trim());
                                            System.out.println("Digite o novo nome da ordem de serviço: ");
                                            nome = sc.nextLine();
                                            System.out.println("Digite a nova descrição da ordem de serviço: ");
                                            descricao = sc.nextLine();
                                            Comando comandoAlterar = new Comando("atualizar", String.valueOf(codigo), nome, descricao);
                                            outProxy.writeObject(comandoAlterar);
                                            outProxy.flush();
                                            break;
                                        case "4":
                                            System.out.println("Digite o código da ordem de serviço que deseja excluir: ");
                                            codigo = Integer.parseInt(sc.nextLine().trim());
                                            Comando comandoExcluir = new Comando("remover", String.valueOf(codigo));
                                            outProxy.writeObject(comandoExcluir);
                                            outProxy.flush();
                                            String rp;
                                            while (inProxy.readObject() instanceof String && !(rp = (String) inProxy.readObject()).isEmpty()) {
                                                System.out.println(rp);
                                            }
                                            break;
                                        case "5":
                                            // Ignorar por enquanto
                                            Comando comando5 = new Comando("5");
                                            outProxy.writeObject(comando5);
                                            outProxy.flush();
                                            break;
                                        case "6":
                                            // Ignorar por enquanto
                                            Comando comando6 = new Comando("6");
                                            outProxy.writeObject(comando6);
                                            outProxy.flush();
                                            break;
                                        case "0":
                                            Comando comandoSair = new Comando("0");
                                            outProxy.writeObject(comandoSair);
                                            outProxy.flush();
                                            return;
                                        default:
                                            System.out.println("Opção inválida");
                                    }
                                }
                            } else {
                                System.out.println("Autenticação falhou");
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}