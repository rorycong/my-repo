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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SyncAndPersistenceTest {
    @TempDir
    Path tempDir;

    private Path usersDb;
    private Path groupsDb;

    @BeforeEach
    public void setUp() {
        usersDb = tempDir.resolve("users.db");
        groupsDb = tempDir.resolve("groups.db");
    }

    @Test
    public void SYN_12_concurrent_confirmations() throws Exception {
        UserRepository uRepo = new UserRepository(usersDb);
        GroupRepository gRepo = new GroupRepository(groupsDb);
        AuthGuard guard = new AuthGuard(uRepo, gRepo);
        GroupLockManager lockManager = new GroupLockManager();
        UserApiController uac = new UserApiController(uRepo, new TokenGenerator());
        GroupApiController gac = new GroupApiController(guard, gRepo, new GroupCodeGenerator(), lockManager);
        BillApiController bac = new BillApiController(guard, gRepo, new SplitCalculator(), lockManager);

        Credentials alice = uac.createUser("Alice").getData();
        Credentials bob = uac.createUser("Bob").getData();

        GroupSnapshot g = gac.createGroup(alice, "Trip", "Al").getData();
        String code = g.getGroupCode().get();
        gac.joinGroup(bob, code, "Bo");

        GroupAccess aliceAccess = new GroupAccess(alice, g.getGroupId(), code);
        Map<String, Integer> weights = Map.of("Al", 50, "Bo", 50);
        long billId = bac.createBill(aliceAccess, new NewBill("Cab", 2000, weights, null)).getData();

        int threads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger alreadyConfirmedCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    Result<Void> res = bac.confirmPayment(aliceAccess, billId, "Bo");
                    if (res.isSuccess()) {
                        successCount.incrementAndGet();
                    } else if (FailureReasons.ALREADY_CONFIRMED.equals(res.getFailureReason())) {
                        alreadyConfirmedCount.incrementAndGet();
                    }
                } catch (Exception ignored) {}
            }));
        }

        startLatch.countDown();
        for (Future<?> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }
        executor.shutdown();

        // Exactly one succeeds
        assertEquals(1, successCount.get(), "Expected exactly 1 confirmation to succeed");
        assertEquals(threads - 1, alreadyConfirmedCount.get(), "Expected all other threads to receive 'already confirmed'");
    }

    @Test
    public void PER_1_to_8_13_restart_preserves_state_and_deletes_cascade() {
        // Run 1: create users, group, members, bills
        UserRepository uRepo1 = new UserRepository(usersDb);
        GroupRepository gRepo1 = new GroupRepository(groupsDb);
        AuthGuard guard1 = new AuthGuard(uRepo1, gRepo1);
        GroupLockManager lockManager1 = new GroupLockManager();
        UserApiController uac1 = new UserApiController(uRepo1, new TokenGenerator());
        GroupApiController gac1 = new GroupApiController(guard1, gRepo1, new GroupCodeGenerator(), lockManager1);
        BillApiController bac1 = new BillApiController(guard1, gRepo1, new SplitCalculator(), lockManager1);

        Credentials alice = uac1.createUser("Alice").getData();
        Credentials bob = uac1.createUser("Bob").getData();

        GroupSnapshot g = gac1.createGroup(alice, "Europe Trip", "Al").getData();
        String code = g.getGroupCode().get();
        gac1.joinGroup(bob, code, "Bo");

        GroupAccess aliceAccess = new GroupAccess(alice, g.getGroupId(), code);
        Map<String, Integer> weights = Map.of("Al", 50, "Bo", 50);
        long bId = bac1.createBill(aliceAccess, new NewBill("Hotel", 50000, weights, null)).getData();
        bac1.confirmPayment(aliceAccess, bId, "Bo");

        // Run 2: Stop and simulate restart by creating fresh repositories pointing to same files
        UserRepository uRepo2 = new UserRepository(usersDb);
        GroupRepository gRepo2 = new GroupRepository(groupsDb);
        AuthGuard guard2 = new AuthGuard(uRepo2, gRepo2);
        GroupLockManager lockManager2 = new GroupLockManager();
        GroupApiController gac2 = new GroupApiController(guard2, gRepo2, new GroupCodeGenerator(), lockManager2);

        // State preserved
        assertTrue(uRepo2.usernameExists("Alice"));
        assertTrue(uRepo2.usernameExists("Bob"));

        GroupSnapshot reloadedGroup = gac2.getGroup(new GroupAccess(alice, g.getGroupId(), code)).getData();
        assertEquals("Europe Trip", reloadedGroup.getGroupName());
        assertEquals(2, reloadedGroup.getMembers().size());
        assertEquals(1, reloadedGroup.getBills().size());
        Bill reloadedBill = reloadedGroup.getBills().get(0);
        assertEquals(50000L, reloadedBill.getTotalCents());
        assertTrue(reloadedBill.findShare("Bo").get().isConfirmed());

        // Deleting group deletes all members and bills
        gRepo2.deleteGroup(g.getGroupId());
        assertTrue(gRepo2.loadGroup(g.getGroupId()).isEmpty());
    }

    @Test
    public void GEN_10_performance_under_load() {
        UserRepository uRepo = new UserRepository(usersDb);
        GroupRepository gRepo = new GroupRepository(groupsDb);
        AuthGuard guard = new AuthGuard(uRepo, gRepo);
        GroupLockManager lockManager = new GroupLockManager();
        UserApiController uac = new UserApiController(uRepo, new TokenGenerator());
        GroupApiController gac = new GroupApiController(guard, gRepo, new GroupCodeGenerator(), lockManager);
        BillApiController bac = new BillApiController(guard, gRepo, new SplitCalculator(), lockManager);

        Credentials owner = uac.createUser("User0").getData();
        GroupSnapshot g = gac.createGroup(owner, "Large Group", "Member0").getData();
        String code = g.getGroupCode().get();

        List<String> memberNames = new ArrayList<>();
        memberNames.add("Member0");
        for (int i = 1; i < 20; i++) {
            Credentials mCreds = uac.createUser("User" + i).getData();
            gac.joinGroup(mCreds, code, "Member" + i);
            memberNames.add("Member" + i);
        }

        GroupAccess access = new GroupAccess(owner, g.getGroupId(), code);
        Map<String, Integer> equalWeights = InputValidator.equalWeights(memberNames);

        long start = System.currentTimeMillis();
        // Create 100 bills quickly
        for (int i = 0; i < 100; i++) {
            bac.createBill(access, new NewBill("Expense " + i, 10000, equalWeights, null));
        }
        long duration = System.currentTimeMillis() - start;

        // Verify retrieval performance under normal conditions (GEN-10: under 1 second)
        long readStart = System.currentTimeMillis();
        Result<GroupSnapshot> readResult = gac.getGroup(access);
        long readDuration = System.currentTimeMillis() - readStart;

        assertTrue(readResult.isSuccess());
        assertEquals(100, readResult.getData().getBills().size());
        assertTrue(readDuration < 1000, "Read took longer than 1s: " + readDuration + "ms");
    }
}
