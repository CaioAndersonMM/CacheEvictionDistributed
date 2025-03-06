package Src.Server.Impl;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import Src.OrdemServico;
import Src.Database.CacheFIFO;
import Src.Comando;

public class ProxyImpl {
    private int porta, portaAplicacao;
    private ServerSocket serverProxy;
    private CacheFIFO cache;
    private String hostAplicacao;

    private Socket appServerSocket;
    private ObjectOutputStream outAppServer;
    private ObjectInputStream inAppServer;

    public ProxyImpl(int porta, String hostAplicacao, int portaAplicacao) {
        this.porta = porta;
        this.cache = new CacheFIFO();
        this.hostAplicacao = hostAplicacao;
        this.portaAplicacao = portaAplicacao;
        conectarAplicacao();
        rodar();
    }

    private void conectarAplicacao() {
        while (appServerSocket == null) {
            try {
                appServerSocket = new Socket(hostAplicacao, portaAplicacao);
                outAppServer = new ObjectOutputStream(appServerSocket.getOutputStream());
                inAppServer = new ObjectInputStream(appServerSocket.getInputStream());
                System.out.println("Conectado ao Servidor de Aplicação " + hostAplicacao + ":" + portaAplicacao);
            } catch (IOException e) {
                System.out.println("Servidor de Aplicação não disponível, tentando novamente...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    public void rodar() {
        try {
            String localHostAddress = InetAddress.getLocalHost().getHostAddress();
            serverProxy = new ServerSocket(porta, 50, InetAddress.getByName(localHostAddress));
            System.out.println("Proxy rodando " + serverProxy.getInetAddress().getHostAddress() + " : " + porta);

            while (true) {
                Socket cliente = serverProxy.accept();

                System.out.println(
                        "Cliente conectado: " + cliente.getInetAddress().getHostAddress() + " : " + cliente.getPort());

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
            try (ObjectInputStream inCliente = new ObjectInputStream(cliente.getInputStream());
                    ObjectOutputStream outCliente = new ObjectOutputStream(cliente.getOutputStream())) {

                System.out.println(
                        "Enviando mensagem de conexão ao cliente: " + cliente.getInetAddress().getHostAddress());
                outCliente.writeObject("Conexão estabelecida com o Proxy");
                outCliente.flush();

                while (true) {
                    String mensagem = (String) inCliente.readObject();
                    System.out.println("Mensagem recebida do cliente: " + mensagem);

                    if (mensagem.equals("Novo cliente querendo conexão, envie localização")) {
                        String host = InetAddress.getLocalHost().getHostAddress();
                        outCliente.writeObject(host + ":" + porta);
                        outCliente.flush();
                    } else {
                        if (autenticarCliente(mensagem)) {
                            outCliente.writeObject("Cliente autenticado");
                            outCliente.flush();
                            while (true) {
                                outCliente.writeObject("Qual funcionalidade deseja acessar no sistema?");
                                outCliente.flush();
                                Comando comando = (Comando) inCliente.readObject();
                                System.out.println("Requisição do cliente: " + comando.getTipo());
                                String[] partes = comando.getParametros();

                                String funcionalidade = comando.getTipo();
                                System.out.println("Cliente selecionou a funcionalidade: " + funcionalidade);

                                switch (funcionalidade) {
                                    case "adicionar":
                                        outCliente.writeObject("Funcionalidade - Cadastrar....");
                                        outCliente.flush();
                                        System.out.println(comando);
                                        outAppServer.writeObject(comando);
                                        outAppServer.flush();

                                        Object resposta = inAppServer.readObject();
                                        if (resposta instanceof OrdemServico) {
                                            cache.adicionar((OrdemServico) resposta);
                                            System.out.println("Ordem de Serviço adicionada ao cache: " + resposta);
                                            outCliente.writeObject(resposta);
                                            outCliente.flush();
                                        } else {
                                            System.out.println("Resposta inesperada do servidor de aplicação: " + resposta);
                                            outCliente.writeObject("Erro ao adicionar ordem de serviço.");
                                            outCliente.flush();
                                        }
                                        break;
                                    case "listar":
                                        outCliente.writeObject("Funcionalidade - Listar OS...");
                                        outCliente.flush();
                                        System.out.println(comando);
                                        outAppServer.writeObject(comando);
                                        outAppServer.flush();

                                        Object resposta2 = inAppServer.readObject();
                                        if (resposta2 instanceof String) {
                                            outCliente.writeObject((String) resposta2);
                                            outCliente.flush();
                                        } else {
                                            System.out.println(
                                                    "Resposta inesperada do servidor de aplicação: " + resposta2);
                                            outCliente.writeObject("Erro ao listar ordens de serviço.");
                                            outCliente.flush();
                                        }
                                        break;
                                    case "atualizar":
                                        outCliente.writeObject("Funcionalidade - Alterar OS...");
                                        outCliente.flush();
                                        int codigo = Integer.parseInt(partes[0]);
                                        String novonome = partes[1];
                                        String novadescricao = partes[2];
                                        OrdemServico osn = cache.buscar(codigo);
                                        if (osn == null) {
                                            System.out.println("Ordem de serviço não encontrada na cache");
                                            outCliente.writeObject("Ordem de serviço não encontrada na cache");
                                            outCliente.flush();

                                            // buscar na base de dados
                                            outAppServer.writeObject(new Comando("buscar", String.valueOf(codigo)));
                                            outAppServer.flush();
                                            Object resposta3 = inAppServer.readObject();
                                            if (resposta3 instanceof OrdemServico) {
                                                osn = (OrdemServico) resposta3;
                                                cache.adicionar(osn);
                                            } else {
                                                System.out.println("Ordem de serviço não encontrada na base de dados");
                                                outCliente.writeObject(
                                                        "Ordem de serviço não encontrada na base de dados");
                                                outCliente.flush();
                                                break;
                                            }
                                        }
                                        osn.setNome(novonome);
                                        osn.setDescricao(novadescricao);

                                        // Enviar a OS atualizada ao servidor de aplicação
                                        outAppServer.writeObject(new Comando("atualizar",
                                                String.valueOf(osn.getCodigo()), osn.getNome(), osn.getDescricao()));
                                        outAppServer.flush();
                                        outCliente.writeObject("Ordem de serviço atualizada com sucesso: " + osn);
                                        outCliente.flush();
                                        break;
                                    case "remover":
                                        outCliente.writeObject("Funcionalidade - Excluir OS...");
                                        outCliente.flush();
                                        int cod = Integer.parseInt(partes[0]);
                                        boolean res = cache.remover(cod);
                                        if (res) {
                                            System.out.println("Ordem de serviço removida com sucesso");
                                            outCliente.writeObject("Ordem de serviço removida com sucesso");
                                            outCliente.flush();
                                        } else {
                                            System.out.println("Ordem de serviço não encontrada");
                                            outCliente.writeObject("Ordem de serviço não encontrada");
                                            outCliente.flush();
                                            // buscar na base de dados
                                            outAppServer.writeObject(new Comando("remover", String.valueOf(cod)));
                                            outAppServer.flush();
                                            Object resposta4 = inAppServer.readObject();
                                            if (resposta4 instanceof String) {
                                                outCliente.writeObject((String) resposta4);
                                                outCliente.flush();
                                            } else {
                                                System.out.println(
                                                        "Resposta inesperada do servidor de aplicação: " + resposta4);
                                                outCliente.writeObject("Erro ao remover ordem de serviço.");
                                                outCliente.flush();
                                            }
                                        }
                                        break;
                                    case "5":
                                        outCliente.writeObject("Funcionalidade - Exibir Cache");
                                        outCliente.flush();
                                        String listaCache = cache.listarCache();
                                        System.out.println("Listando cache: \n" + listaCache);
                                        outCliente.writeObject(listaCache);
                                        outCliente.flush();
                                        break;
                                    case "6":
                                        outCliente.writeObject("Funcionalidade - Exibir Banco de Dados");
                                        outCliente.flush();
                                        System.out.println(comando);
                                        outAppServer.writeObject(new Comando("listar"));
                                        outAppServer.flush();

                                        Object resposta6 = inAppServer.readObject();
                                        if (resposta6 instanceof String) {
                                            outCliente.writeObject((String) resposta6);
                                            outCliente.flush();
                                        } else {
                                            System.out.println(
                                                    "Resposta inesperada do servidor de aplicação: " + resposta6);
                                            outCliente.writeObject("Erro ao exibir banco de dados.");
                                            outCliente.flush();
                                        }
                                        break;
                                    case "0":
                                    case "sair":
                                        outCliente.writeObject("Desconectando...");
                                        outCliente.flush();
                                        cliente.close();
                                        return;
                                    default:
                                        outCliente.writeObject("Opção inválida, tente novamente.");
                                        outCliente.flush();
                                        break;
                                }
                            }
                        } else {
                            outCliente.writeObject("Credenciais inválidas");
                            outCliente.flush();
                            break;
                        }
                    }
                }
            } catch (EOFException e) {
                System.out.println("Conexão encerrada pelo cliente: " + cliente.getInetAddress().getHostAddress());
            } catch (IOException | ClassNotFoundException e) {
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