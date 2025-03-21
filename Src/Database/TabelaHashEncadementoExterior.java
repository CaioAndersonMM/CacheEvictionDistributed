package Src.Database;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Src.MenuLogger;
import Src.OrdemServico;

public class TabelaHashEncadementoExterior implements Iterable<OrdemServico> {
    int espacosOcupados;
    int M;
    ListaAutoAjustavelCF[] tabela;

    public TabelaHashEncadementoExterior(int tam) {
        this.M = tam;
        this.tabela = new ListaAutoAjustavelCF[this.M];
        this.espacosOcupados = 0;

        for (int i = 0; i < this.M; i++) {
            this.tabela[i] = new ListaAutoAjustavelCF();
        }
    }

    public int hash(int ch) {
        return ch % this.M;
    }

    public void inserir(OrdemServico os) {
        int h = this.hash(os.getCodigo());
        this.tabela[h].inserir(os);
        this.espacosOcupados++;

        if (this.espacosOcupados / (double) this.M > 5.0) {
            MenuLogger.escreverLog("Redimensionando a tabela de " + this.M + " para um novo tamanho.");
            ListaAutoAjustavelCF[] tabelaAntiga = this.tabela;

            this.M = encontrarPrimoAbaixoProximaPotenciaDe2(this.M * 2);
            this.tabela = new ListaAutoAjustavelCF[this.M];
            this.espacosOcupados = 0;

            for (ListaAutoAjustavelCF listaAntiga : tabelaAntiga) {
                if (listaAntiga != null) {
                    No atual = listaAntiga.primeiro;
                    while (atual != null) {
                        this.inserir(atual.valor);
                        atual = atual.proximo;
                    }
                }
            }
            MenuLogger.escreverLog("Tabela redimensionada para: " + this.M);
        }
    }

    public OrdemServico buscar(int codigo) {
        int h = this.hash(codigo);
        return this.tabela[h].buscar(codigo);
    }

    public OrdemServico remover(int codigo) {
        int h = this.hash(codigo);
        return this.tabela[h].remover(codigo);
    }

    public boolean isEmpty() {
        return espacosOcupados == 0;
    }

    public void imprimirTabelaHash() {
        for (int i = 0; i < this.M; i++) {
            System.out.println("Lista na posição " + i + ":");
            this.tabela[i].imprimirLista();
        }
    }

    public int encontrarPrimoAbaixoProximaPotenciaDe2(int n) {
        int potencia2 = 1;
        while (potencia2 < n) {
            potencia2 *= 2;
        }

        for (int i = potencia2 - 1; i > 1; i--) {
            if (ehPrimo(i)) {
                return i;
            }
        }
        return -1;
    }

    public boolean ehPrimo(int n) {
        if (n <= 1) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    public void imprimirOSsEmOrdem() {
        List<OrdemServico> listaOSs = new ArrayList<>();

        for (ListaAutoAjustavelCF lista : tabela) {
            No atual = lista.primeiro;
            while (atual != null) {
                listaOSs.add(atual.valor);
                atual = atual.proximo;
            }
        }

        listaOSs.sort(Comparator.comparingInt(OrdemServico::getCodigo));

        System.out.println("Todas as OSs da base de dados em ordem:");
        for (OrdemServico os : listaOSs) {
            System.out.println(os);
        }
    }

    @Override
    public Iterator<OrdemServico> iterator() {
        List<OrdemServico> listaOSs = new ArrayList<>();
        for (ListaAutoAjustavelCF lista : tabela) {
            No atual = lista.primeiro;
            while (atual != null) {
                listaOSs.add(atual.valor);
                atual = atual.proximo;
            }
        }
        return listaOSs.iterator();
    }
}
