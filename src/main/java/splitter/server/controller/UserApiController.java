package splitter.server.controller;

import splitter.server.model.StorageException;
import splitter.server.model.TokenGenerator;
import splitter.server.model.UserRepository;
import splitter.shared.Credentials;
import splitter.shared.FailureReasons;
import splitter.shared.InputValidator;
import splitter.shared.Result;

import java.util.Objects;

/**
 * Controller for user creation.
 */
public class UserApiController {
    private final UserRepository users;
    private final TokenGenerator tokens;

    public UserApiController(UserRepository users, TokenGenerator tokens) {
        this.users = Objects.requireNonNull(users, "UserRepository cannot be null");
        this.tokens = Objects.requireNonNull(tokens, "TokenGenerator cannot be null");
    }

    public Result<Credentials> createUser(String username) {
        try {
            Result<String> validation = InputValidator.validateUsername(username);
            if (!validation.isSuccess()) {
                return Result.fail(validation.getFailureReason());
            }
            String validUsername = validation.getData();

            if (users.usernameExists(validUsername)) {
                return Result.fail(FailureReasons.USERNAME_TAKEN);
            }

            String token = tokens.generateLoginToken();
            users.createUser(validUsername, token);
            return Result.ok(new Credentials(validUsername, token));
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }
}
