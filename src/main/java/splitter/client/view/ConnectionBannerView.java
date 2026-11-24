package splitter.client.view;

import splitter.client.model.ConnectionModel;

import java.util.Objects;

/**
 * Banner shown when connection to the server is lost.
 */
public class ConnectionBannerView {
    private final ConnectionModel connection;
    private boolean visible = false;
    private String message = null;

    public ConnectionBannerView(ConnectionModel connection) {
        this.connection = Objects.requireNonNull(connection, "ConnectionModel cannot be null");
        this.connection.addListener(this::refresh);
        refresh();
    }

    public void refresh() {
        if (!connection.isConnected()) {
            this.visible = true;
            this.message = connection.getErrorMessage();
        } else {
            this.visible = false;
            this.message = null;
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public String getMessage() {
        return message;
    }
}
