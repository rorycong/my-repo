package splitter.client.controller;

import splitter.client.model.SessionModel;
import splitter.client.view.ErrorMessageView;
import splitter.shared.Credentials;
import splitter.shared.InputValidator;
import splitter.shared.Result;

import java.util.Objects;

/**
 * Controller for username creation screen.
 */
public class UsernameController {
    private final ServerConnection server;
    private final SessionModel session;
    private final AppController app;
    private final ErrorMessageView errors;

    public UsernameController(ServerConnection server, SessionModel session, AppController app, ErrorMessageView errors) {
        this.server = Objects.requireNonNull(server, "ServerConnection cannot be null");
        this.session = Objects.requireNonNull(session, "SessionModel cannot be null");
        this.app = Objects.requireNonNull(app, "AppController cannot be null");
        this.errors = Objects.requireNonNull(errors, "ErrorMessageView cannot be null");
    }

    public void createUsernameRequested(String username, String serverAddress) {
        Result<String> validation = InputValidator.validateUsername(username);
        if (!validation.isSuccess()) {
            errors.show(validation.getFailureReason());
            return;
        }

        Result<Credentials> result = server.createUser(validation.getData(), serverAddress);
        if (result.isSuccess()) {
            Credentials creds = result.getData();
            if (serverAddress != null && !serverAddress.isBlank()) {
                session.setServerAddress(serverAddress.trim());
            }
            session.saveIdentity(creds.getUsername(), creds.getLoginToken());
            app.showHomeScreen();
        } else {
            errors.show(result.getFailureReason());
        }
    }
}
