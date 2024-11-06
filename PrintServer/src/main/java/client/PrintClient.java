package client;

import lombok.extern.slf4j.Slf4j;
import service.IPrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@Slf4j
public class PrintClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5099; // Same port as server
    private IPrintService printService;
    private String sessionId;

    public void connect() {
        try {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            printService = (IPrintService) registry.lookup("PrintService");
            log.info("Connected to print server");
        } catch (Exception e) {
            log.error("Client connection failed", e);
            throw new RuntimeException("Failed to connect to server", e);
        }
    }

    public boolean login(String username, String password) {
        try {
            sessionId = printService.login(username, password);
            log.info("Successfully logged in as: {}", username);
            return true;
        } catch (Exception e) {
            log.error("Login failed", e);
            return false;
        }
    }

    public void print(String filename, String printer) {
        try {
            String result = printService.print(sessionId, filename, printer);
            log.info("Print result: {}", result);
        } catch (Exception e) {
            log.error("Print operation failed", e);
        }
    }

    public void logout() {
        try {
            if (sessionId != null) {
                printService.logout(sessionId);
                sessionId = null;
                log.info("Successfully logged out");
            }
        } catch (Exception e) {
            log.error("Logout failed", e);
        }
    }

    // Add a main method to test the client
    public static void main(String[] args) {
        PrintClient client = new PrintClient();
        try {
            client.connect();

            // Test login
            if (client.login("testuser", "password")) {
                // Test print operation
                client.print("test.txt", "printer1");

                // Test logout
                client.logout();
            }
        } catch (Exception e) {
            log.error("Error in client operations", e);
        }
    }
}