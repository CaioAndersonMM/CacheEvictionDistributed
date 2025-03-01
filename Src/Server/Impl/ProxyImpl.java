package Src.Server.Impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import Src.Database.CacheFIFO;

public class ProxyImpl {
    private int porta;
    private ServerSocket serverProxy;
    private CacheFIFO cache;

    public ProxyImpl(int porta) {
        this.porta = porta;
        this.cache = new CacheFIFO();
        rodar();
    }

    public void rodar() {
        try {
            serverProxy = new ServerSocket(porta);
            System.out.println("Proxy rodando " + InetAddress.getLocalHost().getHostAddress() + " : " + porta);

            while (true) {
                Socket cliente = serverProxy.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());

                // Multiplos clientes
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
            try (Scanner inCliente = new Scanner(cliente.getInputStream());
                    PrintWriter outCliente = new PrintWriter(cliente.getOutputStream(), true)) {

                outCliente.println("Conexão estabelecida com o Proxy");

                while (inCliente.hasNextLine()) {
                    String mensagem = inCliente.nextLine();
                    System.out.println("Mensagem recebida do cliente: " + mensagem);

                    if (mensagem.equals("Novo cliente querendo conexão, envie localização")) {
                        //Se for mensagem do servidor de localização
                        String host = InetAddress.getLocalHost().getHostAddress();
                        outCliente.println(host + ":" + porta);
                    } else { // Cliente normal
                        while (cliente.isConnected()) {
                            System.out.println("Qual operação deseja realizar?");
                            System.out.println("1 - Adicionar Ordem de Serviço");
                            System.out.println("2 - Buscar Ordem de Serviço");
                            System.out.println("3 - Remover Ordem de Serviço");
                            System.out.println("4 - Listar Ordem de Serviço");
                            System.out.println("5 - Sair");

                            int opcao = Integer.parseInt(inCliente.nextLine());
                            
                            // Será que vai ficar aqui a lógica de adicionar, buscar, remover e listar?
                            // Ou será que vai ser em outro lugar?


                        }
                    }
                }

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