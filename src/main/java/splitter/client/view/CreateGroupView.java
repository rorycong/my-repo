package splitter.client.view;

import splitter.client.controller.HomeController;

import java.util.Objects;

/**
 * View dialog for creating a new group.
 */
public class CreateGroupView {
    private final HomeController controller;
    private boolean visible = false;

    public CreateGroupView(HomeController controller) {
        this.controller = Objects.requireNonNull(controller, "HomeController cannot be null");
    }

    public void show() {
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public void onSubmit(String groupName, String memberName) {
        hide();
        controller.createGroupRequested(groupName, memberName);
    }

    public boolean isVisible() {
        return visible;
    }
}
