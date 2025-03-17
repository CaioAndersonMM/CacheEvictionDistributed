package Src.Server.Impl;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import Src.OrdemServico;

public interface ProxyRMI extends Remote {
    String verificarStatus() throws RemoteException;
    OrdemServico buscar(int codigobusca) throws RemoteException;
    void notificarNovoProxy(String novoProxy) throws RemoteException;
    String receberMensagem(Object message) throws RemoteException, UnknownHostException;
    void sincronizarCache(String operacao, OrdemServico os) throws RemoteException;
    void removerProxy(String proxyName) throws RemoteException;
}
