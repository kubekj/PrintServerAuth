package client;

import service.PrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PrintClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            PrintService service = (PrintService) registry.lookup("PrintService");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
