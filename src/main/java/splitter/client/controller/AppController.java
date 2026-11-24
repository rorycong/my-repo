package splitter.client.controller;

import splitter.client.model.GroupListModel;
import splitter.client.model.GroupModel;
import splitter.client.model.SessionModel;
import splitter.client.view.ErrorMessageView;
import splitter.client.view.GroupView;
import splitter.client.view.HomeView;
import splitter.client.view.UsernameView;

import java.util.Objects;

/**
 * Main application coordinator that orchestrates navigation across screens.
 */
public class AppController {
    private final SessionModel session;
    private final GroupModel group;
    private final GroupListModel groups;
    private final ErrorMessageView errors;

    private UsernameView usernameView;
    private HomeView homeView;
    private GroupView groupView;
    private SyncController sync;

    public AppController(SessionModel session, GroupModel group, GroupListModel groups, ErrorMessageView errors) {
        this.session = Objects.requireNonNull(session, "SessionModel cannot be null");
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.groups = Objects.requireNonNull(groups, "GroupListModel cannot be null");
        this.errors = Objects.requireNonNull(errors, "ErrorMessageView cannot be null");
    }

    public void wire(UsernameView usernameView, HomeView homeView, GroupView groupView, SyncController sync) {
        this.usernameView = usernameView;
        this.homeView = homeView;
        this.groupView = groupView;
        this.sync = sync;
    }

    public void start() {
        session.load();
        if (session.hasIdentity()) {
            showHomeScreen();
        } else {
            String notice = session.wasConfigUnreadable()
                    ? "Configuration was unreadable. Existing identity could not be recovered."
                    : null;
            showUsernameScreen(notice);
        }
    }

    public void showUsernameScreen(String notice) {
        if (sync != null) sync.stop();
        if (homeView != null) homeView.hide();
        if (groupView != null) groupView.hide();
        if (usernameView != null) usernameView.show(notice);
    }

    public void showHomeScreen() {
        if (group != null) group.clear();
        if (usernameView != null) usernameView.hide();
        if (groupView != null) groupView.hide();
        if (homeView != null) homeView.show();
        if (sync != null) sync.startHomePolling();
    }

    public void showGroupScreen(long groupId) {
        if (usernameView != null) usernameView.hide();
        if (homeView != null) homeView.hide();
        if (groupView != null) groupView.show();
        if (sync != null) sync.startGroupPolling(groupId);
    }

    public void groupUnavailable(String message) {
        showHomeScreen();
        if (errors != null && message != null) {
            errors.show(message);
        }
    }
}
