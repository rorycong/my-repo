package splitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import splitter.server.controller.AuthGuard;
import splitter.server.controller.GroupApiController;
import splitter.server.controller.GroupLockManager;
import splitter.server.controller.UserApiController;
import splitter.server.model.GroupCodeGenerator;
import splitter.server.model.GroupRepository;
import splitter.server.model.TokenGenerator;
import splitter.server.model.UserRepository;
import splitter.shared.*;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GroupAndOwnerRequirementsTest {
    @TempDir
    Path tempDir;

    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private AuthGuard authGuard;
    private GroupCodeGenerator codeGenerator;
    private GroupLockManager lockManager;
    private GroupApiController groupApiController;
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
        userApiController = new UserApiController(userRepository, new TokenGenerator());

        aliceCreds = userApiController.createUser("Alice").getData();
        bobCreds = userApiController.createUser("Bob").getData();
        charlieCreds = userApiController.createUser("Charlie").getData();
    }

    @Test
    public void GRP_6_8_group_names_validation() {
        // Blank rejected
        assertFalse(groupApiController.createGroup(aliceCreds, "", "Al").isSuccess());
        assertFalse(groupApiController.createGroup(aliceCreds, "   ", "Al").isSuccess());

        // Trimmed
        Result<GroupSnapshot> g1 = groupApiController.createGroup(aliceCreds, "  Trip  ", "Al");
        assertTrue(g1.isSuccess());
        assertEquals("Trip", g1.getData().getGroupName());

        // Two same name groups allowed
        Result<GroupSnapshot> g2 = groupApiController.createGroup(aliceCreds, "Trip", "Al");
        assertTrue(g2.isSuccess());
        assertNotEquals(g1.getData().getGroupId(), g2.getData().getGroupId());
    }

    @Test
    public void GRP_9_12_code_generator_properties() {
        Set<Character> allowed = new HashSet<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            if (c != 'I' && c != 'O') allowed.add(c);
        }
        for (char c = '2'; c <= '9'; c++) {
            allowed.add(c);
        }

        for (int i = 0; i < 1000; i++) {
            String code = codeGenerator.generateCode();
            assertEquals(6, code.length());
            for (char c : code.toCharArray()) {
                assertTrue(allowed.contains(c), "Character not allowed: " + c);
            }
        }
    }

    @Test
    public void GRP_14_18_20_access_and_code_visibility() {
        GroupSnapshot created = groupApiController.createGroup(aliceCreds, "Trip", "Al").getData();
        long gid = created.getGroupId();
        String code = created.getGroupCode().get();

        // Creator is owner and receives code
        assertTrue(created.isOwner());
        assertNotNull(created.getGroupCodeValue());

        // Bob joins with lowercase code
        Result<GroupSnapshot> bobJoin = groupApiController.joinGroup(bobCreds, code.toLowerCase(), "Bo");
        assertTrue(bobJoin.isSuccess());
        // Non-owner does NOT get code in snapshot
        assertFalse(bobJoin.getData().isOwner());
        assertTrue(bobJoin.getData().getGroupCode().isEmpty());

        // Bob opens group with right code
        GroupAccess bobAccess = new GroupAccess(bobCreds, gid, code.toLowerCase());
        Result<GroupSnapshot> bobGet = groupApiController.getGroup(bobAccess);
        assertTrue(bobGet.isSuccess());
        assertTrue(bobGet.getData().getGroupCode().isEmpty());

        // Charlie (non-member) tries to open
        GroupAccess charlieAccess = new GroupAccess(charlieCreds, gid, code);
        Result<GroupSnapshot> charlieGet = groupApiController.getGroup(charlieAccess);
        assertFalse(charlieGet.isSuccess());
        assertEquals(FailureReasons.NOT_A_MEMBER, charlieGet.getFailureReason());

        // Member tries with wrong code
        GroupAccess wrongCodeAccess = new GroupAccess(bobCreds, gid, "WRONG9");
        Result<GroupSnapshot> wrongCodeGet = groupApiController.getGroup(wrongCodeAccess);
        assertFalse(wrongCodeGet.isSuccess());
        assertEquals(FailureReasons.CODE_OUTDATED, wrongCodeGet.getFailureReason());
    }

    @Test
    public void OWN_4_to_18_owner_controls_and_permissions() {
        GroupSnapshot created = groupApiController.createGroup(aliceCreds, "Trip", "Al").getData();
        long gid = created.getGroupId();
        String code = created.getGroupCode().get();
        groupApiController.joinGroup(bobCreds, code, "Bo");

        GroupAccess aliceAccess = new GroupAccess(aliceCreds, gid, code);
        GroupAccess bobAccess = new GroupAccess(bobCreds, gid, code);

        // Rename: Member rejected, Owner succeeds
        assertFalse(groupApiController.renameGroup(bobAccess, "New Trip").isSuccess());
        assertTrue(groupApiController.renameGroup(aliceAccess, "Trip 2").isSuccess());

        // Generate new code: Member rejected, Owner succeeds
        assertFalse(groupApiController.generateNewCode(bobAccess).isSuccess());
        Result<String> newCodeRes = groupApiController.generateNewCode(aliceAccess);
        assertTrue(newCodeRes.isSuccess());
        String newCode = newCodeRes.getData();
        assertNotEquals(code, newCode);

        // Old code now fails
        assertFalse(groupApiController.getGroup(aliceAccess).isSuccess());
        assertEquals(FailureReasons.CODE_OUTDATED, groupApiController.getGroup(aliceAccess).getFailureReason());

        // New code succeeds
        GroupAccess aliceNewAccess = new GroupAccess(aliceCreds, gid, newCode);
        assertTrue(groupApiController.getGroup(aliceNewAccess).isSuccess());
    }

    @Test
    public void OWN_1_19_20_owner_leaves_promotes_earliest_remaining_member() {
        GroupSnapshot created = groupApiController.createGroup(aliceCreds, "Trip", "Al").getData();
        long gid = created.getGroupId();
        String code = created.getGroupCode().get();

        groupApiController.joinGroup(bobCreds, code, "Bo");
        groupApiController.joinGroup(charlieCreds, code, "Chuck");

        GroupAccess aliceAccess = new GroupAccess(aliceCreds, gid, code);
        assertTrue(groupApiController.leaveGroup(aliceAccess).isSuccess());

        // Bob opens group: Bob is now the owner
        GroupAccess bobAccess = new GroupAccess(bobCreds, gid, code);
        GroupSnapshot bobSnap = groupApiController.getGroup(bobAccess).getData();
        assertTrue(bobSnap.isOwner());
        assertEquals("Bo", bobSnap.getCurrentMemberName());
        assertTrue(bobSnap.getGroupCode().isPresent());

        // Bob now leaves group of 2: Charlie becomes owner
        assertTrue(groupApiController.leaveGroup(bobAccess).isSuccess());
        GroupAccess charlieAccess = new GroupAccess(charlieCreds, gid, code);
        GroupSnapshot charlieSnap = groupApiController.getGroup(charlieAccess).getData();
        assertTrue(charlieSnap.isOwner());
        assertEquals("Chuck", charlieSnap.getCurrentMemberName());
    }
}
