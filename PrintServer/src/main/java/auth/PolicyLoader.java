package auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PolicyLoader {
    private Map<String, Set<String>> accessControlList = new HashMap<>();

    public PolicyLoader(String policyFilePath) throws IOException {
        loadPolicy(policyFilePath);
    }

    private void loadPolicy(String policyFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(policyFilePath));
        JsonNode rolesNode = root.get("roles");

        // Load all roles and resolve inherited permissions
        rolesNode.fieldNames().forEachRemaining(role -> resolveRolePermissions(role, rolesNode));
    }

    private Set<String> resolveRolePermissions(String role, JsonNode rolesNode) {
        if (accessControlList.containsKey(role)) return accessControlList.get(role); // Return if already resolved

        JsonNode roleNode = rolesNode.get(role);
        Set<String> permissions = new HashSet<>();

        // Inherit permissions if specified
        if (roleNode.has("inherits")) {
            if (roleNode.get("inherits").isArray()) {
                for (JsonNode inheritedRole : roleNode.get("inherits")) {
                    permissions.addAll(resolveRolePermissions(inheritedRole.asText(), rolesNode));
                }
            } else {
                permissions.addAll(resolveRolePermissions(roleNode.get("inherits").asText(), rolesNode));
            }
        }

        // Add defined permissions
        if (roleNode.has("permissions")) {
            for (JsonNode permission : roleNode.get("permissions")) {
                permissions.add(permission.asText());
            }
        }

        // Store resolved permissions
        accessControlList.put(role, permissions);
        return permissions;
    }

    public Set<String> getPermissionForUser(String username) {
        String role = accessControlList.get(username);
        if (role == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        return policyLoader.getPermissions(role);
    }
}