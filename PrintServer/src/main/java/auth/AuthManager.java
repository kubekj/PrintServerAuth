package auth;

import auth.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class AuthManager {
    private final PasswordStorage passwordStorage;
    private final PolicyLoader policyLoader;
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    public AuthManager(String policyFilePath, PasswordStorage passwordStorage) throws IOException {
        this.passwordStorage = passwordStorage;
        this.policyLoader = new PolicyLoader(policyFilePath);
    }

    public Session authenticate(String username, String password) throws AuthenticationException {
        if (passwordStorage.verifyPassword(username, password)) {
            String token = JwtManager.generateToken(username);
            Session session = new Session(username, token);
            activeSessions.put(token, session);
            log.info("User {} successfully authenticated with token: {}", username, token);
            return session;
        }
        log.warn("Authentication failed for user {}", username);
        throw new AuthenticationException("Invalid credentials", username);
    }

    public boolean validateSession(String token) {
        if (token == null) {
            log.warn("Null token provided");
            return false;
        }

        Session session = activeSessions.get(token);
        if (session == null) {
            log.warn("No session found for token: {}", token);
            return false;
        }

        if (!JwtManager.validateToken(token)) {
            log.warn("JWT validation failed for token: {}", token);
            activeSessions.remove(token);
            return false;
        }

        return true;
    }

    public boolean authorize(String username, String operation) {
        String userRole = passwordStorage.getUserRoleFromDatabase(username);
        Set<String> permissions = this.policyLoader.getPermissionForRole(userRole);
        if (permissions.contains(operation)) {
            return true;
        } else {
            throw new SecurityException("User does not have permission for this operation");
        }
    }

    public void logout(String token) {
        if (activeSessions.remove(token) != null) {
            log.info("Successfully logged out token: {}", token);
        } else {
            log.warn("No session found to logout for token: {}", token);
        }
    }
}