package Src.Database;

import java.util.LinkedList;

import Src.OrdemServico;

public class CacheFIFO {
    private final int maxSize;
    private LinkedList<OrdemServico> cache;

    public CacheFIFO() {
        this.maxSize = 31;
        this.cache = new LinkedList<>();
    }

    public void adicionar(OrdemServico os) {
        if (cache.size() >= maxSize) {
            cache.removeFirst();
        }
        cache.addLast(os);
    }

    public OrdemServico buscar(int codigo) {
        for (OrdemServico os : cache) {
            if (os.getCodigo() == codigo) { 
                return os;
            }
        }
        return null;
    }

    public void listarCache() {
        for (OrdemServico os : cache) {
            System.out.println(os);
        }
    }

    public void remover(int codigo) {
        cache.removeIf(os -> os.getCodigo() == codigo);
    }

    public String gerarStringCache() {
        StringBuilder sb = new StringBuilder();
        for (OrdemServico os : cache) {
            sb.append(os.imprimir()).append(", ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "Cache vazia";
    }
}