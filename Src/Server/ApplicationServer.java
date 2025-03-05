package Src.Server;

import Src.Server.Impl.ApplicationServerImpl;

public class ApplicationServer {
    public static void main(String[] args) {
        int porta = 5055;
        new ApplicationServerImpl(porta);
    }
}
