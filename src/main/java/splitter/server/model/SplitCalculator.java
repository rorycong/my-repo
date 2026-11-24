package splitter.server.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Calculates exact shares in cents using integer arithmetic only.
 */
public final class SplitCalculator {
    public SplitCalculator() {}

    /**
     * Computes the shares in cents for each member based on total cents and weights in hundredths.
     * Weights must be provided in member list order and sum to 100.
     *
     * @param totalCents total amount in cents
     * @param weightsHundredths map of member names to weights in hundredths (0 to 100)
     * @return map of member names to share in cents in the same order
     */
    public Map<String, Long> computeShares(long totalCents, Map<String, Integer> weightsHundredths) {
        Objects.requireNonNull(weightsHundredths, "weights cannot be null");
        Map<String, Long> shares = new LinkedHashMap<>();

        long allocated = 0;
        for (Map.Entry<String, Integer> entry : weightsHundredths.entrySet()) {
            int weight = entry.getValue() != null ? entry.getValue() : 0;
            long share = (totalCents * (long) weight) / 100L;
            shares.put(entry.getKey(), share);
            allocated += share;
        }

        long remainder = totalCents - allocated;
        if (remainder > 0) {
            for (Map.Entry<String, Integer> entry : weightsHundredths.entrySet()) {
                if (remainder <= 0) {
                    break;
                }
                int weight = entry.getValue() != null ? entry.getValue() : 0;
                if (weight > 0) {
                    shares.put(entry.getKey(), shares.get(entry.getKey()) + 1L);
                    remainder--;
                }
            }
        }

        return shares;
    }
}
