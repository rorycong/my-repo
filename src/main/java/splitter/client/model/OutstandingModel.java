package splitter.client.model;

import splitter.shared.Bill;
import splitter.shared.BillShare;
import splitter.shared.InputValidator;
import splitter.shared.Member;
import splitter.shared.OutstandingAmounts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Calculates current members' owes and is-owed amounts dynamically from bills.
 */
public class OutstandingModel {
    private final GroupModel group;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public OutstandingModel(GroupModel group) {
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.group.addListener(this::notifyListeners);
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

    public List<OutstandingAmounts> getOutstanding() {
        List<Member> currentMembers = group.getCurrentMembers();
        if (currentMembers.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Long> owesMap = new LinkedHashMap<>();
        Map<String, Long> isOwedMap = new LinkedHashMap<>();
        for (Member m : currentMembers) {
            owesMap.put(m.getName(), 0L);
            isOwedMap.put(m.getName(), 0L);
        }

        List<Bill> bills = group.getSnapshot().map(s -> s.getBills()).orElse(Collections.emptyList());
        for (Bill b : bills) {
            String billOwner = b.getBillOwnerMemberName();
            for (BillShare s : b.getShares()) {
                if (s.isConfirmed() || s.getShareCents() <= 0) {
                    continue;
                }
                String debtor = s.getMemberName();
                if (InputValidator.namesEqual(debtor, billOwner)) {
                    continue; // Bill owner's own share is never counted (OUT-3)
                }

                // Debtor owes
                for (String currentName : owesMap.keySet()) {
                    if (InputValidator.namesEqual(currentName, debtor)) {
                        owesMap.put(currentName, owesMap.get(currentName) + s.getShareCents());
                    }
                }
                // Bill owner is owed
                for (String currentName : isOwedMap.keySet()) {
                    if (InputValidator.namesEqual(currentName, billOwner)) {
                        isOwedMap.put(currentName, isOwedMap.get(currentName) + s.getShareCents());
                    }
                }
            }
        }

        List<OutstandingAmounts> result = new ArrayList<>();
        for (Member m : currentMembers) {
            long owes = owesMap.getOrDefault(m.getName(), 0L);
            long isOwed = isOwedMap.getOrDefault(m.getName(), 0L);
            result.add(new OutstandingAmounts(m.getName(), owes, isOwed));
        }
        return result;
    }
}
