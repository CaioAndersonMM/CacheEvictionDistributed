package Src.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import Src.Server.Impl.ProxyImpl;

public class ProxyServer {
    public static void main(String[] args) {

        try {
            new ProxyImpl(5005, InetAddress.getLocalHost().getHostAddress(), 5055);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
