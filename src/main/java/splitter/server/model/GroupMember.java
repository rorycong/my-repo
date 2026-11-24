package splitter.server.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Server-side record linking a user to their display name in a group.
 */
public final class GroupMember {
    private final String username;
    private final String name;
    private final Instant joinedAt;
    private boolean hasLeft;

    public GroupMember(String username, String name, Instant joinedAt, boolean hasLeft) {
        this.username = Objects.requireNonNull(username, "username cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt cannot be null");
        this.hasLeft = hasLeft;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public boolean hasLeft() {
        return hasLeft;
    }

    public void markLeft() {
        this.hasLeft = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMember)) return false;
        GroupMember that = (GroupMember) o;
        return hasLeft == that.hasLeft &&
                Objects.equals(username, that.username) &&
                Objects.equals(name, that.name) &&
                Objects.equals(joinedAt, that.joinedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, name, joinedAt, hasLeft);
    }
}
