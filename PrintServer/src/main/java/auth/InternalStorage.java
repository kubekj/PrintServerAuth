package auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class InternalStorage {
    private static final int SALT_LENGTH = 16;
    private static final String DB_URL = "jdbc:h2:mem:printserver;DB_CLOSE_DELAY=-1;";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String POLICY_FILE = "policy.json";


    public InternalStorage() {
        initDatabase();
        loadRolesAndPermissions();
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

            // Create roles table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                    role_name VARCHAR(50) PRIMARY KEY
                )
            """);

            // Create role_permissions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS role_permissions (
                    role_name VARCHAR(50),
                    permission VARCHAR(50),
                    PRIMARY KEY (role_name, permission),
                    FOREIGN KEY (role_name) REFERENCES roles(role_name)
                )
            """);

            // Create user_roles table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_roles (
                    username VARCHAR(50),
                    role_name VARCHAR(50),
                    PRIMARY KEY (username, role_name),
                    FOREIGN KEY (username) REFERENCES users(username),
                    FOREIGN KEY (role_name) REFERENCES roles(role_name)
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
            storeUser("alice", "password123");
            assignRoleToUser("alice", "Admin");

            storeUser("bob", "password123");
            assignRoleToUser("bob", "Technician");

            storeUser("cecilia", "password123");
            assignRoleToUser("cecilia", "PowerUser");

            storeUser("david", "password123");
            assignRoleToUser("david", "User");

            log.info("Test users created successfully");
        } catch (Exception e) {
            log.error("Error creating test users", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void storeUser(String username, String password) {
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
             PreparedStatement stmt = conn.prepareStatement("""
                 SELECT COUNT(*) FROM role_permissions rp
                 INNER JOIN user_roles ur ON rp.role_name = ur.role_name
                 WHERE ur.username = ? AND rp.permission = ?
             """)) {

            stmt.setString(1, username);
            stmt.setString(2, operation);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            log.error("Failed to check permissions for user: {} and operation: {}", username, operation, e);
            return false;
        }
    }

    public String getUserRoleFromDatabase(String username) {
        String role = null;
        String query = "SELECT userRole FROM users WHERE username = ?";

        try (Connection conn = this.getConnection(); // Replace with your connection method
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("userRole");
                } else {
                    throw new IllegalArgumentException("User not found: " + username);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch role for user: " + username, e);
        }

        return role;
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

    private void loadRolesAndPermissions() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(POLICY_FILE)) {
            if (inputStream == null) {
                throw new IOException(POLICY_FILE + " not found in resources");
            }

            // Parse JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(inputStream);
            JsonNode rolesNode = root.get("roles");

            try (Connection conn = getConnection()) {
                // Insert all roles first
                for (Iterator<String> it = rolesNode.fieldNames(); it.hasNext(); ) {
                    String roleName = it.next();
                    if (!roleName.equals("permissions") && !roleName.equals("inherits")) {
                        addRole(conn, roleName);
                    }
                }

                // Process each role's permissions
                for (Iterator<String> it = rolesNode.fieldNames(); it.hasNext(); ) {
                    String roleName = it.next();
                    if (!roleName.equals("permissions") && !roleName.equals("inherits")) {
                        JsonNode roleNode = rolesNode.get(roleName);
                        Set<String> permissions = resolvePermissions(roleNode, rolesNode);
                        addPermissionsToRole(conn, roleName, permissions);
                    }
                }
            }

        } catch (IOException e) {
            log.error("Failed to load policy.json", e);
            throw new RuntimeException("Failed to load policies from " + POLICY_FILE, e);
        } catch (Exception e) {
            log.error("Failed to store roles and permissions", e);
            throw new RuntimeException("Failed to store roles and permissions in database", e);
        }
    }

    private void addRole(Connection conn, String roleName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO roles (role_name) VALUES (?)")) {
            stmt.setString(1, roleName);
            stmt.executeUpdate();
            log.info("Added role: {}", roleName);
        }
    }

    private Set<String> resolvePermissions(JsonNode roleNode, JsonNode allRolesNode) {
        Set<String> permissions = new HashSet<>();
        JsonNode permissionsNode = roleNode.get("permissions");
        if (permissionsNode != null) {
            for (JsonNode permission : permissionsNode) {
                permissions.add(permission.asText());
            }
        }

        JsonNode inheritsNode = roleNode.get("inherits");
        if (inheritsNode != null) {
            if (inheritsNode.isArray()) {
                for (JsonNode inheritedRole : inheritsNode) {
                    permissions.addAll(resolvePermissions(allRolesNode.get(inheritedRole.asText()), allRolesNode));
                }
            } else {
                permissions.addAll(resolvePermissions(allRolesNode.get(inheritsNode.asText()), allRolesNode));
            }
        }

        return permissions;
    }

    private void addPermissionsToRole(Connection conn, String roleName, Set<String> permissions) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO role_permissions (role_name, permission) VALUES (?, ?)")) {
            for (String permission : permissions) {
                stmt.setString(1, roleName);
                stmt.setString(2, permission);
                stmt.addBatch();
            }
            stmt.executeBatch();
            log.info("Added permissions for role: {}", roleName);
        }
    }

    private void assignRoleToUser(String username, String roleName) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO user_roles (username, role_name) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, roleName);
            stmt.executeUpdate();
            log.info("Assigned role {} to user {}", roleName, username);
        } catch (SQLException e) {
            log.error("Failed to assign role {} to user {}", roleName, username, e);
        }
    }
}