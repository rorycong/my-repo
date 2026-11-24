package splitter.server.controller;

import splitter.server.model.Group;
import splitter.server.model.GroupMember;
import splitter.server.model.GroupRepository;
import splitter.server.model.User;
import splitter.server.model.UserRepository;
import splitter.shared.Bill;
import splitter.shared.Credentials;
import splitter.shared.FailureReasons;
import splitter.shared.GroupAccess;
import splitter.shared.InputValidator;
import splitter.shared.Result;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates credentials and verifies user permissions for group and bill actions.
 */
public class AuthGuard {
    private final UserRepository users;
    private final GroupRepository groups;

    public AuthGuard(UserRepository users, GroupRepository groups) {
        this.users = Objects.requireNonNull(users, "UserRepository cannot be null");
        this.groups = Objects.requireNonNull(groups, "GroupRepository cannot be null");
    }

    public Result<User> authenticate(Credentials credentials) {
        if (credentials == null) {
            return Result.fail(FailureReasons.INVALID_CREDENTIALS);
        }
        Optional<User> optUser = users.findByUsername(credentials.getUsername());
        if (optUser.isEmpty()) {
            return Result.fail(FailureReasons.INVALID_CREDENTIALS);
        }
        User user = optUser.get();
        if (!user.getLoginToken().equals(credentials.getLoginToken())) {
            return Result.fail(FailureReasons.INVALID_CREDENTIALS);
        }
        return Result.ok(user);
    }

    public Result<Group> authorizeGroupAccess(GroupAccess access) {
        if (access == null) {
            return Result.fail(FailureReasons.INVALID_CREDENTIALS);
        }
        Result<User> authResult = authenticate(access.getCredentials());
        if (!authResult.isSuccess()) {
            return Result.fail(authResult.getFailureReason());
        }
        User user = authResult.getData();

        Optional<Group> optGroup = groups.loadGroup(access.getGroupId());
        if (optGroup.isEmpty()) {
            return Result.fail(FailureReasons.GROUP_NOT_FOUND);
        }
        Group group = optGroup.get();

        String expectedCode = InputValidator.normalizeGroupCode(group.getCurrentCode());
        String actualCode = InputValidator.normalizeGroupCode(access.getGroupCode());
        if (!expectedCode.equals(actualCode)) {
            return Result.fail(FailureReasons.CODE_OUTDATED);
        }

        if (group.findCurrentMemberByUsername(user.getUsername()).isEmpty()) {
            return Result.fail(FailureReasons.NOT_A_MEMBER);
        }

        return Result.ok(group);
    }

    public Result<Void> requireGroupOwner(Group group, String username) {
        if (group == null || username == null) {
            return Result.fail(FailureReasons.PERMISSION_DENIED);
        }
        if (!group.getOwnerUsername().equalsIgnoreCase(username)) {
            return Result.fail(FailureReasons.PERMISSION_DENIED);
        }
        return Result.ok();
    }

    public Result<Void> requireBillOwner(Group group, Bill bill, String username) {
        if (group == null || bill == null || username == null) {
            return Result.fail(FailureReasons.PERMISSION_DENIED);
        }
        if (isBillOwner(group, bill, username)) {
            return Result.ok();
        }
        return Result.fail(FailureReasons.PERMISSION_DENIED);
    }

    public Result<Void> requireBillOwnerOrGroupOwner(Group group, Bill bill, String username) {
        if (group == null || bill == null || username == null) {
            return Result.fail(FailureReasons.PERMISSION_DENIED);
        }
        if (isBillOwner(group, bill, username) || group.getOwnerUsername().equalsIgnoreCase(username)) {
            return Result.ok();
        }
        return Result.fail(FailureReasons.PERMISSION_DENIED);
    }

    public Result<Void> requireMayConfirmPayment(Group group, Bill bill, String username) {
        if (group == null || bill == null || username == null) {
            return Result.fail(FailureReasons.PERMISSION_DENIED);
        }
        if (isBillOwner(group, bill, username)) {
            return Result.ok();
        }
        // Group owner can confirm if the bill owner has left the group
        if (group.getOwnerUsername().equalsIgnoreCase(username)) {
            GroupMember billOwnerMember = findBillOwnerMember(group, bill);
            if (billOwnerMember != null && billOwnerMember.hasLeft()) {
                return Result.ok();
            }
        }
        return Result.fail(FailureReasons.PERMISSION_DENIED);
    }

    private boolean isBillOwner(Group group, Bill bill, String username) {
        GroupMember billOwnerMember = findBillOwnerMember(group, bill);
        return billOwnerMember != null && billOwnerMember.getUsername().equalsIgnoreCase(username);
    }

    private GroupMember findBillOwnerMember(Group group, Bill bill) {
        for (GroupMember m : group.getAllMembers()) {
            if (InputValidator.namesEqual(m.getName(), bill.getBillOwnerMemberName())) {
                return m;
            }
        }
        return null;
    }
}
