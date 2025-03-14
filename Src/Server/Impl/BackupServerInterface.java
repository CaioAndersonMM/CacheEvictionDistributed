package Src.Server.Impl;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Src.OrdemServico;

public interface BackupServerInterface extends Remote {
    boolean backupDatabase(Object databaseContent, OrdemServico os) throws RemoteException;
    void backupLog(String logContent) throws RemoteException;
}