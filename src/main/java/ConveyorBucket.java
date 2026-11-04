// ConveyorBucket.java - PROVIDED. Do not modify.

/**
 * One bucket of the hash table: a small TUNNEL of fixed capacity with two
 * mouths and only one rule for entering: every new entry is pushed in at the
 * LEFT mouth (slot 0).
 *
 * <pre>
 *   LEFT mouth -> [ slot 0 | slot 1 | slot 2 | slot 3 ] <- RIGHT mouth
 *                  newest                      oldest
 * </pre>
 *
 * INVARIANT (the "packed-left" rule): the occupied slots are always exactly
 * slots 0 .. size()-1. There are never gaps between entries; all the empty
 * slots are at the right. Every method below preserves this invariant, which
 * is why:
 * <ul>
 *   <li>the "nearest gap" is always slot size();</li>
 *   <li>a search can stop at the first empty slot;</li>
 *   <li>the entry at the RIGHT mouth (slot size()-1) is always the oldest.</li>
 * </ul>
 *
 * Storage is a plain Java array.
 */
public class ConveyorBucket<K, V> {

    private final Entry<K, V>[] slots;
    private int size;

    @SuppressWarnings("unchecked")
    public ConveyorBucket(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.slots = (Entry<K, V>[]) new Entry[capacity];
        this.size = 0;
    }

    public int capacity() { return slots.length; }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    public boolean isFull() { return size == slots.length; }

    /**
     * Pushes {@code entry} in at the LEFT mouth. Every entry already in the
     * bucket shifts one slot to the right.
     *
     * If the bucket was full, the entry that was sitting at the RIGHT mouth
     * is pushed out and returned to the caller (it is the caller's job to
     * put it somewhere else). If the bucket had a gap, nothing is evicted
     * and null is returned.
     *
     * @return the evicted entry, or null if nothing was evicted
     */
    public Entry<K, V> pushLeft(Entry<K, V> entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        Entry<K, V> evicted = null;
        int last;
        if (size == slots.length) {
            evicted = slots[slots.length - 1];
            last = slots.length - 1;      // the oldest entry falls off the end
        } else {
            last = size;                   // grow into the first free slot
            size++;
        }
        for (int i = last; i > 0; i--) {
            slots[i] = slots[i - 1];
        }
        slots[0] = entry;
        return evicted;
    }

    /**
     * @return the entry in the given slot, or null if that slot is empty
     * @throws IndexOutOfBoundsException if slot is not in 0..capacity()-1
     */
    public Entry<K, V> get(int slot) {
        if (slot < 0 || slot >= slots.length) {
            throw new IndexOutOfBoundsException("slot " + slot);
        }
        return slots[slot];
    }

    /**
     * Scans from the LEFT mouth and returns the slot holding {@code key},
     * or -1 if it is not here. Because of the packed-left invariant, only
     * the occupied slots are examined.
     *
     * Cost accounting used throughout this assignment: a hit at slot i
     * costs i+1 key comparisons; a miss costs size() key comparisons.
     */
    public int indexOf(K key) {
        for (int i = 0; i < size; i++) {
            if (slots[i].getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes the entry with the given key (if present) and closes the gap
     * by sliding every entry to its right one slot LEFT, so the packed-left
     * invariant still holds. Relative order of the survivors is preserved.
     *
     * @return the removed entry, or null if the key was not here
     */
    public Entry<K, V> removeKey(K key) {
        int idx = indexOf(key);
        if (idx < 0) {
            return null;
        }
        Entry<K, V> removed = slots[idx];
        for (int i = idx; i < size - 1; i++) {
            slots[i] = slots[i + 1];
        }
        slots[size - 1] = null;
        size--;
        return removed;
    }

    /**
     * Removes and returns the entry at the RIGHT mouth (the oldest entry),
     * or null if the bucket is empty. Handy when emptying a bucket starting
     * from its oldest entry.
     */
    public Entry<K, V> popRight() {
        if (size == 0) {
            return null;
        }
        Entry<K, V> oldest = slots[size - 1];
        slots[size - 1] = null;
        size--;
        return oldest;
    }

    /**
     * Removes and returns the entry at the LEFT mouth (slot 0), sliding every
     * other entry one slot LEFT. Returns null if the bucket is empty.
     * This is the exact inverse of a pushLeft() that did not evict, and is
     * what you need when rolling back a failed kick chain.
     */
    public Entry<K, V> popLeft() {
        if (size == 0) {
            return null;
        }
        Entry<K, V> newest = slots[0];
        for (int i = 0; i < size - 1; i++) {
            slots[i] = slots[i + 1];
        }
        slots[size - 1] = null;
        size--;
        return newest;
    }

    /**
     * Places an entry in the first free slot, i.e. just to the right of the
     * current oldest entry (so it becomes the new RIGHT-mouth entry).
     * Combined with popLeft() this undoes a pushLeft() that evicted.
     *
     * @throws IllegalStateException if the bucket is full
     */
    public void appendRight(Entry<K, V> entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        if (size == slots.length) {
            throw new IllegalStateException("bucket is full");
        }
        slots[size++] = entry;
    }

    /** Empties the bucket. */
    public void clear() {
        for (int i = 0; i < size; i++) {
            slots[i] = null;
        }
        size = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < slots.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(slots[i] == null ? "." : slots[i].getKey().toString());
        }
        return sb.append("]").toString();
    }
}
