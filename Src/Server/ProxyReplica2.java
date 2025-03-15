package Src.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import Src.Server.Impl.ProxyImpl;

public class ProxyReplica2 {
    public static void main(String[] args) throws RemoteException {
        try {
            new ProxyImpl(5030, InetAddress.getLocalHost().getHostAddress(), 5055);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
