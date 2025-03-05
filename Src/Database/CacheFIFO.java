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
        synchronized(this){
        if (cache.size() >= maxSize) {
            cache.removeFirst();
        }
        cache.addLast(os);
    }
    }

    public OrdemServico buscar(int codigo) {
        synchronized(this){
        for (OrdemServico os : cache) {
            if (os.getCodigo() == codigo) { 
                return os;
            }
        }
        }
        return null;
    }

    public String listarCache() {
        StringBuilder sb = new StringBuilder();
        for (OrdemServico os : cache) {
            sb.append(os.toString()).append("\n");
        }
        return sb.toString();
    }
    

    public boolean remover(int codigo) {
        synchronized(this){
       return  cache.removeIf(os -> os.getCodigo() == codigo);
    }
    }

    public String gerarStringCache() {
        StringBuilder sb = new StringBuilder();
        for (OrdemServico os : cache) {
            sb.append(os.imprimir()).append(", ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "Cache vazia";
    }
}