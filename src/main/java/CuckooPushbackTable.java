// CuckooPushbackTable.java - YOUR CODE
//
// A bucketized cuckoo hash table of FIXED size. Every key has TWO candidate
// buckets (from two hash functions). If both are full, the entry at the RIGHT
// mouth of a full bucket is kicked out and re-inserted into its OTHER
// candidate bucket, possibly kicking another entry, and so on. A chain that
// runs too long parks the entry in the STASH (a DrumStash). If the stash is
// full too, the put is undone and throws TableCapacityException: the table
// never resizes and never changes its hash functions.
// See the README for the exact rules; everything lives in this one class.
//
// RULES: plain arrays only -- no java.util container classes.

public class CuckooPushbackTable<K, V> {

    // ==================================================================
    // PROVIDED: the hash function. Do not modify (the autograder and the
    // README's worked examples depend on these exact values).
    //
    // THE BIG PICTURE
    //   A hash function turns a key into a bucket number 0 .. m-1. We use a whole
    //   FAMILY of such functions of the form
    //          h(x) = ((a * x + b) mod P) mod m
    //   Each choice of the two numbers (a, b) -- the "coefficients" -- is a different
    //   function. The table RECEIVES its coefficients in the constructor (the tests
    //   derive them from small "seeds" with SplitMix64, in the test code, outside this
    //   class), so it has two functions:
    //          h1(key, m) = hash(a1, b1, key, m)
    //          h2(key, m) = hash(a2, b2, key, m)
    //   and it only needs to remember a1, b1, a2, b2.
    // ==================================================================

    /** P = 2^31 - 1, a prime number. Working "mod a prime" keeps the scrambling free of regular patterns.
     *  Valid coefficients satisfy 1 <= a <= P-1 and 0 <= b <= P-1. */
    static final long P = 2147483647L;

    /**
     * One member of the hash family, selected by its coefficients (a, b).
     *   x = key.hashCode() & 0x7fffffff      the key's own hash code with the sign bit cleared (so x >= 0)
     *   (a * x + b) mod P                    multiply and shift: scrambles x (a * x < 2^62, so a long never overflows)
     *   ... mod m                            squeeze the result into the bucket numbers 0 .. m-1
     */
    static int hash(long a, long b, Object key, int m) {
        long x = key.hashCode() & 0x7fffffffL;
        return (int) (((a * x + b) % P) % m);
    }

    // ==================================================================
    // YOUR CODE
    // ==================================================================

    private final ConveyorBucket<K, V>[] buckets;
    private final DrumStash<K, V> stash;
    private final long a1, b1, a2, b2;
    private final int maxKicks;
    private int size;
    private long lookupHits, lookupMisses;
    private long hitComparisons, missComparisons;
    private long insertSteps;
    private int maxInsertSteps;

    /**
     * Creates a fixed-size table with the supplied hash coefficients.
     * @throws IllegalArgumentException if dimensions or coefficients are invalid
     */
    @SuppressWarnings("unchecked")
    public CuckooPushbackTable(int buckets, int bucketCapacity, int stashCapacity, int maxKicks,
                               long a1, long b1, long a2, long b2) {
        if (buckets < 2 || bucketCapacity < 1 || stashCapacity < 0 || maxKicks < 1
                || a1 < 1 || a1 >= P || a2 < 1 || a2 >= P
                || b1 < 0 || b1 >= P || b2 < 0 || b2 >= P) {
            throw new IllegalArgumentException("Invalid table dimensions or hash coefficients.");
        }
        this.buckets = (ConveyorBucket<K, V>[]) new ConveyorBucket<?, ?>[buckets];
        for (int i = 0; i < buckets; i++) {
            this.buckets[i] = new ConveyorBucket<>(bucketCapacity);
        }
        stash = new DrumStash<>(stashCapacity);
        this.maxKicks = maxKicks;
        this.a1 = a1;
        this.b1 = b1;
        this.a2 = a2;
        this.b2 = b2;
    }

    /** Inserts or replaces; failed insertion restores the exact original layout. */
    public V put(K key, V value) {
        Entry<K, V> existing = find(key, false);
        if (existing != null) {
            V previous = existing.getValue();
            existing.setValue(value);
            return previous;
        }
        Entry<K, V> entry = new Entry<>(key, value);
        int first = home1(key);
        int second = home2(key);
        int steps;
        if (!buckets[first].isFull()) {
            buckets[first].pushLeft(entry);
            steps = 1;
        } else if (!buckets[second].isFull()) {
            buckets[second].pushLeft(entry);
            steps = 1;
        } else {
            steps = insertWithKicks(entry, first);
        }
        // Commit statistics only after the complete insertion has succeeded.
        size++;
        insertSteps += steps;
        maxInsertSteps = Math.max(maxInsertSteps, steps);
        return null;
    }

    /**
     * Runs the eviction chain, journaling the target bucket of every kick.
     * Only full-bucket pushes need undoing: a non-evicting push finishes the
     * operation successfully, as does parking the final entry in the stash.
     */
    private int insertWithKicks(Entry<K, V> entry, int target) {
        int[] history = new int[maxKicks];
        Entry<K, V> held = entry;
        for (int kicks = 0; kicks < maxKicks; kicks++) {
            history[kicks] = target;
            held = buckets[target].pushLeft(held);
            int first = home1(held.getKey());
            int alternate = first == target ? home2(held.getKey()) : first;
            if (!buckets[alternate].isFull()) {
                buckets[alternate].pushLeft(held);
                return kicks + 2;
            }
            target = alternate;
        }
        if (stash.insert(held)) {
            return maxKicks;
        }
        // Undo newest-first. Each popped entry is what the preceding push
        // evicted; the final popped entry is the rejected new key.
        for (int i = maxKicks - 1; i >= 0; i--) {
            ConveyorBucket<K, V> bucket = buckets[history[i]];
            Entry<K, V> pushed = bucket.popLeft();
            bucket.appendRight(held);
            held = pushed;
        }
        throw new TableCapacityException("Kick limit reached and stash is full.");
    }

    /** Returns the associated value or null; records one public lookup. */
    public V get(K key) {
        Entry<K, V> entry = find(key, true);
        return entry == null ? null : entry.getValue();
    }

    /** Records one lookup; a key mapped to null is still present. */
    public boolean containsKey(K key) {
        return find(key, true) != null;
    }

    /** Removes a key, then gives every stashed entry one chance to return. */
    public V remove(K key) {
        requireKey(key);
        Entry<K, V> removed = buckets[home1(key)].removeKey(key);
        if (removed == null) {
            removed = buckets[home2(key)].removeKey(key);
        }
        if (removed == null && stash.size() > 0) {
            removed = stash.remove(key);
        }
        if (removed == null) {
            return null;
        }
        size--;
        drainStash();
        return removed.getValue();
    }

    private void drainStash() {
        // Visit through the opening only, completing a full cycle even when
        // entries move out, so the stash's logical alignment is preserved.
        for (int step = 0; step < stash.capacity(); step++) {
            Entry<K, V> entry = stash.peekOpening();
            if (entry != null) {
                int first = home1(entry.getKey());
                int second = home2(entry.getKey());
                if (!buckets[first].isFull()) {
                    buckets[first].pushLeft(stash.eject());
                } else if (!buckets[second].isFull()) {
                    buckets[second].pushLeft(stash.eject());
                }
            }
            stash.rotate(1);
        }
    }

    private Entry<K, V> find(K key, boolean countLookup) {
        requireKey(key);
        long comparisons = 0;
        Entry<K, V> found = null;
        int first = home1(key);
        int second = home2(key);
        for (int choice = 0; choice < 2; choice++) {
            ConveyorBucket<K, V> bucket = buckets[choice == 0 ? first : second];
            int slot = bucket.indexOf(key);
            comparisons += slot < 0 ? bucket.size() : slot + 1;
            if (slot >= 0) {
                found = bucket.get(slot);
                break;
            }
        }
        if (found == null && stash.size() > 0) {
            found = stash.find(key);
            comparisons += stash.lastFindComparisons();
        }
        if (countLookup) {
            if (found == null) {
                lookupMisses++;
                missComparisons += comparisons;
            } else {
                lookupHits++;
                hitComparisons += comparisons;
            }
        }
        return found;
    }

    private void requireKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Null keys are not allowed.");
        }
    }

    private int home1(K key) {
        return hash(a1, b1, key, buckets.length);
    }

    private int home2(K key) {
        int first = home1(key);
        int second = hash(a2, b2, key, buckets.length);
        return second == first ? (second + 1) % buckets.length : second;
    }

    public int size() { return size; }

    public double loadFactor() {
        return size / ((double) bucketCount() * bucketCapacity());
    }

    public int bucketCount() { return buckets.length; }
    public int bucketCapacity() { return buckets[0].capacity(); }

    public Entry<K, V> getEntryAt(int bucket, int slot) {
        if (bucket < 0 || bucket >= buckets.length) {
            throw new IndexOutOfBoundsException("bucket " + bucket);
        }
        return buckets[bucket].get(slot);
    }

    public int stashCapacity() { return stash.capacity(); }
    public int stashSize() { return stash.size(); }

    /** Inspection only; table operations never call this method. */
    public Entry<K, V> getStashAt(int i) { return stash.getAt(i); }

    public long getLookupHits() { return lookupHits; }
    public long getLookupMisses() { return lookupMisses; }
    public long getTotalHitComparisons() { return hitComparisons; }
    public long getTotalMissComparisons() { return missComparisons; }
    public long getTotalInsertSteps() { return insertSteps; }
    public int getMaxInsertSteps() { return maxInsertSteps; }

    /** Resets counters without changing contents or layout. */
    public void resetOperationCounters() {
        lookupHits = 0;
        lookupMisses = 0;
        hitComparisons = 0;
        missComparisons = 0;
        insertSteps = 0;
        maxInsertSteps = 0;
    }
}
