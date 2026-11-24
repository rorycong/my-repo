package splitter.client.view;

import splitter.client.controller.BillActionController;
import splitter.client.model.ConnectionModel;
import splitter.client.model.GroupModel;
import splitter.shared.Bill;
import splitter.shared.BillShare;
import splitter.shared.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * View showing details of the currently selected bill, with actions to confirm payment,
 * change date, or cancel the bill.
 */
public class BillDetailsView {
    private final BillActionController controller;
    private final GroupModel group;
    private final ConnectionModel connection;

    public static class ShareRow {
        public final String memberName;
        public final String weightFormatted;
        public final String shareFormatted;
        public final boolean isPaid;
        public final boolean canConfirm;

        public ShareRow(String memberName, String weightFormatted, String shareFormatted, boolean isPaid, boolean canConfirm) {
            this.memberName = memberName;
            this.weightFormatted = weightFormatted;
            this.shareFormatted = shareFormatted;
            this.isPaid = isPaid;
            this.canConfirm = canConfirm;
        }
    }

    private Long billId = null;
    private String description = "";
    private String totalFormatted = "";
    private String statusText = ""; // "Settled" or "Open"
    private boolean canChangeDate = false;
    private boolean canCancelBill = false;
    private final List<ShareRow> shareRows = new ArrayList<>();

    public BillDetailsView(BillActionController controller, GroupModel group, ConnectionModel connection) {
        this.controller = Objects.requireNonNull(controller, "BillActionController cannot be null");
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.connection = Objects.requireNonNull(connection, "ConnectionModel cannot be null");

        this.group.addListener(this::refresh);
        this.connection.addListener(this::refresh);
        refresh();
    }

    public void refresh() {
        shareRows.clear();
        Optional<Long> optSelected = group.getSelectedBillId();
        if (optSelected.isEmpty()) {
            billId = null;
            description = "";
            totalFormatted = "";
            statusText = "";
            canChangeDate = false;
            canCancelBill = false;
            return;
        }

        long bid = optSelected.get();
        Optional<Bill> optBill = group.findBill(bid);
        if (optBill.isEmpty()) {
            billId = null;
            return;
        }

        Bill bill = optBill.get();
        this.billId = bill.getId();
        this.description = bill.getDescription();
        this.totalFormatted = Formatting.formatCents(bill.getTotalCents());
        this.statusText = bill.isFullyPaid() ? "Settled" : "Open";

        boolean connected = connection.isConnected();
        this.canChangeDate = connected && group.canChangeDate(bid);
        this.canCancelBill = connected && group.canCancelBill(bid);
        boolean canConfirmAny = connected && group.canConfirmPayments(bid);

        for (BillShare s : bill.getShares()) {
            if (s.getShareCents() == 0 && s.getWeightHundredths() == 0) {
                // SPL-9: Zero-weight member in details not shown
                continue;
            }
            boolean isPaid = s.isConfirmed();
            boolean canConfirmThis = canConfirmAny && !isPaid;
            shareRows.add(new ShareRow(
                    s.getMemberName(),
                    Formatting.formatWeight(s.getWeightHundredths()),
                    Formatting.formatCents(s.getShareCents()),
                    isPaid,
                    canConfirmThis
            ));
        }
    }

    public void onConfirmPaymentClicked(long billId, String memberName) {
        controller.confirmPaymentRequested(billId, memberName);
    }

    public void onChangeDateSubmitted(long billId, String newDateText) {
        controller.changeDateRequested(billId, newDateText);
    }

    public void onCancelBillClicked(long billId) {
        controller.cancelBillRequested(billId);
    }

    public boolean hasSelection() {
        return billId != null;
    }

    public Long getBillId() {
        return billId;
    }

    public String getDescription() {
        return description;
    }

    public String getTotalFormatted() {
        return totalFormatted;
    }

    public String getStatusText() {
        return statusText;
    }

    public boolean isCanChangeDate() {
        return canChangeDate;
    }

    public boolean isCanCancelBill() {
        return canCancelBill;
    }

    public List<ShareRow> getShareRows() {
        return shareRows;
    }
}
