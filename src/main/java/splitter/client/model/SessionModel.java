package splitter.client.model;

import splitter.shared.Credentials;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores local client settings: username, login token, server address, and group codes.
 */
public class SessionModel {
    private final Path configFile;
    private String username;
    private String loginToken;
    private String serverAddress = "http://localhost:8080";
    private final Map<Long, String> groupCodes = new ConcurrentHashMap<>();
    private boolean configUnreadable = false;

    public SessionModel(Path configFile) {
        this.configFile = configFile;
    }

    public void load() {
        configUnreadable = false;
        username = null;
        loginToken = null;
        groupCodes.clear();

        if (configFile == null || !Files.exists(configFile)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx < 0) {
                    // Corrupted line
                    configUnreadable = true;
                    username = null;
                    loginToken = null;
                    return;
                }
                String key = line.substring(0, idx).trim();
                String val = line.substring(idx + 1).trim();

                switch (key) {
                    case "username" -> this.username = val;
                    case "loginToken" -> this.loginToken = val;
                    case "serverAddress" -> this.serverAddress = val;
                    default -> {
                        if (key.startsWith("group.")) {
                            try {
                                long gid = Long.parseLong(key.substring(6));
                                groupCodes.put(gid, val);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        } catch (Exception e) {
            configUnreadable = true;
            username = null;
            loginToken = null;
        }
    }

    private void persist() {
        if (configFile == null) return;
        try {
            Path parent = configFile.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Path tmp = Files.createTempFile(parent != null ? parent : Path.of("."), "session", ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                if (username != null) writer.write("username=" + username + "\n");
                if (loginToken != null) writer.write("loginToken=" + loginToken + "\n");
                if (serverAddress != null) writer.write("serverAddress=" + serverAddress + "\n");
                for (Map.Entry<Long, String> entry : groupCodes.entrySet()) {
                    writer.write("group." + entry.getKey() + "=" + entry.getValue() + "\n");
                }
            }
            Files.move(tmp, configFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {}
    }

    public boolean wasConfigUnreadable() {
        return configUnreadable;
    }

    public boolean hasIdentity() {
        return !configUnreadable && username != null && !username.isBlank() && loginToken != null && !loginToken.isBlank();
    }

    public Credentials getCredentials() {
        if (!hasIdentity()) {
            return null;
        }
        return new Credentials(username, loginToken);
    }

    public String getUsername() {
        return username;
    }

    public void saveIdentity(String username, String loginToken) {
        this.username = username;
        this.loginToken = loginToken;
        this.configUnreadable = false;
        persist();
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
        persist();
    }

    public Optional<String> getSavedGroupCode(long groupId) {
        return Optional.ofNullable(groupCodes.get(groupId));
    }

    public void saveGroupCode(long groupId, String groupCode) {
        if (groupCode != null) {
            groupCodes.put(groupId, groupCode);
            persist();
        }
    }

    public void forgetGroupCode(long groupId) {
        if (groupCodes.remove(groupId) != null) {
            persist();
        }
    }
}
