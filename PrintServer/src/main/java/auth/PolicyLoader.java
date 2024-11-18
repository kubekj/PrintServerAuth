package auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PolicyLoader {
    private final Map<String, List<String>> accessControlList;

    public PolicyLoader(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Load policy.json from the resources directory
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IOException("policy.json not found in resources");
            }

            // Read JSON file into Map
            accessControlList = mapper.readValue(inputStream, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load policy.json", e);
        }
    }

    public List<String> getPermissionForUser(String username) {
        return accessControlList.getOrDefault(username, List.of());
    }
}
