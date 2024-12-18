package server;

import lombok.extern.slf4j.Slf4j;
import service.PrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

@Slf4j
public class PrintServer {
    private static final int PORT = 5099;
    private PrintService printService;

    public void start() {
        try {
            Registry registry;

            try {
                registry = LocateRegistry.createRegistry(PORT);
                log.info("RMI registry created on port {}", PORT);
            } catch (RemoteException e) {
                log.info("RMI registry already exists, getting existing one");
                registry = LocateRegistry.getRegistry(PORT);
            }

            printService = new PrintService();
            registry.rebind("PrintService", printService);

            log.info("PrintServer is running on port {}", PORT);
            log.info("Press Ctrl+C to shut down.");
        } catch (Exception e) {
            log.error("PrintServer startup failed", e);
            System.exit(1);
        }
    }

    public void stop() {
        try {
            if (printService == null) return;
            Registry registry = LocateRegistry.getRegistry(PORT);
            registry.unbind("PrintService");
            log.info("PrintServer stopped successfully");
        } catch (Exception e) {
            log.error("Error stopping PrintServer", e);
        }
    }

    //Main method to run the server
    public static void main(String[] args) {
        PrintServer server = new PrintServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down PrintServer...");
            server.stop();
        }));

        server.start();
    }
}
