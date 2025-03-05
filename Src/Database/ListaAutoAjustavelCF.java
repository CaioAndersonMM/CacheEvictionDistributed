package Src.Database;
import Src.OrdemServico;

public class ListaAutoAjustavelCF {
    No primeiro;

    public ListaAutoAjustavelCF() {
        this.primeiro = null;
    }

    public void inserir(OrdemServico os) {

        No novo = new No(os);
        novo.proximo = primeiro;
        primeiro = novo;
    }

    public OrdemServico buscar(int codigo) {
        No atual = primeiro;
        No anterior = null; 

        while (atual != null) {
            if (atual.valor.getCodigo() == codigo) {
                atual.frequencia++;

                if (anterior != null && atual.frequencia > anterior.frequencia) {
                    anterior.proximo = atual.proximo;
                    atual.proximo = primeiro;
                    primeiro = atual;
                }

                return atual.valor; 
            }

            anterior = atual;
            atual = atual.proximo;
        }

        return null;
    }


    public OrdemServico remover(int codigo) {
        No atual = primeiro;
        No anterior = null;

        while (atual != null) {
            if (atual.valor.getCodigo() == codigo) {
                if (anterior == null) {
                    primeiro = atual.proximo;
                } else {
                    anterior.proximo = atual.proximo; 
                }

                return atual.valor;
            }
            anterior = atual;
            atual = atual.proximo;
        }
        return null; 
    }

  
    public void imprimirLista() {
        No atual = primeiro;
        while (atual != null) {
            System.out.println("OS de código " + atual.valor.getCodigo() + " - Frequência: " + atual.frequencia);
            atual = atual.proximo;
        }
        System.out.println("--------------------------------------------------------------------");
    }

}

