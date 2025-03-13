package Src.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import Src.Server.Impl.LocationServerImpl;

public class LocationServer{
    public static void main(String[] args) throws RemoteException {

        try {
            new LocationServerImpl(5000, InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
