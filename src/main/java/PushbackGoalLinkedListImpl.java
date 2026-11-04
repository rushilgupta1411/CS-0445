// PushbackGoalLinkedListImpl.java
//
// TODO: Implement every method below. This is the EXACT SAME interface
// as Assignment 1 -- the observable behavior of every method must be
// identical. What's different is the restriction: you may NOT use a
// plain Java array (or any java.util container class) as your
// storage. You must build your own linked list out of nodes (a
// private nested class with a value and next/prev references is the
// standard approach) and implement every operation -- including
// indexed access like getDrumAt()/getTunnelAt() -- by traversing that
// list, not by indexing into an array.

public class PushbackGoalLinkedListImpl<T> implements PushbackGoalInterface<T> {

    // TODO: declare your fields here. You'll need a node class (make
    // it a private static nested class, with a value
    // field and next/prev references) plus whatever head/tail/opening
    // references and capacity/emptyValue/initialized flags you need
    // for the drum and tunnel.

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
        // TODO: think about this in terms of moving a reference to
        // "which node is currently the opening" rather than moving
        // data around, the way the array version had to.
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
        // ejectDrum() -- no direct access to your node references here.
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