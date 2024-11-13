package auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class AuthManager {
    private final PasswordStorage passwordStorage;
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    public String authenticate(String username, String password) throws AuthenticationException {
        if (passwordStorage.verifyPassword(username, password)) {
            Session session = new Session(username);
            activeSessions.put(session.getSessionId(), session);
            log.info("User {} successfully authenticated", username);
            return session.getSessionId();
        }
        log.warn("Authentication failed for user {}", username);
        throw new AuthenticationException("Invalid credentials");
    }

    public boolean validateSession(String sessionId) {
        Session session = activeSessions.get(sessionId);
        if (session != null && session.isValid()) return true;
        log.debug("Removing invalid session: {}", sessionId);
        activeSessions.remove(sessionId);
        return false;
    }

    public void logout(String sessionId) {
        log.info("Logging out session: {}", sessionId);
        activeSessions.remove(sessionId);
    }

    private void cleanExpiredSessions() {
        log.debug("Cleaning expired sessions");
        activeSessions.entrySet().removeIf(entry -> !entry.getValue().isValid());
    }
}
