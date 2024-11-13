package client;

import lombok.extern.slf4j.Slf4j;
import service.IPrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@Slf4j
public class PrintClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5099;
    private static final String USERNAME = "alice";
    private static final String PASSWORD = "password123";
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

    public void login(String username, String password) {
        try {
            sessionId = printService.login(username, password);
            log.info("Successfully logged in as: {}", username);
        } catch (Exception e) {
            log.error("Login failed", e);
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
            if (sessionId == null) return;
            printService.logout(sessionId);
            sessionId = null;
            log.info("Successfully logged out");
        } catch (Exception e) {
            log.error("Logout failed", e);
        }
    }

    // Main method to test the client
    public static void main(String[] args) {
        PrintClient client = new PrintClient();
        try {
            client.connect();
            client.login(USERNAME, PASSWORD);
            client.print("test.txt", "printer1");
            client.logout();
        } catch (Exception e) {
            log.error("Error in client operations", e);
        }
    }
}