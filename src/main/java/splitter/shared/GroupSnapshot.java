package splitter.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Snapshot of a group's state at one moment: id, name, caller's member name, owner status,
 * code (only for group owner), all members in member list order, and all bills newest first.
 */
public final class GroupSnapshot {
    private final long groupId;
    private final String groupName;
    private final String currentMemberName;
    private final boolean isOwner;
    private final String groupCode;
    private final List<Member> members;
    private final List<Bill> bills;

    public GroupSnapshot(long groupId, String groupName, String currentMemberName, boolean isOwner,
                         String groupCode, List<Member> members, List<Bill> bills) {
        this.groupId = groupId;
        this.groupName = Objects.requireNonNull(groupName, "groupName cannot be null");
        this.currentMemberName = Objects.requireNonNull(currentMemberName, "currentMemberName cannot be null");
        this.isOwner = isOwner;
        this.groupCode = groupCode;
        this.members = Collections.unmodifiableList(new ArrayList<>(members));
        this.bills = Collections.unmodifiableList(new ArrayList<>(bills));
    }

    public long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getCurrentMemberName() {
        return currentMemberName;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public Optional<String> getGroupCode() {
        return Optional.ofNullable(groupCode);
    }

    public String getGroupCodeValue() {
        return groupCode;
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public List<Member> getCurrentMembers() {
        List<Member> current = new ArrayList<>();
        for (Member m : members) {
            if (!m.hasLeft()) {
                current.add(m);
            }
        }
        return current;
    }

    public Optional<Bill> findBill(long billId) {
        for (Bill b : bills) {
            if (b.getId() == billId) {
                return Optional.of(b);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupSnapshot)) return false;
        GroupSnapshot that = (GroupSnapshot) o;
        return groupId == that.groupId &&
                isOwner == that.isOwner &&
                Objects.equals(groupName, that.groupName) &&
                Objects.equals(currentMemberName, that.currentMemberName) &&
                Objects.equals(groupCode, that.groupCode) &&
                Objects.equals(members, that.members) &&
                Objects.equals(bills, that.bills);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, groupName, currentMemberName, isOwner, groupCode, members, bills);
    }
}
