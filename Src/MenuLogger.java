package Src;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MenuLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String exibirMenu() {
        StringBuilder menu = new StringBuilder();
        menu.append("1 - Cadastrar Ordem de Serviço\n");
        menu.append("2 - Listar Ordens de Serviço\n");
        menu.append("3 - Alterar Ordem de Serviço\n");
        menu.append("4 - Excluir Ordem de Serviço\n");
        menu.append("5 - Exibir Cache\n");
        menu.append("6 - Buscar Ordem de Serviço\n");
        menu.append("0 - Sair\n");
        return menu.toString();
    }

    public static void escreverLog(String mensagem) {
        synchronized (MenuLogger.class) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("Src/log.txt", true))) {
                String timestamp = LocalDateTime.now().format(formatter);
                writer.write(timestamp + " - " + mensagem);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}