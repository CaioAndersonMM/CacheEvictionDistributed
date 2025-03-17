package Src.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Scanner;
import Src.MenuLogger;
import Src.OrdemServico;
import Src.Comando;

public class ClienteImpl {
    private int port;
    private String locationHost;
    private int locationPort;
    private boolean executando = true;
    private Scanner scanner;

    public ClienteImpl(int port, String locationHost, int locationPort) {
        this.port = port;
        this.locationHost = locationHost;
        this.locationPort = locationPort;
        this.scanner = new Scanner(System.in);
        rodar();
    }

    public void rodar() {
        System.out.println("Cliente rodando na porta " + port);
        
        // Loop principal - continuará executando enquanto o cliente estiver ativo
        while (executando) {
            conectarAoServidorLocalizacao();
        }
        
        scanner.close();
        System.out.println("Cliente encerrado");
    }
    
    private void conectarAoServidorLocalizacao() {
        Socket locationSocket = null;

        while (locationSocket == null && executando) {
            try {
                locationSocket = new Socket(locationHost, locationPort);
                System.out.println("Conectado ao servidor de localização " + locationHost + ":" + locationPort);
                MenuLogger.escreverLog("Cliente: Conectado ao Servidor de Localização " + locationHost + ":" + locationPort);
            } catch (IOException e) {
                System.out.println("Servidor de localização não disponível, tentando novamente...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        if (!executando) return;

        try (ObjectOutputStream outLocation = new ObjectOutputStream(locationSocket.getOutputStream());
             ObjectInputStream inLocation = new ObjectInputStream(locationSocket.getInputStream())) {

            outLocation.writeObject("Novo cliente querendo conexão, envie localização");
            outLocation.flush();
            System.out.println("Pedido de localização enviado");
            MenuLogger.escreverLog("Cliente: Pedido de localização enviado ao Servidor de Localização");

            Object proxyLocationObj = inLocation.readObject();
            if (proxyLocationObj instanceof String) {
                String proxyLocation = (String) proxyLocationObj;
                System.out.println("Localização do Proxy recebida: " + proxyLocation);
                MenuLogger.escreverLog("Cliente: Localização do Proxy recebida: " + proxyLocation);

                // O proxy vai responder host:porta
                String[] proxyInfo = proxyLocation.split(":");
                String proxyHost = proxyInfo[0];
                int proxyPort = Integer.parseInt(proxyInfo[1]);
                conectarAoProxy(proxyHost, proxyPort);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro durante a comunicação com o servidor de localização: " + e.getMessage());
            MenuLogger.escreverLog("Cliente [" + port + "]: Erro durante a comunicação com o servidor de localização: " + e.getMessage());
        } finally {
            // Fechar o socket de localização
            if (locationSocket != null) {
                try {
                    locationSocket.close();
                    MenuLogger.escreverLog("Cliente [" + port + "]: Conexão com o servidor de localização fechada.");
                } catch (IOException e) {
                    System.err.println("Erro ao fechar o socket: " + e.getMessage());
                }
            }
        }
    }

    private void conectarAoProxy(String proxyHost, int proxyPort) {
        Socket proxySocket = null;
        try {
            proxySocket = new Socket();
            proxySocket.connect(new InetSocketAddress(proxyHost, proxyPort), 10000);
            System.out.println("Conectado ao Proxy " + proxyHost + ":" + proxyPort);
            MenuLogger.escreverLog("Cliente [" + port + "]: Conectado ao Proxy " + proxyHost + ":" + proxyPort);

            ObjectOutputStream outProxy = new ObjectOutputStream(proxySocket.getOutputStream());
            ObjectInputStream inProxy = new ObjectInputStream(proxySocket.getInputStream());

            Object respostaObj = receberResposta(inProxy);
            if (respostaObj instanceof String) {
                String resposta = (String) respostaObj;
                System.out.println("Resposta do Proxy: " + resposta);

                if (autenticarUsuario(outProxy, inProxy)) {
                    boolean continuarNoMesmoProxy = gerenciarMenu(outProxy, inProxy);
                    
                    // Se o usuário escolheu sair completamente
                    if (!continuarNoMesmoProxy) {
                        executando = false;
                    }
                }
            }
            
            outProxy.close();
            inProxy.close();
            
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Conexão com o proxy perdida: " + e.getMessage());
            MenuLogger.escreverLog("Cliente [" + port + "]: Conexão com o proxy perdida: " + e.getMessage());
            System.out.println("Tentando reconectar a um novo proxy...");
        } finally {
            // Fechar o socket do proxy
            if (proxySocket != null) {
                try {
                    proxySocket.close();
                    MenuLogger.escreverLog("Cliente [" + port + "]: Conexão com o proxy fechada");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean autenticarUsuario(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        int tentativas = 0;
        while (tentativas < 3) {
            System.out.println("Qual seu email? ");
            String email = scanner.nextLine();
            System.out.println("Qual sua senha? ");
            String senha = scanner.nextLine();
    
            String mensagem = email + ";" + senha;
            enviarComando(out, mensagem);
    
            Object respostaObj = receberResposta(in);
            if (respostaObj instanceof String) {
                String resposta = (String) respostaObj;
                System.out.println(resposta);
    
                if (resposta.equals("Cliente autenticado")) {
                    MenuLogger.escreverLog("Cliente [" + port + "]: Autenticação bem sucedida");
                    return true;
                } else {
                    MenuLogger.escreverLog("Cliente [" + port + "]: Autenticação falhou");
                    System.out.println("Autenticação falhou");
                    tentativas++;
                }
            }
        }
        System.out.println("Número máximo de tentativas atingido.");
        return false;
    }

    private boolean gerenciarMenu(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        while (true) {
            System.out.println(MenuLogger.exibirMenu());
            String opcao = scanner.nextLine();

            try {
                switch (OpcaoMenu.fromValor(opcao)) {
                    case ADICIONAR:
                        adicionarOrdemServico(out, in);
                        break;
                    case LISTAR:
                        listarOrdensServico(out, in);
                        break;
                    case ALTERAR:
                        alterarOrdemServico(out, in);
                        break;
                    case EXCLUIR:
                        excluirOrdemServico(out, in);
                        break;
                    case EXIBIRCACHE:
                        exibirCache(out, in);
                        break;
                    case BUSCAR:
                        buscarOrdemServico(out, in);
                        break;
                    case SAIR:
                        System.out.println("Escolha uma opção:");
                        System.out.println("1 - Desconectar deste proxy e conectar a outro");
                        System.out.println("0 - Sair completamente");
                        String escolha = scanner.nextLine();
                        
                        enviarComando(out, new Comando("sair"));
                        MenuLogger.escreverLog("Cliente [" + port + "]: Desconectado do Proxy");
                        
                        if (escolha.equals("0")) {
                            return false; // Sair completamente
                        } else {
                            return true;  // Reconectar a outro proxy
                        }
                    default:
                        System.out.println("Opção inválida");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Opção inválida. Por favor, tente novamente.");
            }
        }
    }

    private void adicionarOrdemServico(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        System.out.println("Digite o nome da ordem de serviço: ");
        String nome = scanner.nextLine();
        System.out.println("Digite a descrição da ordem de serviço: ");
        String descricao = scanner.nextLine();
        Comando comandoAdicionar = new Comando("adicionar", nome, descricao);
        enviarComando(out, comandoAdicionar);

        Object respostaAdicionar = receberResposta(in);
        if (respostaAdicionar instanceof OrdemServico) {
            System.out.println("Ordem de Serviço adicionada: " + respostaAdicionar);
        }
    }

    private void buscarOrdemServico(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        System.out.println("Digite o código da ordem de serviço que deseja buscar: ");
        int codigo = validarEntradaInteira();
        Comando comandoBuscar = new Comando("buscar", String.valueOf(codigo));
        enviarComando(out, comandoBuscar);

        Object respostaBuscar = receberResposta(in);
        if (respostaBuscar instanceof OrdemServico) {
            System.out.println("Ordem de Serviço encontrada: " + respostaBuscar);
        } else if (respostaBuscar instanceof String) {
            System.out.println((String) respostaBuscar);
        }
    }

    private void exibirCache(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Comando comandoListarCache = new Comando("listarCache");
        enviarComando(out, comandoListarCache);

        Object respostaListarCache = receberResposta(in);
        if (respostaListarCache instanceof String) {
            System.out.println((String) respostaListarCache);
        }
    }

    private void listarOrdensServico(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Comando comandoListar = new Comando("listar");
        enviarComando(out, comandoListar);

        Object respostaListar = receberResposta(in);
        if (respostaListar instanceof String) {
            System.out.println((String) respostaListar);
        }
    }

    private void alterarOrdemServico(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        System.out.println("Digite o código da ordem de serviço que deseja alterar: ");
        int codigo = validarEntradaInteira();
        System.out.println("Digite o novo nome da ordem de serviço: ");
        String nome = scanner.nextLine();
        System.out.println("Digite a nova descrição da ordem de serviço: ");
        String descricao = scanner.nextLine();
        Comando comandoAlterar = new Comando("atualizar", String.valueOf(codigo), nome, descricao);
        enviarComando(out, comandoAlterar);

        Object respostaAlterar = receberResposta(in);
        if (respostaAlterar instanceof String) {
            System.out.println((String) respostaAlterar);
        }
    }

    private void excluirOrdemServico(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        System.out.println("Digite o código da ordem de serviço que deseja excluir: ");
        int codigo = validarEntradaInteira();
        Comando comandoExcluir = new Comando("remover", String.valueOf(codigo));
        enviarComando(out, comandoExcluir);

        Object respostaRemover = receberResposta(in);
        if (respostaRemover instanceof String) {
            System.out.println((String) respostaRemover);
        }
    }

    private int validarEntradaInteira() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número válido.");
            }
        }
    }

    private void enviarComando(ObjectOutputStream out, Object comando) throws IOException {
        out.writeObject(comando);
        out.flush();
    }

    private Object receberResposta(ObjectInputStream in) throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    enum OpcaoMenu {
        ADICIONAR("1"),
        LISTAR("2"),
        ALTERAR("3"),
        EXCLUIR("4"),
        EXIBIRCACHE("5"),
        BUSCAR("6"),
        SAIR("0");

        private final String valor;

        OpcaoMenu(String valor) {
            this.valor = valor;
        }

        public static OpcaoMenu fromValor(String valor) {
            for (OpcaoMenu opcao : OpcaoMenu.values()) {
                if (opcao.valor.equals(valor)) {
                    return opcao;
                }
            }
            throw new IllegalArgumentException("Opção inválida: " + valor);
        }
    }
}