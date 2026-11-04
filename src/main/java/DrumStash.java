// DrumStash.java - PROVIDED. Do not modify.

/**
 * The overflow stash: a small rotating DRUM with a single opening. You can
 * only touch the entry sitting at the opening (peekOpening, feed, eject); to
 * reach another entry you rotate the ring. That is the whole point of a drum:
 * any code that works on the stash must do so one slot at a time through the
 * opening.
 *
 * Logical slot 0 is the opening. rotate(1) brings logical slot 1 to the
 * opening, rotate(-1) brings the previous slot there. getAt(i) reports logical
 * slots, so it changes when the drum rotates. getAt is for INSPECTION ONLY
 * (tests and visualizers); the table's own logic must not use it.
 *
 * Besides the low-level drum operations (feed / eject / rotate), this class
 * provides three high-level operations built from them. Their exact rotation
 * behaviour is specified, because tests check the drum's layout:
 * <ul>
 *   <li>insert(e):  rotate(1) until the opening is empty, then feed(e).
 *                   The drum is left wherever that stops. Returns false
 *                   (and does not rotate) if the drum is full.</li>
 *   <li>find(key):  examines the opening, rotates by 1, and repeats for
 *                   exactly capacity() steps, so the drum ends exactly where
 *                   it started (it must finish the cycle to restore the
 *                   alignment). Returns the entry or null. Keys are compared
 *                   with the entries in the order the drum presents them
 *                   (logical slots 0, 1, 2, ... skipping empty slots) and the
 *                   comparisons STOP as soon as the key is found: the rest of
 *                   the rotation costs no comparisons. lastFindComparisons()
 *                   reports how many comparisons the last find() made: the
 *                   number of occupied entries up to and including the match,
 *                   or size() if the key is absent.</li>
 *   <li>remove(key): the same capacity()-step cycle, ejecting each entry and
 *                   feeding it back unless it matches. Ends where it started.</li>
 * </ul>
 */
public class DrumStash<K, V> {

    private final Entry<K, V>[] ring;
    private int head;      // physical index of the opening
    private int size;
    private int lastFindComparisons;

    @SuppressWarnings("unchecked")
    public DrumStash(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be >= 0");
        }
        this.ring = (Entry<K, V>[]) new Entry[capacity];
        this.head = 0;
        this.size = 0;
    }

    public int capacity() { return ring.length; }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    public boolean isFull() { return size == ring.length; }

    // ---------------- low-level drum operations ----------------

    /** @return the entry at the opening, or null if the opening is empty */
    public Entry<K, V> peekOpening() {
        return ring.length == 0 ? null : ring[head];
    }

    /** Places an entry at the opening. @return false if the opening is occupied (or capacity is 0) */
    public boolean feed(Entry<K, V> entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        if (ring.length == 0 || ring[head] != null) {
            return false;
        }
        ring[head] = entry;
        size++;
        return true;
    }

    /** Removes and returns the entry at the opening. @throws IllegalStateException if the opening is empty */
    public Entry<K, V> eject() {
        if (ring.length == 0 || ring[head] == null) {
            throw new IllegalStateException("opening is empty");
        }
        Entry<K, V> e = ring[head];
        ring[head] = null;
        size--;
        return e;
    }

    /** Rotates the whole ring; positive steps bring logical slot `steps` to the opening. */
    public void rotate(int steps) {
        if (ring.length == 0) {
            return;
        }
        head = Math.floorMod(head + steps, ring.length);
    }

    /** INSPECTION ONLY (tests, visualizers): the entry at logical slot i (0 = opening), or null if that slot is empty. */
    public Entry<K, V> getAt(int i) {
        if (i < 0 || i >= ring.length) {
            throw new IndexOutOfBoundsException("slot " + i);
        }
        return ring[(head + i) % ring.length];
    }

    // ---------------- high-level operations ----------------

    /** See class comment. @return false if the stash is full */
    public boolean insert(Entry<K, V> entry) {
        if (isFull()) {
            return false;
        }
        while (peekOpening() != null) {
            rotate(1);
        }
        return feed(entry);
    }

    /** See class comment. @return the entry with that key, or null */
    public Entry<K, V> find(K key) {
        Entry<K, V> found = null;
        int comparisons = 0;
        for (int step = 0; step < ring.length; step++) {
            Entry<K, V> e = peekOpening();
            if (found == null && e != null) {          // once found, no more comparisons are needed
                comparisons++;
                if (e.getKey().equals(key)) {
                    found = e;
                }
            }
            rotate(1);                                  // but the cycle is completed to restore the alignment
        }
        lastFindComparisons = comparisons;
        return found;
    }

    /** @return the number of key comparisons made by the most recent find() (0 before the first call) */
    public int lastFindComparisons() {
        return lastFindComparisons;
    }

    /** See class comment. @return the removed entry, or null if the key was not here */
    public Entry<K, V> remove(K key) {
        Entry<K, V> removed = null;
        for (int step = 0; step < ring.length; step++) {
            if (peekOpening() != null) {
                Entry<K, V> e = eject();
                if (e.getKey().equals(key)) {
                    removed = e;          // matching entry vanishes
                } else {
                    feed(e);              // everyone else goes straight back
                }
            }
            rotate(1);
        }
        return removed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < ring.length; i++) {
            if (i > 0) sb.append(", ");
            Entry<K, V> e = getAt(i);
            sb.append(e == null ? "." : e.getKey().toString());
        }
        return sb.append(")").toString();
    }
}
