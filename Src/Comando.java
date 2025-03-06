package Src;

import java.io.Serializable;

public class Comando implements Serializable {
    private String tipo;
    private String[] parametros;

    public Comando(String tipo, String... parametros) {
        this.tipo = tipo;
        this.parametros = parametros;
    }

    public String getTipo() {
        return tipo;
    }

    public String[] getParametros() {
        return parametros;
    }
}