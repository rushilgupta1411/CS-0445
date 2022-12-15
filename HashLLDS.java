/** A HashLLDS is a hybrid of a linear-probing hash table and a linked list.
* It allows hash table access, and FIFO sequential access.
* @author Sherif Khattab
**/

import java.util.Iterator;
import java.util.NoSuchElementException;

public class HashLLDS<V> implements Iterable<V> {
  private static final int INIT_CAPACITY = 13;

  private int N;           // number of key-value pairs in the data structure
  private int M;           // size of hash table
  private Node<V>[] entries;  // the hash table
  private Node<V> head;       // head of the linked list
  private Node<V> tail;       // tail of the linked list

  // create an empty hash table - use 13 as default size
  public HashLLDS() {
    this(INIT_CAPACITY);
  }

  // create a HashLLDS of given prime capacity
  public HashLLDS(int capacity) {
    M = capacity;
    @SuppressWarnings("unchecked")
    Node<V>[] temp = (Node<V>[]) new Node<?>[M];
    entries = temp;
    head = tail = null;
    N = 0;
  }

  public Iterator<V> iterator() {
    return new MyIterator();
  }


  // insert the key-value pair into the hash table
  public void put(String key, V val) {
    // double table size if 50% full
    if (N >= M/2) resize(2*M);

    boolean found = false;

    // linear probing
    int i;
    for (i = hash(key); entries[i] != null; i = (i + 1) % M) {
      // update the value if key already exists
      if (entries[i].key.equals(key)) {
        entries[i].value = val; 
        found = true;
      }
    }

    if(!found){
      // at an empty entry
      entries[i] = new Node<V>(key, val);

      //TODO: insert the node entries[i] into the tail of the linked list in O(1) time.
      //Don't forget the edge case of inserting into an empty linked list
      
      N++;
    }

  }

  // return the value associated with the given key, null if no such value
  public V get(String key) {
    for (int i = hash(key); entries[i] != null; i = (i + 1) % M)
      if (entries[i].key.equals(key))
        return entries[i].value;
    return null;
  }

  // hash function for keys - returns value between 0 and M-1
  private int hash(String key) {
    return (key.hashCode() & 0x7fffffff) % M;
  }

  // resize the hash table to the given capacity by re-hashing all of the keys
  private void resize(int capacity) {
    HashLLDS<V> temp = new HashLLDS<V>(capacity);

    //TODO: reinsert the entries from this into temp in original FIFO order
    //You will need to iterate over the linked list of the this object and call
    //put on the temp object.
    
    
    entries = temp.entries;
    head    = temp.head;
    tail    = temp.tail;
    M       = temp.M;
  }

  public int size(){
    return N;
  }


  //A class to store nodes of a singly-linked list
  //Each node contains a (key, value) pair
  private static class Node<T> {
    private String key;
    private T value;
    private Node<T> next;

    Node(String key, T value){
      this(key, value, null);
    }

    Node(String key, T value, Node<T> next){
      this.key = key;
      this.value = value;
      this.next = next;
    }
  }


  public class MyIterator implements Iterator<V> {
    private Node<V> nextNode;

    public MyIterator() {
      nextNode = head;
    }

    public boolean hasNext() {
      return nextNode != null;
    }

    public V next() {
      if(!hasNext()){
        throw new NoSuchElementException("no more items");
      }
      V result = nextNode.value;
      nextNode = nextNode.next;
      return result;
    }
  }

  private static <V> void show(HashLLDS<V> array){
    // print values in order of insertion
    System.out.println("Values in FIFO order:");
    System.out.print("\t");
    for (V i : array) {
        System.out.print(i + " ");
    }
    System.out.println();

    for (int i = 0; i < array.size(); i++) {
        System.out.println("A[\"Key" + i + "\"] = " + array.get("Key" + i));
    }
  }

  public static void main(String[] args) {
      HashLLDS<Integer> A = new HashLLDS<>();

      for (int i = 200; i >= 0; i--) {
          //insert ("Key0", 0), ("Key1", 1), ...
          A.put("Key" + i, i);
      }

      show(A);
  }
}