/**
 * Thrown when a user is not eligible to borrow a book
 * (e.g., has overdue items or has reached borrowing limit).
 */
public class UserNotEligibleException extends RuntimeException {
    public UserNotEligibleException(String message) {
        super(message);
    }
}
