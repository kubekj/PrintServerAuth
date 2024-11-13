package service;

import auth.AuthManager;
import auth.exceptions.AuthenticationException;
import auth.PasswordStorage;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

@Slf4j
public class PrintService extends UnicastRemoteObject implements IPrintService {
    private final AuthManager authManager;
    private final Map<String, Queue<PrintJob>> printerQueues;
    private final Map<String, String> configParameters;
    private boolean isRunning;

    public PrintService() throws RemoteException {
        super();
        this.authManager = new AuthManager(new PasswordStorage());
        this.printerQueues = new HashMap<>();
        this.configParameters = new HashMap<>();
        this.isRunning = true;
    }

    @Override
    public String print(String token, String filename, String printer) throws RemoteException {
        validateSessionOrThrow(token);

        if (!isRunning) return "Print server is not running";

        printerQueues.putIfAbsent(printer, new LinkedList<>());
        PrintJob job = new PrintJob(filename, printerQueues.get(printer).size() + 1);
        printerQueues.get(printer).offer(job);

        log.info("Print job added: {} for printer {}", filename, printer);
        return "Print job " + job.jobId() + " added to queue for " + printer;
    }

    private String printQueue(Queue<PrintJob> queue, String printer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Print queue for ").append(printer).append(":\n");
        queue.forEach(job -> sb.append(job.jobId()).append(" ").append(job.filename()).append("\n"));

        System.out.println("\n=== Current Queue for " + printer + " ===");
        System.out.println(sb);
        System.out.println("===============================\n");

        return sb.toString();
    }

    @Override
    public String queue(String token, String printer) throws RemoteException {
        validateSessionOrThrow(token);
        Queue<PrintJob> queue = printerQueues.get(printer);
        if (queue == null) return "No queue found for printer: " + printer;
        return printQueue(queue, printer);
    }

    @Override
    public String topQueue(String token, String printer, int job) throws RemoteException {
        validateSessionOrThrow(token);

        Queue<PrintJob> queue = printerQueues.get(printer);
        if (queue == null) return "No queue found for printer " + printer;

        Queue<PrintJob> newQueue = new LinkedList<>();
        PrintJob jobToMove = null;

        // Find the job and reconstruct queue
        for (PrintJob currentJob : queue) {
            if (currentJob.jobId() == job)
                jobToMove = currentJob;
            else
                newQueue.offer(currentJob);
        }

        if (jobToMove != null) {
            newQueue.add(jobToMove);
            printerQueues.put(printer, newQueue);

            System.out.println("\n=== Moving job " + job + " to top of queue ===");
            return "Moved job " + job + " to top of queue:\n" + printQueue(newQueue, printer);
        }

        return "Job " + job + " not found";
    }

    @Override
    public void start(String token) throws RemoteException {
        validateSessionOrThrow(token);
        this.isRunning = true;
        log.info("Print server started");
    }

    @Override
    public void stop(String token) throws RemoteException {
        validateSessionOrThrow(token);
        this.isRunning = false;
        log.info("Print server stopped");
    }

    @Override
    public void restart(String token) throws RemoteException {
        validateSessionOrThrow(token);
        this.isRunning = false;
        printerQueues.clear();
        this.isRunning = true;
        log.info("Print server restarted");
    }

    @Override
    public String status(String token, String printer) throws RemoteException {
        validateSessionOrThrow(token);
        return String.format("Printer %s - Status: %s, Queue size: %d",
                printer,
                isRunning ? "Running" : "Stopped",
                printerQueues.getOrDefault(printer, new LinkedList<>()).size()
        );
    }

    @Override
    public String readConfig(String token, String parameter) throws RemoteException {
        validateSessionOrThrow(token);
        return configParameters.getOrDefault(parameter, "Parameter not found");
    }

    @Override
    public String setConfig(String token, String parameter, String value) throws RemoteException {
        validateSessionOrThrow(token);
        configParameters.put(parameter, value);
        log.info("Configuration updated: {} = {}", parameter, value);
        return "Configuration updated";
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        try {
            return authManager.authenticate(username, password);
        } catch (AuthenticationException e) {
            log.error("Login failed for user: {}", username);
            throw new RemoteException("Authentication failed", e);
        }
    }

    @Override
    public void logout(String token) throws RemoteException {
        authManager.logout(token);
        log.info("User logged out: {}", token);
    }

    private void validateSessionOrThrow(String token) throws RemoteException {
        if (!authManager.validateSession(token)) {
            log.warn("Invalid or expired token: {}", token);
            throw new RemoteException("Invalid or expired token");
        }
    }

    // Inner record to represent print jobs
    private record PrintJob(String filename, int jobId) { }
}