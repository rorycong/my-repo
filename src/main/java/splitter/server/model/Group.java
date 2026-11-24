package splitter.server.model;

import splitter.shared.Bill;
import splitter.shared.InputValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Server-side complete group aggregate.
 */
public final class Group {
    private final long id;
    private String name;
    private String currentCode;
    private String ownerUsername;
    private final List<GroupMember> members;
    private final List<Bill> bills;

    public Group(long id, String name, String currentCode, String ownerUsername,
                 List<GroupMember> members, List<Bill> bills) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "group name cannot be null");
        this.currentCode = Objects.requireNonNull(currentCode, "currentCode cannot be null");
        this.ownerUsername = Objects.requireNonNull(ownerUsername, "ownerUsername cannot be null");
        this.members = new ArrayList<>(members);
        this.bills = new ArrayList<>(bills);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getCurrentCode() {
        return currentCode;
    }

    public void setCurrentCode(String currentCode) {
        this.currentCode = Objects.requireNonNull(currentCode);
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = Objects.requireNonNull(ownerUsername);
    }

    public List<GroupMember> getAllMembers() {
        return members;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public Optional<GroupMember> findCurrentMemberByUsername(String username) {
        if (username == null) return Optional.empty();
        for (GroupMember m : members) {
            if (!m.hasLeft() && m.getUsername().equalsIgnoreCase(username)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public Optional<GroupMember> findCurrentMemberByName(String name) {
        if (name == null) return Optional.empty();
        for (GroupMember m : members) {
            if (!m.hasLeft() && InputValidator.namesEqual(m.getName(), name)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public Optional<GroupMember> findMemberByUsername(String username) {
        if (username == null) return Optional.empty();
        for (GroupMember m : members) {
            if (m.getUsername().equalsIgnoreCase(username)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public List<GroupMember> currentMembersInOrder() {
        List<GroupMember> current = new ArrayList<>();
        for (GroupMember m : members) {
            if (!m.hasLeft()) {
                current.add(m);
            }
        }
        return Collections.unmodifiableList(current);
    }

    public Optional<Bill> findBill(long billId) {
        for (Bill b : bills) {
            if (b.getId() == billId) {
                return Optional.of(b);
            }
        }
        return Optional.empty();
    }

    public String ownerMemberName() {
        for (GroupMember m : members) {
            if (m.getUsername().equalsIgnoreCase(ownerUsername)) {
                return m.getName();
            }
        }
        return "";
    }
}
