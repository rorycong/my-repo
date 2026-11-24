package splitter.server.controller;

import splitter.server.model.Group;
import splitter.server.model.GroupCodeGenerator;
import splitter.server.model.GroupMember;
import splitter.server.model.GroupRepository;
import splitter.server.model.StorageException;
import splitter.server.model.User;
import splitter.shared.Credentials;
import splitter.shared.FailureReasons;
import splitter.shared.GroupAccess;
import splitter.shared.GroupSnapshot;
import splitter.shared.GroupSummary;
import splitter.shared.InputValidator;
import splitter.shared.Member;
import splitter.shared.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller for group management operations.
 */
public class GroupApiController {
    private final AuthGuard guard;
    private final GroupRepository groups;
    private final GroupCodeGenerator codes;
    private final GroupLockManager locks;

    public GroupApiController(AuthGuard guard, GroupRepository groups, GroupCodeGenerator codes, GroupLockManager locks) {
        this.guard = Objects.requireNonNull(guard, "AuthGuard cannot be null");
        this.groups = Objects.requireNonNull(groups, "GroupRepository cannot be null");
        this.codes = Objects.requireNonNull(codes, "GroupCodeGenerator cannot be null");
        this.locks = Objects.requireNonNull(locks, "GroupLockManager cannot be null");
    }

    public Result<List<GroupSummary>> listGroups(Credentials credentials) {
        try {
            Result<User> auth = guard.authenticate(credentials);
            if (!auth.isSuccess()) {
                return Result.fail(auth.getFailureReason());
            }
            List<GroupSummary> list = groups.listGroupsOfUser(auth.getData().getUsername());
            return Result.ok(list);
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    public Result<GroupSnapshot> createGroup(Credentials credentials, String groupName, String memberName) {
        try {
            Result<User> auth = guard.authenticate(credentials);
            if (!auth.isSuccess()) {
                return Result.fail(auth.getFailureReason());
            }
            Result<String> validGName = InputValidator.validateName(groupName, "Group name");
            if (!validGName.isSuccess()) {
                return Result.fail(validGName.getFailureReason());
            }
            Result<String> validMName = InputValidator.validateName(memberName, "Member name");
            if (!validMName.isSuccess()) {
                return Result.fail(validMName.getFailureReason());
            }

            String code;
            do {
                code = codes.generateCode();
            } while (groups.codeExists(code));

            long groupId = groups.createGroup(validGName.getData(), code, auth.getData().getUsername(), validMName.getData());
            Group group = groups.loadGroup(groupId).orElseThrow(() -> new StorageException("Failed to load created group"));
            return Result.ok(buildSnapshot(group, auth.getData().getUsername()));
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    public Result<GroupSnapshot> joinGroup(Credentials credentials, String groupCode, String memberName) {
        try {
            Result<User> auth = guard.authenticate(credentials);
            if (!auth.isSuccess()) {
                return Result.fail(auth.getFailureReason());
            }
            Result<String> validMName = InputValidator.validateName(memberName, "Member name");
            if (!validMName.isSuccess()) {
                return Result.fail(validMName.getFailureReason());
            }
            String normalizedCode = InputValidator.normalizeGroupCode(groupCode);
            if (normalizedCode.isEmpty()) {
                return Result.fail("Group code cannot be blank");
            }

            Optional<Group> optGroup = groups.loadGroupByCode(normalizedCode);
            if (optGroup.isEmpty()) {
                return Result.fail(FailureReasons.GROUP_NOT_FOUND);
            }
            long groupId = optGroup.get().getId();

            locks.lock(groupId);
            try {
                Group group = groups.loadGroup(groupId).orElseThrow(() -> new StorageException(FailureReasons.GROUP_NOT_FOUND));
                if (group.findCurrentMemberByUsername(auth.getData().getUsername()).isPresent()) {
                    return Result.fail(FailureReasons.ALREADY_A_MEMBER);
                }
                if (group.findCurrentMemberByName(validMName.getData()).isPresent()) {
                    return Result.fail("Member name already taken in this group");
                }
                groups.addMember(groupId, auth.getData().getUsername(), validMName.getData());
                Group updatedGroup = groups.loadGroup(groupId).orElseThrow();
                return Result.ok(buildSnapshot(updatedGroup, auth.getData().getUsername()));
            } finally {
                locks.unlock(groupId);
            }
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    public Result<GroupSnapshot> getGroup(GroupAccess access) {
        try {
            Result<Group> authGroup = guard.authorizeGroupAccess(access);
            if (!authGroup.isSuccess()) {
                return Result.fail(authGroup.getFailureReason());
            }
            Group group = authGroup.getData();
            return Result.ok(buildSnapshot(group, access.getCredentials().getUsername()));
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    public Result<Void> renameGroup(GroupAccess access, String newName) {
        try {
            Result<Group> authGroup = guard.authorizeGroupAccess(access);
            if (!authGroup.isSuccess()) {
                return Result.fail(authGroup.getFailureReason());
            }
            Group group = authGroup.getData();
            Result<Void> reqOwner = guard.requireGroupOwner(group, access.getCredentials().getUsername());
            if (!reqOwner.isSuccess()) {
                return Result.fail(reqOwner.getFailureReason());
            }
            Result<String> validName = InputValidator.validateName(newName, "Group name");
            if (!validName.isSuccess()) {
                return Result.fail(validName.getFailureReason());
            }

            locks.lock(group.getId());
            try {
                groups.renameGroup(group.getId(), validName.getData());
                return Result.ok();
            } finally {
                locks.unlock(group.getId());
            }
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    public Result<String> generateNewCode(GroupAccess access) {
        try {
            Result<Group> authGroup = guard.authorizeGroupAccess(access);
            if (!authGroup.isSuccess()) {
                return Result.fail(authGroup.getFailureReason());
            }
            Group group = authGroup.getData();
            Result<Void> reqOwner = guard.requireGroupOwner(group, access.getCredentials().getUsername());
            if (!reqOwner.isSuccess()) {
                return Result.fail(reqOwner.getFailureReason());
            }

            locks.lock(group.getId());
            try {
                String newCode;
                do {
                    newCode = codes.generateCode();
                } while (groups.codeExists(newCode));

                groups.replaceCode(group.getId(), newCode);
                return Result.ok(newCode);
            } finally {
                locks.unlock(group.getId());
            }
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    public Result<Void> leaveGroup(GroupAccess access) {
        try {
            Result<Group> authGroup = guard.authorizeGroupAccess(access);
            if (!authGroup.isSuccess()) {
                return Result.fail(authGroup.getFailureReason());
            }
            Group initialGroup = authGroup.getData();
            String username = access.getCredentials().getUsername();

            locks.lock(initialGroup.getId());
            try {
                Group group = groups.loadGroup(initialGroup.getId()).orElseThrow(() -> new StorageException(FailureReasons.GROUP_NOT_FOUND));
                List<GroupMember> current = group.currentMembersInOrder();
                if (current.size() <= 1) {
                    // Last member leaving deletes the group with all members and bills
                    groups.deleteGroup(group.getId());
                    return Result.ok();
                }

                String newOwner = null;
                if (group.getOwnerUsername().equalsIgnoreCase(username)) {
                    // Pick the earliest remaining current member as the new owner
                    for (GroupMember m : current) {
                        if (!m.getUsername().equalsIgnoreCase(username)) {
                            newOwner = m.getUsername();
                            break;
                        }
                    }
                }
                groups.leaveGroup(group.getId(), username, newOwner);
                return Result.ok();
            } finally {
                locks.unlock(initialGroup.getId());
            }
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    private GroupSnapshot buildSnapshot(Group group, String callerUsername) {
        boolean isOwner = group.getOwnerUsername().equalsIgnoreCase(callerUsername);
        String codeToSend = isOwner ? group.getCurrentCode() : null; // OWN-8, OWN-9

        Optional<GroupMember> callerMember = group.findMemberByUsername(callerUsername);
        String callerName = callerMember.map(GroupMember::getName).orElse("");

        List<Member> memberList = new ArrayList<>();
        for (GroupMember gm : group.getAllMembers()) {
            boolean memberIsOwner = group.getOwnerUsername().equalsIgnoreCase(gm.getUsername());
            memberList.add(new Member(gm.getName(), memberIsOwner, gm.hasLeft(), gm.getJoinedAt()));
        }

        return new GroupSnapshot(group.getId(), group.getName(), callerName, isOwner, codeToSend, memberList, group.getBills());
    }
}
