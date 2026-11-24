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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MemberRequirementsTest {
    @TempDir
    Path tempDir;

    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private AuthGuard authGuard;
    private GroupCodeGenerator codeGenerator;
    private GroupLockManager lockManager;
    private GroupApiController groupApiController;
    private BillApiController billApiController;
    private UserApiController userApiController;

    private Credentials aliceCreds;
    private Credentials bobCreds;
    private Credentials charlieCreds;

    @BeforeEach
    public void setUp() {
        userRepository = new UserRepository(tempDir.resolve("users.db"));
        groupRepository = new GroupRepository(tempDir.resolve("groups.db"));
        authGuard = new AuthGuard(userRepository, groupRepository);
        codeGenerator = new GroupCodeGenerator();
        lockManager = new GroupLockManager();
        groupApiController = new GroupApiController(authGuard, groupRepository, codeGenerator, lockManager);
        billApiController = new BillApiController(authGuard, groupRepository, new SplitCalculator(), lockManager);
        userApiController = new UserApiController(userRepository, new TokenGenerator());

        aliceCreds = userApiController.createUser("Alice").getData();
        bobCreds = userApiController.createUser("Bob").getData();
        charlieCreds = userApiController.createUser("Charlie").getData();
    }

    @Test
    public void MEM_5_to_15_joining_and_names() {
        GroupSnapshot g1 = groupApiController.createGroup(aliceCreds, "Trip", "Al").getData();
        String code = g1.getGroupCode().get();

        // Unknown code fails
        Result<GroupSnapshot> unk = groupApiController.joinGroup(bobCreds, "ZZZZZZ", "Bo");
        assertFalse(unk.isSuccess());
        assertEquals(FailureReasons.GROUP_NOT_FOUND, unk.getFailureReason());

        // Blank name fails
        assertFalse(groupApiController.joinGroup(bobCreds, code, "").isSuccess());
        assertFalse(groupApiController.joinGroup(bobCreds, code, "   ").isSuccess());

        // Duplicate name in group fails
        assertFalse(groupApiController.joinGroup(bobCreds, code, "Al").isSuccess());
        assertFalse(groupApiController.joinGroup(bobCreds, code, "al").isSuccess());

        // Good join, name trimmed
        Result<GroupSnapshot> bobJoin = groupApiController.joinGroup(bobCreds, code, "  Bo  ");
        assertTrue(bobJoin.isSuccess());
        assertEquals("Bo", bobJoin.getData().getCurrentMemberName());

        // Join again fails with already a member
        Result<GroupSnapshot> bobJoinAgain = groupApiController.joinGroup(bobCreds, code, "Bobby");
        assertFalse(bobJoinAgain.isSuccess());
        assertEquals(FailureReasons.ALREADY_A_MEMBER, bobJoinAgain.getFailureReason());

        // Same user in another group with different name succeeds
        GroupSnapshot g2 = groupApiController.createGroup(charlieCreds, "Party", "Chuck").getData();
        String code2 = g2.getGroupCode().get();
        Result<GroupSnapshot> bobJoinG2 = groupApiController.joinGroup(bobCreds, code2, "Robert");
        assertTrue(bobJoinG2.isSuccess());
        assertEquals("Robert", bobJoinG2.getData().getCurrentMemberName());

        // Order preserved: new member added last
        GroupAccess g1Access = new GroupAccess(aliceCreds, g1.getGroupId(), code);
        groupApiController.joinGroup(charlieCreds, code, "Charlie");
        GroupSnapshot latest = groupApiController.getGroup(g1Access).getData();
        List<Member> members = latest.getMembers();
        assertEquals(3, members.size());
        assertEquals("Al", members.get(0).getName());
        assertEquals("Bo", members.get(1).getName());
        assertEquals("Charlie", members.get(2).getName());
    }

    @Test
    public void MEM_20_to_31_leaving_and_last_member_deletes_group() {
        GroupSnapshot g1 = groupApiController.createGroup(aliceCreds, "Trip", "Al").getData();
        String code = g1.getGroupCode().get();
        groupApiController.joinGroup(bobCreds, code, "Bo");

        GroupAccess aliceAccess = new GroupAccess(aliceCreds, g1.getGroupId(), code);
        GroupAccess bobAccess = new GroupAccess(bobCreds, g1.getGroupId(), code);

        // Create a bill with Bo
        Map<String, Integer> weights = new LinkedHashMap<>();
        weights.put("Al", 50);
        weights.put("Bo", 50);
        NewBill bill = new NewBill("Pizza", 1000, weights, null);
        Result<Long> billRes = billApiController.createBill(aliceAccess, bill);
        assertTrue(billRes.isSuccess());
        long billId = billRes.getData();

        // Bo leaves group
        assertTrue(groupApiController.leaveGroup(bobAccess).isSuccess());

        // Bo access now rejected
        Result<GroupSnapshot> boAccessAfter = groupApiController.getGroup(bobAccess);
        assertFalse(boAccessAfter.isSuccess());
        assertEquals(FailureReasons.NOT_A_MEMBER, boAccessAfter.getFailureReason());

        // Bills still intact and unconfirmed share still confirmable by Alice
        GroupSnapshot snapAfterLeave = groupApiController.getGroup(aliceAccess).getData();
        assertEquals(1, snapAfterLeave.getBills().size());
        assertTrue(billApiController.confirmPayment(aliceAccess, billId, "Bo").isSuccess());

        // Alice (last member) leaves: group and all bills are completely deleted
        assertTrue(groupApiController.leaveGroup(aliceAccess).isSuccess());
        Result<GroupSnapshot> deletedGet = groupApiController.getGroup(aliceAccess);
        assertFalse(deletedGet.isSuccess());
        assertEquals(FailureReasons.GROUP_NOT_FOUND, deletedGet.getFailureReason());
    }
}
