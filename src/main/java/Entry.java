// Entry.java - PROVIDED. Do not modify.

/**
 * A key/value pair stored in a bucket or in the stash.
 */
public final class Entry<K, V> {

    private final K key;
    private V value;

    public Entry(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("null keys are not allowed");
        }
        this.key = key;
        this.value = value;
    }

    public K getKey() { return key; }

    public V getValue() { return value; }

    public void setValue(V value) { this.value = value; }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
