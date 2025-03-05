package Src.Server;

import Src.Server.Impl.ProxyImpl;

public class ProxyServer {
    public static void main(String[] args) {

        new ProxyImpl(5005, "192.168.0.100", 5055);
    }
}
