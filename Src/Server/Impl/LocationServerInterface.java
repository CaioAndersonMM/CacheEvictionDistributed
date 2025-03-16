package Src.Server.Impl;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface LocationServerInterface extends Remote {
    void registerProxy(String proxyName) throws RemoteException;
    List<String> getProxies() throws RemoteException;
    void removerProxy(String proxyName) throws RemoteException;
}