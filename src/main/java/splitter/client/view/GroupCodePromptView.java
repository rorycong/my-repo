package splitter.client.view;

import splitter.client.controller.HomeController;

import java.util.Objects;

/**
 * View prompt for entering a group code when opening a group without a saved code.
 */
public class GroupCodePromptView {
    private final HomeController controller;
    private boolean visible = false;
    private long currentGroupId = 0;
    private String currentGroupName = "";

    public GroupCodePromptView(HomeController controller) {
        this.controller = Objects.requireNonNull(controller, "HomeController cannot be null");
    }

    public void show(long groupId, String groupName) {
        this.currentGroupId = groupId;
        this.currentGroupName = groupName != null ? groupName : "";
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
        this.currentGroupId = 0;
        this.currentGroupName = "";
    }

    public void onSubmit(long groupId, String groupCode) {
        hide();
        controller.groupCodeEntered(groupId, groupCode);
    }

    public boolean isVisible() {
        return visible;
    }

    public long getCurrentGroupId() {
        return currentGroupId;
    }

    public String getCurrentGroupName() {
        return currentGroupName;
    }
}
