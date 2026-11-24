package splitter.shared;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of an operation: success flag, optional data payload, and failure reason on failure.
 *
 * @param <T> payload type
 */
public final class Result<T> {
    private final boolean success;
    private final String failureReason;
    private final T data;

    private Result(boolean success, String failureReason, T data) {
        this.success = success;
        this.failureReason = failureReason;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(true, null, data);
    }

    public static <T> Result<T> ok() {
        return new Result<>(true, null, null);
    }

    public static <T> Result<T> fail(String reason) {
        Objects.requireNonNull(reason, "Failure reason cannot be null");
        return new Result<>(false, reason, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public T getData() {
        return data;
    }

    public Optional<T> getOptionalData() {
        return Optional.ofNullable(data);
    }

    @Override
    public String toString() {
        if (success) {
            return "Result.ok(" + data + ")";
        } else {
            return "Result.fail(" + failureReason + ")";
        }
    }
}
