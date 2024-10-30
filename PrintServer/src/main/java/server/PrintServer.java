package server;

import service.PrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PrintServer {
    public static void main(String[] args) {
        try {
            PrintService printService = new PrintService();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("PrintService", printService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
