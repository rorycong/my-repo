package splitter.shared;

import java.util.Objects;

/**
 * Calculated outstanding amounts for one member: owes and is owed.
 */
public final class OutstandingAmounts {
    private final String memberName;
    private final long owesCents;
    private final long isOwedCents;

    public OutstandingAmounts(String memberName, long owesCents, long isOwedCents) {
        this.memberName = Objects.requireNonNull(memberName, "memberName cannot be null");
        this.owesCents = owesCents;
        this.isOwedCents = isOwedCents;
    }

    public String getMemberName() {
        return memberName;
    }

    public long getOwesCents() {
        return owesCents;
    }

    public long getIsOwedCents() {
        return isOwedCents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutstandingAmounts)) return false;
        OutstandingAmounts that = (OutstandingAmounts) o;
        return owesCents == that.owesCents &&
                isOwedCents == that.isOwedCents &&
                Objects.equals(memberName, that.memberName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberName, owesCents, isOwedCents);
    }

    @Override
    public String toString() {
        return "OutstandingAmounts{" +
                "memberName='" + memberName + '\'' +
                ", owesCents=" + owesCents +
                ", isOwedCents=" + isOwedCents +
                '}';
    }
}
