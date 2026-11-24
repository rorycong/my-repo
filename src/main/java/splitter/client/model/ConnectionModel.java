package splitter.client.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tracks connectivity to the server and latest error message. Starts as connected.
 */
public class ConnectionModel {
    private boolean connected = true;
    private String errorMessage = null;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public ConnectionModel() {}

    public void addListener(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (Runnable r : listeners) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }

    public void markConnected() {
        boolean changed = !connected || errorMessage != null;
        this.connected = true;
        this.errorMessage = null;
        if (changed) {
            notifyListeners();
        }
    }

    public void markDisconnected(String message) {
        boolean changed = connected || !java.util.Objects.equals(errorMessage, message);
        this.connected = false;
        this.errorMessage = message != null ? message : "Server unreachable";
        if (changed) {
            notifyListeners();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
