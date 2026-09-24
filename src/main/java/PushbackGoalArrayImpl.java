/**
 * Fixed-capacity drum and tunnel implemented using plain Java arrays.
 * The drum opening is an offset into its ring; tunnel indices run left to right.
 */
public class PushbackGoalArrayImpl<T> implements PushbackGoalInterface<T> {
    private T[] drum;
    private T drumEmpty;
    private int opening;
    private T[] tunnel;
    private T tunnelEmpty;
    private boolean leftBlocked;
    private boolean rightBlocked;

    @Override
    @SuppressWarnings("unchecked")
    public void initializeDrum(int capacity, T emptyValue) {
        requirePositiveCapacity(capacity);
        drum = (T[]) new Object[capacity];
        drumEmpty = emptyValue;
        opening = 0;
        for (int i = 0; i < capacity; i++) {
            drum[i] = emptyValue;
        }
    }

    @Override
    public boolean feedDrum(T value) {
        requireDrum();
        if (!sameValue(drum[opening], drumEmpty)) {
            return false;
        }
        drum[opening] = value;
        return true;
    }

    @Override
    public T ejectDrum() {
        requireDrum();
        if (sameValue(drum[opening], drumEmpty)) {
            throw new IllegalStateException("The drum opening is empty.");
        }
        T value = drum[opening];
        drum[opening] = drumEmpty;
        return value;
    }

    @Override
    public void rotateDrum(int steps) {
        requireDrum();
        // Use long arithmetic so even extreme int rotations cannot overflow.
        opening = (int) Math.floorMod((long) opening + steps, (long) drum.length);
    }

    @Override
    public T getDrumAt(int index) {
        requireDrum();
        requireIndex(index, drum.length);
        return drum[(int) (((long) opening + index) % drum.length)];
    }

    @Override
    public int drumCapacity() {
        requireDrum();
        return drum.length;
    }

    @Override
    public int removeAllOfValue(T value) {
        int capacity = drumCapacity();
        int removed = 0;
        for (int i = 0; i < capacity; i++) {
            T cube;
            try {
                cube = ejectDrum();
            } catch (IllegalStateException emptyOpening) {
                // Empty slots still participate in the complete rotation.
                rotateDrum(1);
                continue;
            }
            if (sameValue(cube, value)) {
                removed++;
            } else {
                feedDrum(cube);
            }
            rotateDrum(1);
        }
        return removed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initializeTunnel(int capacity, T emptyValue) {
        requirePositiveCapacity(capacity);
        tunnel = (T[]) new Object[capacity];
        tunnelEmpty = emptyValue;
        leftBlocked = false;
        rightBlocked = false;
        for (int i = 0; i < capacity; i++) {
            tunnel[i] = emptyValue;
        }
    }

    @Override
    public void setBlocked(TunnelEnd end, boolean blocked) {
        requireTunnel();
        requireEnd(end);
        if (end == TunnelEnd.LEFT) {
            leftBlocked = blocked;
        } else {
            rightBlocked = blocked;
        }
    }

    @Override
    public boolean isBlocked(TunnelEnd end) {
        requireTunnel();
        requireEnd(end);
        return end == TunnelEnd.LEFT ? leftBlocked : rightBlocked;
    }

    @Override
    public int sweepAt(int index, TunnelEnd sweepToward) {
        requireTunnel();
        requireIndex(index, tunnel.length);
        requireEnd(sweepToward);
        int mouth = sweepToward == TunnelEnd.LEFT ? 0 : tunnel.length - 1;
        int step = sweepToward == TunnelEnd.LEFT ? 1 : -1;
        int stop = index + step;
        int removed = 0;
        if (!isBlocked(sweepToward)) {
            for (int i = mouth; i != stop; i += step) {
                if (!sameValue(tunnel[i], tunnelEmpty)) {
                    removed++;
                    tunnel[i] = tunnelEmpty;
                }
            }
        } else {
            // Read from the blocked mouth inward, packing in the same order.
            // The write position never crosses an unread occupied slot.
            int write = mouth;
            for (int read = mouth; read != stop; read += step) {
                if (!sameValue(tunnel[read], tunnelEmpty)) {
                    tunnel[write] = tunnel[read];
                    write += step;
                }
            }
            for (; write != stop; write += step) {
                tunnel[write] = tunnelEmpty;
            }
        }
        return removed;
    }

    @Override
    public T getTunnelAt(int index) {
        requireTunnel();
        requireIndex(index, tunnel.length);
        return tunnel[index];
    }

    @Override
    public int tunnelCapacity() {
        requireTunnel();
        return tunnel.length;
    }

    @Override
    public T determineControl(int startIndex, int endIndex) {
        requireTunnel();
        requireIndex(startIndex, tunnel.length);
        requireIndex(endIndex, tunnel.length);
        if (startIndex > endIndex) {
            throw new IndexOutOfBoundsException("Control range is reversed.");
        }
        // Cancel unlike pairs to find the only possible majority candidate.
        T candidate = null;
        int balance = 0;
        int occupied = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            T cube = tunnel[i];
            if (!sameValue(cube, tunnelEmpty)) {
                occupied++;
                if (balance == 0) {
                    candidate = cube;
                    balance = 1;
                } else if (sameValue(candidate, cube)) {
                    balance++;
                } else {
                    balance--;
                }
            }
        }
        // A candidate need not be a majority, so verify its actual frequency.
        int frequency = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            if (!sameValue(tunnel[i], tunnelEmpty) && sameValue(tunnel[i], candidate)) {
                frequency++;
            }
        }
        return frequency > occupied / 2 ? candidate : null;
    }

    @Override
    public boolean loadTunnelFromDrum(TunnelEnd end) {
        requireDrum();
        requireTunnel();
        requireEnd(end);
        if (sameValue(drum[opening], drumEmpty)) {
            return false;
        }
        T cube = ejectDrum();
        if (isBlocked(end)) {
            feedDrum(cube);
            return false;
        }
        int mouth = end == TunnelEnd.LEFT ? 0 : tunnel.length - 1;
        int step = end == TunnelEnd.LEFT ? 1 : -1;
        int gap = mouth;
        while (gap >= 0 && gap < tunnel.length && !sameValue(tunnel[gap], tunnelEmpty)) {
            gap += step;
        }
        if (gap < 0 || gap >= tunnel.length) {
            TunnelEnd opposite = end == TunnelEnd.LEFT ? TunnelEnd.RIGHT : TunnelEnd.LEFT;
            if (isBlocked(opposite)) {
                feedDrum(cube);
                return false;
            }
            // A full tunnel can accept a cube only by evicting the far end.
            gap = end == TunnelEnd.LEFT ? tunnel.length - 1 : 0;
        }
        for (int i = gap; i != mouth; i -= step) {
            tunnel[i] = tunnel[i - step];
        }
        tunnel[mouth] = cube;
        return true;
    }

    @Override
    public int dumpDrumIntoTunnel(TunnelEnd end) {
        requireDrum();
        requireTunnel();
        requireEnd(end);
        int transferred = 0;
        for (int i = 0; i < drumCapacity(); i++) {
            if (!sameValue(drum[opening], drumEmpty)) {
                if (!loadTunnelFromDrum(end)) {
                    // Keep the rejected cube at the opening; do not rotate.
                    break;
                }
                transferred++;
            }
            rotateDrum(1);
        }
        return transferred;
    }

    private boolean sameValue(T first, T second) {
        return first == null ? second == null : first.equals(second);
    }

    private void requireDrum() {
        if (drum == null) {
            throw new IllegalStateException("Initialize the drum first.");
        }
    }

    private void requireTunnel() {
        if (tunnel == null) {
            throw new IllegalStateException("Initialize the tunnel first.");
        }
    }

    private void requirePositiveCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
    }

    private void requireIndex(int index, int capacity) {
        if (index < 0 || index >= capacity) {
            throw new IndexOutOfBoundsException("Index outside the goal.");
        }
    }

    private void requireEnd(TunnelEnd end) {
        if (end == null) {
            throw new IllegalArgumentException("A tunnel end is required.");
        }
    }
}
