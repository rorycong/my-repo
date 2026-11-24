package splitter.server.model;

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
 * Transactional repository for users. Persists users to disk.
 */
public class UserRepository {
    private final Path dbFile;
    private final Map<String, User> usersByLowerName = new ConcurrentHashMap<>();
    private final Object fileLock = new Object();

    public UserRepository() {
        this(null);
    }

    public UserRepository(Path dbFile) {
        this.dbFile = dbFile;
        if (dbFile != null) {
            load();
        }
    }

    private void load() {
        synchronized (fileLock) {
            usersByLowerName.clear();
            if (dbFile == null || !Files.exists(dbFile)) {
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(dbFile, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] parts = line.split("\t", -1);
                    if (parts.length >= 2) {
                        String username = parts[0];
                        String token = parts[1];
                        usersByLowerName.put(username.toLowerCase(), new User(username, token));
                    }
                }
            } catch (IOException e) {
                throw new StorageException("Failed to load user database", e);
            }
        }
    }

    private void save() {
        if (dbFile == null) return;
        synchronized (fileLock) {
            try {
                Path parent = dbFile.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Path tempFile = Files.createTempFile(parent != null ? parent : Path.of("."), "users", ".tmp");
                try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                    for (User user : usersByLowerName.values()) {
                        writer.write(user.getUsername() + "\t" + user.getLoginToken() + "\n");
                    }
                }
                Files.move(tempFile, dbFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                throw new StorageException("Failed to persist user database", e);
            }
        }
    }

    public synchronized boolean usernameExists(String username) {
        if (username == null) return false;
        return usersByLowerName.containsKey(username.toLowerCase());
    }

    public synchronized void createUser(String username, String loginToken) {
        if (username == null || loginToken == null) {
            throw new StorageException("Username and token cannot be null");
        }
        if (usernameExists(username)) {
            throw new StorageException("User already exists: " + username);
        }
        User user = new User(username, loginToken);
        usersByLowerName.put(username.toLowerCase(), user);
        save();
    }

    public synchronized Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(usersByLowerName.get(username.toLowerCase()));
    }
}
