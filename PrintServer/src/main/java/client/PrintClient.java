package client;

import lombok.extern.slf4j.Slf4j;
import service.IPrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

@Slf4j
public class PrintClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5099;

    private IPrintService printService;
    private String token = null;
    private String username = null;

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
            this.token = printService.login(username, password);
            this.username = username;
            log.info("Login attempt completed for {}", username);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
        }
    }

    public String print(String filename, String printer) {
        try {
            return printService.print(this.token, this.username, filename, printer);
        } catch (Exception e) {
            return "Print operation failed: " + e.getMessage();
        }
    }

    public String queue(String printer) {
        try {
            return printService.queue(this.token, this.username, printer);
        } catch (Exception e) {
            return "Queue operation failed: " + e.getMessage();
        }
    }

    public String topQueue(String printer, int job) {
        try {
            return printService.topQueue(this.token, this.username, printer, job);
        } catch (Exception e) {
            return "TopQueue operation failed: " + e.getMessage();
        }
    }

    public void restart() {
        try {
            printService.restart(this.token, this.username);
        } catch (Exception e) {
            log.error("Restart operation failed: {}", e.getMessage());
        }
    }

    public void start() {
        try {
            printService.start(this.token, this.username);
        } catch (Exception e) {
            log.error("Start operation failed: {}", e.getMessage());
        }
    }

    public void stop() {
        try {
            printService.start(this.token, this.username);
        } catch (Exception e) {
            log.error("Stop operation failed: {}", e.getMessage());
        }
    }

    public String status(String printer) {
        try {
            return printService.status(this.token, this.username, printer);
        } catch (Exception e) {
            return "Status operation failed: " + e.getMessage();
        }
    }

    public String readConfig(String parameter) {
        try {
            return printService.readConfig(this.token, this.username, parameter);
        } catch (Exception e) {
            return "Read Config operation failed: " + e.getMessage();
        }
    }

    public String setConfig(String parameter, String value) {
        try {
            return printService.setConfig(this.token, this.username, parameter, value);
        } catch (Exception e) {
            return "Set Config operation failed: " + e.getMessage();
        }
    }


    // Main method to test the client
    public static void main(String[] args) {
        PrintClient client = new PrintClient();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Print Server!");

        try {
            client.connect();
            while (client.token == null) {
                System.out.print("Enter username: ");
                String username = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                client.login(username, password);
                System.out.println("Login successful!");
            }
            while (true) {
                System.out.print("\nEnter a command (or 'exit' to quit): ");
                String command = scanner.nextLine().trim();

                if (command.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                switch (command) {
                    case "start":
                        client.start();
                        break;
                    case "stop":
                        client.stop();
                        break;
                    case "restart":
                        client.restart();
                        break;
                    case "status":
                        System.out.print("Enter printer ID: ");
                        String printer = scanner.nextLine().trim();
                        System.out.print(client.status(printer));
                        break;
                    case "readConfig":
                        System.out.print("Enter parameter name: ");
                        String parameter = scanner.nextLine().trim();
                        System.out.print(client.readConfig(parameter));
                        break;
                    case "setConfig":
                        System.out.print("Enter parameter name: ");
                        String updateParameter = scanner.nextLine().trim();
                        System.out.print("Enter parameter value: ");
                        String updateValue = scanner.nextLine().trim();
                        System.out.print(client.setConfig(updateParameter, updateValue));
                        break;
                    case "print":
                        System.out.print("Enter filename: ");
                        String filename = scanner.nextLine().trim();
                        System.out.print("Enter printer ID: ");
                        String printerId = scanner.nextLine().trim();
                        System.out.print(client.print(filename, printerId));
                        break;
                    case "queue":
                        System.out.print("Enter printer ID: ");
                        String printerForQueue = scanner.nextLine().trim();
                        System.out.print(client.queue(printerForQueue));
                        break;
                    case "topQueue":
                        System.out.print("Enter printer ID: ");
                        String printerTop = scanner.nextLine().trim();
                        System.out.print("Enter job ID: ");
                        int jobId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print(client.topQueue(printerTop, jobId));
                        break;
                    default:
                        System.out.println("Unknown command. Please try again.");
                }
            }
        } catch (Exception e) {
            log.error("Error in client operations: {}", e.getMessage());
        }
    }
}