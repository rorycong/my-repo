package splitter.shared;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A bill submitted by a client before shares are calculated.
 */
public final class NewBill {
    private final String description;
    private final long totalCents;
    private final Map<String, Integer> weightsHundredths;
    private final LocalDate suggestedDate;

    public NewBill(String description, long totalCents, Map<String, Integer> weightsHundredths, LocalDate suggestedDate) {
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.totalCents = totalCents;
        this.weightsHundredths = Collections.unmodifiableMap(new LinkedHashMap<>(weightsHundredths));
        this.suggestedDate = suggestedDate;
    }

    public String getDescription() {
        return description;
    }

    public long getTotalCents() {
        return totalCents;
    }

    public Map<String, Integer> getWeightsHundredths() {
        return weightsHundredths;
    }

    public Optional<LocalDate> getSuggestedDate() {
        return Optional.ofNullable(suggestedDate);
    }

    public LocalDate getSuggestedDateValue() {
        return suggestedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewBill)) return false;
        NewBill newBill = (NewBill) o;
        return totalCents == newBill.totalCents &&
                Objects.equals(description, newBill.description) &&
                Objects.equals(weightsHundredths, newBill.weightsHundredths) &&
                Objects.equals(suggestedDate, newBill.suggestedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, totalCents, weightsHundredths, suggestedDate);
    }
}
