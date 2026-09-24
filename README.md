# **CS 0445 Assignment 1: Pushback Goal Manager**

## **🤖 Objective**

In this assignment, you will implement a pair of array-based data structures modeling the scoring goal from a robotics competition game: a **drum** (a small, single-opening rotating ring used to load cubes) and a **tunnel** (a longer tube with two open ends, into which the drum feeds cubes one at a time). You must use only **plain Java arrays** to implement all storage — no `ArrayList`, `LinkedList`, `HashMap`, `Queue`, `Deque`, `Stack`, or any other `java.util` container class is allowed anywhere in your implementation.

You will fill in the methods of `PushbackGoalArrayImpl.java`, which implements the provided interface `PushbackGoalInterface<T>`.

**Important:** the tunnel can *only* be loaded through the drum. There is no method that lets you insert an arbitrary value directly into the tunnel — every cube that ever enters the tunnel had to be fed into the drum first and then transferred across.

The game engine (`PushbackGame.java`) is a **bonus** file for experimentation after your implementation is complete. It is a basic, text-based simulator that lets you feed, rotate, load, sweep, block, and score. Bonus credit can be earned by extending it further.

**Note:** This assignment was developed with the assistance of an AI assistant to facilitate brainstorming and generate portions of the scaffolding and documentation files, thereby accelerating the prototyping process.

---

## **📁 Folder Structure**

```
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── TunnelEnd.java
│   │       ├── PushbackGoalInterface.java
│   │       ├── PushbackGoalArrayImpl.java   # Your implementation goes here
│   │       └── PushbackGame.java            # Bonus simulator (text-based)
│   └── test/
│       └── java/
│           └── PushbackGoalTest.java        # JUnit test cases
```

---

## **⚙️ Compilation & Running Tests**

You can use GitHub Codespaces to run, compile, and test this assignment entirely in the cloud — no local setup required.

If working locally, you'll need Maven installed:

### **Linux/macOS**
```
sudo apt install maven   # on Ubuntu/Debian
brew install maven       # on macOS
```

### **Windows**
1. Download from: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
2. Extract to a folder like `C:\Program Files\Apache\maven`
3. Set environment variable `MAVEN_HOME` to that folder
4. Add `%MAVEN_HOME%\bin` to your system `PATH`
5. Open a new command prompt and type `mvn -v` to verify installation

### **🔨 Compile the Project**
```
mvn compile
```

### **✅ Run Tests**
```
mvn test
```

---

## **🔍 Debugging Test Cases in VS Code with Test Runner**

To debug JUnit test cases in VS Code, follow these steps:

### **Prerequisites:**

* Install the **Java Extension Pack** in VS Code.
* You may need to install version **0.40.0** of the **Test Runner for Java** extension if debugging options do not appear.
* You will need to reload your VS Code window and make sure that a Java Project is detected by expanding Java Projects and clicking on Import Project.

#### **Steps:**

1. Open a test file in the editor.
2. Set breakpoints by clicking on the gutter next to the line numbers.
3. Right-click on the gutter next to the line number of the test method name and select **Debug Test**.
4. Use the debug toolbar to step through code, inspect variables, and view call stacks.

This allows you to easily verify internal state, control flow, and ensure correctness of your implementation.

---

## 🎮 Running the Simulator (Bonus)

```
mvn compile
mvn exec:java -Dexec.mainClass=PushbackGame
```

Or with javac/java directly:
```
cd src/main/java
javac *.java
java PushbackGame
```

Commands: `FEED <value>`, `EJECT`, `ROTATE <steps>`, `LOAD <L|R>`, `DUMP <L|R>`, `SWEEP <index> <L|R>`, `BLOCK <L|R>`, `UNBLOCK <L|R>`, `CONTROL <start> <end>`, `Q` to quit.

> This simulator is basic. You're encouraged to implement additional features (GUI, scoring over time, multiple goals, alliance tracking, etc.) for bonus credit.

---

## **📘 Interface and Method Details**

### The Drum

The drum is a small, fixed-capacity **ring** with exactly one accessible opening. You can only add or remove a cube at that opening; the only way to access any other cube is to rotate the whole ring.

#### `initializeDrum(int capacity, T emptyValue)`
Sets up the drum with every slot equal to `emptyValue`.

#### `feedDrum(T value)`
Places `value` at the opening. Returns `false` (does nothing) if the opening is already occupied.

#### `ejectDrum()`
Removes and returns whatever is at the opening. **Throws `IllegalStateException`** if the opening is empty.

#### `rotateDrum(int steps)`
Rotates the *entire* ring — including empty slots — by `steps` positions. **Positive steps rotate toward the opening**: after `rotateDrum(1)`, whatever cube was one slot away from the opening is now sitting at the opening. Negative steps rotate the opposite way, away from the opening.

#### `getDrumAt(int index)` / `drumCapacity()`
Inspection only — for testing and the simulator. Don't use these inside `removeAllOfValue()` (see below).

#### `removeAllOfValue(T value)`
Removes every cube in the drum matching `value`, **using only `rotateDrum(1)`, `feedDrum()`, and `ejectDrum()`** — no direct indexing into your own backing array anywhere in this method's logic. Non-matching cubes must end up back in the drum in their original relative (cyclic) order; matching cubes simply vanish, leaving their slots empty.

**The algorithm:** for exactly `drumCapacity()` iterations: eject whatever is at the opening; if it doesn't match `value`, feed it right back into the (now-empty) opening; if it does match, just leave the opening empty; then rotate by 1. After a full cycle, you're back where you started, and only the value you targeted is missing.

**Worked example** (drum capacity 4, opening shown first): starting drum `[C, A, B, A]`, calling `removeAllOfValue('A')`:

| Step | Eject | Match? | Action | Drum after rotate(1) |
|---|---|---|---|---|
| 1 | C | no | feed C back | `[A, B, A, C]` |
| 2 | A | yes | leave empty | `[B, A, C, .]` |
| 3 | B | no | feed B back | `[A, C, ., B]` |
| 4 | A | yes | leave empty | `[C, ., B, .]` |

Result: 2 cubes removed, final drum `[C, ., B, .]` — notice this is exactly the *original* array with every `A` zeroed out in place. That's not a coincidence: since the method rotates by exactly `capacity()` total, the ring returns to its starting alignment, so the net effect is always "the original layout, minus the target value." Use this as a sanity check on your own implementation.

### The Tunnel

The tunnel is a longer fixed-capacity tube with two ends, `TunnelEnd.LEFT` and `TunnelEnd.RIGHT`.

#### `initializeTunnel(int capacity, T emptyValue)`
Sets up the tunnel with every slot equal to `emptyValue`. Neither end starts blocked.

#### `setBlocked(TunnelEnd end, boolean blocked)` / `isBlocked(TunnelEnd end)`
Represents a defensive robot sitting at an opening. **While an end is blocked, no cube may enter, exit, or be vacated into through that end, by any operation.** A cube already resting at that boundary slot when the block is applied is *not* moved — it simply stays stuck there until the end is unblocked, or until an arm sweep clears it out through the **other**, unblocked end (see `sweepAt` below).

#### `sweepAt(int index, TunnelEnd sweepToward)`
The robot arm reaches in from the top at `index` and reaches toward `sweepToward`'s mouth, covering every slot from `index` to that mouth (inclusive). Slots on the far side of `index` (away from `sweepToward`) are left completely untouched, since they're outside the sweep's reach.

- **If that end is not blocked:** every cube in the swept range is removed from the tunnel entirely (set to `emptyValue`). Returns the number of cubes cleared.
- **If that end is blocked:** the cubes can't be vacated out through it, so instead of clearing, the arm just **compacts** them — every non-empty cell in the range gets pushed as close to the blocked mouth as possible, keeping their relative order, and the empty cells end up bunched together on the far side of the range instead of disappearing. No cube is actually removed, so this returns `0`, even though the tunnel's contents may have shifted.

**Worked example (unblocked):** tunnel `[C, B, A]` (capacity 3), `sweepAt(1, TunnelEnd.LEFT)` clears indices 0 and 1 (`C` and `B`), leaving `[., ., A]`. Calling `sweepAt(1, TunnelEnd.RIGHT)` instead would clear indices 1 and 2 (`B` and `A`), leaving `[C, ., .]`.

**Worked example (freeing a stuck cube):** tunnel `[C, B, A]`, left end blocked. `sweepAt(0, TunnelEnd.LEFT)` returns `0` and changes nothing (no gap inside a single-cell range to compact) — the sweep would need to vacate `C` out the blocked left mouth. But `sweepAt(0, TunnelEnd.RIGHT)` succeeds normally, sweeping indices 0 through 2 out through the *unblocked* right end, clearing all three cubes.

**Worked example (blocked compaction with an actual gap):** tunnel `[C, A, ., B, D]` (capacity 5), right end blocked. `sweepAt(1, TunnelEnd.RIGHT)` covers indices 1–4 (`[A, ., B, D]`). Reading from the mouth (index 4) backward, the non-empty cells are `D`, `B`, `A` in that order — they get packed against the wall in that same order, and the one empty cell ends up at the near edge of the range: result `[C, ., A, B, D]`. Notice `A` moved from index 1 to index 2, and the gap moved from index 2 to index 1 — everything shifted one step closer to the blocked mouth, but nothing left the tunnel, so the return value is still `0`.

#### `getTunnelAt(int index)` / `tunnelCapacity()`
Inspection only.

#### `determineControl(int startIndex, int endIndex)`
Scans the inclusive slot range `[startIndex, endIndex]` (the "control zone") and returns whichever value holds a **strict majority** (more than half) of the non-empty slots in that range — or `null` if there's a tie, no clear majority, or the zone has no cubes at all.

### The Bridge — the only way into the tunnel

#### `loadTunnelFromDrum(TunnelEnd end)`
Transfers exactly one cube: ejects whatever's at the drum's opening (returns `false` immediately if the opening is empty) and attempts to insert it into the tunnel at `end`:
- **Fails immediately if `end` itself is blocked** — a robot sitting right at the mouth you're trying to insert into stops the cube from ever entering.
- Otherwise, scans from that mouth for the nearest empty slot and shifts the occupied chain up to it, one slot per cube in that chain (the **number of cubes shifted** varies — sometimes 0, sometimes many, depending on how close the nearest gap is), placing the new cube right at the mouth.
- If there's no gap anywhere (tunnel completely full), the *entire* tunnel shifts toward the far end and whatever's sitting there gets evicted — **unless that far end is blocked**, in which case the insertion fails outright, even though the block is on the opposite side from where you're inserting.

If the tunnel insertion fails for any reason (either of the two blocked cases above), the ejected cube is fed straight back into the drum before returning `false`.

#### `dumpDrumIntoTunnel(TunnelEnd end)`
Empties the **entire** drum into the tunnel at `end`. Since the drum only exposes one opening, this must rotate between attempts to reach every physical slot: for up to `drumCapacity()` steps, skip (and rotate past) any slot that's currently empty, and call `loadTunnelFromDrum(end)` on any slot that has a cube. If a transfer ever fails because the **tunnel** rejected it (as opposed to the drum simply being empty at that slot), stop immediately — that cube is already back in the drum — and don't rotate any further. Returns the count of cubes successfully transferred.

---

## ✏️ Implementation Header

```java
public class PushbackGoalArrayImpl<T> implements PushbackGoalInterface<T> { ... }
```

---

## **🚫 Restrictions**

* ✅ **You may only modify one file**: `PushbackGoalArrayImpl.java`.
* 💡 **Bonus (optional)**: You may modify `PushbackGame.java` once your implementation is complete.
* ❌ **You may NOT use any Java container classes** (`ArrayList`, `LinkedList`, `HashMap`, `HashSet`, `Queue`, `Deque`, `Stack`, etc. — these are just examples). Only plain Java arrays (`T[]`) are allowed anywhere in your storage or logic.
* ⚠️ Violating these restrictions may result in a **zero** for the implementation portion.

---

## **🧪 Testing**

```
mvn test
```

`PushbackGoalTest.java` includes worked examples matching the ones in this README — if a test fails, set a breakpoint at the start of the failing test method and step through with the debugger (see the Debugging section above) to see exactly where your implementation's state diverges from the README's worked example.

---

## **🏁 Bonus**

`PushbackGame.java` is a working text-based simulator once your class is implemented. Bonus credit may be awarded for:

* A GUI frontend
* Live scoring over a simulated match timer
* Multiple goals / alliance tracking
* Randomized "robot defense" scenarios that toggle blocking automatically

---

## **📊 Grading Rubric**

| Feature                                                              | Points  |
| ---------------------------------------------------------------------- | ------- |
| `initializeDrum`, `feedDrum`, `ejectDrum`, `getDrumAt`, `drumCapacity`   | 10      |
| `rotateDrum`                                                            | 10      |
| `removeAllOfValue` (rotate/feed/eject only, order preserved)            | 15      |
| `initializeTunnel`, `getTunnelAt`, `tunnelCapacity`, blocking            | 10      |
| `sweepAt` (clear, blocked-end compaction, and stuck-cube cases)        | 15      |
| `determineControl`                                                      | 10      |
| `loadTunnelFromDrum` (nearest-gap shift, full-tunnel eviction, blocking)  | 15      |
| `dumpDrumIntoTunnel` (full-drum traversal via rotation)                 | 10      |
| Code style, comments, and modularity                                    | 5       |
| **Total (excluding bonus)**                                            | **100** |
| Bonus (above 100): simulator extensions                                | 10      |

### **💡 Grading Guidelines**

* The core features above are graded using Gradescope's autograder, with both visible and hidden test cases.
* If your autograder score is below 60%, your code will be manually reviewed for partial credit — but manual grading can award no more than 60% of the total autograder points.
* `Code style, comments, and modularity` is graded manually: meaningful names, proper indentation, helper methods to reduce duplication, and comments explaining non-obvious logic (especially around blocking checks and the sweep vs. shift distinction).
