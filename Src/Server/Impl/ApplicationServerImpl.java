package Src.Server.Impl;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import Src.OrdemServico;
import Src.Database.DatabaseOs;
import Src.Comando;
import Src.MenuLogger;

public class ApplicationServerImpl {
    private int porta;
    private DatabaseOs database;
    private int nextId;
    private String enderecoip;

    public ApplicationServerImpl(int porta, String enderecoip) {
        this.porta = porta;
        this.enderecoip = enderecoip;
        this.nextId = 1;
        this.database = new DatabaseOs();
        inicializarOrdemServico();
        rodar();
       
    }

    public void rodar() {
        try (ServerSocket server = new ServerSocket(porta, 50, InetAddress.getByName(enderecoip))) {
            System.out.println("Servidor de Aplicação rodando " + server.getInetAddress().getHostAddress() + " : " + server.getLocalPort());
            MenuLogger.escreverLog("Servidor de Aplicação rodando " + server.getInetAddress().getHostAddress() + " : " + server.getLocalPort());

            while (true) {
                Socket cliente = server.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress() + ":" + cliente.getPort());
                MenuLogger.escreverLog("Novo cliente conectado ao Servidor de Aplicação: " + cliente.getInetAddress().getHostAddress() + ":" + cliente.getPort());
                new Thread(new ClienteThread(cliente)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClienteThread implements Runnable {
        private Socket cliente;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ClienteThread(Socket cliente) {
            this.cliente = cliente;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(cliente.getOutputStream());
                in = new ObjectInputStream(cliente.getInputStream());

                while (true) {
                    Comando comando = (Comando) in.readObject();
                    String tipo = comando.getTipo();
                    String[] parametros = comando.getParametros();

                    switch (tipo) {
                        case "adicionar":
                            String nome = parametros[0];
                            String descricao = parametros[1];
                            OrdemServico os = new OrdemServico(nextId++, nome, descricao);
                            database.adicionar(os);
                            out.writeObject(os);
                            MenuLogger.escreverLog("ServerApp: Ordem de Serviço adicionada: " + nome);
                            break;
                        case "remover":
                            int idRemover = Integer.parseInt(parametros[0]);
                            boolean removido = database.remover(idRemover);
                            out.writeObject(removido ? "Ordem de serviço removida com sucesso." : "Ordem de serviço não encontrada.");
                            MenuLogger.escreverLog("ServerApp: Ordem de Serviço removida: " + idRemover);
                            break;
                        case "atualizar":
                            int idEditar = Integer.parseInt(parametros[0]);
                            String novoNome = parametros[1];
                            String novaDescricao = parametros[2];
                            OrdemServico osEditar = database.buscar(idEditar);
                            if (osEditar != null) {
                                osEditar.setNome(novoNome);
                                osEditar.setDescricao(novaDescricao);
                                out.writeObject("Ordem de serviço atualizada.");
                                MenuLogger.escreverLog("ServerApp: Ordem de Serviço atualizada: " + idEditar);
                            } else {
                                out.writeObject("Ordem de Serviço não encontrada.");
                            }
                            break;
                        case "listar":
                            out.writeObject(database.gerarStringDatabase());
                            System.out.println("Listando Ordens de Serviço");
                            MenuLogger.escreverLog("ServerApp: Listando Ordens de Serviço");
                            break;
                        case "buscar":
                            int idBuscar = Integer.parseInt(parametros[0]);
                            OrdemServico osBuscar = database.buscar(idBuscar);
                            MenuLogger.escreverLog("ServerApp: Buscando Ordem de Serviço: " + idBuscar);
                            System.out.println("Buscando Ordem de Serviço: " + idBuscar);
                            System.out.println(osBuscar != null ? osBuscar : "Ordem de serviço não encontrada.");
                            out.writeObject(osBuscar != null ? osBuscar : "Ordem de serviço não encontrada.");
                            break;
                        default:
                            out.writeObject("Comando inválido.");
                            break;
                    }
                    out.flush();
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

    public void inicializarOrdemServico() {
        for (int i = 0; i < 100; i++) {
            database.adicionar(new OrdemServico(nextId++, "OS " + i, "Descrição " + i));
        }
    }
}