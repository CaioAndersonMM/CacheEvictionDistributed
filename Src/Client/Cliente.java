package Src.Client;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Cliente {
    public static void main(String[] args) {
        String host = "127.0.1.1";
        new ClienteImpl(5001, host, 5000);
    }
}
