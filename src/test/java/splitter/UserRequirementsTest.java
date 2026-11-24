package splitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import splitter.client.model.SessionModel;
import splitter.server.controller.AuthGuard;
import splitter.server.controller.UserApiController;
import splitter.server.model.GroupRepository;
import splitter.server.model.TokenGenerator;
import splitter.server.model.UserRepository;
import splitter.shared.Credentials;
import splitter.shared.FailureReasons;
import splitter.shared.InputValidator;
import splitter.shared.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class UserRequirementsTest {
    @TempDir
    Path tempDir;

    private UserRepository userRepository;
    private UserApiController userApiController;
    private TokenGenerator tokenGenerator;

    @BeforeEach
    public void setUp() {
        userRepository = new UserRepository(tempDir.resolve("users.db"));
        tokenGenerator = new TokenGenerator();
        userApiController = new UserApiController(userRepository, tokenGenerator);
    }

    @Test
    public void USR_3_rejects_short_name() {
        Result<String> res0 = InputValidator.validateUsername("");
        assertFalse(res0.isSuccess());
        Result<String> res2 = InputValidator.validateUsername("ab");
        assertFalse(res2.isSuccess());
    }

    @Test
    public void USR_3_rejects_long_name() {
        Result<String> res21 = InputValidator.validateUsername("abcdefghijklmnopqrstu"); // 21 chars
        assertFalse(res21.isSuccess());
    }

    @Test
    public void USR_3_accepts_valid_length() {
        Result<String> res3 = InputValidator.validateUsername("abc");
        assertTrue(res3.isSuccess());
        Result<String> res20 = InputValidator.validateUsername("abcdefghijklmnopqrst"); // 20 chars
        assertTrue(res20.isSuccess());
    }

    @Test
    public void USR_4_rejects_invalid_chars() {
        assertFalse(InputValidator.validateUsername("a b c").isSuccess());
        assertFalse(InputValidator.validateUsername("abc-def").isSuccess());
        assertFalse(InputValidator.validateUsername("alice@home").isSuccess());
    }

    @Test
    public void USR_4_accepts_valid_chars() {
        Result<String> res = InputValidator.validateUsername("abc_123");
        assertTrue(res.isSuccess());
        assertEquals("abc_123", res.getData());
    }

    @Test
    public void USR_5_rejects_without_server_call() {
        Result<Credentials> createRes = userApiController.createUser("bad name!");
        assertFalse(createRes.isSuccess());
        assertFalse(userRepository.usernameExists("bad name!"));
    }

    @Test
    public void USR_6_rejects_case_insensitive_duplicate() {
        Result<Credentials> res1 = userApiController.createUser("Alice");
        assertTrue(res1.isSuccess());

        Result<Credentials> res2 = userApiController.createUser("alice");
        assertFalse(res2.isSuccess());
        assertEquals(FailureReasons.USERNAME_TAKEN, res2.getFailureReason());

        Result<Credentials> res3 = userApiController.createUser("ALICE");
        assertFalse(res3.isSuccess());
        assertEquals(FailureReasons.USERNAME_TAKEN, res3.getFailureReason());
    }

    @Test
    public void USR_7_preserves_display_name_casing() {
        Result<Credentials> res = userApiController.createUser("Alice");
        assertTrue(res.isSuccess());
        assertEquals("Alice", res.getData().getUsername());
        assertEquals("Alice", userRepository.findByUsername("alice").get().getUsername());
    }

    @Test
    public void USR_10_generates_tokens_at_least_32_chars_and_unique() {
        String t1 = tokenGenerator.generateLoginToken();
        String t2 = tokenGenerator.generateLoginToken();
        assertNotNull(t1);
        assertNotNull(t2);
        assertTrue(t1.length() >= 32);
        assertTrue(t2.length() >= 32);
        assertNotEquals(t1, t2);
    }

    @Test
    public void USR_14_and_15_authenticates_credentials() {
        userApiController.createUser("Alice");
        Credentials validCreds = userApiController.createUser("Bob").getData();

        AuthGuard guard = new AuthGuard(userRepository, new GroupRepository());

        // Mismatched token
        Credentials wrongToken = new Credentials("Bob", "wrong-token-value");
        assertFalse(guard.authenticate(wrongToken).isSuccess());

        // Missing/wrong user
        Credentials unknownUser = new Credentials("Charlie", validCreds.getLoginToken());
        assertFalse(guard.authenticate(unknownUser).isSuccess());

        // Null credentials
        assertFalse(guard.authenticate(null).isSuccess());

        // Valid credentials
        assertTrue(guard.authenticate(validCreds).isSuccess());
    }

    @Test
    public void USR_11_12_16_session_model_saves_and_handles_corrupt_config() throws IOException {
        Path cfg = tempDir.resolve("session.cfg");
        SessionModel session = new SessionModel(cfg);

        // Missing file -> no identity, no throw
        session.load();
        assertFalse(session.hasIdentity());
        assertFalse(session.wasConfigUnreadable());

        // Save identity and reload
        session.saveIdentity("Alice", "secret-token-12345678901234567890");
        session.saveGroupCode(42, "CODE42");

        SessionModel session2 = new SessionModel(cfg);
        session2.load();
        assertTrue(session2.hasIdentity());
        assertEquals("Alice", session2.getUsername());
        assertEquals("CODE42", session2.getSavedGroupCode(42).get());

        // Corrupted file
        Files.writeString(cfg, "GARBAGE_NO_EQUALS_SIGN_CORRUPTED\n");
        SessionModel session3 = new SessionModel(cfg);
        session3.load();
        assertFalse(session3.hasIdentity());
        assertTrue(session3.wasConfigUnreadable());
    }
}
