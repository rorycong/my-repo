package splitter.shared;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Share of a bill owed by one member: member name, weight in hundredths, share in cents,
 * confirmation status, and optional confirmation timestamp.
 */
public final class BillShare {
    private final String memberName;
    private final int weightHundredths;
    private final long shareCents;
    private final boolean isConfirmed;
    private final Instant confirmedAt;

    public BillShare(String memberName, int weightHundredths, long shareCents, boolean isConfirmed, Instant confirmedAt) {
        this.memberName = Objects.requireNonNull(memberName, "memberName cannot be null");
        this.weightHundredths = weightHundredths;
        this.shareCents = shareCents;
        this.isConfirmed = isConfirmed;
        this.confirmedAt = confirmedAt;
    }

    public String getMemberName() {
        return memberName;
    }

    public int getWeightHundredths() {
        return weightHundredths;
    }

    public long getShareCents() {
        return shareCents;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public Optional<Instant> getConfirmedAt() {
        return Optional.ofNullable(confirmedAt);
    }

    public Instant getConfirmedAtInstant() {
        return confirmedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BillShare)) return false;
        BillShare billShare = (BillShare) o;
        return weightHundredths == billShare.weightHundredths &&
                shareCents == billShare.shareCents &&
                isConfirmed == billShare.isConfirmed &&
                Objects.equals(memberName, billShare.memberName) &&
                Objects.equals(confirmedAt, billShare.confirmedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberName, weightHundredths, shareCents, isConfirmed, confirmedAt);
    }

    @Override
    public String toString() {
        return "BillShare{" +
                "memberName='" + memberName + '\'' +
                ", weightHundredths=" + weightHundredths +
                ", shareCents=" + shareCents +
                ", isConfirmed=" + isConfirmed +
                ", confirmedAt=" + confirmedAt +
                '}';
    }
}
