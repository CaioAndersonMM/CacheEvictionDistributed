package Src.Server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Src.Server.Impl.BackupServerImpl;

public class ApplicationBackup {
     public static void main(String[] args) {
        try {
            // Não precisou de um ServerSocket
            BackupServerImpl backupServer = new BackupServerImpl();
            Registry registry = LocateRegistry.createRegistry(6055);
            registry.rebind("BackupServer", backupServer);
            System.out.println("Servidor de Backup rodando...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
