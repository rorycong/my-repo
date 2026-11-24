package splitter.client.view;

/**
 * View showing error and notice messages.
 */
public class ErrorMessageView {
    private String lastMessage = null;
    private boolean visible = false;

    public ErrorMessageView() {}

    public void show(String message) {
        this.lastMessage = message;
        this.visible = true;
    }

    public void dismiss() {
        this.visible = false;
        this.lastMessage = null;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public boolean isVisible() {
        return visible;
    }
}
