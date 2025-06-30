/**
 * Thrown when a book is currently unavailable for borrowing
 * (e.g., already borrowed or reserved).
 */
public class BookUnavailableException extends RuntimeException {
    public BookUnavailableException(String message) {
        super(message);
    }
}