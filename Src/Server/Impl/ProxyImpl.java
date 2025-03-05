package Src.Server.Impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import Src.OrdemServico;
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

                        if (autenticarCliente(mensagem)) {
                            outCliente.println("Cliente autenticado");
                            while (true) {
                                outCliente.println("Qual funcionalidade deseja acessar no sistema?");
                                String requisição = inCliente.nextLine();
                                System.out.println("Requisição do cliente: " + requisição);
                                String[] partes = requisição.split(";");
                                for (String parte : partes) {
                                    System.out.println(parte + " ");
                                }
                               
                                String funcionalidade = requisição.split(";")[0];
                                System.out.println("Cliente selecionou a funcionalidade: " + funcionalidade);
                                switch (funcionalidade) {
                                    case "1":
                                    outCliente.println("Funcionalidade 1 selecionada");
                                        String nome = requisição.split(";")[1];
                                        String descricao = requisição.split(";")[2];
                                        System.out.println("Nome: " + nome + " Descrição: " + descricao);

                                        OrdemServico os = new OrdemServico(nome, descricao);
                                        cache.adicionar(os);
                                        break;
                                    case "2":
                                        outCliente.println("Funcionalidade 2 selecionada");
                                        String listaCache = cache.listarCache();
                                        System.out.println("Listando cache: \n" + listaCache);
                                        outCliente.println(listaCache);
                                        outCliente.println("FIM");
                                        outCliente.flush();
                                        break;
                                    case "3":
                                        outCliente.println("Funcionalidade 3 selecionada");
                                        int codigo = Integer.parseInt(requisição.split(";")[1]);
                                        String novonome = requisição.split(";")[2];
                                        String novadescricao = requisição.split(";")[3];
                                        OrdemServico osn =cache.buscar(codigo);
                                        if(osn==null){
                                            System.out.println("Ordem de serviço não encontrada");
                                            outCliente.println("Ordem de serviço não encontrada");
                                            break;
                                        }
                                        osn.setNome(novonome);
                                        osn.setDescricao(novadescricao);

                                        break;
                                    case "4":
                                        outCliente.println("Funcionalidade 4 selecionada");
                                        int cod = Integer.parseInt(requisição.split(";")[1]);
                                        boolean res = cache.remover(cod);
                                        System.out.println(res);
                                        if(res){
                                            System.out.println("Ordem de serviço removida com sucesso");
                                            outCliente.println("Ordem de serviço removida com sucesso");
                                        }else{
                                            System.out.println("Ordem de serviço não encontrada");
                                            outCliente.println("Ordem de serviço não encontrada");
                                        }
                                        break;
                                    case "5":
                                        outCliente.println("Funcionalidade 5 selecionada");
                                        break;
                                    case "6":
                                        outCliente.println("Funcionalidade 6 selecionada");
                                        break;
                                    case "0":
                                    case "sair":
                                        outCliente.println("Desconectando...");
                                        cliente.close();
                                        return;
                                    default:
                                        outCliente.println("Opção inválida, tente novamente.");
                                        break;
                                }
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