package auth;

import auth.exceptions.AuthenticationException;

public class AuthManager {
    private final PasswordStorage passwordStorage;
    private final TokenManager tokenManager;

    public AuthManager(PasswordStorage passwordStorage, TokenManager tokenManager) {
        this.passwordStorage = passwordStorage;
        this.tokenManager = tokenManager;
    }

    public String authenticate(String username, String password) throws AuthenticationException {
        if (!passwordStorage.verifyPassword(username, password))
            throw new AuthenticationException("Invalid credentials", username);

        return tokenManager.generateToken(username);
    }

    public boolean validateToken(String token) {
        return tokenManager.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return tokenManager.getUsernameFromToken(token);
    }
}