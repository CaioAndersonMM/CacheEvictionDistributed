package Src.Server.Impl;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Src.OrdemServico;

public interface ProxyRMI extends Remote {
    String sincronizarCache(String dados) throws RemoteException;
    String verificarStatus() throws RemoteException;
    OrdemServico buscar(int codigobusca) throws RemoteException;
    void notificarNovoProxy(String novoProxy) throws RemoteException;
    Object receberMensagem(Object message) throws RemoteException;
}
