package service;

import auth.Session;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPrintService extends Remote {
    // Base operations
    String print(Session session, String filename, String printer) throws RemoteException;
    String queue(Session session, String printer) throws RemoteException;
    String topQueue(Session session, String printer, int job) throws RemoteException;
    void start(Session session) throws RemoteException;
    void stop(Session session) throws RemoteException;
    void restart(Session session) throws RemoteException;
    String status(Session session, String printer) throws RemoteException;
    String readConfig(Session session, String parameter) throws RemoteException;
    String setConfig(Session session, String parameter, String value) throws RemoteException;

    // Auth operations
    Session login(String username, String password) throws RemoteException;
    void logout(Session session) throws RemoteException;
}
