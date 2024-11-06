package auth;

import lombok.Getter;

@Getter
public class AuthenticationException extends Exception {
    private final String username;

    public AuthenticationException(String message) {
        super(message);
        this.username = null;
    }

    public AuthenticationException(String message, String username) {
        super(message);
        this.username = username;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.username = null;
    }

    public AuthenticationException(String message, String username, Throwable cause) {
        super(message, cause);
        this.username = username;
    }
}
