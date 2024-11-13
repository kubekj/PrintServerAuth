package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPrintService extends Remote {
    // Base operations
    String print(String sessionId, String filename, String printer) throws RemoteException;
    String queue(String sessionId, String printer) throws RemoteException;
    String topQueue(String sessionId, String printer, int job) throws RemoteException;
    void start(String sessionId) throws RemoteException;
    void stop(String sessionId) throws RemoteException;
    void restart(String sessionId) throws RemoteException;
    String status(String sessionId, String printer) throws RemoteException;
    String readConfig(String sessionId, String parameter) throws RemoteException;
    String setConfig(String sessionId, String parameter, String value) throws RemoteException;

    // Auth operations
    String login(String username, String password) throws RemoteException;
    void logout(String sessionId) throws RemoteException;
}
