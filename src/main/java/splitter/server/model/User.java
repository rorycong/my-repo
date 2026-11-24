package splitter.server.model;

import java.util.Objects;

/**
 * Server-side representation of a registered user.
 */
public final class User {
    private final String username;
    private final String loginToken;

    public User(String username, String loginToken) {
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
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(loginToken, user.loginToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, loginToken);
    }
}
