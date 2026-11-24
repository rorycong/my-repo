package splitter;

import org.junit.jupiter.api.Test;
import splitter.client.model.GroupModel;
import splitter.client.model.OutstandingModel;
import splitter.shared.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OutstandingAmountsTest {

    @Test
    public void OUT_3_to_6_outstanding_calculation_properties() {
        GroupModel groupModel = new GroupModel();
        OutstandingModel outstandingModel = new OutstandingModel(groupModel);

        Instant now = Instant.now();
        List<Member> members = new ArrayList<>();
        members.add(new Member("Alice", true, false, now));
        members.add(new Member("Bob", false, false, now));
        members.add(new Member("Charlie", false, false, now));

        // Bill 1: Alice owns $30.00.
        // Alice weight 34, share $10.00 (confirmed).
        // Bob weight 33, share $10.00 (unconfirmed).
        // Charlie weight 33, share $10.00 (unconfirmed).
        List<BillShare> b1Shares = List.of(
                new BillShare("Alice", 34, 1000, true, now),
                new BillShare("Bob", 33, 1000, false, null),
                new BillShare("Charlie", 33, 1000, false, null)
        );
        Bill b1 = new Bill(1, "Dinner", 3000, "Alice", now, null, b1Shares);

        // Bill 2: Bob owns $15.00.
        // Alice weight 50, share $7.50 (unconfirmed).
        // Bob weight 50, share $7.50 (confirmed).
        // Charlie weight 0, share $0.00 (confirmed).
        List<BillShare> b2Shares = List.of(
                new BillShare("Alice", 50, 750, false, null),
                new BillShare("Bob", 50, 750, true, now),
                new BillShare("Charlie", 0, 0, true, now)
        );
        Bill b2 = new Bill(2, "Taxi", 1500, "Bob", now, null, b2Shares);

        GroupSnapshot snapshot = new GroupSnapshot(1, "Trip", "Alice", true, "CODE1", members, List.of(b1, b2));
        groupModel.replaceSnapshot(snapshot);

        List<OutstandingAmounts> balances = outstandingModel.getOutstanding();
        assertEquals(3, balances.size());

        // Alice:
        // Owes: from b2 ($7.50) = 750 cents
        // Is owed: from b1 (Bob owes 1000, Charlie owes 1000) = 2000 cents (Alice's own share 1000 is never counted!)
        OutstandingAmounts aliceBal = balances.get(0);
        assertEquals("Alice", aliceBal.getMemberName());
        assertEquals(750, aliceBal.getOwesCents());
        assertEquals(2000, aliceBal.getIsOwedCents());

        // Bob:
        // Owes: from b1 ($10.00) = 1000 cents
        // Is owed: from b2 (Alice owes 750) = 750 cents (Bob's own share 750 is never counted!)
        OutstandingAmounts bobBal = balances.get(1);
        assertEquals("Bob", bobBal.getMemberName());
        assertEquals(1000, bobBal.getOwesCents());
        assertEquals(750, bobBal.getIsOwedCents());

        // Charlie:
        // Owes: from b1 ($10.00) = 1000 cents
        // Is owed: 0 cents
        OutstandingAmounts charlieBal = balances.get(2);
        assertEquals("Charlie", charlieBal.getMemberName());
        assertEquals(1000, charlieBal.getOwesCents());
        assertEquals(0, charlieBal.getIsOwedCents());

        // Property check: Total owes = Total is owed across all current members
        long totalOwes = balances.stream().mapToLong(OutstandingAmounts::getOwesCents).sum();
        long totalIsOwed = balances.stream().mapToLong(OutstandingAmounts::getIsOwedCents).sum();
        assertEquals(2750, totalOwes);
        assertEquals(2750, totalIsOwed);
        assertEquals(totalOwes, totalIsOwed);
    }
}
