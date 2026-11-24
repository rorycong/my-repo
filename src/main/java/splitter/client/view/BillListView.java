package splitter.client.view;

import splitter.client.controller.BillActionController;
import splitter.client.model.GroupModel;
import splitter.shared.Bill;
import splitter.shared.Formatting;
import splitter.shared.InputValidator;
import splitter.shared.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * View showing the scrollable list of bills in the open group.
 */
public class BillListView {
    private final BillActionController controller;
    private final GroupModel group;

    public static class BillRow {
        public final long id;
        public final String description;
        public final String totalFormatted;
        public final String ownerDisplay;
        public final String dateFormatted;
        public final String paidStatus;
        public final boolean isSelected;

        public BillRow(long id, String description, String totalFormatted, String ownerDisplay,
                       String dateFormatted, String paidStatus, boolean isSelected) {
            this.id = id;
            this.description = description;
            this.totalFormatted = totalFormatted;
            this.ownerDisplay = ownerDisplay;
            this.dateFormatted = dateFormatted;
            this.paidStatus = paidStatus;
            this.isSelected = isSelected;
        }
    }

    private final List<BillRow> rows = new ArrayList<>();

    public BillListView(BillActionController controller, GroupModel group) {
        this.controller = Objects.requireNonNull(controller, "BillActionController cannot be null");
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.group.addListener(this::refresh);
        refresh();
    }

    public void refresh() {
        rows.clear();
        Optional<Long> selectedId = group.getSelectedBillId();
        List<Bill> bills = group.getSnapshot().map(s -> s.getBills()).orElse(Collections.emptyList());
        List<Member> allMembers = group.getSnapshot().map(s -> s.getMembers()).orElse(Collections.emptyList());

        for (Bill b : bills) {
            String ownerName = b.getBillOwnerMemberName();
            boolean ownerHasLeft = false;
            for (Member m : allMembers) {
                if (InputValidator.namesEqual(m.getName(), ownerName)) {
                    ownerHasLeft = m.hasLeft();
                    break;
                }
            }
            String ownerDisplay = ownerHasLeft ? ownerName + " (left)" : ownerName;
            String dateFormatted = Formatting.formatDateOrNone(b.getSuggestedDateValue());
            String paidStatus = b.getPaidSharesCount() + " of " + b.getPayableSharesCount() + " paid";
            boolean isSel = selectedId.isPresent() && selectedId.get() == b.getId();

            rows.add(new BillRow(
                    b.getId(),
                    b.getDescription(),
                    Formatting.formatCents(b.getTotalCents()),
                    ownerDisplay,
                    dateFormatted,
                    paidStatus,
                    isSel
            ));
        }
    }

    public void onBillSelected(long billId) {
        controller.billSelected(billId);
    }

    public List<BillRow> getRows() {
        return rows;
    }

    public boolean hasBills() {
        return !rows.isEmpty();
    }
}
