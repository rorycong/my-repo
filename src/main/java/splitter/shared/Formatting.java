package splitter.shared;

import java.time.LocalDate;

/**
 * Display formatting for money, weights, and dates.
 * All formatting uses integer arithmetic only (no float/double).
 */
public final class Formatting {
    private Formatting() {}

    public static String formatCents(long cents) {
        boolean negative = cents < 0;
        long abs = Math.abs(cents);
        long dollars = abs / 100;
        long remainingCents = abs % 100;
        return String.format("%s$%d.%02d", negative ? "-" : "", dollars, remainingCents);
    }

    public static String formatWeight(int hundredths) {
        int units = hundredths / 100;
        int fraction = Math.abs(hundredths % 100);
        return String.format("%d.%02d", units, fraction);
    }

    public static String formatDateOrNone(LocalDate date) {
        if (date == null) {
            return "No date";
        }
        return date.toString();
    }
}
