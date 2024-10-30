package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPrintService extends Remote {
    void print(String filename, String printer) throws RemoteException;
    String queue(String printer) throws RemoteException;
}
