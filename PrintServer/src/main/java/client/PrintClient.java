package client;

import lombok.extern.slf4j.Slf4j;
import service.IPrintService;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@Slf4j
public class PrintClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5099;
    private static final String USERNAME = "alice";
    private static final String PASSWORD = "password123";

    private IPrintService printService;
    private String token;

    public void connect() {
        try {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            printService = (IPrintService) registry.lookup("PrintService");
            log.info("Connected to print server");
        } catch (Exception e) {
            log.error("Client connection failed: {}", e.getMessage());
        }
    }

    public void login(String username, String password) {
        try {
            token = printService.login(username, password);
            log.info("Login attempt completed for {}", username);
        } catch (RemoteException e) {
            log.error("Login failed: {}", e.getMessage());
        }
    }

    public String print(String filename, String printer) {
        try {
            return printService.print(token, filename, printer);
        } catch (RemoteException e) {
            return "Print operation failed";
        }
    }

    public String queue(String printer) {
        try {
            return printService.queue(token, printer);
        } catch (RemoteException e) {
            return "Queue operation failed";
        }
    }

    public String topQueue(String printer, int job) {
        try {
            return printService.topQueue(token, printer, job);
        } catch (RemoteException e) {
            return "TopQueue operation failed";
        }
    }

    public void restart() {
        try {
            printService.restart(token);
        } catch (RemoteException e) {
            log.error("Restart operation failed: {}", e.getMessage());
        }
    }

    // Main method to test the client
    public static void main(String[] args) {
        PrintClient client = new PrintClient();
        try {
            client.connect();
            client.login(USERNAME, PASSWORD);
            System.out.println(client.print("test.txt", "printer1"));
            System.out.println(client.print("test2.txt", "printer1"));
            System.out.println(client.queue("printer1"));
            System.out.println(client.topQueue("printer1", 1));
            client.restart();
        } catch (Exception e) {
            log.error("Error in client operations: {}", e.getMessage());
        }
    }
}
