package splitter.server.controller;

import splitter.server.model.Group;
import splitter.server.model.GroupMember;
import splitter.server.model.GroupRepository;
import splitter.server.model.SplitCalculator;
import splitter.server.model.StorageException;
import splitter.shared.Bill;
import splitter.shared.BillShare;
import splitter.shared.FailureReasons;
import splitter.shared.GroupAccess;
import splitter.shared.InputValidator;
import splitter.shared.NewBill;
import splitter.shared.Result;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller for bill creation, date changes, payment confirmations, and bill cancellations.
 */
public class BillApiController {
    private final AuthGuard guard;
    private final GroupRepository groups;
    private final SplitCalculator splitter;
    private final GroupLockManager locks;
    private final Clock clock;

    public BillApiController(AuthGuard guard, GroupRepository groups, SplitCalculator splitter, GroupLockManager locks) {
        this(guard, groups, splitter, locks, Clock.systemDefaultZone());
    }

    public BillApiController(AuthGuard guard, GroupRepository groups, SplitCalculator splitter,
                             GroupLockManager locks, Clock clock) {
        this.guard = Objects.requireNonNull(guard, "AuthGuard cannot be null");
        this.groups = Objects.requireNonNull(groups, "GroupRepository cannot be null");
        this.splitter = Objects.requireNonNull(splitter, "SplitCalculator cannot be null");
        this.locks = Objects.requireNonNull(locks, "GroupLockManager cannot be null");
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");
    }

    public Result<Long> createBill(GroupAccess access, NewBill bill) {
        try {
            if (bill == null) {
                return Result.fail("Bill cannot be null");
            }
            Result<Group> authGroup = guard.authorizeGroupAccess(access);
            if (!authGroup.isSuccess()) {
                return Result.fail(authGroup.getFailureReason());
            }
            Group initialGroup = authGroup.getData();
            String username = access.getCredentials().getUsername();
            GroupMember callerMember = initialGroup.findCurrentMemberByUsername(username).orElseThrow();

            Result<String> validDesc = InputValidator.validateName(bill.getDescription(), "Description");
            if (!validDesc.isSuccess()) {
                return Result.fail(validDesc.getFailureReason());
            }

            if (bill.getTotalCents() <= 0) {
                return Result.fail("Amount must be greater than zero");
            }

            if (bill.getSuggestedDate().isPresent()) {
                LocalDate today = LocalDate.now(clock);
                if (!InputValidator.isNotInPast(bill.getSuggestedDateValue(), today)) {
                    return Result.fail("Suggested date cannot be in the past");
                }
            }

            Result<Void> weightsSum = InputValidator.validateWeightsSum(bill.getWeightsHundredths().values());
            if (!weightsSum.isSuccess()) {
                return Result.fail(weightsSum.getFailureReason());
            }

            locks.lock(initialGroup.getId());
            try {
                Group group = groups.loadGroup(initialGroup.getId()).orElseThrow(() -> new StorageException(FailureReasons.GROUP_NOT_FOUND));
                List<GroupMember> currentMembers = group.currentMembersInOrder();

                // Weights must name exactly the current members
                if (bill.getWeightsHundredths().size() != currentMembers.size()) {
                    return Result.fail("Weights must match current members");
                }
                for (GroupMember m : currentMembers) {
                    if (!bill.getWeightsHundredths().containsKey(m.getName())) {
                        return Result.fail("Missing weight for member: " + m.getName());
                    }
                }
                for (String wName : bill.getWeightsHundredths().keySet()) {
                    if (group.findCurrentMemberByName(wName).isEmpty()) {
                        return Result.fail("Weight specified for non-current member: " + wName);
                    }
                }

                Map<String, Long> sharesMap = splitter.computeShares(bill.getTotalCents(), bill.getWeightsHundredths());
                List<BillShare> sharesList = new ArrayList<>();
                Instant now = Instant.now(clock);

                for (GroupMember m : currentMembers) {
                    String mName = m.getName();
                    int weight = bill.getWeightsHundredths().get(mName);
                    long shareCents = sharesMap.getOrDefault(mName, 0L);
                    boolean isCaller = InputValidator.namesEqual(mName, callerMember.getName());
                    boolean isConfirmed = isCaller && shareCents > 0;
                    Instant confirmedAt = isConfirmed ? now : null;
                    sharesList.add(new BillShare(mName, weight, shareCents, isConfirmed, confirmedAt));
                }

                Bill newBill = new Bill(0, validDesc.getData(), bill.getTotalCents(),
                        callerMember.getName(), now, bill.getSuggestedDateValue(), sharesList);

                long billId = groups.addBill(group.getId(), newBill);
                return Result.ok(billId);
            } finally {
                locks.unlock(initialGroup.getId());
            }
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    public Result<Void> changeSuggestedDate(GroupAccess access, long billId, LocalDate newDate) {
        try {
            Result<Group> authGroup = guard.authorizeGroupAccess(access);
            if (!authGroup.isSuccess()) {
                return Result.fail(authGroup.getFailureReason());
            }
            Group group = authGroup.getData();
            Optional<Bill> optBill = group.findBill(billId);
            if (optBill.isEmpty()) {
                return Result.fail(FailureReasons.BILL_NOT_FOUND);
            }
            Bill bill = optBill.get();

            Result<Void> reqOwner = guard.requireBillOwner(group, bill, access.getCredentials().getUsername());
            if (!reqOwner.isSuccess()) {
                return Result.fail(reqOwner.getFailureReason());
            }

            if (newDate != null) {
                LocalDate today = LocalDate.now(clock);
                if (!InputValidator.isNotInPast(newDate, today)) {
                    return Result.fail("Suggested date cannot be in the past");
                }
            }

            locks.lock(group.getId());
            try {
                boolean ok = groups.setBillDate(group.getId(), billId, newDate);
                if (!ok) {
                    return Result.fail(FailureReasons.BILL_NOT_FOUND);
                }
                return Result.ok();
            } finally {
                locks.unlock(group.getId());
            }
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    public Result<Void> confirmPayment(GroupAccess access, long billId, String memberName) {
        try {
            Result<Group> authGroup = guard.authorizeGroupAccess(access);
            if (!authGroup.isSuccess()) {
                return Result.fail(authGroup.getFailureReason());
            }
            Group group = authGroup.getData();
            Optional<Bill> optBill = group.findBill(billId);
            if (optBill.isEmpty()) {
                return Result.fail(FailureReasons.BILL_NOT_FOUND);
            }
            Bill bill = optBill.get();

            Result<Void> reqAuth = guard.requireMayConfirmPayment(group, bill, access.getCredentials().getUsername());
            if (!reqAuth.isSuccess()) {
                return Result.fail(reqAuth.getFailureReason());
            }

            Optional<BillShare> optShare = bill.findShare(memberName);
            if (optShare.isEmpty()) {
                return Result.fail("Share not found for member: " + memberName);
            }
            if (optShare.get().isConfirmed()) {
                return Result.fail(FailureReasons.ALREADY_CONFIRMED);
            }

            locks.lock(group.getId());
            try {
                Group reloaded = groups.loadGroup(group.getId()).orElseThrow(() -> new StorageException(FailureReasons.GROUP_NOT_FOUND));
                Optional<Bill> reloadedBill = reloaded.findBill(billId);
                if (reloadedBill.isEmpty()) {
                    return Result.fail(FailureReasons.BILL_NOT_FOUND);
                }
                Optional<BillShare> reloadedShare = reloadedBill.get().findShare(memberName);
                if (reloadedShare.isEmpty() || reloadedShare.get().isConfirmed()) {
                    return Result.fail(FailureReasons.ALREADY_CONFIRMED);
                }

                boolean ok = groups.confirmShare(group.getId(), billId, memberName, Instant.now(clock));
                if (!ok) {
                    return Result.fail(FailureReasons.BILL_NOT_FOUND);
                }
                return Result.ok();
            } finally {
                locks.unlock(group.getId());
            }
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    public Result<Void> cancelBill(GroupAccess access, long billId) {
        try {
            Result<Group> authGroup = guard.authorizeGroupAccess(access);
            if (!authGroup.isSuccess()) {
                return Result.fail(authGroup.getFailureReason());
            }
            Group group = authGroup.getData();
            Optional<Bill> optBill = group.findBill(billId);
            if (optBill.isEmpty()) {
                return Result.fail(FailureReasons.BILL_NOT_FOUND);
            }
            Bill bill = optBill.get();

            Result<Void> reqAuth = guard.requireBillOwnerOrGroupOwner(group, bill, access.getCredentials().getUsername());
            if (!reqAuth.isSuccess()) {
                return Result.fail(reqAuth.getFailureReason());
            }

            locks.lock(group.getId());
            try {
                boolean ok = groups.deleteBill(group.getId(), billId);
                if (!ok) {
                    return Result.fail(FailureReasons.BILL_NOT_FOUND);
                }
                return Result.ok();
            } finally {
                locks.unlock(group.getId());
            }
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }

    public Result<Void> cancelAllBills(GroupAccess access) {
        try {
            Result<Group> authGroup = guard.authorizeGroupAccess(access);
            if (!authGroup.isSuccess()) {
                return Result.fail(authGroup.getFailureReason());
            }
            Group group = authGroup.getData();
            Result<Void> reqOwner = guard.requireGroupOwner(group, access.getCredentials().getUsername());
            if (!reqOwner.isSuccess()) {
                return Result.fail(reqOwner.getFailureReason());
            }

            locks.lock(group.getId());
            try {
                groups.deleteAllBills(group.getId());
                return Result.ok();
            } finally {
                locks.unlock(group.getId());
            }
        } catch (StorageException e) {
            return Result.fail("Storage error: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail("Unexpected error: " + e.getMessage());
        }
    }
}
