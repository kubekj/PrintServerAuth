package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPrintService extends Remote {
    // Base operations
    String print(String token, String username, String filename, String printer) throws RemoteException;
    String queue(String token, String username, String printer) throws RemoteException;
    String topQueue(String token, String username, String printer, int job) throws RemoteException;
    void start(String token, String username) throws RemoteException;
    void stop(String token, String username) throws RemoteException;
    void restart(String token, String username) throws RemoteException;
    String status(String token, String username, String printer) throws RemoteException;
    String readConfig(String token, String username, String parameter) throws RemoteException;
    String setConfig(String token, String username, String parameter, String value) throws RemoteException;

    // Auth operations
    String login(String username, String password) throws RemoteException;
}