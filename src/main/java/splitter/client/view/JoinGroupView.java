package splitter.client.view;

import splitter.client.controller.HomeController;
import splitter.client.model.ConnectionModel;

import java.util.Objects;

/**
 * View dialog for joining an existing group with a group code.
 */
public class JoinGroupView {
    private final HomeController controller;
    private final ConnectionModel connection;
    private boolean visible = false;

    public JoinGroupView(HomeController controller, ConnectionModel connection) {
        this.controller = Objects.requireNonNull(controller, "HomeController cannot be null");
        this.connection = Objects.requireNonNull(connection, "ConnectionModel cannot be null");
    }

    public void show() {
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isSubmitEnabled(String groupCode, String memberName) {
        if (!connection.isConnected()) {
            return false;
        }
        return groupCode != null && !groupCode.trim().isEmpty() &&
                memberName != null && !memberName.trim().isEmpty();
    }

    public void onSubmit(String groupCode, String memberName) {
        if (isSubmitEnabled(groupCode, memberName)) {
            hide();
            controller.joinGroupRequested(groupCode, memberName);
        }
    }

    public boolean isVisible() {
        return visible;
    }
}
