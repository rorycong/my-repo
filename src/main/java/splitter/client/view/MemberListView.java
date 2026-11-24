package splitter.client.view;

import splitter.client.model.GroupModel;
import splitter.client.model.OutstandingModel;
import splitter.shared.Formatting;
import splitter.shared.InputValidator;
import splitter.shared.Member;
import splitter.shared.OutstandingAmounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * View showing the list of members in the open group with owner label, 'you' marker, and balances.
 */
public class MemberListView {
    private final GroupModel group;
    private final OutstandingModel outstanding;

    public static class MemberRow {
        public final String name;
        public final boolean isOwner;
        public final boolean isYou;
        public final String owesFormatted;
        public final String isOwedFormatted;

        public MemberRow(String name, boolean isOwner, boolean isYou, String owesFormatted, String isOwedFormatted) {
            this.name = name;
            this.isOwner = isOwner;
            this.isYou = isYou;
            this.owesFormatted = owesFormatted;
            this.isOwedFormatted = isOwedFormatted;
        }
    }

    private final List<MemberRow> rows = new ArrayList<>();

    public MemberListView(GroupModel group, OutstandingModel outstanding) {
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.outstanding = Objects.requireNonNull(outstanding, "OutstandingModel cannot be null");

        this.group.addListener(this::refresh);
        this.outstanding.addListener(this::refresh);
        refresh();
    }

    public void refresh() {
        rows.clear();
        String currentMemberName = group.getSnapshot().map(s -> s.getCurrentMemberName()).orElse("");
        List<Member> members = group.getCurrentMembers();
        List<OutstandingAmounts> balances = outstanding.getOutstanding();

        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            boolean isYou = InputValidator.namesEqual(m.getName(), currentMemberName);
            long owes = 0;
            long isOwed = 0;
            if (i < balances.size()) {
                owes = balances.get(i).getOwesCents();
                isOwed = balances.get(i).getIsOwedCents();
            }
            rows.add(new MemberRow(
                    m.getName(),
                    m.isOwner(),
                    isYou,
                    Formatting.formatCents(owes),
                    Formatting.formatCents(isOwed)
            ));
        }
    }

    public List<MemberRow> getRows() {
        return rows;
    }
}
