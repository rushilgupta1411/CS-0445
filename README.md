# **CS 0445 Assignment 4: Pushback Hash Table**

## **🤖 Objective**

A **hash table** stores key/value pairs and finds a key quickly by working out, from the key itself, *where* to look. In this assignment you build a hash table whose design is borrowed from the scoring goal of the Pushback robotics game:

* every **bucket** (a spot where entries live) is a **tunnel**: a short tube that holds a few entries and has two open ends;
* entries that do not fit anywhere are parked in a **drum**: a small ring with a single opening. We call it the **stash**.

The central idea is an existing technique, **cuckoo hashing**, introduced by Rasmus Pagh and Flemming Friche Rodler [1] and studied and refined by many others since; this assignment only dresses it in the Pushback theme (the tunnels and the drum) and asks you to implement it carefully. In cuckoo hashing, every key is allowed to live in one of *two* buckets, so looking a key up never takes more than two checks (plus a glance at the stash). When both of a new key's buckets are full, the table makes room by **kicking** an entry out of one of them and moving it to *its* other bucket, which may kick another entry in turn, and so on. This is why it is named after the cuckoo bird, which pushes other birds' eggs out of the nest.

### Key words in one minute

Each of these is explained again, with examples, where it first matters. For now, a one-line meaning is enough.

| Word | Meaning |
|---|---|
| **bucket / tunnel** | one spot in the table; it holds a few entries (its capacity is called `c` later on), and new entries are pushed in at its left end |
| **home** | one of the two buckets where a key is allowed to live (`home1` and `home2`) |
| **kick** | pushing an entry out of a full bucket so a new entry can take its place |
| **stash / drum** | a small overflow area for the rare entry that kicking cannot place |
| **hash function** | a recipe that turns a key into a bucket number. Your table uses two, called `h1` and `h2` (one per home). The recipe is **already written for you** inside your class file; two numbers `a` and `b` (the **coefficients**) decide which function you get, and the table receives them in its constructor |
| **seed** | a small, human-friendly number from which the *tests* work out a function's coefficients, using a random-number generator called SplitMix64. That generator lives in the **test code, outside your class**; it just makes tests easy to write ("`h1` is the function of seed 2") and exactly repeatable |
| **capacity exception** | the table has a fixed size and fixed hash functions. If a new entry cannot be placed even after kicking and using the stash, `put` undoes everything it did and throws `TableCapacityException`, leaving the table exactly as it was |
| **statistics** | simple counters the table keeps about its own work (how many lookups, how many comparisons they took in total, how much work the insertions needed), which you expose through getter methods such as `getTotalHitComparisons()` |

### What you will do

1. **Write the table.** One Java class, `CuckooPushbackTable`, that supports `put`, `get`, `containsKey` and `remove` using the ideas above.
2. **Keep the counters.** Inside the same class, count what the table does and expose the counts with getters.
3. **(Bonus) Build a visualizer** that shows a kick chain rippling through the tunnels. *You may use AI tools for the bonus part only.*

Everything you write lives in that one class. To keep the work focused on the table itself, these pieces are **provided**: the tunnel (`ConveyorBucket`), the drum (`DrumStash`), the key/value `Entry`, `TableCapacityException`, and the hash-function recipe.

**Note:** This assignment was developed with the assistance of an AI assistant to facilitate brainstorming and generate portions of the scaffolding and documentation files, thereby accelerating the prototyping process. This was done over multiple iterations, with full manual read-through and edits.

### 🧑‍💻 Policy on AI tools

| Part | AI assistants allowed? |
|---|---|
| `CuckooPushbackTable` | **No.** |
| Bonus visualizer (`Visualizer*.java`) | **Yes**, and encouraged. You must include an `AI_USE.md` listing the tools you used and what you asked them, and you must be able to explain every line you submit. |

---

## **📁 Folder Structure**

```
├── pom.xml
├── README.md
├── src/
│   ├── main/java/
│   │   ├── Entry.java                         # PROVIDED  key/value pair
│   │   ├── ConveyorBucket.java                # PROVIDED  the tunnel
│   │   ├── DrumStash.java                     # PROVIDED  the drum
│   │   ├── TableCapacityException.java        # PROVIDED
│   │   └── CuckooPushbackTable.java           # YOUR CODE (the hash helpers inside are provided)
│   └── test/java/                             # visible tests (hidden tests run on Gradescope); also SplitMix64 and
│                                              # TestHelpers: test code that turns seeds into coefficients
```

---

## **⚙️ Compilation, Tests, and Tools**

```
mvn compile        # compile
mvn test           # run the visible tests
```

GitHub Codespaces can be used to build and test in the cloud (`.devcontainer/` is included). To debug in VS Code, set a breakpoint at the start of a failing test and compare your layout to the README example it came from.

---

## **📦 What Is Provided**

You may read the provided classes, call them, and learn from them, but you may not edit them (the autograder uses its own copies).

| Class | What it is | Key methods |
|---|---|---|
| `Entry<K,V>` | A key/value pair. | `getKey`, `getValue`, `setValue` |
| `ConveyorBucket<K,V>` | **The tunnel.** A tube with two mouths and fixed capacity `c`. New entries enter at the LEFT mouth (slot 0). Occupied slots are *always* `0 .. size-1` (the "packed-left" rule). | `pushLeft`, `get(slot)`, `indexOf`, `removeKey`, `popRight`, `popLeft`, `appendRight`, `size`, `isFull`, `clear` |
| `DrumStash<K,V>` | **The drum.** One opening: you can only touch the entry at the opening, and you rotate the drum to bring another slot there (`rotate(1)` brings the next slot to the opening). | `insert`, `find`, `lastFindComparisons`, `remove`, `size`, `capacity`, the low-level `peekOpening`/`feed`/`eject`/`rotate`, and `getAt` (**inspection only**) |
| `TableCapacityException` | Thrown by `put` when the new entry cannot be placed (the kick limit was reached and the stash is full). | |
| *(inside your file)* hash function | The constant `P` and the package-visible `hash(a, b, key, m)`. See "The hash function" below. Do not modify them. | |
| *(test code)* `SplitMix64`, `TestHelpers` | `TestHelpers.coefficients(seed)` turns a seed into the coefficients `(a, b)` with SplitMix64; `TestHelpers.hash(seed, key, m)` and `TestHelpers.tableFromSeeds(...)` build on it. They live in `src/test/java`, **outside your class**. | |

### The tunnel in one picture

```
 LEFT mouth -> [ slot 0 | slot 1 | slot 2 | slot 3 ] <- RIGHT mouth
                newest                       oldest
```

`pushLeft(e)` shifts every entry one slot right and puts `e` at slot 0. If the tunnel was full, the entry that was at the RIGHT mouth falls out and `pushLeft` **returns it** to you. If the tunnel had a gap, nothing falls out and it returns `null`. Because the occupied slots are always packed to the left, the "nearest gap" is always slot `size()`, so `pushLeft` never needs to search.

---

## **🐦 The `CuckooPushbackTable` Class (100 pts)**

### Restrictions

* The class may use **plain arrays only**, plus the provided classes. **No `java.util` container classes** (`ArrayList`, `HashMap`, `HashSet`, `LinkedList`, `Queue`, `Deque`, `Stack`, ...). `java.util.Arrays` and `Math` are fine.
* Everything is in this one class: no helper classes in other files.
* You may modify only `CuckooPushbackTable.java` (everything *except* the marked hash helpers), plus your own new `Visualizer*.java` files (bonus).
* Violating the restrictions may result in a **zero** for the implementation portion.

### The constructor and injectable coefficients

There is a single constructor:

```java
CuckooPushbackTable(int buckets, int bucketCapacity, int stashCapacity, int maxKicks,
                    long a1, long b1, long a2, long b2)
```

The table has a **fixed size**: it never resizes and never changes its hash functions. The meaning of the parameters:

| Parameter | Meaning |
|---|---|
| `buckets` (m) | number of buckets (at least 2) |
| `bucketCapacity` (c) | slots per bucket (tunnel length), at least 1 |
| `stashCapacity` (s) | stash (drum) size; 0 disables the stash |
| `maxKicks` | maximum evictions in one kick chain before the entry in hand goes to the stash (at least 1) |
| `a1`, `b1` | the coefficients of `h1`: `h1(key, m) = hash(a1, b1, key, m)` |
| `a2`, `b2` | the coefficients of `h2`: `h2(key, m) = hash(a2, b2, key, m)` |

The coefficients must be in range: `a1` and `a2` in `1 .. P - 1`, and `b1` and `b2` in `0 .. P - 1`, where `P = 2147483647` (see "The hash function"). Invalid arguments throw `IllegalArgumentException`.

**Injectable coefficients.** The table never creates random numbers itself: the four coefficients are plain constructor arguments, and they stay the same for the table's whole life. That is what makes the hash functions *injectable*: a test (or the visualizer) decides exactly which two functions the table uses, so every layout is reproducible. Where the numbers come from is not the table's business. In this assignment's tests they are worked out from small seeds by code that lives **outside your class** (explained in step 3 below); you can equally well pass numbers you picked yourself. Use two *different* functions: if `(a2, b2)` equals `(a1, b1)` then `h2` equals `h1` and the two homes would always be neighbouring buckets.

### The hash function (provided inside your file)

Your file provides **one** hash function, `hash(a, b, key, m)`, and you should understand what it does. This section has four small steps: what a hash function is, a *family* of functions with two knobs (`a` and `b`, the **coefficients**), where the tests get those two numbers from (**seeds** and **SplitMix64**, in the test code), and finally how `h1` and `h2` are built.

#### 1. What a hash function does

A hash function turns a key into a bucket number between `0` and `m - 1`. A good one gives the same answer every time for the same key, and spreads different keys evenly over the buckets. Cuckoo hashing needs **two** such functions, `h1` and `h2` (one for each of a key's two homes), and they should behave like two *independent* functions. So instead of writing two unrelated functions, we use one *recipe* with two adjustable numbers.

#### 2. A family of functions with two knobs, `a` and `b`

```
h(key, m) = ((a * x + b) mod P) mod m          where  x = key.hashCode() & 0x7fffffff,   P = 2^31 - 1 = 2147483647
```

Reading it from the inside out:

* `x = key.hashCode() & 0x7fffffff`: every Java object has a `hashCode()`, which can be negative. The mask clears the sign bit, so `x` is never negative.
* `a * x + b`: multiply and shift. This scrambles `x`; a different `a` and `b` scramble it differently, so **each pair `(a, b)` is a different hash function**. (Because `a` and `x` are both below 2^31, the product is below 2^62 and fits in a `long`: no overflow. That is why `a` must stay below `P`.)
* `mod P`: wraps the result into `0 .. P-1`. `P` is a **prime number**; working modulo a prime keeps the scrambling free of regular patterns.
* `mod m`: squeezes the result into the bucket numbers `0 .. m-1`.

The two knobs have rules: `a` must be in `1 .. P-1` (with `a = 0` every key would land in the same bucket), and `b` in `0 .. P-1`.

**Example** (`a = 659044138` and `b = 71743339`, the numbers that seed `42` produces in step 3), key `12345`, `m = 1024`:

```
x          = 12345
a * x      = 659044138 * 12345        = 8135899883610
a * x + b  = 8135899883610 + 71743339 = 8135971626949
mod P      = 8135971626949 mod 2147483647 = 1303572113
mod m      = 1303572113 mod 1024     = 657          -> bucket 657
```

With `m = 1000` the same key goes to bucket `113`, and with `m = 3` to bucket `2`: only the last step changes.

#### 3. Where `a` and `b` come from: seeds and SplitMix64 (in the test code)

Your class only *receives* `a` and `b`; it does not care where they come from. But typing two ten-digit numbers per function is awkward, and in a test we would rather say "`h1` is the function of seed 2". So the **test code** (not your class) contains a tiny random-number generator, **SplitMix64** (`src/test/java/SplitMix64.java`), and a helper `TestHelpers.coefficients(seed)` that turns a **seed** into a pair `(a, b)`.

*What a generator does.* Given a starting number (the seed) it produces a stream of numbers that look random but are completely determined by the seed: the same seed always gives the same numbers (that is what makes tests reproducible), and even boring neighbouring seeds such as `1` and `2` give unrelated-looking numbers (that is what makes two different seeds produce two genuinely different functions). Seeds `1`, `2` and `42` give:

| seed | `a` | `b` |
|---|---|---|
| 1 | 1294668908 | 1667631016 |
| 2 | 2046307903 | 193768033 |
| 42 | 659044138 | 71743339 |

*One SplitMix64 step.* It keeps a 64-bit `state`, and every step does this (all arithmetic on 64-bit `long`s that wrap around on overflow, `>>>` is "shift right filling with zeros", `xor` mixes bits):

```
state  = state + GOLDEN                           // move the state forward by a fixed odd constant
z      = state
z      = (z xor (z >>> 30)) * 0xBF58476D1CE4E5B9  // scramble: fold high bits onto low bits, then multiply
z      = (z xor (z >>> 27)) * 0x94D049BB133111EB  // scramble again
output = z xor (z >>> 31)                         // one last mix
```

`GOLDEN` is `0x9E3779B97F4A7C15`, which is 2^64 divided by the golden ratio. You do not need to understand why each constant was chosen: they are the published SplitMix64 constants, and what matters is the behavior, namely that changing even one bit of the seed changes about half of the bits of every output. Treat it as a black box that turns a seed into well-scattered 64-bit numbers.

*From outputs to coefficients.* `TestHelpers.coefficients(seed)` runs two steps (starting with `state = seed`) and calls the results `output1` and `output2`. They are 64-bit numbers and can be negative, so they are squeezed into the ranges the family needs:

```
a = floorMod(output1, P - 1) + 1        // in 1 .. P-1   (never 0)
b = floorMod(output2, P)                // in 0 .. P-1
```

`Math.floorMod` is used because Java's `%` keeps the sign of the dividend (`-7 % 5` is `-2`), while `floorMod(-7, 5)` is `3`: always in range. For seed `42` the outputs are `-4767286540954276203` and `2949826092126892291`, which become `a = 659044138` and `b = 71743339`.

#### 4. Building `h1` and `h2`

Your table receives two pairs of coefficients. The first pair defines `h1` and the second defines `h2`:

```
(a1, b1)  ->  h1(key, m) = hash(a1, b1, key, m)
(a2, b2)  ->  h2(key, m) = hash(a2, b2, key, m)
```

`h1` and `h2` are therefore not separate methods: each is the same `hash` called with a different pair of coefficients. Your table **stores the four numbers `a1, b1, a2, b2`** and calls `hash(a1, b1, key, m)` and `hash(a2, b2, key, m)`. In the tests, the pairs come from seeds: `TestHelpers.tableFromSeeds(m, c, s, maxKicks, seed1, seed2)` computes `coefficients(seed1)` and `coefficients(seed2)` and passes the four numbers to your constructor.

The three hash-related helpers, and where each lives:

| Helper | Parameters | What it does | Lives in | Who calls it |
|---|---|---|---|---|
| `hash(a, b, key, m)` | 4 | one member of the family: the formula from step 2 | your class file (provided) | your table, on every lookup and insertion |
| `TestHelpers.coefficients(seed)` | 1 | runs SplitMix64 and returns the pair `(a, b)` | test code | the tests |
| `TestHelpers.hash(seed, key, m)` | **3** | shortcut for "the function this seed produces": `coefficients(seed)` followed by the four-parameter `hash` | test code | the tests |

The reference values in the table below are for the **three-parameter** test shortcut, which is why it lists `hash(42, key, 1024)`: seed `42`, then the key, then `m`. They are checked by `HashFunctionTest`:

| key | `hash(42, key, 1024)` | `hash(42, key, 1000)` |
|---|---|---|
| 0 | 875 | 339 |
| 1 | 661 | 477 |
| 12345 | 657 | 113 |
| 987654321 | 468 | 964 |

Seed `7` maps key `12345` (with `m = 1024`) to `157`. Negative keys work too: with seed `42`, key `-5` maps to `705` (with `m = 1024`).

**A caution.** Not every pair of coefficients is good for every table size and every set of keys. A function from this family can occasionally be poor for a particular key range and modulus: for example, seed `2` sends the keys `2` through `23` all to the same bucket when `m = 3`, and all to one (other) bucket when `m = 16`. This is why coefficients are worth choosing with a little care. The worked examples below use seed `2` on purpose, because it makes collisions easy to produce with just a few keys.

The two homes of a key are

```
home1(k) = h1(k, m) = hash(a1, b1, k, m)
x        = h2(k, m) = hash(a2, b2, k, m)
home2(k) = (x == home1(k)) ? (x + 1) % m : x          // the two homes are always different
```

### The operations

| Method | Behavior |
|---|---|
| `V put(K key, V value)` | insert or replace (rules below); may throw `TableCapacityException` |
| `V get(K key)` | the value or `null`; counts as a lookup |
| `boolean containsKey(K key)` | counts as a lookup, exactly like `get` |
| `V remove(K key)` | the removed value or `null`; then drains the stash |
| `int size()` | number of entries, **including** those in the stash |
| `double loadFactor()` | `size() / (bucketCount() * bucketCapacity())` |
| `int bucketCount()`, `int bucketCapacity()` | current `m` and `c` |
| `Entry<K,V> getEntryAt(int bucket, int slot)` | the entry in that slot (slot 0 = LEFT mouth) or `null` |
| `int stashCapacity()`, `int stashSize()`, `Entry<K,V> getStashAt(int i)` | stash inspection for tests and the visualizer (`i` is the logical slot, 0 = the opening). **Not for the table's own logic**: a drum only gives access to its opening. |
| statistics getters | see "Statistics" |

### `put(key, value)`

1. A `null` key throws `IllegalArgumentException`.
2. **Existing key:** replace the value *in place*. Nothing moves; return the old value. (A replacement is not an insert.)
3. **New key:** insert the entry with the kick-chain algorithm below. If it returns an entry in hand (the chain reached `maxKicks` and the stash is full), **undo the chain and throw `TableCapacityException`** (see below). Otherwise increase `size`, update the statistics, and return `null`.

### `TableCapacityException` and atomicity

The table never grows and never changes its hash functions, so sooner or later a `put` can fail: the kick limit is reached and the stash has no room for the entry in hand. In that case `put` throws `TableCapacityException`. **A `put` that throws must leave the table exactly as it was**: same entries, same layout (buckets *and* stash), same `size()`, as if `put` had never been called, and the table must stay fully usable afterwards. The kick chain modifies buckets as it goes, so you need to be able to undo it; see "Rolling back a failed `put`". A `put` that throws is not counted as an insert.

### Lookup cost accounting

The statistics measure *work done*, so everyone must count the same way. The cost of one call to `get` or `containsKey` is its number of **comparisons**: how many stored keys you compared with `equals`. The two kinds of place are counted differently, and the reasons come from how the provided classes work:

  * **A bucket (tunnel).** A `ConveyorBucket` is *packed to the left*: its occupied slots are always exactly `0 .. size-1`, with every empty slot after them. `ConveyorBucket.indexOf` therefore examines only the occupied slots, scanning from slot 0, and it never looks at the empty tail. A **hit at slot *i*** costs **i + 1** comparisons (slots `0` through `i`), and a **miss** costs **`size()`** comparisons (every occupied slot).
  * **The stash (drum).** A drum only lets you touch the entry at its single opening, so `DrumStash.find` has to rotate. It rotates one step at a time for exactly `capacity()` steps and compares the entry at the opening each time (so the entries are compared in the order the drum presents them: logical slots `0, 1, 2, ...`, skipping empty slots). **Once the key is found, no more comparisons are made.** The drum still finishes its full cycle so that it ends up exactly as it started (the tests and the worked examples compare the drum's contents, so a lookup must not leave it rotated), but those remaining rotations cost no comparisons. So a stash lookup costs **the number of occupied entries up to and including the match** for a hit, and **`stash.size()`** for a miss, and it happens only if `stash.size() > 0`. `DrumStash.lastFindComparisons()` reports exactly this number for the most recent `find`, so your table can simply add it. (We count comparisons, not rotations: the rotations always number `capacity()`, and empty slots involve no key comparison.)

### Statistics (getters)

Keep the counters as fields and expose them through these getters. They are running **totals** since the table was created (or since the last `resetOperationCounters()`); nothing is averaged.

| Getter | Meaning and when it changes |
|---|---|
| `getLookupHits()`, `getLookupMisses()` | number of public `get`/`containsKey` calls that found / did not find the key. **Only** those calls count; the lookup inside `put` or `remove` does not. |
| `getTotalHitComparisons()`, `getTotalMissComparisons()` | the **sum** of the `comparisons` (see "Lookup cost accounting") of all the lookups that were hits / of all the lookups that were misses |
| `getTotalInsertSteps()` | the **sum**, over all successful client `put` calls that inserted a new key, of the **steps** of that `put`, where the steps of one `put` are the number of `pushLeft` calls it made. Replacements do not add anything, and a `put` that throws adds nothing (its pushes were undone). |
| `getMaxInsertSteps()` | the largest number of steps of any single successful insert |
| `void resetOperationCounters()` | sets every one of the counters above back to zero |

(There is no counter for the number of inserts: `size()` already tells you how many entries the table holds.)

### Idea

**Cuckoo hashing** was introduced by Pagh and Rodler [1] and is named after the cuckoo bird, which lays its egg in another bird's nest and pushes the egg that was already there out. In the original scheme every key may live in exactly one of two cells, determined by two independent hash functions, so a lookup inspects at most two places no matter how full the table is. To insert a key, you place it in one of its two cells. If that cell is occupied, the old occupant is evicted ("kicked out") and must move to *its* other cell, which may in turn evict another key, and so on. Most of these chains end quickly at an empty cell. If a chain runs too long (it can even cycle forever), the original scheme gives up, picks fresh hash functions, and rebuilds the whole table. **This assignment keeps things simpler: the table has a fixed size and fixed hash functions, so it never rebuilds.** It uses a **bucketized** variant: each "cell" is a tunnel that holds up to `c` entries, which typically lets the table reach a much higher load factor before insertions get expensive, and it adds a small **stash** to absorb the rare entry that a chain cannot place. If even the stash is full, the insertion is refused.

Every key has **two** candidate tunnels, `home1(key)` and `home2(key)`, from two independent hash functions. A lookup never needs to look at more than those two buckets (plus the stash, if it is not empty). The price is paid on insertion: when both candidate tunnels are full, you **kick** the oldest entry out of one of them and re-home it in *its* other tunnel, which might kick someone else, and so on.

### State

```
ConveyorBucket[] buckets    // m tunnels, capacity c
DrumStash stash             // capacity s (may be 0)
long a1, b1, a2, b2         // (a1, b1) defines h1; (a2, b2) defines h2 (received in the constructor)
int size                    // includes the stash
```

The **stash** is a `DrumStash`: a drum with a **single opening**. You can only touch the entry sitting at the opening (`peekOpening`, `feed`, `eject`); to reach any other entry you rotate the drum. The provided `insert`, `find` and `remove` already work this way, and `find` and `remove` each cost a full rotation cycle. **Your table must touch the stash only through its opening.** `DrumStash.getAt(i)` and the table's `getStashAt(i)` are for *inspection* (tests and the visualizer); they are not for the table's own logic.

**Invariant C.** Every entry lives in `home1`, in `home2`, or in the stash.

### Insert (the kick chain)

`insert(entry)` returns `null` on success, or **the entry left in hand** if the chain failed and the stash is full.

```
a = home1(entry.key);  b = home2(entry.key)
if !buckets[a].isFull():  buckets[a].pushLeft(entry);  return null      // prefer home1
if !buckets[b].isFull():  buckets[b].pushLeft(entry);  return null

cur = entry;  target = a;  kicks = 0
loop:
    victim = buckets[target].pushLeft(cur)         // bucket was full: evicts its RIGHT-mouth entry (a "kick")
    kicks++
    cur = victim
    alt = (home1(cur) == target) ? home2(cur) : home1(cur)     // cur's OTHER bucket
    if !buckets[alt].isFull():  buckets[alt].pushLeft(cur);  return null
    if kicks == maxKicks:  break
    target = alt
if stash.insert(cur):  return null                  // park the entry in hand in the drum
return cur                                          // stash full: the caller must undo the chain and throw
```

Notes: a **kick** is one eviction (one `pushLeft` that returned a non-null entry). The entry parked in the stash is the entry *in hand when the limit hits*, **not** the key the user originally passed to `put` (that one was already placed in a bucket by the first step). Every other entry moved by the chain is in a valid home, so Invariant C holds.

### Lookup

Search `home1`, then `home2`, then (only if `stash.size() > 0`) the stash via `stash.find(key)`. Count comparisons as described in "Lookup cost accounting".

### Remove and stash draining

Find the entry (bucket or stash) and remove it (`removeKey` / `stash.remove`). Then, **after any successful removal**, try to move stash entries back into the table. The drum has a single opening, so do it the way a drum works: run **one full cycle** using only `peekOpening`, `eject` and `rotate`. For `capacity()` steps:

```
e = stash.peekOpening()
if e != null:
    if home1(e)'s bucket is not full:      buckets[home1(e)].pushLeft(stash.eject())
    else if home2(e)'s bucket is not full: buckets[home2(e)].pushLeft(stash.eject())
    // otherwise e stays in the drum
stash.rotate(1)
```

After `capacity()` steps the drum is back where it started, and every entry has been looked at once, in the order the drum presents them. Do **not** peek at other slots with `getAt` or `getStashAt`: reaching a slot without rotating is exactly what a drum does not allow. Without this step the stash would fill permanently.

### Rolling back a failed `put`

`insert` modifies buckets as it kicks. If it ends with an entry in hand that has nowhere to go, the `put` must be undone so the table is exactly as before (see "Atomicity"). Hint: while you run the chain, remember **which buckets you pushed into, in order**. To undo, walk that list **backwards**; for each bucket, `popLeft()` removes the entry you pushed (which is precisely what the *previous* bucket evicted), and `appendRight(held)` puts back what that bucket had evicted (at first, the entry that was in hand). When you reach the start, the entry you hold is the new key; discard it. Then throw `TableCapacityException`.

### Worked examples

All examples use `m = 3` buckets of capacity `c = 2`, and the hash functions of **seed 2** (for `h1`) and **seed 1** (for `h2`): in the tests, `TestHelpers.tableFromSeeds(3, 2, stash, maxKicks, 2, 1)`. Written as the coefficients your constructor receives (see the table in step 3), that is `a1 = 2046307903, b1 = 193768033` and `a2 = 1294668908, b2 = 1667631016`. So `h1(key, 3) = hash(2046307903, 193768033, key, 3)` and `h2(key, 3) = hash(1294668908, 1667631016, key, 3)`. With these, the two homes of the keys used below are (`home2` is the bumped value when `h2` equals `h1`):

| key | 1 | 2 | 3 | 9 | 11 |
|---|---|---|---|---|---|
| home1 | 1 | 2 | 2 | 2 | 2 |
| home2 | 2 | 1 | 0 (bumped from 2) | 1 | 1 |

(With a modulus as small as 3 the two functions are not very random; the keys and seeds were chosen so that every behavior below happens within five insertions. The tests in `CuckooPushbackTableTest` replay these examples.)

**A. Two choices and a kick chain** (`maxKicks = 8`, stash capacity 2). `put 1, 2, 3, 9` gives B0 `[., .]`, B1 `[9, 1]`, B2 `[3, 2]`: keys 1, 2 and 3 went to their first choice, and 9 found B2 full and used its second choice, B1. Now `put 11`: both of its buckets (B2 and B1) are full.

1. Push 11 into B2: `[11, 3]`, evicting **2** (kick 1). 2 lives in B2, its first choice, so its other bucket is B1, which is full.
2. Push 2 into B1: `[2, 9]`, evicting **1** (kick 2). 1 lives in B1, its first choice, so its other bucket is B2, which is full.
3. Push 1 into B2: `[1, 11]`, evicting **3** (kick 3). 3 lives in B2, its first choice, so its other bucket is B0, which has room.
4. Push 3 into B0: `[3, .]`. Done.

Result: B0 `[3, .]`, B1 `[2, 9]`, B2 `[1, 11]`, three kicks happened, and this `put` made **4** `pushLeft` calls (`getMaxInsertSteps() == 4`).

**B. Kick limit → stash** (`maxKicks = 2`, stash capacity 2). Same puts. After kick 2, **1** is in hand; its other bucket (B2) is full and the limit is reached, so 1 goes into the stash:

| B0 | B1 | B2 | stash (opening first) |
|---|---|---|---|
| `[., .]` | `[2, 9]` | `[11, 3]` | `(1, .)` |

`get(1)` now costs **5 comparisons**: B1 (2), B2 (2), then the stash, where 1 is at the opening (1). With several entries in the stash, a key further along the drum costs more, and a key that is absent costs `stash.size()`: see `testStashLookupStopsComparingOnceTheKeyIsFound`.

**C. Remove drains the stash.** Continuing from B, `remove(2)` frees a slot in B1, which is 1's first choice. 1 moves back: B0 `[., .]`, B1 `[1, 9]`, B2 `[11, 3]`, stash `(., .)`.

**D. A put that fails and is rolled back** (`maxKicks = 1`, **no stash**). After `put 1, 2, 3, 9` the layout is B0 `[., .]`, B1 `[9, 1]`, B2 `[3, 2]`. `put 11` pushes 11 into B2 and kicks **2** out (kick 1, which is the limit). 2 is now in hand, its other bucket B1 is full, and there is no stash to park it in. So the `put` undoes its one push (B2 is `[3, 2]` again) and throws `TableCapacityException`. Afterwards the table must again be B0 `[., .]`, B1 `[9, 1]`, B2 `[3, 2]`, `size() == 4`, `get(2)` still works, `get(11)` is `null`, and `getTotalInsertSteps() == 4` (the one push of the failed `put` was undone and is not counted). (This is the test `testFailedPutRollsBackTheKickChain`. With `maxKicks = 2` the failed chain is two pushes long and is undone the same way; see `testPutThrowsWhenTheEntryInHandHasNowhereToGo`.)

---

## **🎬 Bonus: Visualizer (+10)**

Build a visualizer that makes your table *visible*. Technology is up to you: Swing, JavaFX, a terminal UI with ANSI colors, or a Java program that writes a self-contained HTML page. **You may use AI tools for this part**, subject to the AI-use policy above (`AI_USE.md`).

**How the visualizer gets its information is part of the design, and it is up to you.** The class already exposes everything needed to *draw* a table: `bucketCount()`, `bucketCapacity()`, `getEntryAt(bucket, slot)`, `stashCapacity()`, `getStashAt(i)`, `size()`, `loadFactor()`, and the statistics getters. A picture of the table before and after an operation needs nothing more. Showing what happens *inside* a `put` (the kick chain, the entry in hand, the stash being used, a refused `put` being undone) needs more, and you decide how to get it. For example, you could add your own listener or event-recording hooks to `CuckooPushbackTable`, or work out the steps by comparing snapshots. Some things worth thinking about, whichever design you choose:

* A `put` runs to completion before it returns, so showing its steps over time means recording them and replaying them later (or running the table on another thread).
* While a kick chain is running, the entry that was just kicked out is in neither a bucket nor the stash.
* A refused `put` pushes entries around and then undoes everything; decide what your visualizer shows.
* A `remove` can let stash entries move back into buckets.

**Rules for your design.** Any hooks you add must not change how the table behaves: the table must still pass all the tests, and must work exactly as specified when nothing is listening. Describe your design (what information your visualizer needs and how it gets it) in your README section.

| Requirement | Points |
|---|---|
| Draws `m` buckets as tunnels (with left/right mouths marked) and the stash as a ring with its opening marked | 4 |
| Shows a `put` step by step (the kick chain, the entry in hand, stash parking, a refused `put`) with step/pause/speed controls | 3 |
| Shows live numbers (load factor, stash occupancy, how many puts were refused with `TableCapacityException`) | 2 |
| README section with a GIF or screenshot, instructions, and a short explanation of your design | 1 |

Files must be named `Visualizer*.java`.

---

## **🧪 Testing**

```
mvn test
```

The visible tests include the worked examples above (`CuckooPushbackTableTest`) and property tests on bigger inputs (`TableContractTest`). `HashFunctionTest`, `ConveyorBucketTest`, and `DrumStashTest` test the *provided* code (and the test-side `SplitMix64` and seed helpers) and pass from the start; read them, they show exactly how the provided pieces behave. If a test fails, set a breakpoint at its start, step through it, and compare your layout to the README example it came from.

**Hidden tests (Gradescope)** also check your table for exact layouts after longer operation sequences, every statistics getter, exception safety and rollback (including a full stash, and repeated failures), and basic performance sanity (a large insert/lookup/remove workload must finish in a reasonable time).

---

## **📥 Submission**

* **Gradescope (autograded):** `CuckooPushbackTable.java`.
* **Course submission (manually graded):** for the bonus only, `Visualizer*.java` with `AI_USE.md`.

---

## **📊 Grading Rubric**

| Component | Points |
|---|---|
| `put`: two homes, kick chain, stash parking | 35 |
| `get`/`containsKey`/`remove`, stash search, stash draining | 30 |
| Atomic failure: rollback and `TableCapacityException` | 15 |
| Statistics getters | 20 |
| **Total (excluding bonus)** | **100** |
| Bonus (above 100): visualizer | 10 |

### 💡 Grading Guidelines

* The four rows above (100 points) are graded by Gradescope with visible and hidden tests. If your autograder score is below 60%, your code will be manually reviewed for partial credit, capped at 60% of the autograder points.
* The bonus is graded manually.

---

## **❓ Common Pitfalls**

* **Mixing up `h1` and `h2`.** `(a1, b1)` defines `h1` and `(a2, b2)` defines `h2`; swapping the pairs swaps the homes and changes every layout.
* **Charging the stash too much.** Once the key is found in the stash, no more comparisons are counted; use `DrumStash.lastFindComparisons()`.
* **Stashing the wrong entry.** Stash the entry **in hand** when the kick limit hits, not the key passed to `put`.
* **Forgetting to undo the pushes when throwing**, or undoing them in the wrong order (walk the chain *backwards*). The stash must also be left exactly as it was.
* **Counting a failed `put` as an insert**, or counting lookups made inside `put`/`remove`.
* **Using Java's `%` on negative numbers** (use `Math.floorMod`; the provided helpers already do).
* **Not testing `remove` after a failed `put`** (the table must stay fully usable, and the stash-draining step must still work).

---

## **📚 Reference**

[1] R. Pagh and F. F. Rodler. Cuckoo hashing. *Journal of Algorithms*, 51(2):122-144, 2004. doi:10.1016/j.jalgor.2003.12.002