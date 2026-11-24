package splitter.client.view;

import splitter.client.controller.HomeController;
import splitter.client.model.ConnectionModel;
import splitter.client.model.GroupListModel;
import splitter.client.model.SessionModel;
import splitter.shared.GroupSummary;

import java.util.List;
import java.util.Objects;

/**
 * View representing the main home screen.
 */
public class HomeView {
    private final HomeController controller;
    private final GroupListModel groups;
    private final SessionModel session;
    private final ConnectionModel connection;
    private final CreateGroupView createGroup;
    private final JoinGroupView joinGroup;

    private boolean visible = false;

    public HomeView(HomeController controller, GroupListModel groups, SessionModel session,
                    ConnectionModel connection, CreateGroupView createGroup, JoinGroupView joinGroup) {
        this.controller = Objects.requireNonNull(controller, "HomeController cannot be null");
        this.groups = Objects.requireNonNull(groups, "GroupListModel cannot be null");
        this.session = Objects.requireNonNull(session, "SessionModel cannot be null");
        this.connection = Objects.requireNonNull(connection, "ConnectionModel cannot be null");
        this.createGroup = Objects.requireNonNull(createGroup, "CreateGroupView cannot be null");
        this.joinGroup = Objects.requireNonNull(joinGroup, "JoinGroupView cannot be null");

        this.groups.addListener(this::refresh);
        this.connection.addListener(this::refresh);
    }

    public void show() {
        this.visible = true;
        refresh();
    }

    public void hide() {
        this.visible = false;
    }

    public void refresh() {
        // Redraw/update data
    }

    public void onGroupSelected(long groupId, String groupName) {
        controller.groupSelected(groupId, groupName);
    }

    public void onCreateGroupClicked() {
        createGroup.show();
    }

    public void onJoinGroupClicked() {
        joinGroup.show();
    }

    public void onServerAddressEdited(String address) {
        controller.serverAddressChanged(address);
    }

    public boolean isVisible() {
        return visible;
    }

    public String getUsername() {
        return session.getUsername();
    }

    public String getServerAddress() {
        return session.getServerAddress();
    }

    public List<GroupSummary> getGroupList() {
        return groups.getGroups();
    }

    public boolean hasGroups() {
        return !groups.getGroups().isEmpty();
    }

    public boolean isConnected() {
        return connection.isConnected();
    }
}
