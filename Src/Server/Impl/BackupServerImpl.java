package Src.Server.Impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Src.MenuLogger;
import Src.OrdemServico;
import Src.Database.DatabaseOs;

public class BackupServerImpl extends UnicastRemoteObject implements BackupServerInterface {
    private DatabaseOs database;
    
    public BackupServerImpl() throws RemoteException {
        super();
        this.database = new DatabaseOs();
    }

    @Override
    public boolean backupDatabase(Object comando, OrdemServico os) throws RemoteException {
        try {

            if (os == null) {
                // Erro inesperado, deve sempre vim OS.
                return false;
            } else {
                if (comando instanceof String) {
                    String comandoString = (String) comando;
                    if (comandoString.equals("inserir")) {
                        database.adicionar(os);
                        MenuLogger.escreverLog("Ordem de serviço inserida no backup: " + os.getCodigo());
                        // Assuming listarDatabase() should return a String representation of the database
                        System.out.println(database.toString());
                        return true;
                    } else if (comandoString.equals("atualizar")) {
                        OrdemServico osn = database.buscar(os.getCodigo());
                        osn.setNome(os.getNome());
                        osn.setDescricao(os.getDescricao());
                        osn.setHoraSolicitacao(os.getHoraSolicitacao());
                        MenuLogger.escreverLog("Ordem de serviço atualizada no backup: " + os.getCodigo());
                        return true;
                    } else if (comandoString.equals("remover")) {
                        database.remover(os.getCodigo());
                        MenuLogger.escreverLog("Ordem de serviço removida no backup: " + os.getCodigo());
                        return true;
                    } else {
                        // Comando inválido
                        return false;
                    }
                } else {
                    return false;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void backupLog(String logContent) throws RemoteException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("backup_log.txt"))) {
            writer.write(logContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}