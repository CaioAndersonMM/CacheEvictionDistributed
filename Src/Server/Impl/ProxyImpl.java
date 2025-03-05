package Src.Server.Impl;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import Src.OrdemServico;
import Src.Database.CacheFIFO;

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

                System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress()+" : "+cliente.getPort());

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

                System.out.println("Enviando mensagem de conexão ao cliente: "+ cliente.getInetAddress().getHostAddress());
                outCliente.writeObject("Conexão estabelecida com o Proxy");

                while (true) {
                    String mensagem = (String) inCliente.readObject();
                    System.out.println("Mensagem recebida do cliente: " + mensagem);

                    if (mensagem.equals("Novo cliente querendo conexão, envie localização")) {
                        String host = InetAddress.getLocalHost().getHostAddress();
                        outCliente.writeObject(host + ":" + porta);
                    } else {
                        if (autenticarCliente(mensagem)) {
                            outCliente.writeObject("Cliente autenticado");
                            while (true) {
                                outCliente.writeObject("Qual funcionalidade deseja acessar no sistema?");
                                String requisicao = (String) inCliente.readObject();
                                System.out.println("Requisição do cliente: " + requisicao);
                                String[] partes = requisicao.split(";");

                                String funcionalidade = partes[0];
                                System.out.println("Cliente selecionou a funcionalidade: " + funcionalidade);

                                switch (funcionalidade) {
                                    case "1":
                                        outCliente.writeObject("Funcionalidade - Cadastrar....");
                                        System.out.println(requisicao);
                                        outAppServer.writeObject(requisicao);
                                        outAppServer.flush();

                                        OrdemServico resposta = (OrdemServico) inAppServer.readObject();
                                        cache.adicionar(resposta);
                                        System.out.println("Ordem de Serviço adicionada ao cache: " + resposta);
                                        break;
                                    case "2":
                                        outCliente.writeObject("Funcionalidade - Listar OS...");
                                        System.out.println(requisicao);
                                        outAppServer.writeObject(requisicao);
                                        outAppServer.flush();

                                        String resposta2 = (String) inAppServer.readObject();
                                        outCliente.writeObject(resposta2);
                                        break;
                                    case "3":
                                        outCliente.writeObject("Funcionalidade - Alterar OS...");
                                        int codigo = Integer.parseInt(partes[1]);
                                        String novonome = partes[2];
                                        String novadescricao = partes[3];
                                        OrdemServico osn = cache.buscar(codigo);
                                        if (osn == null) {
                                            System.out.println("Ordem de serviço não encontrada na cache");
                                            outCliente.writeObject("Ordem de serviço não encontrada na cache");

                                            // buscar na base de dados
                                            outAppServer.writeObject("buscar;" + codigo);
                                            outAppServer.flush();
                                            osn = (OrdemServico) inAppServer.readObject();
                                            if (osn == null) {
                                                System.out.println("Ordem de serviço não encontrada na base de dados");
                                                outCliente.writeObject(
                                                        "Ordem de serviço não encontrada na base de dados");
                                                break;
                                            }

                                            cache.adicionar(osn);
                                        }
                                        osn.setNome(novonome);
                                        osn.setDescricao(novadescricao);

                                        // Enviar a OS atualizada ao servidor de aplicação
                                        outAppServer.writeObject("atualizar;" + osn.getCodigo() + ";" + osn.getNome()
                                                + ";" + osn.getDescricao());
                                        outAppServer.flush();
                                        outCliente.writeObject("Ordem de serviço atualizada com sucesso: " + osn);
                                        break;
                                    case "4":
                                        outCliente.writeObject("Funcionalidade - Excluir OS...");
                                        int cod = Integer.parseInt(partes[1]);
                                        boolean res = cache.remover(cod);
                                        if (res) {
                                            System.out.println("Ordem de serviço removida com sucesso");
                                            outCliente.writeObject("Ordem de serviço removida com sucesso");
                                        } else {
                                            System.out.println("Ordem de serviço não encontrada");
                                            outCliente.writeObject("Ordem de serviço não encontrada");
                                            // buscar na base de dados
                                            outAppServer.writeObject("remover;" + cod);
                                            outAppServer.flush();
                                            String resposta4 = (String) inAppServer.readObject();
                                            outCliente.writeObject(resposta4);
                                        }
                                        break;
                                    case "5":
                                        outCliente.writeObject("Funcionalidade - Exibir Cache");
                                        String listaCache = cache.listarCache();
                                        System.out.println("Listando cache: \n" + listaCache);
                                        outCliente.writeObject(listaCache);
                                        break;
                                    case "6":
                                        outCliente.writeObject("Funcionalidade - Exibir Banco de Dados");
                                        System.out.println(requisicao);
                                        outAppServer.writeObject("listar");
                                        outAppServer.flush();

                                        String resposta6 = (String) inAppServer.readObject();
                                        outCliente.writeObject(resposta6);
                                        break;
                                    case "0":
                                    case "sair":
                                        outCliente.writeObject("Desconectando...");
                                        cliente.close();
                                        return;
                                    default:
                                        outCliente.writeObject("Opção inválida, tente novamente.");
                                        break;
                                }
                            }
                        } else {
                            outCliente.writeObject("Credenciais inválidas");
                            break;
                        }
                    }
                }
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
