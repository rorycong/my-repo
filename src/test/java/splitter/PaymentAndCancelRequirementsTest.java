package splitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import splitter.server.controller.AuthGuard;
import splitter.server.controller.BillApiController;
import splitter.server.controller.GroupApiController;
import splitter.server.controller.GroupLockManager;
import splitter.server.controller.UserApiController;
import splitter.server.model.GroupCodeGenerator;
import splitter.server.model.GroupRepository;
import splitter.server.model.SplitCalculator;
import splitter.server.model.TokenGenerator;
import splitter.server.model.UserRepository;
import splitter.shared.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentAndCancelRequirementsTest {
    @TempDir
    Path tempDir;

    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private AuthGuard authGuard;
    private GroupLockManager lockManager;
    private GroupApiController groupApiController;
    private BillApiController billApiController;

    private Credentials aliceCreds;
    private Credentials bobCreds;
    private Credentials charlieCreds;

    @BeforeEach
    public void setUp() {
        userRepository = new UserRepository(tempDir.resolve("users.db"));
        groupRepository = new GroupRepository(tempDir.resolve("groups.db"));
        authGuard = new AuthGuard(userRepository, groupRepository);
        lockManager = new GroupLockManager();
        groupApiController = new GroupApiController(authGuard, groupRepository, new GroupCodeGenerator(), lockManager);
        billApiController = new BillApiController(authGuard, groupRepository, new SplitCalculator(), lockManager);

        UserApiController uac = new UserApiController(userRepository, new TokenGenerator());
        aliceCreds = uac.createUser("Alice").getData();
        bobCreds = uac.createUser("Bob").getData();
        charlieCreds = uac.createUser("Charlie").getData();
    }

    @Test
    public void BLD_8_to_10_display_formatting() {
        assertEquals("$12.50", Formatting.formatCents(1250));
        assertEquals("$0.05", Formatting.formatCents(5));
        assertEquals("-$12.50", Formatting.formatCents(-1250));
        assertEquals("$0.00", Formatting.formatCents(0));

        assertEquals("0.25", Formatting.formatWeight(25));
        assertEquals("1.00", Formatting.formatWeight(100));
        assertEquals("0.00", Formatting.formatWeight(0));

        assertEquals("2026-11-20", Formatting.formatDateOrNone(LocalDate.of(2026, 11, 20)));
        assertEquals("No date", Formatting.formatDateOrNone(null));
    }

    @Test
    public void DAT_3_4_8_dates_and_changing() {
        assertFalse(InputValidator.parseOptionalDate("2026-02-30").isSuccess()); // Invalid leap year
        assertFalse(InputValidator.parseOptionalDate("26-1-1").isSuccess()); // Non YYYY-MM-DD
        assertTrue(InputValidator.parseOptionalDate("2026-11-20").isSuccess());

        GroupSnapshot g1 = groupApiController.createGroup(aliceCreds, "Trip", "Al").getData();
        String code = g1.getGroupCode().get();
        groupApiController.joinGroup(bobCreds, code, "Bo");
        GroupAccess aliceAccess = new GroupAccess(aliceCreds, g1.getGroupId(), code);
        GroupAccess bobAccess = new GroupAccess(bobCreds, g1.getGroupId(), code);

        // Bob creates a bill
        Map<String, Integer> weights = new LinkedHashMap<>();
        weights.put("Al", 50);
        weights.put("Bo", 50);
        NewBill bill = new NewBill("Cab", 2000, weights, null);
        long billId = billApiController.createBill(bobAccess, bill).getData();

        // Alice (group owner, but NOT bill owner) tries to change date -> fails (DAT-8)
        Result<Void> nonOwnerChange = billApiController.changeSuggestedDate(aliceAccess, billId, LocalDate.now().plusDays(5));
        assertFalse(nonOwnerChange.isSuccess());

        // Bob (bill owner) changes date -> succeeds
        Result<Void> ownerChange = billApiController.changeSuggestedDate(bobAccess, billId, LocalDate.now().plusDays(5));
        assertTrue(ownerChange.isSuccess());
        GroupSnapshot snap = groupApiController.getGroup(bobAccess).getData();
        assertEquals(LocalDate.now().plusDays(5), snap.getBills().get(0).getSuggestedDateValue());
    }

    @Test
    public void PAY_1_to_12_confirming_payment_permissions_and_state() {
        GroupSnapshot g1 = groupApiController.createGroup(aliceCreds, "Trip", "Al").getData();
        String code = g1.getGroupCode().get();
        groupApiController.joinGroup(bobCreds, code, "Bo");
        GroupAccess aliceAccess = new GroupAccess(aliceCreds, g1.getGroupId(), code);
        GroupAccess bobAccess = new GroupAccess(bobCreds, g1.getGroupId(), code);

        // Bob creates a bill
        Map<String, Integer> weights = new LinkedHashMap<>();
        weights.put("Al", 50);
        weights.put("Bo", 50);
        NewBill bill = new NewBill("Cab", 2000, weights, null);
        long billId = billApiController.createBill(bobAccess, bill).getData();

        // Alice (debtor) tries to confirm payment for herself -> rejected
        assertFalse(billApiController.confirmPayment(aliceAccess, billId, "Al").isSuccess());

        // Bob (bill owner) confirms Al's share -> succeeds
        assertTrue(billApiController.confirmPayment(bobAccess, billId, "Al").isSuccess());

        // Confirming again fails with "already confirmed"
        Result<Void> repeat = billApiController.confirmPayment(bobAccess, billId, "Al");
        assertFalse(repeat.isSuccess());
        assertEquals(FailureReasons.ALREADY_CONFIRMED, repeat.getFailureReason());

        // Test bill owner leaves group: now group owner CAN confirm remaining unpaid shares (PAY-1)
        groupApiController.joinGroup(charlieCreds, code, "Chuck");
        GroupAccess charlieAccess = new GroupAccess(charlieCreds, g1.getGroupId(), code);

        Map<String, Integer> weights3 = new LinkedHashMap<>();
        weights3.put("Al", 34);
        weights3.put("Bo", 33);
        weights3.put("Chuck", 33);
        long billId2 = billApiController.createBill(bobAccess, new NewBill("Snacks", 3000, weights3, null)).getData();

        // Bob leaves
        groupApiController.leaveGroup(bobAccess);

        // Group owner Alice can now confirm Chuck's payment on Bob's bill
        assertTrue(billApiController.confirmPayment(aliceAccess, billId2, "Chuck").isSuccess());
    }

    @Test
    public void CAN_7_to_24_cancelling_bills() {
        GroupSnapshot g1 = groupApiController.createGroup(aliceCreds, "Trip", "Al").getData();
        String code = g1.getGroupCode().get();
        groupApiController.joinGroup(bobCreds, code, "Bo");
        groupApiController.joinGroup(charlieCreds, code, "Chuck");

        GroupAccess aliceAccess = new GroupAccess(aliceCreds, g1.getGroupId(), code);
        GroupAccess bobAccess = new GroupAccess(bobCreds, g1.getGroupId(), code);
        GroupAccess charlieAccess = new GroupAccess(charlieCreds, g1.getGroupId(), code);

        Map<String, Integer> weights = new LinkedHashMap<>();
        weights.put("Al", 34);
        weights.put("Bo", 33);
        weights.put("Chuck", 33);

        long bill1 = billApiController.createBill(bobAccess, new NewBill("B1", 1000, weights, null)).getData();
        long bill2 = billApiController.createBill(bobAccess, new NewBill("B2", 1000, weights, null)).getData();

        // Charlie (other member) tries to cancel bill1 -> rejected
        assertFalse(billApiController.cancelBill(charlieAccess, bill1).isSuccess());

        // Bob (bill owner) cancels bill1 -> succeeds
        assertTrue(billApiController.cancelBill(bobAccess, bill1).isSuccess());

        // Cancelling missing bill returns "bill not found"
        Result<Void> missingRes = billApiController.cancelBill(bobAccess, 9999);
        assertFalse(missingRes.isSuccess());
        assertEquals(FailureReasons.BILL_NOT_FOUND, missingRes.getFailureReason());

        // Alice (group owner) cancels bill2 -> succeeds
        assertTrue(billApiController.cancelBill(aliceAccess, bill2).isSuccess());

        // Add 2 more bills, then Alice cancels all
        billApiController.createBill(aliceAccess, new NewBill("B3", 500, weights, null));
        billApiController.createBill(bobAccess, new NewBill("B4", 500, weights, null));
        assertEquals(2, groupApiController.getGroup(aliceAccess).getData().getBills().size());

        // Bob (member) cannot cancel all
        assertFalse(billApiController.cancelAllBills(bobAccess).isSuccess());

        // Alice (owner) cancels all
        assertTrue(billApiController.cancelAllBills(aliceAccess).isSuccess());
        assertEquals(0, groupApiController.getGroup(aliceAccess).getData().getBills().size());
    }
}
