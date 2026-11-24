package splitter.client.view;

import splitter.client.controller.GroupAdminController;
import splitter.client.model.ConnectionModel;
import splitter.client.model.GroupModel;
import splitter.shared.GroupSnapshot;

import java.util.Objects;
import java.util.Optional;

/**
 * Top-level group screen view containing header controls, member list, bill list,
 * bill details, create bill form, and connection banner.
 */
public class GroupView {
    private final GroupAdminController controller;
    private final GroupModel group;
    private final ConnectionModel connection;
    private final MemberListView members;
    private final BillListView bills;
    private final BillDetailsView details;
    private final CreateBillView createBill;
    private final ConnectionBannerView banner;

    private boolean visible = false;
    private boolean codeVisible = false;

    public GroupView(GroupAdminController controller, GroupModel group, ConnectionModel connection,
                     MemberListView members, BillListView bills, BillDetailsView details,
                     CreateBillView createBill, ConnectionBannerView banner) {
        this.controller = Objects.requireNonNull(controller, "GroupAdminController cannot be null");
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.connection = Objects.requireNonNull(connection, "ConnectionModel cannot be null");
        this.members = Objects.requireNonNull(members, "MemberListView cannot be null");
        this.bills = Objects.requireNonNull(bills, "BillListView cannot be null");
        this.details = Objects.requireNonNull(details, "BillDetailsView cannot be null");
        this.createBill = Objects.requireNonNull(createBill, "CreateBillView cannot be null");
        this.banner = Objects.requireNonNull(banner, "ConnectionBannerView cannot be null");

        this.group.addListener(this::refresh);
        this.connection.addListener(this::refresh);
    }

    public void show() {
        this.visible = true;
        this.codeVisible = false;
        refresh();
    }

    public void hide() {
        this.visible = false;
        this.codeVisible = false;
    }

    public void refresh() {
        // Redraw child views if needed
    }

    public void onBackClicked() {
        controller.backToHomeRequested();
    }

    public void onLeaveClicked() {
        controller.leaveGroupRequested();
    }

    public void onRenameSubmitted(String newName) {
        if (isOwnerControlsEnabled()) {
            controller.renameGroupRequested(newName);
        }
    }

    public void onShowCodeToggled() {
        if (isOwnerControlsEnabled()) {
            this.codeVisible = !this.codeVisible;
        }
    }

    public void onGenerateCodeClicked() {
        if (isOwnerControlsEnabled()) {
            controller.generateNewCodeRequested();
        }
    }

    public void onCancelAllClicked() {
        if (isOwnerControlsEnabled()) {
            controller.cancelAllBillsRequested();
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public String getGroupName() {
        return group.getSnapshot().map(GroupSnapshot::getGroupName).orElse("");
    }

    public boolean isOwner() {
        return group.currentUserIsGroupOwner();
    }

    public boolean isOwnerControlsEnabled() {
        return isOwner() && connection.isConnected();
    }

    public boolean isCodeVisible() {
        return codeVisible;
    }

    public Optional<String> getDisplayedCode() {
        if (codeVisible && isOwner()) {
            return group.getSnapshot().flatMap(GroupSnapshot::getGroupCode);
        }
        return Optional.empty();
    }

    public MemberListView getMembers() {
        return members;
    }

    public BillListView getBills() {
        return bills;
    }

    public BillDetailsView getDetails() {
        return details;
    }

    public CreateBillView getCreateBill() {
        return createBill;
    }

    public ConnectionBannerView getBanner() {
        return banner;
    }
}
