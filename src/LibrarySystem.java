import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Contract for a library service with fixed, pre-populated users, book copies,
 * and branches. Each Book identifies one physical copy; distinct copies of a
 * title are distinct books. Entity identity is the system's registered identity,
 * not a caller-created object with similar fields.
 *
 * <p>Registration here means enrolling a known user at a known branch, not
 * creating users. A book has exactly one owning branch. A reservation is an
 * ordered, per-copy FIFO queue; at most one active loan exists per copy.
 * Only the first waiting user may borrow a reserved copy. A return makes the
 * copy available to that user, without automatically creating a new loan.
 *
 * <p>All date comparisons use the service's current local calendar date
 * (its configured time zone), evaluated once per call. Due dates are inclusive:
 * a book becomes overdue the following day. Borrowing limits and the nonnegative
 * daily fine rate are fixed branch policies and do not change during a call.
 * Money is in the service's single currency, with two decimal places.
 *
 * <p>Null arguments are programming errors and cause NullPointerException.
 * Unknown entities cause UnregisteredEntityException. Rule violations use
 * exceptions; normal absence of a recommendation uses Optional.empty(), never
 * null. Validation occurs before mutation, so any rejected operation leaves
 * loans, queues, membership, ownership and charges unchanged. Queries have no
 * side effects. Concurrent operations must behave as if executed serially.
 */
public interface LibrarySystem {

    /**
     * Creates a loan of an available copy at its owning branch. The user must be
     * enrolled at that branch, have no overdue loans anywhere in the system,
     * and have fewer active loans than that branch's borrowing limit.
     * A queue permits borrowing only by its first user; a successful loan removes
     * that user's reservation. Other reservations keep their relative order.
     *
     * @param user the registered borrower
     * @param book the registered physical copy to borrow
     * @param library the registered branch owning the copy
     * @param dueDate the inclusive return deadline, strictly after today
     * @throws NullPointerException if any argument is null
     * @throws UnregisteredEntityException if an entity is unknown or the user
     *         is not enrolled at the branch
     * @throws InvalidDueDateException if dueDate is today or in the past
     * @throws BookUnavailableException if the copy belongs to another branch,
     *         is lost, is already on loan (including to this user), or its
     *         reservation queue is headed by another user
     * @throws UserNotEligibleException if the user has overdue loans or has
     *         reached the borrowing limit
     */
    void borrowBook(User user, Book book, Library library, LocalDate dueDate);

    /**
     * Closes the user's active loan at the owning branch and records today's
     * return date. Cross-branch returns are not supported. Records the overdue
     * charge under the original loan's branch policy, if any; payment is outside
     * this interface. Remaining reservations are retained in FIFO order.
     * Being overdue or at the borrowing limit never prevents a valid return.
     *
     * @param user the registered current borrower
     * @param book the registered copy being returned
     * @param library the registered branch receiving and owning the copy
     * @throws NullPointerException if any argument is null
     * @throws UnregisteredEntityException if any entity is unknown
     * @throws IllegalArgumentException if library is not the owning branch
     * @throws IllegalStateException if the copy has no active loan to user,
     *         was already returned, or was reported lost
     */
    void returnBook(User user, Book book, Library library);

    /**
     * Enrolls an existing system user at an existing branch. Does not create
     * or modify either entity or remove other branch memberships. Existing
     * membership is an idempotent no-op, rather than an error.
     *
     * @param user the known user to enroll
     * @param library the known branch to join
     * @return true if a new membership was added; false if already enrolled
     * @throws NullPointerException if either argument is null
     * @throws UnregisteredEntityException if either entity is unknown
     */
    boolean registerUser(User user, Library library);

    /**
     * Appends the user to a copy's reservation queue and returns their one-based
     * position at the instant of insertion. A copy may be reserved if it is on
     * loan or already has a queue; an available unreserved copy must instead be
     * borrowed. The user must be enrolled at the owning branch and currently
     * eligible to borrow. Eligibility is checked again when borrowing later.
     *
     * @param user the registered user requesting the reservation
     * @param book the registered physical copy to reserve
     * @param library the registered branch owning the copy
     * @return the positive position in the reservation queue
     * @throws NullPointerException if any argument is null
     * @throws UnregisteredEntityException if an entity is unknown or the user
     *         is not enrolled at the branch
     * @throws BookUnavailableException if the book is lost or belongs to another
     *         branch
     * @throws IllegalStateException if user already has a reservation or the
     *         active loan for this copy, or the unreserved copy is available now
     * @throws UserNotEligibleException if the user has overdue loans or has
     *         reached the branch borrowing limit
     */
    int reserveBook(User user, Book book, Library library);

    /**
     * Cancels only this user's reservation and closes its queue gap without
     * changing any other user's relative order. No reservation is normal
     * absence: return false. Cancelling is allowed even if the user is overdue
     * or no longer enrolled; known users may always cancel their own requests.
     *
     * @param user the registered reservation holder
     * @param book the registered reserved copy
     * @param library the registered owning branch
     * @return true if a reservation was removed; false if none existed
     * @throws NullPointerException if any argument is null
     * @throws UnregisteredEntityException if any entity is unknown
     * @throws IllegalArgumentException if library does not own book
     */
    boolean cancelReservation(User user, Book book, Library library);

    /**
     * Reports a copy lost by its current borrower. Closes the active loan on
     * today's date, records its overdue fine (if any), marks the copy lost and
     * unavailable, and cancels all reservations for that copy. Ownership remains
     * unchanged. Any replacement charge is outside this API; it is not included
     * in the returned overdue fine. A repeated report is rejected, so no charge
     * can be recorded twice. Borrowing ineligibility does not prevent reporting.
     *
     * @param user the registered current borrower
     * @param book the registered lost copy
     * @param library the registered branch owning the copy
     * @return the nonnegative overdue fine recorded for this loan
     * @throws NullPointerException if any argument is null
     * @throws UnregisteredEntityException if any entity is unknown
     * @throws IllegalArgumentException if library does not own book
     * @throws IllegalStateException if book has no active loan to user or is
     *         already reported lost
     */
    BigDecimal reportLostBook(User user, Book book, Library library);

    /**
     * Quotes the overdue fine on an active loan without recording a charge.
     * The result is the number of calendar days strictly after the inclusive
     * due date through asOfDate, times the daily rate fixed at checkout.
     * Dates on or before the deadline produce zero. Uses exact decimal money
     * with scale two; no floating point, interest or replacement fee is involved.
     *
     * @param user the registered current borrower
     * @param book the registered loaned copy
     * @param asOfDate the quote date, on or after checkout and no later than today
     * @return the nonnegative fine, with scale two
     * @throws NullPointerException if any argument is null
     * @throws UnregisteredEntityException if user or book is unknown
     * @throws IllegalStateException if book has no active loan to user, including
     *         a returned or lost copy
     * @throws IllegalArgumentException if asOfDate precedes checkout or is in
     *         the future
     */
    BigDecimal computeFine(User user, Book book, LocalDate asOfDate);

    /**
     * Atomically changes an available copy's owner from source to destination.
     * The copy must not be on loan, lost, or reserved. Transfers neither create a
     * new copy nor change users' memberships. Equal source and destination is an
     * argument error, rather than a transfer or silent success.
     *
     * @param book the registered physical copy to transfer
     * @param source the registered current owning branch
     * @param destination the registered new owning branch
     * @throws NullPointerException if any argument is null
     * @throws UnregisteredEntityException if any entity is unknown
     * @throws IllegalArgumentException if source does not own book or source
     *         and destination identify the same branch
     * @throws BookUnavailableException if book is borrowed, reserved, or lost
     */
    void transferBookBetweenLibraries(Book book, Library source, Library destination);

    /**
     * Recommends an immediately borrowable copy at the requested branch.
     * Recommendations are advisory: no copy is borrowed or reserved and a
     * concurrent checkout may change availability before borrowBook is called.
     * The user must be enrolled and eligible as for borrowing. Excludes lost,
     * loaned and reserved copies. If multiple copies qualify the service may
     * choose any one, without a promised ranking. An empty catalog or no suitable
     * copy is expected absence, represented by Optional.empty(), not an error.
     *
     * @param user the registered user seeking a recommendation
     * @param library the registered branch to search
     * @return a non-null Optional containing an eligible copy, or empty if none
     * @throws NullPointerException if either argument is null
     * @throws UnregisteredEntityException if either entity is unknown or the user
     *         is not enrolled at the branch
     * @throws UserNotEligibleException if the user has overdue loans or has
     *         reached the branch borrowing limit
     */
    Optional<Book> suggestBook(User user, Library library);
}
