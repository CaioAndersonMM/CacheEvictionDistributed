package Src.Server.Impl;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import Src.OrdemServico;
import Src.Database.DatabaseOs;

public class ApplicationServerImpl {
    private int porta;
    private DatabaseOs database;
    private int nextId;

    public ApplicationServerImpl(int porta) {
        this.porta = porta;
        this.nextId = 1;
        this.database = new DatabaseOs();
        rodar();
    }

    public void rodar() {
        try {
            ServerSocket server = new ServerSocket(porta);
            System.out.println("Servidor de Aplicação rodando " + InetAddress.getLocalHost().getHostAddress() + " : " + porta);

            while (true) {
                Socket cliente = server.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
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
                    String mensagem = (String) in.readObject();
                    String[] partes = mensagem.split(";");
                    String comando = partes[0];

                    switch (comando) {
                        case "adicionar":
                            String nome = partes[1];
                            String descricao = partes[2];
                            OrdemServico os = new OrdemServico(nextId++, nome, descricao);
                            database.adicionar(os);
                            out.writeObject(os);
                            break;
                        case "remover":
                            int idRemover = Integer.parseInt(partes[1]);
                            boolean removido = database.remover(idRemover);
                            out.writeObject(removido ? "Ordem de serviço removida com sucesso." : "Ordem de serviço não encontrada.");
                            break;
                        case "editar":
                            int idEditar = Integer.parseInt(partes[1]);
                            String novoNome = partes[2];
                            String novaDescricao = partes[3];
                            OrdemServico osEditar = database.buscar(idEditar);
                            if (osEditar != null) {
                                osEditar.setNome(novoNome);
                                osEditar.setDescricao(novaDescricao);
                                out.writeObject(osEditar);
                            } else {
                                out.writeObject("Ordem de serviço não encontrada.");
                            }
                            break;
                        case "listar":
                            out.writeObject(database.gerarStringDatabase());
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
}
