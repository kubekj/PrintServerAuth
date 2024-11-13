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
    private String jwtToken;
    private boolean isAuthenticated;

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
            String token = printService.login(username, password);
            if (token != null && !token.isEmpty()) {
                this.jwtToken = token;
                this.isAuthenticated = true;
                log.info("Successfully logged in as {}", username);
            }
        } catch (RemoteException e) {
            log.error("Login failed: {}", e.getMessage());
        }
    }

    public String print(String filename, String printer) {
        if (!validateState()) return "Not authenticated";

        try {
            return printService.print(jwtToken, filename, printer);
        } catch (RemoteException e) {
            isAuthenticated = false;
            jwtToken = null;
            return "Print failed - please login again";
        }
    }

    public void logout() {
        if (!isAuthenticated || jwtToken == null) {
            log.warn("Not logged in");
            return;
        }

        try {
            printService.logout(jwtToken);
            log.info("Successfully logged out");
            isAuthenticated = false;
            jwtToken = null;
        } catch (RemoteException e) {
            log.error("Logout failed: {}", e.getMessage());
        }
    }

    private boolean validateState() {
        if (!isAuthenticated || jwtToken == null) {
            log.error("Not authenticated. Please login first.");
            return false;
        }
        return true;
    }

    public String queue(String printer) {
        if (!validateState()) return "Not authenticated";
        try {
            return printService.queue(jwtToken, printer);
        } catch (RemoteException e) {
            return "Queue operation failed";
        }
    }

    public String topQueue(String printer, int job) {
        if (!validateState()) return "Not authenticated";
        try {
            return printService.topQueue(jwtToken, printer, job);
        } catch (RemoteException e) {
            return "TopQueue operation failed";
        }
    }

    public void restart() {
        if (!validateState()) log.warn("Not authenticated");
        try {
            printService.restart(jwtToken);
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
            client.logout();
        } catch (Exception e) {
            log.error("Error in client operations: {}", e.getMessage());
        } finally {
            if (client.isAuthenticated) client.logout();
        }
    }
}