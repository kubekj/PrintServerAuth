package auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PolicyLoader {
    private Map<String, Set<String>> accessControlList = new HashMap<>();

    public PolicyLoader(String policyFilePath) throws IOException {
        loadPolicy(policyFilePath);
    }

    private void loadPolicy(String policyFilePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(policyFilePath)) {
            if (inputStream == null) {
                throw new IOException("policy.json not found in resources");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(inputStream);
            JsonNode rolesNode = root.get("roles");

            // Load all roles and resolve inherited permissions
            rolesNode.fieldNames().forEachRemaining(role -> resolveRolePermissions(role, rolesNode));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load policy.json", e);
        }
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

    public Set<String> getPermissionForRole(String role) {
        Set<String> permissions = accessControlList.get(role);
        if (permissions == null) {
            throw new IllegalArgumentException("Role not found: " + role);
        }
        return permissions;
    }
}