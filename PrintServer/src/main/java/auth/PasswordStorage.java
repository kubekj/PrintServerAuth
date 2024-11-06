package auth;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;

@Slf4j
public class PasswordStorage {
    @Getter
    private Map<String, String> userPasswords = new HashMap<>();
    private static final int SALT_LENGTH = 16;

    public PasswordStorage() {
        loadPasswords();
    }

    public void storePassword(String username, String password) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        log.info("Storing password for user: {}", username);
        saveToStorage(username, salt + ":" + hashedPassword);
    }


    public boolean verifyPassword(String username, String password) {
        String storedValue = userPasswords.get(username);
        if (storedValue == null) {
            log.warn("No password found for user: {}", username);
            return false;
        }

        String[] parts = storedValue.split(":");
        String salt = parts[0];
        String storedHash = parts[1];

        String hashedInput = hashPassword(password, salt);
        boolean verified = storedHash.equals(hashedInput);
        log.debug("Password verification for user {}: {}", username, verified ? "successful" : "failed");
        return verified;
    }

    private String generateSalt() {
        return "";
    }

    private String hashPassword(String password, String salt) {
        return null;
    }

    private void loadPasswords() {
        log.info("Loading passwords from storage");
    }

    private void saveToStorage(String username, String hashedPassword) {
        log.info("Saving password for user: {}", username);
    }
}
