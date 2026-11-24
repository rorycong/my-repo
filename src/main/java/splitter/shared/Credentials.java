package splitter.shared;

import java.util.Objects;

/**
 * User credentials containing username and secret login token.
 */
public final class Credentials {
    private final String username;
    private final String loginToken;

    public Credentials(String username, String loginToken) {
        this.username = Objects.requireNonNull(username, "username cannot be null");
        this.loginToken = Objects.requireNonNull(loginToken, "loginToken cannot be null");
    }

    public String getUsername() {
        return username;
    }

    public String getLoginToken() {
        return loginToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Credentials)) return false;
        Credentials that = (Credentials) o;
        return Objects.equals(username, that.username) && Objects.equals(loginToken, that.loginToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, loginToken);
    }

    @Override
    public String toString() {
        return "Credentials{" + "username='" + username + '\'' + '}';
    }
}
