package auth.exceptions;

import lombok.Getter;

@Getter
public class ExpiredTokenException extends Exception {
    private final String token;
    private final String message = "Token has expired";

    public ExpiredTokenException(String message, String token) {
        super(message);
        this.token = token;
    }
}