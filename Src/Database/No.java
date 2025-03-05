package Src.Database;

import Src.OrdemServico;

public class No {
    OrdemServico valor;
    No proximo;
    int frequencia;

    public No(OrdemServico valor) {
        this.valor = valor;
        this.frequencia = 1;
        this.proximo = null;
    }
}