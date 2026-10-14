/**
 * Fixed-capacity goal using a circular drum and a linear tunnel.
 * Both structures share one doubly-linked node type.
 */
public class PushbackGoalLinkedListImpl<T> implements PushbackGoalInterface<T> {
    private static class Node<E> {
        private E value;
        private Node<E> next;
        private Node<E> prev;
        private Node(E value) { this.value = value; }
    }

    private Node<T> opening;
    private int drumSize;
    private T drumEmpty;
    private Node<T> tunnelHead;
    private Node<T> tunnelTail;
    private int tunnelSize;
    private T tunnelEmpty;
    private boolean leftBlocked;
    private boolean rightBlocked;

    @Override
    public void initializeDrum(int capacity, T emptyValue) {
        requirePositiveCapacity(capacity);
        Node<T> first = new Node<>(emptyValue);
        Node<T> last = first;
        for (int i = 1; i < capacity; i++) {
            Node<T> node = new Node<>(emptyValue);
            last.next = node;
            node.prev = last;
            last = node;
        }
        last.next = first;
        first.prev = last;
        opening = first;
        drumSize = capacity;
        drumEmpty = emptyValue;
    }

    @Override
    public boolean feedDrum(T value) {
        requireDrum();
        if (!sameValue(opening.value, drumEmpty)) {
            return false;
        }
        opening.value = value;
        return true;
    }

    @Override
    public T ejectDrum() {
        requireDrum();
        if (sameValue(opening.value, drumEmpty)) {
            throw new IllegalStateException("The drum opening is empty.");
        }
        T value = opening.value;
        opening.value = drumEmpty;
        return value;
    }

    @Override
    public void rotateDrum(int steps) {
        requireDrum();
        // Normalize first, including extreme negative int rotations.
        int forward = Math.floorMod(steps, drumSize);
        for (int i = 0; i < forward; i++) {
            opening = opening.next;
        }
    }

    @Override
    public T getDrumAt(int index) {
        requireDrum();
        requireIndex(index, drumSize);
        Node<T> node = opening;
        for (int i = 0; i < index; i++) {
            node = node.next;
        }
        return node.value;
    }

    @Override
    public int drumCapacity() {
        requireDrum();
        return drumSize;
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
    public void initializeTunnel(int capacity, T emptyValue) {
        requirePositiveCapacity(capacity);
        Node<T> first = new Node<>(emptyValue);
        Node<T> last = first;
        for (int i = 1; i < capacity; i++) {
            Node<T> node = new Node<>(emptyValue);
            last.next = node;
            node.prev = last;
            last = node;
        }
        tunnelHead = first;
        tunnelTail = last;
        tunnelSize = capacity;
        tunnelEmpty = emptyValue;
        leftBlocked = false;
        rightBlocked = false;
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
        requireIndex(index, tunnelSize);
        requireEnd(sweepToward);
        boolean fromLeft = sweepToward == TunnelEnd.LEFT;
        int count = fromLeft ? index + 1 : tunnelSize - index;
        Node<T> read = fromLeft ? tunnelHead : tunnelTail;
        Node<T> write = read;
        boolean blocked = isBlocked(sweepToward);
        int removed = 0;
        for (int i = 0; i < count; i++) {
            if (!sameValue(read.value, tunnelEmpty)) {
                if (blocked) {
                    // Pack toward the mouth in order, swapping values only.
                    // The write reference never crosses an unread cube.
                    T gap = write.value;
                    write.value = read.value;
                    read.value = gap;
                    write = inward(write, sweepToward);
                } else {
                    read.value = tunnelEmpty;
                    removed++;
                }
            }
            read = inward(read, sweepToward);
        }
        return removed;
    }

    @Override
    public T getTunnelAt(int index) {
        requireTunnel();
        requireIndex(index, tunnelSize);
        return tunnelNodeAt(index).value;
    }

    @Override
    public int tunnelCapacity() {
        requireTunnel();
        return tunnelSize;
    }

    @Override
    public T determineControl(int startIndex, int endIndex) {
        requireTunnel();
        requireIndex(startIndex, tunnelSize);
        requireIndex(endIndex, tunnelSize);
        if (startIndex > endIndex) {
            throw new IndexOutOfBoundsException("Control range is reversed.");
        }
        Node<T> start = tunnelNodeAt(startIndex);
        Node<T> node = start;
        T candidate = null;
        int balance = 0;
        int occupied = 0;
        // Cancel unlike pairs to find the only possible majority candidate.
        for (int i = startIndex; i <= endIndex; i++, node = node.next) {
            T cube = node.value;
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
        // Verify the candidate; cancellation alone does not prove a majority.
        int frequency = 0;
        node = start;
        for (int i = startIndex; i <= endIndex; i++, node = node.next) {
            if (!sameValue(node.value, tunnelEmpty) && sameValue(node.value, candidate)) {
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
        if (isBlocked(end) || sameValue(opening.value, drumEmpty)) {
            return false;
        }
        Node<T> target = end == TunnelEnd.LEFT ? tunnelHead : tunnelTail;
        while (target != null && !sameValue(target.value, tunnelEmpty)) {
            target = inward(target, end);
        }
        if (target == null) {
            TunnelEnd opposite = end == TunnelEnd.LEFT ? TunnelEnd.RIGHT : TunnelEnd.LEFT;
            if (isBlocked(opposite)) {
                return false;
            }
            target = end == TunnelEnd.LEFT ? tunnelTail : tunnelHead;
        }
        // Reuse the nearest gap or far-end eviction node in the same way.
        // Relinking shifts the intervening chain without allocating any nodes.
        detachTunnelNode(target);
        target.value = ejectDrum();
        attachAtMouth(target, end);
        return true;
    }

    @Override
    public int dumpDrumIntoTunnel(TunnelEnd end) {
        requireDrum();
        requireTunnel();
        requireEnd(end);
        int transferred = 0;
        for (int i = 0; i < drumCapacity(); i++) {
            if (!sameValue(opening.value, drumEmpty)) {
                if (!loadTunnelFromDrum(end)) {
                    // Leave a rejected cube at the opening without rotating.
                    break;
                }
                transferred++;
            }
            rotateDrum(1);
        }
        return transferred;
    }

    private Node<T> inward(Node<T> node, TunnelEnd mouth) {
        return mouth == TunnelEnd.LEFT ? node.next : node.prev;
    }

    private Node<T> tunnelNodeAt(int index) {
        Node<T> node = tunnelHead;
        for (int i = 0; i < index; i++) {
            node = node.next;
        }
        return node;
    }

    private void detachTunnelNode(Node<T> node) {
        if (node.prev == null) {
            tunnelHead = node.next;
        } else {
            node.prev.next = node.next;
        }
        if (node.next == null) {
            tunnelTail = node.prev;
        } else {
            node.next.prev = node.prev;
        }
        node.next = null;
        node.prev = null;
    }

    private void attachAtMouth(Node<T> node, TunnelEnd end) {
        // Detaching a capacity-one tunnel temporarily leaves both ends null.
        if (tunnelHead == null) {
            tunnelHead = node;
            tunnelTail = node;
        } else if (end == TunnelEnd.LEFT) {
            node.next = tunnelHead;
            tunnelHead.prev = node;
            tunnelHead = node;
        } else {
            node.prev = tunnelTail;
            tunnelTail.next = node;
            tunnelTail = node;
        }
    }

    private boolean sameValue(T first, T second) {
        return first == null ? second == null : first.equals(second);
    }

    private void requireDrum() {
        if (opening == null) {
            throw new IllegalStateException("Initialize the drum first.");
        }
    }

    private void requireTunnel() {
        if (tunnelHead == null) {
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
