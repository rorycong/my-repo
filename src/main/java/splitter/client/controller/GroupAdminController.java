package splitter.client.controller;

import splitter.client.model.GroupModel;
import splitter.client.model.SessionModel;
import splitter.client.view.ConfirmDialogView;
import splitter.client.view.ErrorMessageView;
import splitter.shared.Result;

import java.util.Objects;

/**
 * Controller for group-level admin operations: rename, new code, leave, and cancel all bills.
 */
public class GroupAdminController {
    private final ServerConnection server;
    private final SessionModel session;
    private final GroupModel group;
    private final AppController app;
    private final SyncController sync;
    private final ConfirmDialogView confirm;
    private final ErrorMessageView errors;

    public GroupAdminController(ServerConnection server, SessionModel session, GroupModel group,
                                AppController app, SyncController sync, ConfirmDialogView confirm,
                                ErrorMessageView errors) {
        this.server = Objects.requireNonNull(server, "ServerConnection cannot be null");
        this.session = Objects.requireNonNull(session, "SessionModel cannot be null");
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.app = Objects.requireNonNull(app, "AppController cannot be null");
        this.sync = sync; // may be wired or injected
        this.confirm = Objects.requireNonNull(confirm, "ConfirmDialogView cannot be null");
        this.errors = Objects.requireNonNull(errors, "ErrorMessageView cannot be null");
    }

    public void renameGroupRequested(String newName) {
        long groupId = group.getGroupId();
        Result<Void> result = server.renameGroup(groupId, newName);
        if (result.isSuccess()) {
            if (sync != null) sync.refreshNow();
        } else {
            errors.show(result.getFailureReason());
        }
    }

    public void generateNewCodeRequested() {
        long groupId = group.getGroupId();
        Result<String> result = server.generateNewCode(groupId);
        if (result.isSuccess()) {
            session.saveGroupCode(groupId, result.getData());
            if (sync != null) sync.refreshNow();
        } else {
            errors.show(result.getFailureReason());
        }
    }

    public void leaveGroupRequested() {
        long groupId = group.getGroupId();
        confirm.ask("Leave Group", "Are you sure you want to leave this group? This action cannot be undone.", () -> {
            Result<Void> result = server.leaveGroup(groupId);
            if (result.isSuccess()) {
                session.forgetGroupCode(groupId);
                app.showHomeScreen();
            } else {
                errors.show(result.getFailureReason());
            }
        });
    }

    public void cancelAllBillsRequested() {
        long groupId = group.getGroupId();
        confirm.ask("Cancel All Bills", "Are you sure you want to cancel all bills? All bills will be permanently deleted.", () -> {
            Result<Void> result = server.cancelAllBills(groupId);
            if (result.isSuccess()) {
                if (sync != null) sync.refreshNow();
            } else {
                errors.show(result.getFailureReason());
            }
        });
    }

    public void backToHomeRequested() {
        app.showHomeScreen();
    }
}
