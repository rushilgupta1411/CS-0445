/**
 * Thrown when a user, book, or library is not registered in the system.
 */
public class UnregisteredEntityException extends RuntimeException {
    public UnregisteredEntityException(String message) {
        super(message);
    }
}
