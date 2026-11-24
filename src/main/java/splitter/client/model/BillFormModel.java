package splitter.client.model;

import splitter.shared.Formatting;
import splitter.shared.InputValidator;
import splitter.shared.Member;
import splitter.shared.NewBill;
import splitter.shared.Result;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Model backing the Create Bill form: tracks input fields, computes validations and equal split.
 */
public class BillFormModel {
    private final GroupModel group;
    private final ConnectionModel connection;

    private String description = "";
    private String amountText = "";
    private final Map<String, String> weightTexts = new LinkedHashMap<>();
    private String dateText = "";

    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public BillFormModel(GroupModel group, ConnectionModel connection) {
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.connection = Objects.requireNonNull(connection, "ConnectionModel cannot be null");

        this.group.addListener(this::syncWithGroupMembers);
        this.connection.addListener(this::notifyListeners);
    }

    public void addListener(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (Runnable r : listeners) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }

    private void syncWithGroupMembers() {
        // Keep existing typed weights, ensure all current members are present
        List<Member> members = group.getCurrentMembers();
        for (Member m : members) {
            weightTexts.putIfAbsent(m.getName(), "0.00");
        }
        notifyListeners();
    }

    public synchronized void setDescription(String text) {
        this.description = text != null ? text : "";
        notifyListeners();
    }

    public synchronized String getDescription() {
        return description;
    }

    public synchronized void setAmountText(String text) {
        this.amountText = text != null ? text : "";
        notifyListeners();
    }

    public synchronized String getAmountText() {
        return amountText;
    }

    public synchronized void setWeightText(String memberName, String text) {
        if (memberName != null) {
            weightTexts.put(memberName, text != null ? text : "");
            notifyListeners();
        }
    }

    public synchronized String getWeightText(String memberName) {
        return weightTexts.getOrDefault(memberName, "");
    }

    public synchronized void setDateText(String text) {
        this.dateText = text != null ? text : "";
        notifyListeners();
    }

    public synchronized String getDateText() {
        return dateText;
    }

    public synchronized void applyEqualSplit() {
        List<String> names = getWeightRowNames();
        Map<String, Integer> equal = InputValidator.equalWeights(names);
        for (Map.Entry<String, Integer> entry : equal.entrySet()) {
            weightTexts.put(entry.getKey(), Formatting.formatWeight(entry.getValue()));
        }
        notifyListeners();
    }

    public synchronized List<String> getWeightRowNames() {
        List<Member> current = group.getCurrentMembers();
        List<String> names = new ArrayList<>();
        for (Member m : current) {
            names.add(m.getName());
        }
        return names;
    }

    public synchronized boolean hasWeightError(String memberName) {
        String w = weightTexts.get(memberName);
        if (w == null) return true;
        return !InputValidator.parseWeightHundredths(w).isSuccess();
    }

    public synchronized int getWeightSumHundredths() {
        List<String> names = getWeightRowNames();
        int sum = 0;
        for (String name : names) {
            String wt = weightTexts.get(name);
            Result<Integer> res = InputValidator.parseWeightHundredths(wt);
            if (!res.isSuccess()) {
                return -1;
            }
            sum += res.getData();
        }
        return sum;
    }

    public synchronized boolean isSubmitEnabled() {
        if (!connection.isConnected()) {
            return false;
        }
        if (!InputValidator.validateName(description, "Description").isSuccess()) {
            return false;
        }
        Result<Long> amountRes = InputValidator.parseAmountCents(amountText);
        if (!amountRes.isSuccess() || amountRes.getData() <= 0) {
            return false;
        }
        Result<LocalDate> dateRes = InputValidator.parseOptionalDate(dateText);
        if (!dateRes.isSuccess()) {
            return false;
        }
        if (dateRes.getData() != null && dateRes.getData().isBefore(LocalDate.now())) {
            return false;
        }
        List<String> names = getWeightRowNames();
        if (names.isEmpty()) {
            return false;
        }
        for (String name : names) {
            if (hasWeightError(name)) {
                return false;
            }
        }
        return getWeightSumHundredths() == 100;
    }

    public synchronized Optional<NewBill> buildNewBill() {
        if (!isSubmitEnabled()) {
            return Optional.empty();
        }
        String validDesc = InputValidator.validateName(description, "Description").getData();
        long totalCents = InputValidator.parseAmountCents(amountText).getData();
        LocalDate date = InputValidator.parseOptionalDate(dateText).getData();

        Map<String, Integer> weights = new LinkedHashMap<>();
        for (String name : getWeightRowNames()) {
            weights.put(name, InputValidator.parseWeightHundredths(weightTexts.get(name)).getData());
        }
        return Optional.of(new NewBill(validDesc, totalCents, weights, date));
    }

    public synchronized void clear() {
        this.description = "";
        this.amountText = "";
        this.dateText = "";
        this.weightTexts.clear();
        syncWithGroupMembers();
        notifyListeners();
    }
}
