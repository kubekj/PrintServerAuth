package auth;

import auth.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AuthManager {
    private final PasswordStorage passwordStorage;
    private final PolicyLoader policyLoader;
    private final TokenManager tokenManager;


    public AuthManager(String policyFilePath, PasswordStorage passwordStorage, TokenManager tokenManager) throws IOException {
        this.passwordStorage = passwordStorage;
        this.policyLoader = new PolicyLoader(policyFilePath);
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

    public boolean authorize(String username, String operation) {
        List<String> permissions = this.policyLoader.getPermissionForUser(username);
        if (permissions.contains(operation)) {
            return true;
        } else {
            throw new SecurityException("User does not have permission for this operation");
        }
    }
}