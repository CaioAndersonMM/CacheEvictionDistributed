package Src.Database;

import java.util.HashMap;

import Src.OrdemServico;

public class DatabaseOs {
    private HashMap<Integer, OrdemServico> database;

    public DatabaseOs() {
        this.database = new HashMap<>();
    }

    public void adicionar(OrdemServico os) {
        database.put(os.getCodigo(), os);
    }

    public OrdemServico buscar(int codigo) {
        return database.get(codigo);
    }

    public void remover(int codigo) {
        database.remove(codigo);
    }

    public void listarDatabase() {
        for (OrdemServico os : database.values()) {
            System.out.println(os);
        }
    }

    public String gerarStringDatabase() {
        StringBuilder sb = new StringBuilder();
        for (OrdemServico os : database.values()) {
            sb.append(os.imprimir()).append(", ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "Database vazia";
    }

    public boolean existe(int codigo) {
        return database.containsKey(codigo);
    }

    public int tamanho() {
        return database.size();
    }
}
