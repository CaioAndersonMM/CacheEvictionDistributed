package Src.Client;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Cliente {
    public static void main(String[] args) {
        try {
            new ClienteImpl(5001, InetAddress.getLocalHost().getHostAddress(), 5000);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
