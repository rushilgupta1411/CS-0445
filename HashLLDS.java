
/** Bonus lab for CS 0445. A HashLLDS is a hybrid of a linear-probing hash table and a linked list.
 * Key type is String and Value type is generic. HashLLDS allows both hash table 
 * access using get() and FIFO sequential access using an Iterator.
* @author Sherif Khattab
* Some code adopted from Textbook's code by Frank M. Carrano and Timothy M. Henry
**/

import java.util.Iterator;
import java.util.NoSuchElementException;

public class HashLLDS<V> implements Iterable<V> {
  private static final int INIT_CAPACITY = 13;

  private int N; // number of key-value pairs in the data structure
  private int M; // size of hash table
  private Node<V>[] entries; // the hash table
  private Node<V> head; // head of the linked list
  private Node<V> tail; // tail of the linked list

  // create an empty hash table
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

  // insert the key-value pair into the hash table
  public void put(String key, V val) {
    boolean found = false;
    // follow the linear-probing probe sequence
    int i;
    for (i = hash(key); entries[i] != null; i = (i + 1) % M) {
      // update value if key already exists
      if (entries[i].key.equals(key)) {
        entries[i].value = val;
        found = true;
      }
    }

    if (!found) {// key wasn't found; need to insert an new entry
      // i points to an empty entry in the hash table
      entries[i] = new Node<V>(key, val);

      // TODO: insert the node entries[i] into the tail of the linked list in O(1)
      // time. Don't forget the edge case of inserting into an empty linked list

      N++;
      // double table size if 50% full
      if (N >= M / 2)
        resize(getNextPrime(2 * M));
    }
  }

  // return the value associated with the given key, null if no such value
  public V get(String key) {
    // follow the linear-probing probe sequence
    for (int i = hash(key); entries[i] != null; i = (i + 1) % M)
      if (entries[i].key.equals(key))// if key was found
        return entries[i].value; // return corresponding value
    return null;
  }

  // hash function for keys - returns value between 0 and M-1
  private int hash(String key) {
    // Horner's method followed by modular hashing
    // hashCode of String implements Horner's method
    int hash = key.hashCode() % M;
    if (hash < 0)
      hash += M;
    return hash;
  }

  // resize the hash table to the given capacity and re-hash all existing keys
  private void resize(int capacity) {
    HashLLDS<V> temp = new HashLLDS<V>(capacity);

    // TODO: reinsert the existing entries into temp in original FIFO order.
    // You will need to iterate over the linked list and possibly call
    // temp.put().

    //shallow copy from temp's variables into this
    entries = temp.entries;
    head = temp.head;
    tail = temp.tail;
    M = temp.M;
  }

  public int size() {
    return N;
  }

  // A class to store nodes of a singly-linked list
  // Each node contains a (key, value) pair
  private static class Node<T> {
    private String key;
    private T value;
    private Node<T> next;

    Node(String key, T value) {
      this(key, value, null);
    }

    Node(String key, T value, Node<T> next) {
      this.key = key;
      this.value = value;
      this.next = next;
    }
  }

  public Iterator<V> iterator() {
    // TODO implement this method
    return null;
  }

  private class MyIterator implements Iterator<V> {
    // TODO add instance variable(s) to keep track of
    // iterator's cursor 
    private MyIterator() {
      // TODO implement the constructor
    }

    public boolean hasNext() {
      // TODO implement this method
      return false;
    }

    public V next() {
      // TODO implement this method to return the next value
      return null;
    }

    public void remove() {
      throw new UnsupportedOperationException("remove() is not implemented for this iterator.");
    }
  }

  private void show() {
    // print values in order of insertion
    System.out.println("Values in FIFO order:");
    System.out.print("\t");
    for (V i : this) {
      System.out.print(i + " ");
    }
    System.out.println();

    for (int i = 0; i < N; i++) {
      System.out.println("A[\"Key" + i + "\"] = " + get("Key" + i));
    }
  }

  // Returns a prime integer that is >= the given integer.
  private int getNextPrime(int integer) {
    // if even, add 1 to make odd
    if (integer % 2 == 0) {
      integer++;
    } // end if

    // test odd integers
    while (!isPrime(integer)) {
      integer = integer + 2;
    } // end while

    return integer;
  } // end getNextPrime

  // Returns true if the given integer is prime.
  private boolean isPrime(int integer) {
    boolean result;
    boolean done = false;

    // 1 and even numbers are not prime
    if ((integer == 1) || (integer % 2 == 0)) {
      result = false;
    }

    // 2 and 3 are prime
    else if ((integer == 2) || (integer == 3)) {
      result = true;
    }

    else // integer is odd and >= 5
    {
      assert (integer % 2 != 0) && (integer >= 5);

      // a prime is odd and not divisible by every odd integer up to its square root
      result = true; // assume prime
      for (int divisor = 3; !done && (divisor * divisor <= integer); divisor = divisor + 2) {
        if (integer % divisor == 0) {
          result = false; // divisible; not prime
          done = true;
        } // end if
      } // end for
    } // end if

    return result;
  } // end isPrime

  public static void main(String[] args) {
    HashLLDS<Integer> A = new HashLLDS<>();

    for (int i = 200; i >= 0; i--) {
      // insert ("Key200", 200), ("Key199", 199), ... , ("Key0", 0)
      A.put("Key" + i, i);
    }

    A.show();
    Iterator<Integer> iter = A.iterator();
    while(iter.hasNext()){
      iter.next();
    }
    System.out.println();
    try{
      iter.next();
      System.out.println("ERROR: Should raise a NoSuchElementException exception");
    } catch (NoSuchElementException e){
      System.out.println("NoSuchElementException raised.");
    }
  }
}