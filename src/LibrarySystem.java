import java.time.LocalDate;

/**
 * An abstract data type representing a library management system for books,
 * users, and library branches. The interface provides methods for borrowing,
 * returning, reserving books, and more.
 */
public interface LibrarySystem {

    /**
     * Attempts to borrow a book for a user from a specific library.
     * The book must be available (not already borrowed or reserved),
     * and the user must be eligible to borrow (i.e., has no overdue books and is
     * within the borrowing limit).
     *
     * @param user    the user attempting to borrow the book
     * @param book    the book to be borrowed
     * @param library the library where the book is located
     * @param dueDate the expected due date for returning the book
     *
     * @throws UnregisteredEntityException if the user, book, or library is not
     *                                     known to the system
     * @throws InvalidDueDateException     if the provided due date is in the past
     * @throws BookUnavailableException    if the book is already borrowed or
     *                                     reserved
     * @throws UserNotEligibleException    if the user has overdue books or has
     *                                     reached the borrowing limit
     * @throws NullPointerException        if any parameter is null
     */
    void borrowBook(User user, Book book, Library library, LocalDate dueDate);

    void returnBook();

    void registerUser();

    void reserveBook();

    void cancelReservation();

    void reportLostBook();

    void computeFine();

    void transferBookBetweenLibraries();

    void suggestBook();
}
