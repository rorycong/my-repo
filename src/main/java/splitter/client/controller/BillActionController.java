package splitter.client.controller;

import splitter.client.model.BillFormModel;
import splitter.client.model.GroupModel;
import splitter.client.view.ConfirmDialogView;
import splitter.client.view.ErrorMessageView;
import splitter.shared.FailureReasons;
import splitter.shared.InputValidator;
import splitter.shared.NewBill;
import splitter.shared.Result;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller for bill actions: creating bills, editing dates, confirming payments, cancelling bills.
 */
public class BillActionController {
    private final ServerConnection server;
    private final GroupModel group;
    private final BillFormModel form;
    private final SyncController sync;
    private final ConfirmDialogView confirm;
    private final ErrorMessageView errors;

    public BillActionController(ServerConnection server, GroupModel group, BillFormModel form,
                                SyncController sync, ConfirmDialogView confirm, ErrorMessageView errors) {
        this.server = Objects.requireNonNull(server, "ServerConnection cannot be null");
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.form = Objects.requireNonNull(form, "BillFormModel cannot be null");
        this.sync = sync; // may be wired or injected
        this.confirm = Objects.requireNonNull(confirm, "ConfirmDialogView cannot be null");
        this.errors = Objects.requireNonNull(errors, "ErrorMessageView cannot be null");
    }

    public void descriptionChanged(String text) {
        form.setDescription(text);
    }

    public void amountChanged(String text) {
        form.setAmountText(text);
    }

    public void weightChanged(String memberName, String text) {
        form.setWeightText(memberName, text);
    }

    public void dateChanged(String text) {
        form.setDateText(text);
    }

    public void splitEquallyRequested() {
        form.applyEqualSplit();
    }

    public void createBillRequested() {
        Optional<NewBill> optBill = form.buildNewBill();
        if (optBill.isEmpty()) {
            errors.show("Please ensure all fields are valid and weights sum to 1.00");
            return;
        }

        long groupId = group.getGroupId();
        Result<Long> result = server.createBill(groupId, optBill.get());
        if (result.isSuccess()) {
            form.clear();
            if (sync != null) sync.refreshNow();
        } else {
            errors.show(result.getFailureReason());
        }
    }

    public void billSelected(long billId) {
        group.selectBill(billId);
    }

    public void changeDateRequested(long billId, String newDateText) {
        Result<LocalDate> dateRes = InputValidator.parseOptionalDate(newDateText);
        if (!dateRes.isSuccess()) {
            errors.show(dateRes.getFailureReason());
            return;
        }

        long groupId = group.getGroupId();
        Result<Void> result = server.changeSuggestedDate(groupId, billId, dateRes.getData());
        if (result.isSuccess()) {
            if (sync != null) sync.refreshNow();
        } else {
            if (FailureReasons.BILL_NOT_FOUND.equals(result.getFailureReason())) {
                errors.show("The bill was already cancelled.");
            } else {
                errors.show(result.getFailureReason());
            }
        }
    }

    public void confirmPaymentRequested(long billId, String memberName) {
        long groupId = group.getGroupId();
        Result<Void> result = server.confirmPayment(groupId, billId, memberName);
        if (result.isSuccess()) {
            if (sync != null) sync.refreshNow();
        } else {
            if (FailureReasons.BILL_NOT_FOUND.equals(result.getFailureReason())) {
                errors.show("The bill was already cancelled.");
            } else {
                errors.show(result.getFailureReason());
            }
        }
    }

    public void cancelBillRequested(long billId) {
        long groupId = group.getGroupId();
        confirm.ask("Cancel Bill", "Are you sure you want to cancel this bill? This action cannot be undone.", () -> {
            Result<Void> result = server.cancelBill(groupId, billId);
            if (result.isSuccess()) {
                if (sync != null) sync.refreshNow();
            } else {
                if (FailureReasons.BILL_NOT_FOUND.equals(result.getFailureReason())) {
                    errors.show("The bill was already cancelled.");
                } else {
                    errors.show(result.getFailureReason());
                }
            }
        });
    }
}
