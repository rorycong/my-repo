package splitter.shared;

import java.util.Objects;

/**
 * Access descriptor for group operations: credentials, group id, and supplied group code.
 */
public final class GroupAccess {
    private final Credentials credentials;
    private final long groupId;
    private final String groupCode;

    public GroupAccess(Credentials credentials, long groupId, String groupCode) {
        this.credentials = Objects.requireNonNull(credentials, "credentials cannot be null");
        this.groupId = groupId;
        this.groupCode = groupCode != null ? groupCode.trim() : "";
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getGroupCode() {
        return groupCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupAccess)) return false;
        GroupAccess that = (GroupAccess) o;
        return groupId == that.groupId &&
                Objects.equals(credentials, that.credentials) &&
                Objects.equals(groupCode, that.groupCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentials, groupId, groupCode);
    }
}
