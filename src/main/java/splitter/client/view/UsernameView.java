package splitter.client.view;

import splitter.client.controller.UsernameController;
import splitter.client.model.ConnectionModel;
import splitter.client.model.SessionModel;
import splitter.shared.InputValidator;

import java.util.Objects;

/**
 * View for username creation screen.
 */
public class UsernameView {
    private final UsernameController controller;
    private final SessionModel session;
    private final ConnectionModel connection;

    private boolean visible = false;
    private String notice = null;

    public UsernameView(UsernameController controller, SessionModel session, ConnectionModel connection) {
        this.controller = Objects.requireNonNull(controller, "UsernameController cannot be null");
        this.session = Objects.requireNonNull(session, "SessionModel cannot be null");
        this.connection = Objects.requireNonNull(connection, "ConnectionModel cannot be null");
    }

    public void show(String notice) {
        this.visible = true;
        this.notice = notice;
    }

    public void hide() {
        this.visible = false;
        this.notice = null;
    }

    public boolean isCreateEnabled(String username) {
        if (!connection.isConnected()) {
            return false;
        }
        return InputValidator.validateUsername(username).isSuccess();
    }

    public void onCreateClicked(String username, String serverAddress) {
        if (isCreateEnabled(username)) {
            controller.createUsernameRequested(username, serverAddress);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public String getNotice() {
        return notice;
    }

    public String getSavedServerAddress() {
        return session.getServerAddress();
    }
}
