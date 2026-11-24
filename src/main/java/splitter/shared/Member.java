package splitter.shared;

import java.time.Instant;
import java.util.Objects;

/**
 * Shared member representation: name, owner status, former status, and join timestamp.
 * Deliberately does not expose username across members.
 */
public final class Member {
    private final String name;
    private final boolean isOwner;
    private final boolean hasLeft;
    private final Instant joinedAt;

    public Member(String name, boolean isOwner, boolean hasLeft, Instant joinedAt) {
        this.name = Objects.requireNonNull(name, "member name cannot be null");
        this.isOwner = isOwner;
        this.hasLeft = hasLeft;
        this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt cannot be null");
    }

    public String getName() {
        return name;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public boolean hasLeft() {
        return hasLeft;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return isOwner == member.isOwner &&
                hasLeft == member.hasLeft &&
                Objects.equals(name, member.name) &&
                Objects.equals(joinedAt, member.joinedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isOwner, hasLeft, joinedAt);
    }

    @Override
    public String toString() {
        return "Member{" + "name='" + name + '\'' +
                ", isOwner=" + isOwner +
                ", hasLeft=" + hasLeft +
                ", joinedAt=" + joinedAt +
                '}';
    }
}
