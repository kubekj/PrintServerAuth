package service;

import auth.AuthManager;
import auth.AuthenticationException;
import auth.PasswordStorage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;

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
    public String print(String sessionId, String filename, String printer) throws RemoteException {
        if (!authManager.validateSession(sessionId)) {
            log.warn("Invalid session attempting to print: {}", sessionId);
            throw new RemoteException("Invalid or expired session");
        }

        if (!isRunning) {
            return "Print server is not running";
        }

        printerQueues.putIfAbsent(printer, new LinkedList<>());
        PrintJob job = new PrintJob(filename, printerQueues.get(printer).size() + 1);
        printerQueues.get(printer).offer(job);

        log.info("Print job added: {} for printer {}", filename, printer);
        return "Print job " + job.getJobId() + " added to queue for " + printer;
    }

    @Override
    public String queue(String sessionId, String printer) throws RemoteException {
        if (!authManager.validateSession(sessionId)) {
            throw new RemoteException("Invalid or expired session");
        }

        Queue<PrintJob> queue = printerQueues.get(printer);
        if (queue == null) {
            return "No queue found for printer " + printer;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Print queue for ").append(printer).append(":\n");
        queue.forEach(job -> sb.append(job.getJobId()).append(" ").append(job.getFilename()).append("\n"));
        return sb.toString();
    }

    @Override
    public String topQueue(String sessionId, String printer, int job) throws RemoteException {
        if (!authManager.validateSession(sessionId)) {
            throw new RemoteException("Invalid or expired session");
        }

        Queue<PrintJob> queue = printerQueues.get(printer);
        if (queue == null) {
            return "No queue found for printer " + printer;
        }

        Queue<PrintJob> newQueue = new LinkedList<>();
        PrintJob jobToMove = null;

        // Find the job and reconstruct queue
        for (PrintJob currentJob : queue) {
            if (currentJob.getJobId() == job) {
                jobToMove = currentJob;
            } else {
                newQueue.offer(currentJob);
            }
        }

        if (jobToMove != null) {
            newQueue.add(jobToMove);
            printerQueues.put(printer, newQueue);
            return "Moved job " + job + " to top of queue";
        }

        return "Job " + job + " not found";
    }

    @Override
    public String start(String sessionId) throws RemoteException {
        if (!authManager.validateSession(sessionId)) {
            throw new RemoteException("Invalid or expired session");
        }
        isRunning = true;
        log.info("Print server started");
        return "Print server started";
    }

    @Override
    public String stop(String sessionId) throws RemoteException {
        if (!authManager.validateSession(sessionId)) {
            throw new RemoteException("Invalid or expired session");
        }
        isRunning = false;
        log.info("Print server stopped");
        return "Print server stopped";
    }

    @Override
    public String restart(String sessionId) throws RemoteException {
        if (!authManager.validateSession(sessionId)) {
            throw new RemoteException("Invalid or expired session");
        }
        isRunning = false;
        printerQueues.clear();
        isRunning = true;
        log.info("Print server restarted");
        return "Print server restarted";
    }

    @Override
    public String status(String sessionId, String printer) throws RemoteException {
        if (!authManager.validateSession(sessionId)) {
            throw new RemoteException("Invalid or expired session");
        }
        return String.format("Printer %s - Status: %s, Queue size: %d",
                printer,
                isRunning ? "Running" : "Stopped",
                printerQueues.getOrDefault(printer, new LinkedList<>()).size()
        );
    }

    @Override
    public String readConfig(String sessionId, String parameter) throws RemoteException {
        if (!authManager.validateSession(sessionId)) {
            throw new RemoteException("Invalid or expired session");
        }
        return configParameters.getOrDefault(parameter, "Parameter not found");
    }

    @Override
    public String setConfig(String sessionId, String parameter, String value) throws RemoteException {
        if (!authManager.validateSession(sessionId)) {
            throw new RemoteException("Invalid or expired session");
        }
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
    public void logout(String sessionId) throws RemoteException {
        authManager.logout(sessionId);
        log.info("User logged out: {}", sessionId);
    }

    // Inner class to represent print jobs
    @Data
    private static class PrintJob {
        private final String filename;
        private final int jobId;
    }
}