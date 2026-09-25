// PushbackGoalInterface.java - Behavior contract for the drum + tunnel scoring goal

/**
 * An interface representing the array-based operations for a "pushback"
 * style scoring goal made of two parts:
 *
 * <ul>
 *   <li>A DRUM: a small fixed-capacity rotating ring with a single
 *       opening. Cubes can only be fed into or ejected from the opening;
 *       rotating the ring is the only way to bring a different cube to
 *       the opening.</li>
 *   <li>A TUNNEL: a longer fixed-capacity tube with two open ends (LEFT
 *       and RIGHT). The tunnel can ONLY be loaded by transferring a cube
 *       out of the drum's opening into one of its two ends -- there is
 *       no way to insert an arbitrary value into the tunnel directly.</li>
 * </ul>
 *
 * All storage must be implemented using plain Java arrays. No
 * java.util container classes (ArrayList, LinkedList, HashMap, Deque,
 * Queue, Stack, etc.) may be used anywhere in the implementation.
 */
public interface PushbackGoalInterface<T> {

    // ------------------------------------------------------------------
    // Drum
    // ------------------------------------------------------------------

    /**
     * Initializes the drum with the given fixed capacity. Every slot
     * starts out empty (equal to emptyValue). Must be called before any
     * other drum method.
     *
     * @param capacity   the fixed number of slots in the drum
     * @param emptyValue the value representing an empty slot
     */
    void initializeDrum(int capacity, T emptyValue);

    /**
     * Places a cube into the drum's single opening.
     *
     * @param value the cube to feed in
     * @return true if the opening was empty and the cube was placed;
     *         false if the opening was already occupied (nothing happens)
     * @throws IllegalStateException if the drum is not initialized
     */
    boolean feedDrum(T value);

    /**
     * Removes and returns whatever cube is currently at the drum's
     * opening.
     *
     * @return the cube that was at the opening
     * @throws IllegalStateException if the opening is empty, or if the
     *                                drum is not initialized
     */
    T ejectDrum();

    /**
     * Rotates the entire drum ring (including empty slots) so that a
     * different physical slot arrives at the opening. Positive steps
     * rotate TOWARD the opening -- e.g. after rotateDrum(1), whatever
     * cube was one slot away from the opening is now sitting at the
     * opening. Negative steps rotate the opposite way, away from the
     * opening.
     *
     * @param steps number of slots to rotate by (may be negative)
     * @throws IllegalStateException if the drum is not initialized
     */
    void rotateDrum(int steps);

    /**
     * Returns the value at the given physical slot of the drum, without
     * altering the drum. Intended for inspection/testing.
     *
     * @param index the physical slot index (0 to drumCapacity() - 1)
     * @return the value at that slot
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException     if the drum is not initialized
     */
    T getDrumAt(int index);

    /**
     * @return the fixed capacity of the drum
     * @throws IllegalStateException if the drum is not initialized
     */
    int drumCapacity();

    /**
     * Removes every cube in the drum matching the given value, using
     * ONLY rotateDrum(1) (or repeated calls to it), feedDrum(), and
     * ejectDrum() -- no direct indexing into the drum's internal
     * storage is allowed inside this method. Non-matching cubes must
     * end up back in the drum in their original relative (cyclic)
     * order; matching cubes are simply gone (their slots become
     * empty). After this method returns, the opening should be back at
     * the same physical slot it started at.
     *
     * @param value the value to remove all instances of
     * @return the number of cubes removed
     * @throws IllegalStateException if the drum is not initialized
     */
    int removeAllOfValue(T value);

    // ------------------------------------------------------------------
    // Tunnel
    // ------------------------------------------------------------------

    /**
     * Initializes the tunnel with the given fixed capacity. Every slot
     * starts out empty (equal to emptyValue). Neither end starts
     * blocked. Must be called before any other tunnel method.
     *
     * @param capacity   the fixed number of slots in the tunnel
     * @param emptyValue the value representing an empty slot
     */
    void initializeTunnel(int capacity, T emptyValue);

    /**
     * Blocks or unblocks the given end of the tunnel (representing a
     * defensive robot sitting at that opening). While an end is
     * blocked, no cube may enter, exit, or be vacated into through that
     * end by ANY operation. A cube that is already resting at that
     * boundary slot when the end becomes blocked stays there until the
     * end is unblocked (it is not moved or evicted by this call).
     *
     * @param end     which end to block/unblock
     * @param blocked true to block, false to unblock
     * @throws IllegalStateException if the tunnel is not initialized
     */
    void setBlocked(TunnelEnd end, boolean blocked);

    /**
     * @param end which end to check
     * @return true if that end is currently blocked
     * @throws IllegalStateException if the tunnel is not initialized
     */
    boolean isBlocked(TunnelEnd end);

    /**
     * The robot arm reaches in at the top and sweeps every cube from
     * index all the way to the given end's mouth (inclusive of index).
     *
     * If sweepToward's end is NOT blocked, every cube in that range is
     * removed from the tunnel entirely (set to emptyValue). Slots
     * beyond the swept range (on the far side of index, away from
     * sweepToward) are left completely untouched.
     *
     * If sweepToward's end IS blocked, the cubes can't be vacated out
     * through it, so instead the arm just compacts them: every
     * non-empty cell within the range is pushed as close to the
     * blocked mouth as possible, preserving relative order, and any
     * empty cells end up bunched together on the far side of the
     * range (away from the mouth) instead of disappearing. No cube is
     * actually removed in this case.
     *
     * @param index       the starting index for the sweep
     * @param sweepToward which end the sweep clears (or compacts) toward
     * @return the number of cubes actually removed from the tunnel (0
     *         if the range was blocked -- in which case cubes may still
     *         have been compacted -- or if every slot in range was
     *         already empty)
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException     if the tunnel is not initialized
     */
    int sweepAt(int index, TunnelEnd sweepToward);

    /**
     * Returns the value at the given slot of the tunnel, without
     * altering it. Intended for inspection/testing.
     *
     * @param index the slot index (0 to tunnelCapacity() - 1)
     * @return the value at that slot
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException     if the tunnel is not initialized
     */
    T getTunnelAt(int index);

    /**
     * @return the fixed capacity of the tunnel
     * @throws IllegalStateException if the tunnel is not initialized
     */
    int tunnelCapacity();

    /**
     * Scans the (inclusive) slot range [startIndex, endIndex] -- the
     * tunnel's "control zone" -- and determines which single value
     * occupies a strict majority of the non-empty slots in that range.
     *
     * @param startIndex the first index of the control zone (inclusive)
     * @param endIndex   the last index of the control zone (inclusive)
     * @return the value holding a strict majority in the zone, or null
     *         if there is a tie or no single value holds a majority
     * @throws IndexOutOfBoundsException if startIndex or endIndex is out
     *                                    of range, or startIndex > endIndex
     * @throws IllegalStateException     if the tunnel is not initialized
     */
    T determineControl(int startIndex, int endIndex);

    // ------------------------------------------------------------------
    // Bridge: the only way a cube can enter the tunnel
    // ------------------------------------------------------------------

    /**
     * Transfers a single cube from the drum's opening into the tunnel
     * at the given end. This is the ONLY way a cube may enter the
     * tunnel -- there is no method to insert an arbitrary value into
     * the tunnel directly.
     *
     * Ejects the cube at the drum's opening (failing immediately,
     * returning false, if the opening is empty). Then attempts to
     * insert it into the tunnel at the given end:
     *
     * <ul>
     *   <li>Fails immediately (before touching the tunnel at all) if
     *       {@code end} itself is currently blocked.</li>
     *   <li>Otherwise, finds the nearest empty slot from that mouth and
     *       shifts the occupied chain up to it, one slot per cube in
     *       that chain -- the number of cubes shifted varies depending
     *       on how close the nearest gap is -- placing the new cube at
     *       the mouth.</li>
     *   <li>If the tunnel has no gaps at all, shifts the whole tunnel
     *       and evicts the opposite end -- unless that opposite end is
     *       blocked, in which case the insertion fails.</li>
     * </ul>
     *
     * If the tunnel insertion fails for either of the above reasons,
     * the cube that was ejected from the drum is fed back into the
     * drum's opening before returning false.
     *
     * @param end which tunnel end to load into
     * @return true if the cube was successfully moved into the tunnel;
     *         false otherwise (the cube remains in the drum in that case)
     * @throws IllegalStateException if either the drum or tunnel is not
     *                                initialized
     */
    boolean loadTunnelFromDrum(TunnelEnd end);

    /**
     * Empties the ENTIRE drum into the tunnel at the given end. Since
     * the drum only exposes a single opening, this must rotate the
     * drum between attempts to reach every physical slot: for up to
     * drumCapacity() steps, it skips (and rotates past) any slot that
     * is currently empty, and calls loadTunnelFromDrum(end) on any slot
     * that has a cube. If loadTunnelFromDrum(end) ever returns false
     * because the TUNNEL rejected the cube (as opposed to the drum
     * simply being empty at that slot), the transfer stops immediately
     * -- that cube remains at the drum's opening (loadTunnelFromDrum
     * already puts it back there), and no further rotation happens.
     *
     * @param end which tunnel end to load into
     * @return the number of cubes successfully transferred
     * @throws IllegalStateException if either the drum or tunnel is not
     *                                initialized
     */
    int dumpDrumIntoTunnel(TunnelEnd end);
}