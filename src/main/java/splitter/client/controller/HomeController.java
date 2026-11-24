package splitter.client.controller;

import splitter.client.model.GroupListModel;
import splitter.client.model.GroupModel;
import splitter.client.model.SessionModel;
import splitter.client.view.ErrorMessageView;
import splitter.client.view.GroupCodePromptView;
import splitter.shared.FailureReasons;
import splitter.shared.GroupSnapshot;
import splitter.shared.InputValidator;
import splitter.shared.Result;

import java.util.Objects;
import java.util.Optional;

/**
 * Controller for home screen actions: creating, joining, and selecting groups.
 */
public class HomeController {
    private final ServerConnection server;
    private final SessionModel session;
    private final GroupModel group;
    private final GroupListModel groups;
    private final AppController app;
    private final ErrorMessageView errors;
    private GroupCodePromptView promptView;

    public HomeController(ServerConnection server, SessionModel session, GroupModel group,
                          GroupListModel groups, AppController app, ErrorMessageView errors) {
        this.server = Objects.requireNonNull(server, "ServerConnection cannot be null");
        this.session = Objects.requireNonNull(session, "SessionModel cannot be null");
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.groups = Objects.requireNonNull(groups, "GroupListModel cannot be null");
        this.app = Objects.requireNonNull(app, "AppController cannot be null");
        this.errors = Objects.requireNonNull(errors, "ErrorMessageView cannot be null");
    }

    public void setGroupCodePromptView(GroupCodePromptView prompt) {
        this.promptView = prompt;
    }

    public void createGroupRequested(String groupName, String memberName) {
        Result<GroupSnapshot> result = server.createGroup(groupName, memberName);
        if (result.isSuccess()) {
            GroupSnapshot snap = result.getData();
            group.replaceSnapshot(snap);
            if (snap.getGroupCode().isPresent()) {
                session.saveGroupCode(snap.getGroupId(), snap.getGroupCode().get());
            }
            app.showGroupScreen(snap.getGroupId());
        } else {
            errors.show(result.getFailureReason());
        }
    }

    public void joinGroupRequested(String groupCode, String memberName) {
        String normalizedCode = InputValidator.normalizeGroupCode(groupCode);
        Result<GroupSnapshot> result = server.joinGroup(normalizedCode, memberName);
        if (result.isSuccess()) {
            GroupSnapshot snap = result.getData();
            group.replaceSnapshot(snap);
            session.saveGroupCode(snap.getGroupId(), normalizedCode);
            app.showGroupScreen(snap.getGroupId());
        } else {
            errors.show(result.getFailureReason());
        }
    }

    public void groupSelected(long groupId, String groupName) {
        Optional<String> savedCode = session.getSavedGroupCode(groupId);
        if (savedCode.isPresent()) {
            Result<GroupSnapshot> result = server.getGroup(groupId, savedCode.get());
            if (result.isSuccess()) {
                group.replaceSnapshot(result.getData());
                app.showGroupScreen(groupId);
            } else {
                if (FailureReasons.CODE_OUTDATED.equals(result.getFailureReason())) {
                    session.forgetGroupCode(groupId);
                    if (promptView != null) {
                        promptView.show(groupId, groupName);
                    } else {
                        errors.show("The group code has changed. Please enter the new code.");
                    }
                } else {
                    errors.show(result.getFailureReason());
                }
            }
        } else {
            if (promptView != null) {
                promptView.show(groupId, groupName);
            } else {
                errors.show("Please enter the group code.");
            }
        }
    }

    public void groupCodeEntered(long groupId, String groupCode) {
        String normalized = InputValidator.normalizeGroupCode(groupCode);
        Result<GroupSnapshot> result = server.getGroup(groupId, normalized);
        if (result.isSuccess()) {
            session.saveGroupCode(groupId, normalized);
            group.replaceSnapshot(result.getData());
            app.showGroupScreen(groupId);
        } else {
            errors.show(result.getFailureReason());
        }
    }

    public void serverAddressChanged(String address) {
        if (address != null && !address.isBlank()) {
            session.setServerAddress(address.trim());
        }
    }
}
