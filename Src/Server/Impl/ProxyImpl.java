package Src.Server.Impl;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import Src.OrdemServico;
import Src.Database.CacheFIFO;
import Src.Comando;
import Src.MenuLogger;

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
        iniciarlizarCache();
       
    }

    private void conectarAplicacao() {
        while (appServerSocket == null) {
            try {
                appServerSocket = new Socket(hostAplicacao, portaAplicacao);
                outAppServer = new ObjectOutputStream(appServerSocket.getOutputStream());
                inAppServer = new ObjectInputStream(appServerSocket.getInputStream());
                System.out.println("Conectado ao Servidor de Aplicação " + hostAplicacao + ":" + portaAplicacao);
                MenuLogger.escreverLog(
                        "Proxy foi conectado ao Servidor de Aplicação " + hostAplicacao + ":" + portaAplicacao);
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

                MenuLogger.escreverLog("Novo cliente conectado ao Proxy " + cliente.getInetAddress().getHostAddress()
                        + " : " + cliente.getPort());

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
            try 
            ( ObjectOutputStream outCliente = new ObjectOutputStream(cliente.getOutputStream());
                ObjectInputStream inCliente = new ObjectInputStream(cliente.getInputStream());
                   ) {

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
                        MenuLogger.escreverLog("Proxy: Localização do Proxy enviada ao cliente " + host + ":" + porta);
                    } else {
                        if (autenticarCliente(mensagem)) {
                            outCliente.writeObject("Cliente autenticado");
                            outCliente.flush();
                            MenuLogger.escreverLog("Proxy: Cliente " + cliente.getInetAddress().getHostAddress()
                                    + " autenticado com sucesso");
                            while (true) {
                                Comando comando = (Comando) inCliente.readObject();
                                System.out.println("Requisição do cliente: " + comando.getTipo());
                                String[] partes = comando.getParametros();

                                String funcionalidade = comando.getTipo();
                                System.out.println("Cliente selecionou a funcionalidade: " + funcionalidade);

                                switch (funcionalidade) {
                                    case "adicionar":
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
                                            System.out.println(
                                                    "Resposta inesperada do servidor de aplicação: " + resposta);
                                            outCliente.writeObject("Erro ao adicionar ordem de serviço.");
                                            outCliente.flush();
                                        }
                                        MenuLogger.escreverLog("Proxy: Ordem de Serviço adicionada ");
                                        break;
                                    case "listar":
                                        System.out.println(comando);
                                        outAppServer.writeObject(comando);
                                        outAppServer.flush();

                                        Object resposta2 = inAppServer.readObject();
                                        if (resposta2 instanceof String) {
                                            outCliente.writeObject((String) resposta2);
                                            outCliente.flush();
                                            System.out.println("Resposta enviada ao cliente: " + resposta2);
                                        } else {
                                            System.out.println(
                                                    "Resposta inesperada do servidor de aplicação: " + resposta2);
                                            outCliente.writeObject("Erro ao listar ordens de serviço.");
                                            outCliente.flush();
                                        }
                                        MenuLogger.escreverLog("Proxy: Ordens de serviço listadas");
                                        break;
                                    case "atualizar":
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
                                            MenuLogger.escreverLog("Proxy: Ordem de serviço buscada no banco de dados");
                                        }
                                        osn.setNome(novonome);
                                        osn.setDescricao(novadescricao);

                                        // Enviar a OS atualizada ao servidor de aplicação
                                        outAppServer.writeObject(new Comando("atualizar",
                                                String.valueOf(osn.getCodigo()), osn.getNome(), osn.getDescricao()));
                                        outAppServer.flush();
                                        outCliente.writeObject("Ordem de serviço atualizada com sucesso: " + osn);
                                        outCliente.flush();
                                        MenuLogger.escreverLog("Proxy: Ordem de serviço atualizada " + osn);
                                        break;
                                    case "remover":
                                        int cod = Integer.parseInt(partes[0]);
                                        boolean res = cache.remover(cod);
                                        if (res) {
                                            System.out.println("Ordem de serviço removida com sucesso da cache");
                                            MenuLogger.escreverLog("Proxy: Ordem de serviço removida da cache: " + cod);
                                        } else {
                                            System.out.println("Ordem de serviço não encontrada na cache");
                                        }
                                        // Remover do banco de dados
                                        outAppServer.writeObject(new Comando("remover", String.valueOf(cod)));
                                        outAppServer.flush();
                                        Object resposta4 = inAppServer.readObject();
                                        if (resposta4 instanceof String) {
                                            outCliente.writeObject((String) resposta4);
                                            outCliente.flush();
                                            if (((String) resposta4).contains("sucesso")) {
                                                MenuLogger.escreverLog(
                                                        "Proxy: Ordem de serviço removida do banco de dados: " + cod);
                                            } else {
                                                MenuLogger.escreverLog(
                                                        "Proxy: Ordem de Serviço não foi encontrada na Base de Dados para remoção"
                                                                + cod);
                                            }
                                        } else {
                                            System.out.println(
                                                    "Resposta inesperada do servidor de aplicação: " + resposta4);
                                            outCliente.writeObject("Erro ao remover ordem de serviço.");
                                            outCliente.flush();
                                        }
                                        break;
                                    case "listarCache":
                                        String listaCache = cache.listarCache();
                                        System.out.println("Listando cache: \n" + listaCache);
                                        outCliente.writeObject(listaCache);
                                        outCliente.flush();
                                        MenuLogger.escreverLog("Proxy: Cache exibido");
                                        break;
                                    case "buscar":
                                        System.out.println(comando);
                                        int codigobusca = Integer.parseInt(partes[0]);
                                        OrdemServico os = cache.buscar(codigobusca);
                                        if(os == null) {
                                            System.out.println("Ordem de serviço não encontrada na cache");
                                            outCliente.writeObject("Ordem de serviço não encontrada na cache");
                                            outCliente.flush();

                                            // buscar na base de dados
                                            outAppServer.writeObject(new Comando("buscar", String.valueOf(codigobusca)));
                                            outAppServer.flush();
                                            Object resposta5 = inAppServer.readObject();
                                            if (resposta5 instanceof OrdemServico) {
                                                os = (OrdemServico) resposta5;
                                                cache.adicionar(os);
                                            } else {
                                                System.out.println("Ordem de serviço não encontrada na base de dados");
                                                outCliente.writeObject("Ordem de serviço não encontrada na base de dados");
                                                outCliente.flush();
                                                break;
                                            }
                                            MenuLogger.escreverLog("Proxy: Ordem de serviço buscada no banco de dados");
                                        }
                                        MenuLogger.escreverLog("Proxy: Banco de dados exibido");
                                        break;
                                    case "0":
                                    case "sair":
                                        outCliente.writeObject("Desconectando...");
                                        outCliente.flush();
                                        cliente.close();
                                        MenuLogger.escreverLog("Cliente desconectado do Proxy: "
                                                + cliente.getInetAddress().getHostAddress());
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

    private void iniciarlizarCache()
    {
        
            for(int i =1; i<=30; i++)
            {    
            try {
                outAppServer.writeObject(new Comando("buscar", String.valueOf(i)));
                outAppServer.flush();
                Object resposta5 = inAppServer.readObject();
                if (resposta5 instanceof OrdemServico) {
                    OrdemServico os = (OrdemServico) resposta5;
                    cache.adicionar(os);
                    MenuLogger.escreverLog("Proxy: Ordem de serviço adicionada ao cache: " + os);
                } else {
                    System.out.println("Ordem de serviço não encontrada na base de dados");
                    MenuLogger.escreverLog("Proxy: Ordem de serviço  nao encontrada no banco de dados");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            
        }
       
    }
}