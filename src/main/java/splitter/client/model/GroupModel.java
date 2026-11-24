package splitter.client.model;

import splitter.shared.Bill;
import splitter.shared.GroupSnapshot;
import splitter.shared.InputValidator;
import splitter.shared.Member;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stores the active group's snapshot and current selection state.
 */
public class GroupModel {
    private GroupSnapshot snapshot;
    private Long selectedBillId = null;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public GroupModel() {}

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

    public synchronized void replaceSnapshot(GroupSnapshot newSnapshot) {
        if (!Objects.equals(this.snapshot, newSnapshot)) {
            this.snapshot = newSnapshot;
            if (newSnapshot != null && selectedBillId != null) {
                if (newSnapshot.findBill(selectedBillId).isEmpty()) {
                    selectedBillId = null;
                }
            }
            notifyListeners();
        }
    }

    public synchronized void clear() {
        this.snapshot = null;
        this.selectedBillId = null;
        notifyListeners();
    }

    public synchronized Optional<GroupSnapshot> getSnapshot() {
        return Optional.ofNullable(snapshot);
    }

    public synchronized long getGroupId() {
        return snapshot != null ? snapshot.getGroupId() : 0L;
    }

    public synchronized List<Member> getCurrentMembers() {
        if (snapshot == null) return Collections.emptyList();
        return snapshot.getCurrentMembers();
    }

    public synchronized boolean currentUserIsGroupOwner() {
        return snapshot != null && snapshot.isOwner();
    }

    public synchronized Optional<Bill> findBill(long billId) {
        if (snapshot == null) return Optional.empty();
        return snapshot.findBill(billId);
    }

    public synchronized boolean canConfirmPayments(long billId) {
        if (snapshot == null) return false;
        Optional<Bill> optBill = snapshot.findBill(billId);
        if (optBill.isEmpty()) return false;
        Bill bill = optBill.get();

        String currentMemberName = snapshot.getCurrentMemberName();
        if (InputValidator.namesEqual(currentMemberName, bill.getBillOwnerMemberName())) {
            return true;
        }

        if (snapshot.isOwner()) {
            for (Member m : snapshot.getMembers()) {
                if (InputValidator.namesEqual(m.getName(), bill.getBillOwnerMemberName())) {
                    if (m.hasLeft()) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    public synchronized boolean canChangeDate(long billId) {
        if (snapshot == null) return false;
        Optional<Bill> optBill = snapshot.findBill(billId);
        if (optBill.isEmpty()) return false;
        return InputValidator.namesEqual(snapshot.getCurrentMemberName(), optBill.get().getBillOwnerMemberName());
    }

    public synchronized boolean canCancelBill(long billId) {
        if (snapshot == null) return false;
        Optional<Bill> optBill = snapshot.findBill(billId);
        if (optBill.isEmpty()) return false;
        if (InputValidator.namesEqual(snapshot.getCurrentMemberName(), optBill.get().getBillOwnerMemberName())) {
            return true;
        }
        return snapshot.isOwner();
    }

    public synchronized void selectBill(Long billId) {
        if (!Objects.equals(this.selectedBillId, billId)) {
            this.selectedBillId = billId;
            notifyListeners();
        }
    }

    public synchronized Optional<Long> getSelectedBillId() {
        return Optional.ofNullable(selectedBillId);
    }
}
