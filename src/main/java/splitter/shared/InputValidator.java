package splitter.shared;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validation and parsing rules for user inputs.
 * Uses integer arithmetic only (no float/double).
 */
public final class InputValidator {
    private static final Pattern USERNAME_CHARS = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern DATE_FORMAT = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private InputValidator() {}

    public static Result<String> validateUsername(String raw) {
        if (raw == null || raw.length() < 3 || raw.length() > 20) {
            return Result.fail("Username must be 3 to 20 characters long");
        }
        if (!USERNAME_CHARS.matcher(raw).matches()) {
            return Result.fail("Username may only contain letters, digits, and underscores");
        }
        return Result.ok(raw);
    }

    public static Result<String> validateName(String raw, String fieldLabel) {
        String label = (fieldLabel != null && !fieldLabel.isBlank()) ? fieldLabel : "Name";
        if (raw == null || raw.trim().isEmpty()) {
            return Result.fail(label + " cannot be blank");
        }
        return Result.ok(raw.trim());
    }

    public static Result<Long> parseAmountCents(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Result.fail("Amount cannot be blank");
        }
        String s = text.trim();
        if (s.startsWith("$")) {
            s = s.substring(1).trim();
        }
        if (s.startsWith("-")) {
            return Result.fail("Amount must be positive");
        }
        String[] parts = s.split("\\.", -1);
        if (parts.length > 2) {
            return Result.fail("Invalid amount format");
        }
        long dollars = 0;
        long cents = 0;
        if (!parts[0].isEmpty()) {
            if (!parts[0].matches("^\\d+$")) {
                return Result.fail("Invalid amount format");
            }
            try {
                dollars = Long.parseLong(parts[0]);
            } catch (NumberFormatException e) {
                return Result.fail("Amount is too large");
            }
        }
        if (parts.length == 2) {
            String fraction = parts[1];
            if (fraction.length() > 2) {
                return Result.fail("Amount cannot have more than 2 decimal places");
            }
            if (!fraction.isEmpty()) {
                if (!fraction.matches("^\\d+$")) {
                    return Result.fail("Invalid amount format");
                }
                if (fraction.length() == 1) {
                    cents = (fraction.charAt(0) - '0') * 10L;
                } else {
                    cents = (fraction.charAt(0) - '0') * 10L + (fraction.charAt(1) - '0');
                }
            }
        }
        long totalCents = dollars * 100L + cents;
        if (totalCents <= 0) {
            return Result.fail("Amount must be greater than zero");
        }
        return Result.ok(totalCents);
    }

    public static Result<Integer> parseWeightHundredths(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Result.fail("Weight cannot be blank");
        }
        String s = text.trim();
        if (s.startsWith("-")) {
            return Result.fail("Weight cannot be negative");
        }
        String[] parts = s.split("\\.", -1);
        if (parts.length > 2) {
            return Result.fail("Invalid weight format");
        }
        int units = 0;
        int fraction = 0;
        if (!parts[0].isEmpty()) {
            if (!parts[0].matches("^\\d+$")) {
                return Result.fail("Invalid weight format");
            }
            try {
                units = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                return Result.fail("Weight is too large");
            }
        }
        if (parts.length == 2) {
            String frac = parts[1];
            if (frac.length() > 2) {
                return Result.fail("Weight cannot have more than 2 decimal places");
            }
            if (!frac.isEmpty()) {
                if (!frac.matches("^\\d+$")) {
                    return Result.fail("Invalid weight format");
                }
                if (frac.length() == 1) {
                    fraction = (frac.charAt(0) - '0') * 10;
                } else {
                    fraction = (frac.charAt(0) - '0') * 10 + (frac.charAt(1) - '0');
                }
            }
        }
        int totalHundredths = units * 100 + fraction;
        if (totalHundredths < 0 || totalHundredths > 100) {
            return Result.fail("Weight must be between 0.00 and 1.00");
        }
        return Result.ok(totalHundredths);
    }

    public static Result<LocalDate> parseOptionalDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Result.ok(null);
        }
        String s = text.trim();
        if (!DATE_FORMAT.matcher(s).matches()) {
            return Result.fail("Date must be in YYYY-MM-DD format");
        }
        try {
            LocalDate date = LocalDate.parse(s);
            return Result.ok(date);
        } catch (DateTimeParseException e) {
            return Result.fail("Invalid date: " + s);
        }
    }

    public static Result<Void> validateWeightsSum(Collection<Integer> weightsHundredths) {
        if (weightsHundredths == null) {
            return Result.fail("Weights cannot be null");
        }
        int sum = 0;
        for (int w : weightsHundredths) {
            sum += w;
        }
        if (sum != 100) {
            return Result.fail("Weights must sum to 1.00 (currently " + Formatting.formatWeight(sum) + ")");
        }
        return Result.ok();
    }

    public static Map<String, Integer> equalWeights(List<String> memberNamesInOrder) {
        Map<String, Integer> result = new LinkedHashMap<>();
        if (memberNamesInOrder == null || memberNamesInOrder.isEmpty()) {
            return result;
        }
        int n = memberNamesInOrder.size();
        int base = 100 / n;
        int remainder = 100 % n;
        for (int i = 0; i < n; i++) {
            int weight = base + (i < remainder ? 1 : 0);
            result.put(memberNamesInOrder.get(i), weight);
        }
        return result;
    }

    public static boolean isNotInPast(LocalDate date, LocalDate today) {
        if (date == null || today == null) {
            return true;
        }
        return !date.isBefore(today);
    }

    public static boolean namesEqual(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.trim().equalsIgnoreCase(b.trim());
    }

    public static String normalizeGroupCode(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toUpperCase();
    }
}
