import org.junit.Test;
import static org.junit.Assert.*;

public class PushbackGoalTest {

    // ------------------------------------------------------------------
    // Drum basics
    // ------------------------------------------------------------------

    @Test
    public void testFeedAndEjectDrum() {
        PushbackGoalInterface<Character> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(3, '.');
        assertTrue(g.feedDrum('A'));
        assertEquals(Character.valueOf('A'), g.getDrumAt(0));
        assertFalse(g.feedDrum('B')); // opening already occupied
        assertEquals(Character.valueOf('A'), g.ejectDrum());
        assertEquals(Character.valueOf('.'), g.getDrumAt(0));
    }

    @Test(expected = IllegalStateException.class)
    public void testEjectEmptyDrumThrows() {
        PushbackGoalInterface<Character> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(3, '.');
        g.ejectDrum();
    }

    @Test(expected = IllegalStateException.class)
    public void testDrumMethodBeforeInitialize() {
        PushbackGoalInterface<Character> g = new PushbackGoalLinkedListImpl<>();
        g.drumCapacity();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDrumAtOutOfBounds() {
        PushbackGoalInterface<Character> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(3, '.');
        g.getDrumAt(3);
    }

    /**
     * Builds drum = [D, A, B, C] (physical slots 0..3, opening at slot 0)
     * by feeding A, B, C, D one at a time and rotating once after each
     * feed. Verified by hand: after feeding X and rotating once, X ends
     * up one slot further from the opening each time.
     */
    private PushbackGoalInterface<Character> buildDrumDABC() {
        PushbackGoalInterface<Character> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(4, '.');
        g.feedDrum('A'); g.rotateDrum(1);
        g.feedDrum('B'); g.rotateDrum(1);
        g.feedDrum('C'); g.rotateDrum(1);
        g.feedDrum('D');
        return g; // [D, A, B, C]
    }

    @Test
    public void testDrumConstructionSequence() {
        PushbackGoalInterface<Character> g = buildDrumDABC();
        assertEquals(Character.valueOf('D'), g.getDrumAt(0));
        assertEquals(Character.valueOf('A'), g.getDrumAt(1));
        assertEquals(Character.valueOf('B'), g.getDrumAt(2));
        assertEquals(Character.valueOf('C'), g.getDrumAt(3));
    }

    @Test
    public void testRotateDrumPositiveSteps() {
        PushbackGoalInterface<Character> g = buildDrumDABC(); // [D,A,B,C]
        g.rotateDrum(2);
        assertEquals(Character.valueOf('B'), g.getDrumAt(0));
        assertEquals(Character.valueOf('C'), g.getDrumAt(1));
        assertEquals(Character.valueOf('D'), g.getDrumAt(2));
        assertEquals(Character.valueOf('A'), g.getDrumAt(3));
    }

    @Test
    public void testRotateDrumNegativeSteps() {
        PushbackGoalInterface<Character> g = buildDrumDABC(); // [D,A,B,C]
        g.rotateDrum(-1);
        assertEquals(Character.valueOf('C'), g.getDrumAt(0));
        assertEquals(Character.valueOf('D'), g.getDrumAt(1));
        assertEquals(Character.valueOf('A'), g.getDrumAt(2));
        assertEquals(Character.valueOf('B'), g.getDrumAt(3));
    }

    // ------------------------------------------------------------------
    // removeAllOfValue
    // ------------------------------------------------------------------

    /**
     * Builds drum = [C, A, B, A] (opening at slot 0) via feed/rotate.
     */
    private PushbackGoalInterface<Character> buildDrumCABA() {
        PushbackGoalInterface<Character> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(4, '.');
        g.feedDrum('C'); g.rotateDrum(1); // [.,.,.,C]
        g.feedDrum('A'); g.rotateDrum(1); // [.,.,C,A]
        g.feedDrum('B'); g.rotateDrum(1); // [.,C,A,B]
        g.feedDrum('A');                  // [A,C,A,B]
        g.rotateDrum(1);                  // [C,A,B,A]
        return g;
    }

    @Test
    public void testRemoveAllOfColor() {
        PushbackGoalInterface<Character> g = buildDrumCABA(); // [C,A,B,A]
        int removed = g.removeAllOfValue('A');
        assertEquals(2, removed);
        // Net rotation over the whole method is a full cycle (capacity
        // rotations of 1 each), so the result is simply the original
        // array with every 'A' zeroed out in place.
        assertEquals(Character.valueOf('C'), g.getDrumAt(0));
        assertEquals(Character.valueOf('.'), g.getDrumAt(1));
        assertEquals(Character.valueOf('B'), g.getDrumAt(2));
        assertEquals(Character.valueOf('.'), g.getDrumAt(3));
    }

    @Test
    public void testRemoveAllOfColorNoMatches() {
        PushbackGoalInterface<Character> g = buildDrumCABA(); // [C,A,B,A]
        int removed = g.removeAllOfValue('Z');
        assertEquals(0, removed);
        assertEquals(Character.valueOf('C'), g.getDrumAt(0));
        assertEquals(Character.valueOf('A'), g.getDrumAt(1));
        assertEquals(Character.valueOf('B'), g.getDrumAt(2));
        assertEquals(Character.valueOf('A'), g.getDrumAt(3));
    }

    // ------------------------------------------------------------------
    // Tunnel: insertion is ONLY reachable via the drum bridge
    // ------------------------------------------------------------------

    private PushbackGoalInterface<Character> freshGoal(int drumCap, int tunnelCap) {
        PushbackGoalInterface<Character> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(drumCap, '.');
        g.initializeTunnel(tunnelCap, '.');
        return g;
    }

    @Test
    public void testLoadTunnelFromDrumFillsNearestGapFromMouth() {
        PushbackGoalInterface<Character> g = freshGoal(1, 5);

        g.feedDrum('A');
        assertTrue(g.loadTunnelFromDrum(TunnelEnd.LEFT));
        // [A,.,.,.,.]
        assertEquals(Character.valueOf('A'), g.getTunnelAt(0));

        g.feedDrum('B');
        assertTrue(g.loadTunnelFromDrum(TunnelEnd.LEFT));
        // nearest gap from left is index 1; A shifts right, B takes slot 0
        // [B,A,.,.,.]
        assertEquals(Character.valueOf('B'), g.getTunnelAt(0));
        assertEquals(Character.valueOf('A'), g.getTunnelAt(1));

        g.feedDrum('C');
        assertTrue(g.loadTunnelFromDrum(TunnelEnd.RIGHT));
        // nearest gap from right is index 4, placed directly
        // [B,A,.,.,C]
        assertEquals(Character.valueOf('C'), g.getTunnelAt(4));
        assertEquals(Character.valueOf('.'), g.getTunnelAt(2));

        g.feedDrum('D');
        assertTrue(g.loadTunnelFromDrum(TunnelEnd.RIGHT));
        // nearest gap from right is index 3; C shifts left, D takes slot 4
        // [B,A,.,C,D]
        assertEquals(Character.valueOf('C'), g.getTunnelAt(3));
        assertEquals(Character.valueOf('D'), g.getTunnelAt(4));
    }

    @Test
    public void testLoadTunnelFromDrumFailsWhenDrumEmpty() {
        PushbackGoalInterface<Character> g = freshGoal(1, 3);
        assertFalse(g.loadTunnelFromDrum(TunnelEnd.LEFT));
    }

    @Test
    public void testFullTunnelInsertionShiftsAndEvictsFarEnd() {
        PushbackGoalInterface<Character> g = freshGoal(1, 5);
        // Fill the tunnel completely: [E,B,A,C,D]
        String[] loads = {"A:LEFT", "B:LEFT", "C:RIGHT", "D:RIGHT", "E:LEFT"};
        for (String step : loads) {
            String[] parts = step.split(":");
            g.feedDrum(parts[0].charAt(0));
            g.loadTunnelFromDrum(TunnelEnd.valueOf(parts[1]));
        }
        // Confirm the tunnel is full (no dots)
        for (int i = 0; i < 5; i++) {
            assertNotEquals(Character.valueOf('.'), g.getTunnelAt(i));
        }

        g.feedDrum('F');
        assertTrue(g.loadTunnelFromDrum(TunnelEnd.LEFT));
        // Whole tunnel shifts right, rightmost (D) is evicted, F takes slot 0
        assertEquals(Character.valueOf('F'), g.getTunnelAt(0));
        for (int i = 0; i < 5; i++) {
            assertNotEquals(Character.valueOf('D'), g.getTunnelAt(i));
        }
    }

    @Test
    public void testInsertFailsWhenThatEndBlocked() {
        PushbackGoalInterface<Character> g = freshGoal(1, 3);
        g.setBlocked(TunnelEnd.LEFT, true);
        g.feedDrum('X');
        assertFalse(g.loadTunnelFromDrum(TunnelEnd.LEFT));
        // Cube must be returned to the drum, tunnel untouched
        assertEquals(Character.valueOf('X'), g.getDrumAt(0));
        assertEquals(Character.valueOf('.'), g.getTunnelAt(0));
    }

    @Test
    public void testInsertFailsWhenFullAndOppositeEndBlocked() {
        PushbackGoalInterface<Character> g = freshGoal(1, 3);
        // Fill tunnel completely from the left: [C,B,A]
        g.feedDrum('A'); g.loadTunnelFromDrum(TunnelEnd.LEFT);
        g.feedDrum('B'); g.loadTunnelFromDrum(TunnelEnd.LEFT);
        g.feedDrum('C'); g.loadTunnelFromDrum(TunnelEnd.LEFT);
        assertEquals(Character.valueOf('C'), g.getTunnelAt(0));
        assertEquals(Character.valueOf('B'), g.getTunnelAt(1));
        assertEquals(Character.valueOf('A'), g.getTunnelAt(2));

        g.setBlocked(TunnelEnd.LEFT, true);
        g.feedDrum('D');
        // Inserting from RIGHT would need to evict from LEFT -- blocked, so fails
        assertFalse(g.loadTunnelFromDrum(TunnelEnd.RIGHT));
        assertEquals(Character.valueOf('D'), g.getDrumAt(0)); // returned to drum
        assertEquals(Character.valueOf('C'), g.getTunnelAt(0)); // tunnel unchanged
    }

    // ------------------------------------------------------------------
    // sweepAt (arm sweep)
    // ------------------------------------------------------------------

    private PushbackGoalInterface<Character> tunnelCBA() {
        PushbackGoalInterface<Character> g = freshGoal(1, 3);
        g.feedDrum('A'); g.loadTunnelFromDrum(TunnelEnd.LEFT);
        g.feedDrum('B'); g.loadTunnelFromDrum(TunnelEnd.LEFT);
        g.feedDrum('C'); g.loadTunnelFromDrum(TunnelEnd.LEFT);
        return g; // [C,B,A]
    }

    @Test
    public void testSweepAtTowardLeft() {
        PushbackGoalInterface<Character> g = tunnelCBA(); // [C,B,A]
        int cleared = g.sweepAt(1, TunnelEnd.LEFT);
        assertEquals(2, cleared); // C and B cleared
        assertEquals(Character.valueOf('.'), g.getTunnelAt(0));
        assertEquals(Character.valueOf('.'), g.getTunnelAt(1));
        assertEquals(Character.valueOf('A'), g.getTunnelAt(2)); // untouched
    }

    @Test
    public void testSweepAtTowardRight() {
        PushbackGoalInterface<Character> g = tunnelCBA(); // [C,B,A]
        int cleared = g.sweepAt(1, TunnelEnd.RIGHT);
        assertEquals(2, cleared); // B and A cleared
        assertEquals(Character.valueOf('C'), g.getTunnelAt(0)); // untouched
        assertEquals(Character.valueOf('.'), g.getTunnelAt(1));
        assertEquals(Character.valueOf('.'), g.getTunnelAt(2));
    }

    @Test
    public void testSweepAtFailsWhenTargetEndBlocked() {
        PushbackGoalInterface<Character> g = tunnelCBA(); // [C,B,A]
        g.setBlocked(TunnelEnd.RIGHT, true);
        int cleared = g.sweepAt(1, TunnelEnd.RIGHT);
        assertEquals(0, cleared);
        assertEquals(Character.valueOf('C'), g.getTunnelAt(0));
        assertEquals(Character.valueOf('B'), g.getTunnelAt(1));
        assertEquals(Character.valueOf('A'), g.getTunnelAt(2));
    }

    @Test
    public void testStuckCubeFreedThroughOtherEnd() {
        PushbackGoalInterface<Character> g = tunnelCBA(); // [C,B,A]
        g.setBlocked(TunnelEnd.LEFT, true);

        // Sweeping toward the blocked LEFT end fails entirely
        assertEquals(0, g.sweepAt(0, TunnelEnd.LEFT));
        assertEquals(Character.valueOf('C'), g.getTunnelAt(0));

        // But sweeping the same slot toward the unblocked RIGHT end works
        int cleared = g.sweepAt(0, TunnelEnd.RIGHT);
        assertEquals(3, cleared); // C, B, A all cleared
        for (int i = 0; i < 3; i++) {
            assertEquals(Character.valueOf('.'), g.getTunnelAt(i));
        }
    }

    /**
     * Builds tunnel = [C, A, ., B, D] (capacity 5), which has a gap
     * sandwiched between two occupied cells -- possible because the
     * cubes were fed in from alternating ends.
     */
    private PushbackGoalInterface<Character> tunnelCAgapBD() {
        PushbackGoalInterface<Character> g = freshGoal(1, 5);
        g.feedDrum('A'); g.loadTunnelFromDrum(TunnelEnd.LEFT);  // [A,.,.,.,.]
        g.feedDrum('B'); g.loadTunnelFromDrum(TunnelEnd.RIGHT); // [A,.,.,.,B]
        g.feedDrum('C'); g.loadTunnelFromDrum(TunnelEnd.LEFT);  // [C,A,.,.,B]
        g.feedDrum('D'); g.loadTunnelFromDrum(TunnelEnd.RIGHT); // [C,A,.,B,D]
        return g;
    }

    @Test
    public void testSweepAtBlockedCompactsTowardRightMouth() {
        PushbackGoalInterface<Character> g = tunnelCAgapBD(); // [C,A,.,B,D]
        g.setBlocked(TunnelEnd.RIGHT, true);

        int cleared = g.sweepAt(1, TunnelEnd.RIGHT);
        assertEquals(0, cleared); // nothing actually removed

        // Range [1,4] = [A,.,B,D] compacts toward the mouth (index 4):
        // D and B were already flush; A slides in next to B, and the
        // one gap ends up at the near edge of the range (index 1).
        assertEquals(Character.valueOf('C'), g.getTunnelAt(0)); // untouched, outside range
        assertEquals(Character.valueOf('.'), g.getTunnelAt(1));
        assertEquals(Character.valueOf('A'), g.getTunnelAt(2));
        assertEquals(Character.valueOf('B'), g.getTunnelAt(3));
        assertEquals(Character.valueOf('D'), g.getTunnelAt(4));
    }

    /**
     * Builds tunnel = [D, B, ., A, C] (capacity 5) -- the mirror image
     * of tunnelCAgapBD(), for testing compaction toward the LEFT mouth.
     */
    private PushbackGoalInterface<Character> tunnelDBgapAC() {
        PushbackGoalInterface<Character> g = freshGoal(1, 5);
        g.feedDrum('A'); g.loadTunnelFromDrum(TunnelEnd.RIGHT); // [.,.,.,.,A]
        g.feedDrum('B'); g.loadTunnelFromDrum(TunnelEnd.LEFT);  // [B,.,.,.,A]
        g.feedDrum('C'); g.loadTunnelFromDrum(TunnelEnd.RIGHT); // [B,.,.,A,C]
        g.feedDrum('D'); g.loadTunnelFromDrum(TunnelEnd.LEFT);  // [D,B,.,A,C]
        return g;
    }

    @Test
    public void testSweepAtBlockedCompactsTowardLeftMouth() {
        PushbackGoalInterface<Character> g = tunnelDBgapAC(); // [D,B,.,A,C]
        g.setBlocked(TunnelEnd.LEFT, true);

        int cleared = g.sweepAt(3, TunnelEnd.LEFT);
        assertEquals(0, cleared); // nothing actually removed

        // Range [0,3] = [D,B,.,A] compacts toward the mouth (index 0):
        // D and B were already flush; A slides in next to B, and the
        // one gap ends up at the near edge of the range (index 3).
        assertEquals(Character.valueOf('D'), g.getTunnelAt(0));
        assertEquals(Character.valueOf('B'), g.getTunnelAt(1));
        assertEquals(Character.valueOf('A'), g.getTunnelAt(2));
        assertEquals(Character.valueOf('.'), g.getTunnelAt(3));
        assertEquals(Character.valueOf('C'), g.getTunnelAt(4)); // untouched, outside range
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSweepAtOutOfBounds() {
        PushbackGoalInterface<Character> g = freshGoal(1, 3);
        g.sweepAt(3, TunnelEnd.LEFT);
    }

    // ------------------------------------------------------------------
    // determineControl
    // ------------------------------------------------------------------

    @Test
    public void testDetermineControlMajority() {
        PushbackGoalInterface<Character> g = freshGoal(1, 5);
        g.feedDrum('R'); g.loadTunnelFromDrum(TunnelEnd.LEFT); // [R,.,.,.,.]
        g.feedDrum('R'); g.loadTunnelFromDrum(TunnelEnd.LEFT); // [R,R,.,.,.]
        g.feedDrum('B'); g.loadTunnelFromDrum(TunnelEnd.LEFT); // [B,R,R,.,.]

        assertEquals(Character.valueOf('R'), g.determineControl(0, 2));
    }

    @Test
    public void testDetermineControlTieReturnsNull() {
        PushbackGoalInterface<Character> g = freshGoal(1, 3);
        g.feedDrum('R'); g.loadTunnelFromDrum(TunnelEnd.LEFT);  // [R,.,.]
        g.feedDrum('B'); g.loadTunnelFromDrum(TunnelEnd.RIGHT); // [R,.,B]

        assertNull(g.determineControl(0, 2));
    }

    @Test
    public void testDetermineControlEmptyZoneReturnsNull() {
        PushbackGoalInterface<Character> g = freshGoal(1, 5);
        assertNull(g.determineControl(0, 4));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDetermineControlInvalidRange() {
        PushbackGoalInterface<Character> g = freshGoal(1, 5);
        g.determineControl(3, 1); // start > end
    }

    // ------------------------------------------------------------------
    // dumpDrumIntoTunnel
    // ------------------------------------------------------------------

    @Test
    public void testDumpDrumIntoTunnelTransfersEntireDrum() {
        PushbackGoalInterface<Character> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(4, '.');
        g.initializeTunnel(5, '.');

        // Build drum = [D,A,B,C]
        g.feedDrum('A'); g.rotateDrum(1);
        g.feedDrum('B'); g.rotateDrum(1);
        g.feedDrum('C'); g.rotateDrum(1);
        g.feedDrum('D');

        int transferred = g.dumpDrumIntoTunnel(TunnelEnd.LEFT);
        assertEquals(4, transferred);

        // Drum should be completely empty afterward
        for (int i = 0; i < 4; i++) {
            assertEquals(Character.valueOf('.'), g.getDrumAt(i));
        }

        // Tunnel ends up [C,B,A,D,.] per hand-traced simulation
        assertEquals(Character.valueOf('C'), g.getTunnelAt(0));
        assertEquals(Character.valueOf('B'), g.getTunnelAt(1));
        assertEquals(Character.valueOf('A'), g.getTunnelAt(2));
        assertEquals(Character.valueOf('D'), g.getTunnelAt(3));
        assertEquals(Character.valueOf('.'), g.getTunnelAt(4));
    }

    @Test
    public void testDumpDrumIntoTunnelStopsIfBlocked() {
        PushbackGoalInterface<Character> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(4, '.');
        g.initializeTunnel(3, '.');
        g.setBlocked(TunnelEnd.LEFT, true);
        g.feedDrum('X');

        int transferred = g.dumpDrumIntoTunnel(TunnelEnd.LEFT);
        assertEquals(0, transferred);
        // The cube must remain in the drum, untouched
        assertEquals(Character.valueOf('X'), g.getDrumAt(0));
    }

    // ------------------------------------------------------------------
    // Genericity: T isn't limited to Character
    // ------------------------------------------------------------------

    @Test
    public void testDrumWithIntegerType() {
        PushbackGoalInterface<Integer> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(3, 0); // 0 represents an empty slot here

        g.feedDrum(5);
        assertEquals(Integer.valueOf(5), g.getDrumAt(0));

        // rotate toward the opening: slot 1 (empty) arrives at the opening
        g.rotateDrum(1);
        assertEquals(Integer.valueOf(0), g.getDrumAt(0));
        assertEquals(Integer.valueOf(5), g.getDrumAt(2));

        try {
            g.ejectDrum();
            fail("Expected IllegalStateException ejecting an empty opening");
        } catch (IllegalStateException expected) {
            // correct
        }

        // rotate the 5 back around to the opening
        g.rotateDrum(2);
        assertEquals(Integer.valueOf(5), g.getDrumAt(0));
        assertEquals(Integer.valueOf(5), g.ejectDrum());
    }

    @Test
    public void testTunnelAndControlZoneWithStringType() {
        PushbackGoalInterface<String> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(1, "EMPTY");
        g.initializeTunnel(3, "EMPTY");

        g.feedDrum("RED");
        g.loadTunnelFromDrum(TunnelEnd.LEFT); // [RED, EMPTY, EMPTY]

        g.feedDrum("RED");
        g.loadTunnelFromDrum(TunnelEnd.LEFT); // nearest gap at index 1 -> [RED, RED, EMPTY]

        g.feedDrum("BLUE");
        g.loadTunnelFromDrum(TunnelEnd.RIGHT); // nearest gap at index 2 -> [RED, RED, BLUE]

        assertEquals("RED", g.getTunnelAt(0));
        assertEquals("RED", g.getTunnelAt(1));
        assertEquals("BLUE", g.getTunnelAt(2));
        assertEquals("RED", g.determineControl(0, 2)); // 2 of 3 slots
    }

    @Test
    public void testRemoveAllOfValueWithStringType() {
        PushbackGoalInterface<String> g = new PushbackGoalLinkedListImpl<>();
        g.initializeDrum(3, "EMPTY");

        // Build drum = [RED, RED, BLUE] via feed/rotate
        g.feedDrum("RED"); g.rotateDrum(1); // [EMPTY, EMPTY, RED]
        g.feedDrum("BLUE"); g.rotateDrum(1); // [EMPTY, RED, BLUE]
        g.feedDrum("RED"); // [RED, RED, BLUE]

        int removed = g.removeAllOfValue("RED");
        assertEquals(2, removed);
        // Net rotation is a full cycle, so this is the original array
        // with every "RED" zeroed out in place.
        assertEquals("EMPTY", g.getDrumAt(0));
        assertEquals("EMPTY", g.getDrumAt(1));
        assertEquals("BLUE", g.getDrumAt(2));
    }
}