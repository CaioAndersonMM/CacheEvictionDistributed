package Src.Database;

import java.util.LinkedList;
import Src.OrdemServico;
import Src.MenuLogger;

public class CacheFIFO {
    private final int maxSize;
    private LinkedList<OrdemServico> cache;

    public CacheFIFO() {
        this.maxSize = 31;
        this.cache = new LinkedList<>();
    }

    public void adicionar(OrdemServico os) {
        synchronized(this) {
            if (cache.size() >= maxSize) {
                cache.removeFirst();
            }
            cache.addLast(os);
            MenuLogger.escreverLog("Cache: Ordem de Serviço adicionada: " + os.getCodigo());
            MenuLogger.escreverLog("Estado atual da cache: " + gerarStringCache());
        }
    }

    public OrdemServico buscar(int codigo) {
       
            for (OrdemServico os : cache) {
                if (os.getCodigo() == codigo) {
                    MenuLogger.escreverLog("Ordem de Serviço encontrada na cache: " + os);
                    return os;
                }
        }
        MenuLogger.escreverLog("Cache: Ordem de Serviço não encontrada: " + codigo);
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
        synchronized(this) {
            boolean removed = cache.removeIf(os -> os.getCodigo() == codigo);
            if (removed) {
                MenuLogger.escreverLog("Ordem de Serviço removida da cache: " + codigo);
            } else {
                MenuLogger.escreverLog("Ordem de Serviço não encontrada na cache para remoção: " + codigo);
            }
            MenuLogger.escreverLog("Estado atual da cache: " + gerarStringCache());
            return removed;
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