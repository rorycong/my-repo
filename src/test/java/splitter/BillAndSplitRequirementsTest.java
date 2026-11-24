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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BillAndSplitRequirementsTest {
    @TempDir
    Path tempDir;

    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private AuthGuard authGuard;
    private GroupLockManager lockManager;
    private GroupApiController groupApiController;
    private BillApiController billApiController;
    private SplitCalculator splitCalculator;

    private Credentials aliceCreds;
    private Credentials bobCreds;
    private Credentials charlieCreds;

    @BeforeEach
    public void setUp() {
        userRepository = new UserRepository(tempDir.resolve("users.db"));
        groupRepository = new GroupRepository(tempDir.resolve("groups.db"));
        authGuard = new AuthGuard(userRepository, groupRepository);
        lockManager = new GroupLockManager();
        splitCalculator = new SplitCalculator();
        groupApiController = new GroupApiController(authGuard, groupRepository, new GroupCodeGenerator(), lockManager);
        billApiController = new BillApiController(authGuard, groupRepository, splitCalculator, lockManager);

        UserApiController uac = new UserApiController(userRepository, new TokenGenerator());
        aliceCreds = uac.createUser("Alice").getData();
        bobCreds = uac.createUser("Bob").getData();
        charlieCreds = uac.createUser("Charlie").getData();
    }

    @Test
    public void BIL_10_to_14_amount_parsing() {
        assertEquals(1250L, InputValidator.parseAmountCents("12.50").getData());
        assertEquals(1250L, InputValidator.parseAmountCents("12.5").getData());
        assertEquals(30L, InputValidator.parseAmountCents("0.30").getData());
        assertEquals(30L, InputValidator.parseAmountCents(".30").getData());
        assertEquals(1000L, InputValidator.parseAmountCents("$10.00").getData());

        // Invalid cases
        assertFalse(InputValidator.parseAmountCents("12.345").isSuccess()); // > 2 decimals
        assertFalse(InputValidator.parseAmountCents("abc").isSuccess());
        assertFalse(InputValidator.parseAmountCents("0").isSuccess());
        assertFalse(InputValidator.parseAmountCents("-5").isSuccess());
        assertFalse(InputValidator.parseAmountCents("").isSuccess());
    }

    @Test
    public void BIL_11_weight_parsing() {
        assertEquals(0, InputValidator.parseWeightHundredths("0").getData());
        assertEquals(100, InputValidator.parseWeightHundredths("1").getData());
        assertEquals(100, InputValidator.parseWeightHundredths("1.00").getData());
        assertEquals(50, InputValidator.parseWeightHundredths("0.50").getData());
        assertEquals(25, InputValidator.parseWeightHundredths("0.25").getData());

        // Invalid cases
        assertFalse(InputValidator.parseWeightHundredths("0.005").isSuccess()); // > 2 decimals
        assertFalse(InputValidator.parseWeightHundredths("1.01").isSuccess()); // > 1.00
        assertFalse(InputValidator.parseWeightHundredths("-0.1").isSuccess());
        assertFalse(InputValidator.parseWeightHundredths("abc").isSuccess());
    }

    @Test
    public void BIL_16_to_19_weights_sum_and_equal_split_P2_proof() {
        // Only 1.00 accepted
        assertFalse(InputValidator.validateWeightsSum(List.of(50, 49)).isSuccess()); // 0.99
        assertFalse(InputValidator.validateWeightsSum(List.of(50, 51)).isSuccess()); // 1.01
        assertTrue(InputValidator.validateWeightsSum(List.of(50, 50)).isSuccess()); // 1.00

        // P2: Equal weights for 1 to 20 members always sums to 100 hundredths
        for (int n = 1; n <= 20; n++) {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < n; i++) names.add("Member" + i);
            Map<String, Integer> eq = InputValidator.equalWeights(names);
            assertEquals(n, eq.size());
            int sum = 0;
            for (int w : eq.values()) sum += w;
            assertEquals(100, sum, "Sum failed for n=" + n);
        }
    }

    @Test
    public void SPL_1_to_7_shares_calculation_examples() {
        // $10.00 (1000 cents) three ways with weights 34, 33, 33 -> 340, 330, 330 (and for 100 cents: 34, 33, 33)
        Map<String, Integer> w3 = new LinkedHashMap<>();
        w3.put("A", 34);
        w3.put("B", 33);
        w3.put("C", 33);
        Map<String, Long> shares3 = splitCalculator.computeShares(1000, w3);
        assertEquals(340L, shares3.get("A"));
        assertEquals(330L, shares3.get("B"));
        assertEquals(330L, shares3.get("C"));

        Map<String, Long> shares100 = splitCalculator.computeShares(100, w3);
        assertEquals(34L, shares100.get("A"));
        assertEquals(33L, shares100.get("B"));
        assertEquals(33L, shares100.get("C"));

        // $0.01 (1 cent) two ways (50, 50): 1¢, 0¢
        Map<String, Integer> w2 = new LinkedHashMap<>();
        w2.put("A", 50);
        w2.put("B", 50);
        Map<String, Long> shares2 = splitCalculator.computeShares(1, w2);
        assertEquals(1L, shares2.get("A"));
        assertEquals(0L, shares2.get("B"));

        // One person at 1.00: full amount
        Map<String, Integer> w1 = new LinkedHashMap<>();
        w1.put("A", 100);
        Map<String, Long> shares1 = splitCalculator.computeShares(500, w1);
        assertEquals(500L, shares1.get("A"));

        // Zero weight member gets 0¢
        Map<String, Integer> wZero = new LinkedHashMap<>();
        wZero.put("A", 100);
        wZero.put("B", 0);
        Map<String, Long> sharesZero = splitCalculator.computeShares(500, wZero);
        assertEquals(500L, sharesZero.get("A"));
        assertEquals(0L, sharesZero.get("B"));
    }

    @Test
    public void SPL_4_P1_proof_1000_random_bills() {
        Random rnd = new Random(42);
        for (int i = 0; i < 1000; i++) {
            long total = rnd.nextInt(1_000_000) + 1; // 1¢ to $10,000.00
            int n = rnd.nextInt(10) + 1; // 1 to 10 members

            // Generate weights summing to 100
            int[] cuts = new int[n - 1];
            for (int c = 0; c < n - 1; c++) cuts[c] = rnd.nextInt(101);
            Arrays.sort(cuts);

            Map<String, Integer> weights = new LinkedHashMap<>();
            int prev = 0;
            for (int m = 0; m < n - 1; m++) {
                int w = cuts[m] - prev;
                weights.put("M" + m, w);
                prev = cuts[m];
            }
            weights.put("M" + (n - 1), 100 - prev);

            Map<String, Long> shares = splitCalculator.computeShares(total, weights);

            // Check exact sum equals total
            long sum = 0;
            for (Map.Entry<String, Long> entry : shares.entrySet()) {
                String name = entry.getKey();
                long share = entry.getValue();
                sum += share;
                int w = weights.get(name);
                long floorShare = (total * w) / 100L;
                // Nobody gets more than 1 cent extra over floor
                assertTrue(share == floorShare || share == floorShare + 1,
                        "Share exceeds floor + 1: share=" + share + ", floor=" + floorShare);
            }
            assertEquals(total, sum, "Shares sum does not equal total");
        }
    }

    @Test
    public void BIL_8_30_to_35_bill_creation_and_validation() {
        GroupSnapshot g1 = groupApiController.createGroup(aliceCreds, "Trip", "Al").getData();
        String code = g1.getGroupCode().get();
        groupApiController.joinGroup(bobCreds, code, "Bo");
        GroupAccess aliceAccess = new GroupAccess(aliceCreds, g1.getGroupId(), code);

        // Past date rejected
        Map<String, Integer> weights = new LinkedHashMap<>();
        weights.put("Al", 50);
        weights.put("Bo", 50);
        NewBill pastBill = new NewBill("Dinner", 2000, weights, LocalDate.now().minusDays(1));
        assertFalse(billApiController.createBill(aliceAccess, pastBill).isSuccess());

        // Missing member in weights rejected
        Map<String, Integer> partialWeights = new LinkedHashMap<>();
        partialWeights.put("Al", 100);
        NewBill partialBill = new NewBill("Dinner", 2000, partialWeights, null);
        assertFalse(billApiController.createBill(aliceAccess, partialBill).isSuccess());

        // Good bill created: Alice's own share is confirmed, Bo's is unconfirmed
        NewBill goodBill = new NewBill("Dinner", 2000, weights, LocalDate.now().plusDays(2));
        Result<Long> res = billApiController.createBill(aliceAccess, goodBill);
        assertTrue(res.isSuccess());

        GroupSnapshot snap = groupApiController.getGroup(aliceAccess).getData();
        Bill savedBill = snap.getBills().get(0);
        assertEquals("Dinner", savedBill.getDescription());
        assertEquals(2000L, savedBill.getTotalCents());
        assertEquals("Al", savedBill.getBillOwnerMemberName());

        BillShare alShare = savedBill.findShare("Al").get();
        assertTrue(alShare.isConfirmed());
        assertNotNull(alShare.getConfirmedAtInstant());

        BillShare boShare = savedBill.findShare("Bo").get();
        assertFalse(boShare.isConfirmed());
        assertNull(boShare.getConfirmedAtInstant());
    }
}
