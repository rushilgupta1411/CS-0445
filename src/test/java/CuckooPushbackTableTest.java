import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Worked examples from the README. The injected seeds are (2, 1): h1 is the hash function that
 * seed 2 produces and h2 the one that seed 1 produces, with 3 buckets of capacity 2. For these
 * seeds, the two homes of the keys used are (home2 is the bumped value when h2 equals h1):
 *
 *   key      1      2      3           9      11
 *   home1    1      2      2           2      2
 *   home2    2      1      0 (bumped)  1      1
 */
public class CuckooPushbackTableTest {

    /**
     * 3 buckets of capacity 2 with the given stash capacity and kick limit. h1 and h2 are the hash
     * functions that seed1 and seed2 produce (TestHelpers turns each seed into coefficients).
     */
    private CuckooPushbackTable<Integer, String> table(int stash, int maxKicks, long seed1, long seed2) {
        return TestHelpers.tableFromSeeds(3, 2, stash, maxKicks, seed1, seed2);
    }

    @Test
    public void testSimplePutGetRemove() {
        CuckooPushbackTable<Integer, String> t = table(2, 8, 2, 1);
        assertNull(t.put(3, "three"));
        assertEquals("three", t.get(3));
        assertEquals("three", t.put(3, "THREE"));
        assertEquals("THREE", t.remove(3));
        assertNull(t.get(3));
        assertEquals(0, t.size());
    }

    @Test
    public void testSecondChoiceBucketUsedWhenFirstIsFull() {
        CuckooPushbackTable<Integer, String> t = table(2, 8, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9);
        assertEquals("[., .]", TestHelpers.bucket(t, 0));
        assertEquals("[9, 1]", TestHelpers.bucket(t, 1));   // 9 found B2 (its first choice) full and used B1
        assertEquals("[3, 2]", TestHelpers.bucket(t, 2));   // 2 and 3 filled their first choice, B2
    }

    @Test
    public void testKickChain() {
        CuckooPushbackTable<Integer, String> t = table(2, 8, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9, 11);              // 11: both of its buckets (B2, B1) are full
        assertEquals("[3, .]", TestHelpers.bucket(t, 0));
        assertEquals("[2, 9]", TestHelpers.bucket(t, 1));
        assertEquals("[1, 11]", TestHelpers.bucket(t, 2));
        assertEquals("(., .)", TestHelpers.stash(t));
        assertEquals(4, t.getMaxInsertSteps());              // three evicting pushes + the final placement
        assertEquals(5, t.size());
        assertTrue(TestHelpers.allBucketsPackedLeft(t));
    }

    @Test
    public void testKickLimitParksTheEntryInHandInTheStash() {
        CuckooPushbackTable<Integer, String> t = table(2, 2, 2, 1);   // at most 2 kicks per chain
        TestHelpers.putAll(t, 1, 2, 3, 9, 11);
        assertEquals("[., .]", TestHelpers.bucket(t, 0));
        assertEquals("[2, 9]", TestHelpers.bucket(t, 1));
        assertEquals("[11, 3]", TestHelpers.bucket(t, 2));
        assertEquals("(1, .)", TestHelpers.stash(t));         // 1 was in hand when the limit hit
        assertEquals(1, t.stashSize());
        assertEquals(5, t.size());
    }

    @Test
    public void testLookupConsultsStashLast() {
        CuckooPushbackTable<Integer, String> t = table(2, 2, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9, 11);
        t.resetOperationCounters();
        assertEquals("v1", t.get(1));                // B1 (2 comparisons), B2 (2), stash (1)
        assertEquals(5, t.getTotalHitComparisons());
        t.resetOperationCounters();
        assertEquals("v2", t.get(2));                // first choice B2: miss (2 compares); second choice B1: hit at slot 0 (1)
        assertEquals(3, t.getTotalHitComparisons());             // the stash is never reached
    }

    @Test
    public void testStashLookupStopsComparingOnceTheKeyIsFound() {
        // stash of 3, one kick per chain: after these puts the stash holds three entries
        CuckooPushbackTable<Integer, String> t = table(3, 1, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9, 11, 12, 13, 14, 15);
        assertEquals("[3, 12]", TestHelpers.bucket(t, 0));
        assertEquals("[9, 1]", TestHelpers.bucket(t, 1));
        assertEquals("[15, 14]", TestHelpers.bucket(t, 2));
        assertEquals("(13, 2, 11)", TestHelpers.stash(t));    // the drum presents 13, then 2, then 11
        // every one of these keys has two full buckets (2 + 2 = 4 comparisons) before the stash is searched
        t.resetOperationCounters();
        assertEquals("v13", t.get(13));
        assertEquals(4 + 1, t.getTotalHitComparisons());      // 13 is at the opening: 1 comparison in the stash
        t.resetOperationCounters();
        assertEquals("v2", t.get(2));
        assertEquals(4 + 2, t.getTotalHitComparisons());      // 13, then 2: the search stops comparing after the match
        t.resetOperationCounters();
        assertEquals("v11", t.get(11));
        assertEquals(4 + 3, t.getTotalHitComparisons());      // 13, 2, 11
        t.resetOperationCounters();
        assertNull(t.get(16));
        assertEquals(4 + 3, t.getTotalMissComparisons());     // a miss compares every stash entry: stash.size()
        assertEquals("(13, 2, 11)", TestHelpers.stash(t));    // the drum ends where it started
    }

    @Test
    public void testAFullTableRefusesFurtherPutsAndStaysUnchanged() {
        CuckooPushbackTable<Integer, String> t = table(3, 1, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9, 11, 12, 13, 14, 15);   // buckets and stash are now all occupied
        for (int k = 16; k < 20; k++) {
            try {
                t.put(k, "v" + k);
                fail("expected TableCapacityException for " + k);
            } catch (TableCapacityException expected) {
                // fine
            }
        }
        assertEquals("[3, 12]", TestHelpers.bucket(t, 0));
        assertEquals("[9, 1]", TestHelpers.bucket(t, 1));
        assertEquals("[15, 14]", TestHelpers.bucket(t, 2));
        assertEquals("(13, 2, 11)", TestHelpers.stash(t));
        assertEquals(9, t.size());
        assertEquals(10, t.getTotalInsertSteps());           // the refused puts added nothing
    }

    @Test
    public void testDrainingFollowsTheDrumOrderOneEntryAtATime() {
        // The stash holds 13, 2, 11 (in the order the drum presents them) and all three have the same two homes, B2 and B1.
        CuckooPushbackTable<Integer, String> t = table(3, 1, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9, 11, 12, 13, 14, 15);
        assertEquals("(13, 2, 11)", TestHelpers.stash(t));

        assertEquals("v12", t.remove(12));                    // B0 is not a home of any stashed key: nothing moves
        assertEquals("[3, .]", TestHelpers.bucket(t, 0));
        assertEquals("(13, 2, 11)", TestHelpers.stash(t));    // and the drum ends exactly where it started

        assertEquals("v9", t.remove(9));                      // frees one slot in B1: only the entry at the opening, 13, moves back
        assertEquals("[13, 1]", TestHelpers.bucket(t, 1));
        assertEquals("(., 2, 11)", TestHelpers.stash(t));

        // Frees one slot in B2. The opening is now empty, so the cycle rotates one step, finds 2 there and moves it back;
        // 11 stays in the drum, and after the full cycle the drum is back where it started.
        assertEquals("v15", t.remove(15));
        assertEquals("[2, 14]", TestHelpers.bucket(t, 2));
        assertEquals("(., ., 11)", TestHelpers.stash(t));

        assertEquals("v14", t.remove(14));                    // two steps further along the drum: 11 moves back
        assertEquals("[11, 2]", TestHelpers.bucket(t, 2));
        assertEquals("(., ., .)", TestHelpers.stash(t));
        assertEquals(5, t.size());                            // 9 entries, 4 removed
        for (int k : new int[] {1, 2, 3, 11, 13}) assertEquals("v" + k, t.get(k));
    }

    @Test
    public void testStashIsNotConsultedWhenEmpty() {
        CuckooPushbackTable<Integer, String> t = table(2, 8, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9);
        t.resetOperationCounters();
        assertNull(t.get(4));                        // homes B2 and B0: B2 holds 2 entries, B0 is empty
        assertEquals(2, t.getTotalMissComparisons());
    }

    @Test
    public void testRemoveDrainsStashBackIntoTheTable() {
        CuckooPushbackTable<Integer, String> t = table(2, 2, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9, 11);
        assertEquals("v2", t.remove(2));             // frees a slot in B1, which is 1's first choice
        assertEquals("[., .]", TestHelpers.bucket(t, 0));
        assertEquals("[1, 9]", TestHelpers.bucket(t, 1));
        assertEquals("[11, 3]", TestHelpers.bucket(t, 2));
        assertEquals("(., .)", TestHelpers.stash(t));
        assertEquals(4, t.size());
        assertEquals("v1", t.get(1));
    }

    @Test
    public void testPutThrowsWhenTheEntryInHandHasNowhereToGo() {
        // no stash and 2 kicks: the kick chain of 11 ends with 1 in hand and nothing to park it in
        CuckooPushbackTable<Integer, String> t = table(0, 2, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9);
        try {
            t.put(11, "v11");
            fail("expected TableCapacityException");
        } catch (TableCapacityException expected) {
            // fine
        }
        // two pushes were undone: the table is exactly as before the put
        assertEquals("[., .]", TestHelpers.bucket(t, 0));
        assertEquals("[9, 1]", TestHelpers.bucket(t, 1));
        assertEquals("[3, 2]", TestHelpers.bucket(t, 2));
        assertEquals(4, t.size());
        assertEquals("v1", t.get(1));
        assertEquals("v2", t.get(2));
        assertNull(t.get(11));
    }

    @Test
    public void testFailedPutRollsBackTheKickChain() {
        // 1 kick allowed, no stash: 11 kicks 2 out of B2 and 2 has nowhere to go
        CuckooPushbackTable<Integer, String> t = table(0, 1, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9);
        assertEquals("[., .]", TestHelpers.bucket(t, 0));
        assertEquals("[9, 1]", TestHelpers.bucket(t, 1));
        assertEquals("[3, 2]", TestHelpers.bucket(t, 2));
        try {
            t.put(11, "v11");
            fail("expected TableCapacityException");
        } catch (TableCapacityException expected) {
            // fine
        }
        // The failed put must leave the table exactly as it was: 2 is NOT lost, 11 is NOT inserted.
        assertEquals("[., .]", TestHelpers.bucket(t, 0));
        assertEquals("[9, 1]", TestHelpers.bucket(t, 1));
        assertEquals("[3, 2]", TestHelpers.bucket(t, 2));
        assertEquals(4, t.size());
        assertEquals("v2", t.get(2));
        assertNull(t.get(11));
        assertEquals(4, t.getTotalInsertSteps());             // the failed put's pushes were undone and are not counted
    }

    @Test
    public void testFailedPutWithAFullStashKeepsTheStashIntact() {
        // stash capacity 1: it takes the entry in hand once, and then the next failing put must throw
        CuckooPushbackTable<Integer, String> t = table(1, 2, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9, 11);                // 1 ends up in the stash
        assertEquals("(1)", TestHelpers.stash(t));
        String b0 = TestHelpers.bucket(t, 0), b1 = TestHelpers.bucket(t, 1), b2 = TestHelpers.bucket(t, 2);
        int size = t.size();
        // keep inserting until the table refuses; whenever it does, nothing may have changed
        for (int k = 20; k < 60; k++) {
            String x0 = TestHelpers.bucket(t, 0), x1 = TestHelpers.bucket(t, 1), x2 = TestHelpers.bucket(t, 2);
            String st = TestHelpers.stash(t);
            int sz = t.size();
            try {
                t.put(k, "v" + k);
            } catch (TableCapacityException e) {
                assertEquals(x0, TestHelpers.bucket(t, 0));
                assertEquals(x1, TestHelpers.bucket(t, 1));
                assertEquals(x2, TestHelpers.bucket(t, 2));
                assertEquals(st, TestHelpers.stash(t));
                assertEquals(sz, t.size());
                assertNull(t.get(k));
                return;
            }
        }
        fail("the table has 7 slots, so it must refuse some put");
    }

    @Test
    public void testFirstCoefficientsDefineH1AndSecondDefineH2() {
        // Swapping the two seeds swaps the roles of h1 and h2: key 1 has homes (1, 2) with seeds (2, 1)
        // and homes (2, 1) with seeds (1, 2), so its first choice changes.
        CuckooPushbackTable<Integer, String> a = table(2, 8, 2, 1);
        CuckooPushbackTable<Integer, String> b = table(2, 8, 1, 2);
        a.put(1, "v1");
        b.put(1, "v1");
        assertEquals("v1", a.getEntryAt(1, 0).getValue());   // first choice of key 1 under seeds (2, 1) is B1
        assertEquals("v1", b.getEntryAt(2, 0).getValue());   // under seeds (1, 2) it is B2
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooFewBucketsRejected() {
        new CuckooPushbackTable<Integer, String>(1, 2, 2, 8, 5, 6, 7, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroMultiplierRejected() {
        new CuckooPushbackTable<Integer, String>(3, 2, 2, 8, 0, 6, 7, 8);       // a1 must be at least 1
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOffsetOutOfRangeRejected() {
        new CuckooPushbackTable<Integer, String>(3, 2, 2, 8, 5, 6, 7, 2147483647L);   // b2 must be below P
    }

    @Test
    public void testCoefficientsAreUsedDirectly() {
        // The same hash functions, given as coefficients instead of seeds, give the same table.
        long[] c1 = TestHelpers.coefficients(2);
        long[] c2 = TestHelpers.coefficients(1);
        CuckooPushbackTable<Integer, String> direct = new CuckooPushbackTable<>(3, 2, 2, 8, c1[0], c1[1], c2[0], c2[1]);
        CuckooPushbackTable<Integer, String> viaSeeds = table(2, 8, 2, 1);
        TestHelpers.putAll(direct, 1, 2, 3, 9, 11);
        TestHelpers.putAll(viaSeeds, 1, 2, 3, 9, 11);
        for (int b = 0; b < 3; b++) assertEquals(TestHelpers.bucket(viaSeeds, b), TestHelpers.bucket(direct, b));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullKeyRejected() {
        table(2, 8, 2, 1).put(null, "x");
    }

    @Test
    public void testStatisticsBookkeeping() {
        CuckooPushbackTable<Integer, String> t = table(2, 8, 2, 1);
        TestHelpers.putAll(t, 1, 2, 3, 9, 11);               // layout: B0 [3, .]  B1 [2, 9]  B2 [1, 11]
        assertEquals(8, t.getTotalInsertSteps());            // steps 1 + 1 + 1 + 1 + 4
        assertEquals(4, t.getMaxInsertSteps());
        assertEquals(0, t.getLookupHits());
        assertEquals(0, t.getTotalHitComparisons());
        assertEquals("v2", t.put(2, "new"));                 // a replacement adds no steps, and is not a lookup
        assertEquals(8, t.getTotalInsertSteps());
        assertEquals(0, t.getLookupHits());
        assertEquals("new", t.get(2));                       // B2 [1, 11]: 2 comparisons, then B1 [2, 9]: hit at slot 0 = 3
        assertTrue(t.containsKey(3));                        // B2: 2 comparisons, then B0 [3, .]: hit at slot 0 = 3
        assertFalse(t.containsKey(4));                       // B2: 2 comparisons, then B0 [3, .]: miss = 1 -> 3
        assertEquals("v9", t.remove(9));                     // the lookup inside remove is not counted
        assertEquals(2, t.getLookupHits());
        assertEquals(1, t.getLookupMisses());
        assertEquals(6, t.getTotalHitComparisons());         // 3 + 3
        assertEquals(3, t.getTotalMissComparisons());
        t.resetOperationCounters();
        assertEquals(0, t.getTotalInsertSteps());
        assertEquals(0, t.getMaxInsertSteps());
        assertEquals(0, t.getLookupHits());
        assertEquals(0, t.getLookupMisses());
        assertEquals(0, t.getTotalHitComparisons());
        assertEquals(0, t.getTotalMissComparisons());
    }
}
