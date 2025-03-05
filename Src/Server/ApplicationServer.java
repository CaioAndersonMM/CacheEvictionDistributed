package Src.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import Src.Server.Impl.ApplicationServerImpl;

public class ApplicationServer {
    public static void main(String[] args) {
        int porta = 5055;
        try {
            new ApplicationServerImpl(porta, InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
