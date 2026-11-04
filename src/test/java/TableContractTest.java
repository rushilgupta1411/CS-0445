import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

/** Behaviour on bigger inputs with realistic seeds. */
public class TableContractTest {

    private static CuckooPushbackTable<Integer, String> table(int buckets, int capacity, int stash, long seed) {
        Random r = new Random(seed);
        return TestHelpers.tableFromSeeds(buckets, capacity, stash, 64, r.nextLong(), r.nextLong());
    }

    @Test
    public void testRoundTrip() {
        // 2048 buckets x 4 slots = 8192 slots for 5000 keys (load about 0.6)
        CuckooPushbackTable<Integer, String> t = table(2048, 4, 4, 5);
        int[] keys = TestHelpers.distinctKeys(5000, 77);
        for (int k : keys) assertNull(t.put(k, "v" + k));
        assertEquals(5000, t.size());
        for (int k : keys) assertEquals("v" + k, t.get(k));
        for (int i = 0; i < keys.length; i += 2) assertEquals("v" + keys[i], t.remove(keys[i]));
        assertEquals(2500, t.size());
        for (int i = 0; i < keys.length; i++) {
            if (i % 2 == 0) assertNull(t.get(keys[i])); else assertEquals("v" + keys[i], t.get(keys[i]));
        }
        assertTrue(TestHelpers.allBucketsPackedLeft(t));
    }

    @Test
    public void testFillsUntilFullAndSurvivesTheException() {
        // 64 buckets x 2 slots + a stash of 4 can hold at most 132 entries, so 400 puts must fail somewhere
        CuckooPushbackTable<Integer, String> t = table(64, 2, 4, 9);
        int[] keys = TestHelpers.distinctKeys(400, 3);
        int inserted = 0;
        try {
            for (int k : keys) { t.put(k, "v" + k); inserted++; }
            fail("expected TableCapacityException");
        } catch (TableCapacityException expected) {
            // fine
        }
        assertEquals(inserted, t.size());
        assertTrue(inserted <= 64 * 2 + 4);
        assertTrue("a cuckoo table should fill most of its slots before failing", inserted > 100);
        for (int i = 0; i < inserted; i++) assertEquals("v" + keys[i], t.get(keys[i]));
        assertNull(t.get(keys[inserted]));                      // the failed key is absent
        assertTrue(TestHelpers.allBucketsPackedLeft(t));
        // the table is still fully usable after the failure
        assertEquals("v" + keys[0], t.remove(keys[0]));
        assertEquals(inserted - 1, t.size());
    }

    @Test
    public void testChurn() {
        CuckooPushbackTable<Integer, String> t = table(256, 4, 4, 21);
        int[] pool = TestHelpers.distinctKeys(30000, 8);
        int live = 200;
        for (int i = 0; i < live; i++) t.put(pool[i], "v" + pool[i]);
        Random g = new Random(4);
        int next = live;
        for (int op = 0; op < 20000; op++) {
            int victim = g.nextInt(next);
            if (t.remove(pool[victim]) == null) continue;       // already removed earlier
            t.put(pool[next], "v" + pool[next]);
            next++;
        }
        assertTrue(TestHelpers.allBucketsPackedLeft(t));
        int found = 0;
        for (int i = 0; i < next; i++) if (t.containsKey(pool[i])) found++;
        assertEquals(t.size(), found);
    }
}
