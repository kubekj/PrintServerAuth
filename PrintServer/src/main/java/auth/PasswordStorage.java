package auth;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
public class PasswordStorage {
    private static final int SALT_LENGTH = 16;
    private static final String DB_URL = "jdbc:h2:mem:printserver;DB_CLOSE_DELAY=-1;";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    public PasswordStorage() {
        initDatabase();
        addTestUsers();
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
            log.info("Database initialized successfully");
        } catch (SQLException e) {
            log.error("Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void addTestUsers() {
        try {
            storePassword("alice", "password123");
            storePassword("bob", "password123");
            storePassword("cecilia", "password123");
            storePassword("david", "password123");
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

            return storedHash.equals(hashedInput);
        } catch (SQLException e) {
            log.error("Password verification failed for user: {}", username, e);
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
}