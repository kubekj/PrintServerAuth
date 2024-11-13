package auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Session {
    private String username;
    private String token;
    private long createdTime;

    public Session(String username, String token) {
        this.username = username;
        this.token = token;
        this.createdTime = System.currentTimeMillis();
    }
}