package splitter.client.view;

import java.util.Objects;

/**
 * Confirmation dialog for destructive actions.
 */
public class ConfirmDialogView {
    private String lastTitle;
    private String lastMessage;
    private Runnable pendingConfirm;
    private boolean autoConfirmForTest = false;

    public ConfirmDialogView() {}

    public void setAutoConfirmForTest(boolean autoConfirm) {
        this.autoConfirmForTest = autoConfirm;
    }

    public void ask(String title, String message, Runnable onConfirm) {
        this.lastTitle = title;
        this.lastMessage = message;
        this.pendingConfirm = onConfirm;

        if (autoConfirmForTest && onConfirm != null) {
            onConfirm.run();
            this.pendingConfirm = null;
        }
    }

    public void userConfirms() {
        if (pendingConfirm != null) {
            Runnable r = pendingConfirm;
            pendingConfirm = null;
            r.run();
        }
    }

    public void userCancels() {
        pendingConfirm = null;
    }

    public String getLastTitle() {
        return lastTitle;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public boolean isAsking() {
        return pendingConfirm != null;
    }
}
