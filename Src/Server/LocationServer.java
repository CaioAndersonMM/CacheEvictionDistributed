package Src.Server;

import Src.Server.Impl.LocationServerImpl;

public class LocationServer {
    public static void main(String[] args) {
        String proxyHost = "192.168.0.101";
        int proxyPort = 5005;
        new LocationServerImpl(5000, proxyHost, proxyPort);
    }
    
}
