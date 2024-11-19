package auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
public class InternalStorage {
    private static final int SALT_LENGTH = 16;
    private static final String DB_URL = "jdbc:h2:mem:printserver;DB_CLOSE_DELAY=-1;";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String POLICY_FILE = "policy.json";

    public InternalStorage() {
        initDatabase();
        addTestUsers();
        loadPoliciesFromJson();
    }

    private void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            username VARCHAR(50) PRIMARY KEY,
                            salt VARCHAR(64) NOT NULL,
                            password_hash VARCHAR(256) NOT NULL
                        )
                    """);

            // Create policies table
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS policies (
                            username VARCHAR(50),
                            operation VARCHAR(50),
                            PRIMARY KEY (username, operation),
                            FOREIGN KEY (username) REFERENCES users(username)
                        )
                    """);
            log.info("Database initialized successfully");
        } catch (SQLException e) {
            log.error("Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void addTestUsers() {
        try {
            storePassword("alice", "password123");  // admin
            storePassword("bob", "password123");    // technician
            storePassword("cecilia", "password123"); // power user
            storePassword("david", "password123"); // normal user
            storePassword("erica", "password123"); // normal users
            storePassword("fred", "password123"); // normal users
            storePassword("george", "password123"); // normal users
            log.info("Test users created successfully");
        } catch (Exception e) {
            log.error("Error creating test users", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void storePassword(String username, String password) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "MERGE INTO users (username, salt, password_hash) KEY(username) VALUES (?, ?, ?)")) {

            stmt.setString(1, username);
            stmt.setString(2, salt);
            stmt.setString(3, hashedPassword);
            stmt.executeUpdate();
            log.info("Stored password for user: {}", username);
        } catch (SQLException e) {
            log.error("Failed to store password for user: {}", username, e);
            throw new RuntimeException("Failed to store password", e);
        }
    }

    public boolean verifyPassword(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT salt, password_hash FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                log.warn("No password found for user: {}", username);
                return false;
            }

            String salt = rs.getString("salt");
            String storedHash = rs.getString("password_hash");
            String hashedInput = hashPassword(password, salt);

            boolean verified = storedHash.equals(hashedInput);
            log.debug("Password verification for user {}: {}",
                    username, verified ? "successful" : "failed");
            return verified;

        } catch (SQLException e) {
            log.error("Password verification failed for user: {}", username, e);
            return false;
        }
    }

    public boolean verifyOperation(String username, String operation) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM policies WHERE username = ? AND operation = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, operation);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            log.error("Failed to check operation permissions for user: {}", username, e);
            return false;
        }
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String passwordWithSalt = password + salt;
            byte[] hash = digest.digest(passwordWithSalt.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    private void loadPoliciesFromJson() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(POLICY_FILE)) {
            if (inputStream == null) {
                throw new IOException(POLICY_FILE + " not found in resources");
            }

            // Read JSON into a Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<String>> accessControlList = mapper.readValue(inputStream, Map.class);

            // Store policies in the database
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "MERGE INTO policies (username, operation) VALUES (?, ?)")) {

                for (Map.Entry<String, List<String>> entry : accessControlList.entrySet()) {
                    String username = entry.getKey();
                    List<String> operations = entry.getValue();

                    for (String operation : operations) {
                        stmt.setString(1, username);
                        stmt.setString(2, operation);
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
                log.info("Policies loaded successfully from {}", POLICY_FILE);
            }

        } catch (IOException e) {
            log.error("Failed to load policy.json", e);
            throw new RuntimeException("Failed to load policies from " + POLICY_FILE, e);
        } catch (Exception e) {
            log.error("Failed to store policies in the database", e);
            throw new RuntimeException("Failed to store policies in database", e);
        }
    }
}