package auth;

import auth.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;

@Slf4j
public class AuthManager {
    private final InternalStorage internalStorage;
    private final TokenManager tokenManager;

    public AuthManager(InternalStorage internalStorage, TokenManager tokenManager) throws IOException {
        this.internalStorage = internalStorage;
        this.tokenManager = tokenManager;
    }

    public String authenticate(String username, String password) throws AuthenticationException {
        if (!internalStorage.verifyPassword(username, password))
            throw new AuthenticationException("Invalid credentials", username);

        return tokenManager.generateToken(username);
    }

    public boolean validateToken(String token) {
        return tokenManager.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return tokenManager.getUsernameFromToken(token);
    }

    public boolean authorize(String username, String operation) {
        if (!internalStorage.verifyOperation(username, operation)) {
            throw new SecurityException("User does not have permission for this operation");
        }
        return true;
    }
}