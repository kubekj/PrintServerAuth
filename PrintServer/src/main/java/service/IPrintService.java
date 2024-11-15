package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPrintService extends Remote {
    // Base operations
    String print(String token, String filename, String printer) throws RemoteException;
    String queue(String token, String printer) throws RemoteException;
    String topQueue(String token, String printer, int job) throws RemoteException;
    void start(String token) throws RemoteException;
    void stop(String token) throws RemoteException;
    void restart(String token) throws RemoteException;
    String status(String token, String printer) throws RemoteException;
    String readConfig(String token, String parameter) throws RemoteException;
    String setConfig(String token, String parameter, String value) throws RemoteException;

    // Auth operations
    String login(String username, String password) throws RemoteException;
}
