package service;

import auth.AuthManager;
import auth.Session;
import auth.exceptions.AuthenticationException;
import auth.PasswordStorage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

@Slf4j
public class PrintService extends UnicastRemoteObject implements IPrintService {
    private final AuthManager authManager;
    private final String policyFilePath = "policy.json";
    private final Map<String, Queue<PrintJob>> printerQueues;
    private final Map<String, String> configParameters;
    private boolean isRunning;

    public PrintService() throws RemoteException, IOException {
        super();
        this.authManager = new AuthManager(policyFilePath, new PasswordStorage());
        this.printerQueues = new HashMap<>();
        this.configParameters = new HashMap<>();
        this.isRunning = true;
    }

    @Override
    public String print(Session session, String filename, String printer) throws RemoteException, SecurityException {
        validateSessionOrThrow(session.getToken());
        authorizeOrThrow(session.getUsername(), "print");

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
    public String queue(Session session, String printer) throws RemoteException, SecurityException {
        validateSessionOrThrow(session.getToken());
        authorizeOrThrow(session.getUsername(), "print");
        Queue<PrintJob> queue = printerQueues.get(printer);
        if (queue == null) return "No queue found for printer: " + printer;
        return printQueue(queue, printer);
    }

    @Override
    public String topQueue(Session session, String printer, int job) throws RemoteException, SecurityException {
        validateSessionOrThrow(session.getToken());
        authorizeOrThrow(session.getUsername(), "topQueue");

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
    public void start(Session session) throws RemoteException, SecurityException {
        validateSessionOrThrow(session.getToken());
        authorizeOrThrow(session.getUsername(), "start");
        this.isRunning = true;
        log.info("Print server started");
    }

    @Override
    public void stop(Session session) throws RemoteException, SecurityException {
        validateSessionOrThrow(session.getToken());
        authorizeOrThrow(session.getUsername(), "stop");
        this.isRunning = false;
        log.info("Print server stopped");
    }

    @Override
    public void restart(Session session) throws RemoteException, SecurityException {
        validateSessionOrThrow(session.getToken());
        authorizeOrThrow(session.getUsername(), "restart");
        this.isRunning = false;
        printerQueues.clear();
        this.isRunning = true;
        log.info("Print server restarted");
    }

    @Override
    public String status(Session session, String printer) throws RemoteException, SecurityException {
        validateSessionOrThrow(session.getToken());
        authorizeOrThrow(session.getUsername(), "status");
        return String.format("Printer %s - Status: %s, Queue size: %d",
                printer,
                isRunning ? "Running" : "Stopped",
                printerQueues.getOrDefault(printer, new LinkedList<>()).size()
        );
    }

    @Override
    public String readConfig(Session session, String parameter) throws RemoteException, SecurityException {
        validateSessionOrThrow(session.getToken());
        authorizeOrThrow(session.getUsername(), "readConfig");
        return configParameters.getOrDefault(parameter, "Parameter not found");
    }

    @Override
    public String setConfig(Session session, String parameter, String value) throws RemoteException, SecurityException {
        validateSessionOrThrow(session.getToken());
        authorizeOrThrow(session.getUsername(), "setConfig");
        configParameters.put(parameter, value);
        log.info("Configuration updated: {} = {}", parameter, value);
        return "Configuration updated";
    }

    @Override
    public Session login(String username, String password) throws RemoteException, SecurityException {
        try {
            return authManager.authenticate(username, password);
        } catch (AuthenticationException e) {
            log.error("Login failed for user: {}", username);
            throw new RemoteException("Authentication failed", e);
        }
    }

    @Override
    public void logout(Session session) throws RemoteException {
        authManager.logout(session.getToken());
        log.info("User logged out: {}", session.getToken());
    }

    private void validateSessionOrThrow(String token) throws RemoteException {
        if (!authManager.validateSession(token)) {
            log.warn("Invalid or expired token: {}", token);
            throw new RemoteException("Invalid or expired token");
        }
    }

    private void authorizeOrThrow(String username, String operation) throws RemoteException {
        if (!authManager.authorize(username, operation)) {
            log.warn("User {} not authorized to perform operation {}", username, operation);
            throw new SecurityException("Invalid or expired token");
        }
    }

    // Inner record to represent print jobs
    private record PrintJob(String filename, int jobId) {
    }
}