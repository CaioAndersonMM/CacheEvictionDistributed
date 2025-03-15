package Src.Client;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Cliente {
    public static void main(String[] args) {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
            new ClienteImpl(5001, host, 5002);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        
    }
}
