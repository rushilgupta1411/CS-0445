/**
 * TEST CODE (not part of CuckooPushbackTable): the standard SplitMix64 pseudo-random generator.
 *
 * Given a starting number (the seed) it produces a stream of 64-bit numbers that look random but are
 * completely determined by the seed. The tests use it to turn a small, human-friendly seed such as
 * 1, 2 or 42 into the two coefficients (a, b) of a hash function: see TestHelpers.coefficients.
 *
 * One step:  state += GOLDEN; then scramble the state with two "xor-shift then multiply" rounds
 * and one last xor-shift. GOLDEN = 0x9E3779B97F4A7C15 is 2^64 divided by the golden ratio.
 */
public final class SplitMix64 {

    private static final long GOLDEN = 0x9E3779B97F4A7C15L;

    private long state;

    public SplitMix64(long seed) {
        this.state = seed;
    }

    public long nextLong() {
        state += GOLDEN;                                     // move the state forward by a fixed odd constant
        long z = state;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;          // scramble: fold high bits onto low bits, then multiply
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;          // scramble again
        return z ^ (z >>> 31);                               // one last mix
    }
}
