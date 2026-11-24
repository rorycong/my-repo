package splitter.server.model;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Generates cryptographically secure login tokens of at least 32 characters.
 */
public final class TokenGenerator {
    private final SecureRandom random;

    public TokenGenerator() {
        this(new SecureRandom());
    }

    public TokenGenerator(SecureRandom random) {
        this.random = random;
    }

    public String generateLoginToken() {
        byte[] bytes = new byte[32]; // 32 bytes -> 64 hex characters >= 32 chars
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
