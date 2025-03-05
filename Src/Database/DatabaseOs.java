package Src.Database;

import Src.OrdemServico;

public class DatabaseOs {
    private TabelaHashEncadementoExterior database;

    public DatabaseOs() {
        this.database = new TabelaHashEncadementoExterior(127);
    }

    public void adicionar(OrdemServico os) {
        database.inserir(os);
    }

    public OrdemServico buscar(int codigo) {
        return database.buscar(codigo);
    }

    public boolean remover(int codigo) {
        return database.remover(codigo) != null;
    }

    public void listarDatabase() {
       database.imprimirOSsEmOrdem();
    }

    public String gerarStringDatabase() {
        StringBuilder sb = new StringBuilder();
        for (OrdemServico os : database) {
            sb.append(os.imprimir()).append(", ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "Database vazia";
    }

    public boolean isEmpty() {
        return database.isEmpty();
    }

    public boolean existe(int codigo) {
        return database.buscar(codigo) != null;
    }

    public int tamanho() {
        return database.espacosOcupados;
    }
}
