package splitter.shared;

import java.util.Objects;

/**
 * Summary information for a group in user's group list.
 */
public final class GroupSummary {
    private final long groupId;
    private final String name;

    public GroupSummary(long groupId, String name) {
        this.groupId = groupId;
        this.name = Objects.requireNonNull(name, "group name cannot be null");
    }

    public long getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupSummary)) return false;
        GroupSummary that = (GroupSummary) o;
        return groupId == that.groupId && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, name);
    }

    @Override
    public String toString() {
        return "GroupSummary{" + "groupId=" + groupId + ", name='" + name + '\'' + '}';
    }
}
