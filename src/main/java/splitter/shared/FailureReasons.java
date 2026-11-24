package splitter.shared;

/**
 * Standard failure reason constants recognized by client and server.
 */
public final class FailureReasons {
    public static final String CODE_OUTDATED = "code outdated";
    public static final String GROUP_NOT_FOUND = "group not found";
    public static final String NOT_A_MEMBER = "not a member";
    public static final String BILL_NOT_FOUND = "bill not found";
    public static final String USERNAME_TAKEN = "username already taken";
    public static final String ALREADY_A_MEMBER = "already a member";
    public static final String ALREADY_CONFIRMED = "already confirmed";
    public static final String INVALID_CREDENTIALS = "invalid credentials";
    public static final String PERMISSION_DENIED = "permission denied";
    public static final String INVALID_INPUT = "invalid input";
    public static final String SERVER_UNREACHABLE = "server unreachable";

    private FailureReasons() {}
}
