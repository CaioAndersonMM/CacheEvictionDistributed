package Src;

public class Menu {

    public static String exibirMenu() {
        StringBuilder menu = new StringBuilder();
        menu.append("1 - Cadastrar Ordem de Serviço\n");
        menu.append("2 - Listar Ordens de Serviço\n");
        menu.append("3 - Alterar Ordem de Serviço\n");
        menu.append("4 - Excluir Ordem de Serviço\n");
        menu.append("5 - Exibir Cache\n");
        menu.append("6 - Exibir Banco de Dados\n");
        menu.append("0 - Sair\n");
        return menu.toString();
    }
}