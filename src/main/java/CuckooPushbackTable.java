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

    // TODO: fields -- the buckets (ConveyorBucket[]), the stash (DrumStash), the
    // coefficients a1, b1, a2, b2 of the two hash functions, m, size, maxKicks, and
    // the counters behind the statistics (see the README).

    /**
     * The one constructor. Validate the arguments (IllegalArgumentException): buckets >= 2,
     * bucketCapacity >= 1, stashCapacity >= 0, maxKicks >= 1, a1 and a2 in 1 .. P-1,
     * b1 and b2 in 0 .. P-1.
     *
     * @param a1 multiplier of h1,  @param b1 offset of h1:  h1(key, m) = hash(a1, b1, key, m)
     * @param a2 multiplier of h2,  @param b2 offset of h2:  h2(key, m) = hash(a2, b2, key, m)
     */
    public CuckooPushbackTable(int buckets, int bucketCapacity, int stashCapacity, int maxKicks,
                               long a1, long b1, long a2, long b2) {
        // TODO
    }

    // ---------------- operations ----------------

    /**
     * Inserts or replaces; returns the previous value or null. If the entry cannot be placed
     * (the kick limit was reached and the stash is full), the table is restored to exactly what
     * it was and TableCapacityException is thrown.
     */
    public V put(K key, V value) { throw new UnsupportedOperationException("put() not implemented yet"); }

    /** @return the value, or null if absent. Counts as a lookup in the statistics. */
    public V get(K key) { throw new UnsupportedOperationException("get() not implemented yet"); }

    /** @return true if present. Counts as a lookup in the statistics, exactly like get(). */
    public boolean containsKey(K key) { throw new UnsupportedOperationException("containsKey() not implemented yet"); }

    /** @return the removed value, or null if absent */
    public V remove(K key) { throw new UnsupportedOperationException("remove() not implemented yet"); }

    // ---------------- inspection ----------------

    /** @return number of entries, including those in the stash */
    public int size() { throw new UnsupportedOperationException(); }

    /** @return size() / (bucketCount() * bucketCapacity()) */
    public double loadFactor() { throw new UnsupportedOperationException(); }

    public int bucketCount() { throw new UnsupportedOperationException(); }

    public int bucketCapacity() { throw new UnsupportedOperationException(); }

    /** @return the entry in that slot of that bucket (slot 0 = LEFT mouth), or null if empty */
    public Entry<K, V> getEntryAt(int bucket, int slot) { throw new UnsupportedOperationException(); }

    public int stashCapacity() { throw new UnsupportedOperationException(); }

    public int stashSize() { throw new UnsupportedOperationException(); }

    /** INSPECTION ONLY (tests, visualizers): the entry at logical slot i of the stash drum (0 = opening), or null.
     *  The table's own logic must reach the drum only through its opening. */
    public Entry<K, V> getStashAt(int i) { throw new UnsupportedOperationException(); }

    // ---------------- statistics (see the README for exactly when each one changes) ----------------

    public long getLookupHits() { throw new UnsupportedOperationException(); }
    public long getLookupMisses() { throw new UnsupportedOperationException(); }
    public long getTotalHitComparisons() { throw new UnsupportedOperationException(); }
    public long getTotalMissComparisons() { throw new UnsupportedOperationException(); }
    public long getTotalInsertSteps() { throw new UnsupportedOperationException(); }
    public int getMaxInsertSteps() { throw new UnsupportedOperationException(); }

    /** Zeroes every statistics counter. */
    public void resetOperationCounters() { throw new UnsupportedOperationException(); }

    // BONUS (visualizer) only: if your visualizer needs more information than the inspection methods
    // above give, design and add your own hooks here (see the README). Leave them out otherwise.
}
