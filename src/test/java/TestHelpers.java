import java.util.HashSet;
import java.util.Random;

/** Small helpers shared by the visible tests (tests may use any java.util class). */
final class TestHelpers {
    private TestHelpers() {}

    // ------------------------------------------------------------------
    // From a seed to the coefficients of a hash function (SplitMix64 lives here, outside the table)
    // ------------------------------------------------------------------

    /**
     * Turns a seed into the coefficients (a, b) of one hash function: run SplitMix64 for two
     * steps, then squeeze the two 64-bit outputs into the allowed ranges
     *     a = floorMod(output1, P - 1) + 1    in 1 .. P-1
     *     b = floorMod(output2, P)            in 0 .. P-1
     * (floorMod because the outputs can be negative).
     */
    static long[] coefficients(long seed) {
        SplitMix64 gen = new SplitMix64(seed);
        long output1 = gen.nextLong();
        long output2 = gen.nextLong();
        long p = CuckooPushbackTable.P;
        return new long[] { Math.floorMod(output1, p - 1) + 1, Math.floorMod(output2, p) };
    }

    /** hash(seed, key, m): the hash function that this seed produces. */
    static int hash(long seed, Object key, int m) {
        long[] c = coefficients(seed);
        return CuckooPushbackTable.hash(c[0], c[1], key, m);
    }

    /** A table whose h1 comes from seed1 and whose h2 comes from seed2. */
    static <K, V> CuckooPushbackTable<K, V> tableFromSeeds(int buckets, int bucketCapacity, int stashCapacity,
                                                           int maxKicks, long seed1, long seed2) {
        long[] c1 = coefficients(seed1);
        long[] c2 = coefficients(seed2);
        return new CuckooPushbackTable<>(buckets, bucketCapacity, stashCapacity, maxKicks, c1[0], c1[1], c2[0], c2[1]);
    }

    /** e.g. "[12, 9]" -- keys of one bucket from the LEFT mouth to the RIGHT mouth, "." for empty. */
    static String bucket(CuckooPushbackTable<Integer, String> t, int b) {
        StringBuilder sb = new StringBuilder("[");
        for (int s = 0; s < t.bucketCapacity(); s++) {
            Entry<Integer, String> e = t.getEntryAt(b, s);
            if (s > 0) sb.append(", ");
            sb.append(e == null ? "." : e.getKey().toString());
        }
        return sb.append("]").toString();
    }

    /** e.g. "(9, .)" -- the stash from its opening onward. */
    static String stash(CuckooPushbackTable<Integer, String> t) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < t.stashCapacity(); i++) {
            Entry<Integer, String> e = t.getStashAt(i);
            if (i > 0) sb.append(", ");
            sb.append(e == null ? "." : e.getKey().toString());
        }
        return sb.append(")").toString();
    }

    static void putAll(CuckooPushbackTable<Integer, String> t, int... keys) {
        for (int k : keys) {
            t.put(k, "v" + k);
        }
    }

    /** Checks the "packed-left" invariant for every bucket. */
    static boolean allBucketsPackedLeft(CuckooPushbackTable<Integer, String> t) {
        for (int b = 0; b < t.bucketCount(); b++) {
            boolean sawEmpty = false;
            for (int s = 0; s < t.bucketCapacity(); s++) {
                boolean empty = t.getEntryAt(b, s) == null;
                if (sawEmpty && !empty) return false;
                sawEmpty = sawEmpty || empty;
            }
        }
        return true;
    }

    /** n distinct non-negative keys (reproducible). */
    static int[] distinctKeys(int n, long seed) {
        Random r = new Random(seed);
        HashSet<Integer> seen = new HashSet<>();
        int[] keys = new int[n];
        int i = 0;
        while (i < n) {
            int k = r.nextInt(Integer.MAX_VALUE);
            if (seen.add(k)) keys[i++] = k;
        }
        return keys;
    }
}
