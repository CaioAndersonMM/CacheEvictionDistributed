package Src.Server.Impl;

import java.io.BufferedReader;
import java.io.FileReader;
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

    public boolean autenticarCliente(String mensagem) {
        try (BufferedReader br = new BufferedReader(new FileReader("Src/autenticacao.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.equals(mensagem)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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
                        // Se for mensagem do servidor de localização
                        String host = InetAddress.getLocalHost().getHostAddress();
                        outCliente.println(host + ":" + porta);
                    } else { // Cliente normal

                        if(autenticarCliente(mensagem)){
                            outCliente.println("Cliente autenticado");
                            while (true) {
                                outCliente.println("Qual funcionalidade deseja acessar no sistema?");
                                String funcionalidade = inCliente.nextLine();
                            }
                        } else {
                            outCliente.println("Credenciais inválidas");
                            break;
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