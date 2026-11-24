package splitter.server.model;

import java.security.SecureRandom;

/**
 * Generates 6-character group codes using secure random selection from A-Z (no I, O) and 2-9.
 */
public final class GroupCodeGenerator {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random;

    public GroupCodeGenerator() {
        this(new SecureRandom());
    }

    public GroupCodeGenerator(SecureRandom random) {
        this.random = random;
    }

    public String generateCode() {
        char[] chars = new char[6];
        for (int i = 0; i < 6; i++) {
            chars[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }
        return new String(chars);
    }
}
