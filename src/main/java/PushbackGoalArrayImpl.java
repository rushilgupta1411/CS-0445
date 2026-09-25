// PushbackGoalArrayImpl.java
//
// TODO: Implement every method below. Only plain Java arrays may be
// used to store the drum's and tunnel's contents -- no ArrayList,
// LinkedList, HashMap, Queue, Deque, Stack, or any other java.util
// container class.

/**
 * Array-based implementation of a drum + tunnel pushback scoring goal.
 *
 * You are free to choose your internal representation for each part
 * (e.g. a single backing array plus an "opening index" for the drum) as
 * long as it is built entirely from plain Java arrays.
 */
public class PushbackGoalArrayImpl<T> implements PushbackGoalInterface<T> {

    // TODO: declare your fields here (backing arrays, capacities,
    // emptyValue(s), an "opening index" for the drum, blocked flags for
    // the tunnel's two ends, initialized flags, etc.)

    // ------------------------------------------------------------------
    // Drum
    // ------------------------------------------------------------------

    @Override
    public void initializeDrum(int capacity, T emptyValue) {
        // TODO
        throw new UnsupportedOperationException("initializeDrum() not implemented yet");
    }

    @Override
    public boolean feedDrum(T value) {
        // TODO
        throw new UnsupportedOperationException("feedDrum() not implemented yet");
    }

    @Override
    public T ejectDrum() {
        // TODO
        throw new UnsupportedOperationException("ejectDrum() not implemented yet");
    }

    @Override
    public void rotateDrum(int steps) {
        // TODO
        throw new UnsupportedOperationException("rotateDrum() not implemented yet");
    }

    @Override
    public T getDrumAt(int index) {
        // TODO
        throw new UnsupportedOperationException("getDrumAt() not implemented yet");
    }

    @Override
    public int drumCapacity() {
        // TODO
        throw new UnsupportedOperationException("drumCapacity() not implemented yet");
    }

    @Override
    public int removeAllOfValue(T value) {
        // TODO: implement using ONLY rotateDrum(1), feedDrum(), and
        // ejectDrum() -- no direct indexing into your drum array here.
        throw new UnsupportedOperationException("removeAllOfValue() not implemented yet");
    }

    // ------------------------------------------------------------------
    // Tunnel
    // ------------------------------------------------------------------

    @Override
    public void initializeTunnel(int capacity, T emptyValue) {
        // TODO
        throw new UnsupportedOperationException("initializeTunnel() not implemented yet");
    }

    @Override
    public void setBlocked(TunnelEnd end, boolean blocked) {
        // TODO
        throw new UnsupportedOperationException("setBlocked() not implemented yet");
    }

    @Override
    public boolean isBlocked(TunnelEnd end) {
        // TODO
        throw new UnsupportedOperationException("isBlocked() not implemented yet");
    }

    @Override
    public int sweepAt(int index, TunnelEnd sweepToward) {
        // TODO
        throw new UnsupportedOperationException("sweepAt() not implemented yet");
    }

    @Override
    public T getTunnelAt(int index) {
        // TODO
        throw new UnsupportedOperationException("getTunnelAt() not implemented yet");
    }

    @Override
    public int tunnelCapacity() {
        // TODO
        throw new UnsupportedOperationException("tunnelCapacity() not implemented yet");
    }

    @Override
    public T determineControl(int startIndex, int endIndex) {
        // TODO
        throw new UnsupportedOperationException("determineControl() not implemented yet");
    }

    // ------------------------------------------------------------------
    // Bridge
    // ------------------------------------------------------------------

    @Override
    public boolean loadTunnelFromDrum(TunnelEnd end) {
        // TODO
        throw new UnsupportedOperationException("loadTunnelFromDrum() not implemented yet");
    }

    @Override
    public int dumpDrumIntoTunnel(TunnelEnd end) {
        // TODO
        throw new UnsupportedOperationException("dumpDrumIntoTunnel() not implemented yet");
    }
}