package service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class PrintService extends UnicastRemoteObject implements IPrintService {

    public PrintService() throws RemoteException { }

    @Override
    public void print(String filename, String printer) throws RemoteException {

    }

    @Override
    public String queue(String printer) throws RemoteException {
        return "";
    }
}
