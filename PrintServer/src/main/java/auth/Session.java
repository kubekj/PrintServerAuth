package auth;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Session {
    private String sessionId;
    private String username;
    private long createdTime;
    private long expiryTime;

    public Session(String username) {
        this.username = username;
        this.sessionId = generateSessionId();
        this.createdTime = System.currentTimeMillis();
        this.expiryTime = this.createdTime + (30 * 60 * 1000); // 30 minutes default
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    public boolean isValid() {
        return System.currentTimeMillis() < expiryTime;
    }
}
