// TableCapacityException.java - PROVIDED. Do not modify.

/**
 * Thrown by put() when the new entry cannot be placed: the kick chain reached its limit and
 * the stash is full. The table is left exactly as it was before the put (the entry that could
 * not be inserted is simply not inserted).
 */
public class TableCapacityException extends RuntimeException {
    public TableCapacityException(String message) {
        super(message);
    }
}
