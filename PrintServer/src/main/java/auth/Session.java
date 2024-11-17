package auth;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.*;

@Data
@NoArgsConstructor
public class Session implements Serializable {
    private String username;
    private String token;
    private long createdTime;

    public Session(String username, String token) {
        this.username = username;
        this.token = token;
        this.createdTime = System.currentTimeMillis();
    }
}