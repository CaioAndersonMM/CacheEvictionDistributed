package Src.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import Src.Server.Impl.LocationServerImpl;

public class LocationServer {
    public static void main(String[] args) {
        String proxyHost = "0.0.0.0";
        int proxyPort = 5005;
        try {
            new LocationServerImpl(5000, InetAddress.getLocalHost().getHostAddress(), proxyPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
