/**
 * Thrown when a due date provided is invalid (e.g., in the past).
 */
public class InvalidDueDateException extends RuntimeException {
    public InvalidDueDateException(String message) {
        super(message);
    }
}