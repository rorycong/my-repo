package splitter.shared;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Bill with calculated shares, owner name, creation timestamp, and optional suggested date.
 */
public final class Bill {
    private final long id;
    private final String description;
    private final long totalCents;
    private final String billOwnerMemberName;
    private final Instant createdAt;
    private final LocalDate suggestedDate;
    private final List<BillShare> shares;

    public Bill(long id, String description, long totalCents, String billOwnerMemberName,
                Instant createdAt, LocalDate suggestedDate, List<BillShare> shares) {
        this.id = id;
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.totalCents = totalCents;
        this.billOwnerMemberName = Objects.requireNonNull(billOwnerMemberName, "billOwnerMemberName cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.suggestedDate = suggestedDate;
        this.shares = Collections.unmodifiableList(new ArrayList<>(shares));
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public long getTotalCents() {
        return totalCents;
    }

    public String getBillOwnerMemberName() {
        return billOwnerMemberName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Optional<LocalDate> getSuggestedDate() {
        return Optional.ofNullable(suggestedDate);
    }

    public LocalDate getSuggestedDateValue() {
        return suggestedDate;
    }

    public List<BillShare> getShares() {
        return shares;
    }

    public Optional<BillShare> findShare(String memberName) {
        for (BillShare share : shares) {
            if (InputValidator.namesEqual(share.getMemberName(), memberName)) {
                return Optional.of(share);
            }
        }
        return Optional.empty();
    }

    public boolean isFullyPaid() {
        for (BillShare share : shares) {
            if (share.getShareCents() > 0 && !share.isConfirmed()) {
                return false;
            }
        }
        return true;
    }

    public int getPaidSharesCount() {
        int count = 0;
        for (BillShare share : shares) {
            // Count shares that are either confirmed or where member is the bill owner or owed amount
            // BLD-5: "n of m paid", where m is the number of shares that have an amount > 0,
            // or other members' shares. Let's see: BLD-5 definition:
            // "n of m paid: n is the number of confirmed shares, m is total non-zero shares (or owed shares)"
            if (share.getShareCents() > 0 && share.isConfirmed()) {
                count++;
            }
        }
        return count;
    }

    public int getPayableSharesCount() {
        int count = 0;
        for (BillShare share : shares) {
            if (share.getShareCents() > 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bill)) return false;
        Bill bill = (Bill) o;
        return id == bill.id &&
                totalCents == bill.totalCents &&
                Objects.equals(description, bill.description) &&
                Objects.equals(billOwnerMemberName, bill.billOwnerMemberName) &&
                Objects.equals(createdAt, bill.createdAt) &&
                Objects.equals(suggestedDate, bill.suggestedDate) &&
                Objects.equals(shares, bill.shares);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, totalCents, billOwnerMemberName, createdAt, suggestedDate, shares);
    }
}
