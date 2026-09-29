# **CS 0445 Assignment 2: Pushback Goal Manager — Linked List Edition**

## **🎯 Objective**

This is the **exact same interface** as Assignment 1 — every method, every parameter, every documented behavior is identical. If you implemented Assignment 1 correctly, you already know precisely what this assignment's class needs to do. What's different is **how you're allowed to build it**.

In Assignment 1, you stored the drum and tunnel using plain Java arrays. In this assignment, **you may not use an array as your storage** (and, as before, no `java.util` container classes either). Instead, you must build your own **linked list** — a chain of node objects, each holding a value and reference(s) to other node(s) — and implement every operation, including indexed access like `getDrumAt()`/`getTunnelAt()`, by traversing that chain rather than indexing into an array.

Specifically, you must use a **doubly-linked** structure (each node holds both a `next` and a `prev` reference, not just `next`). This isn't just an arbitrary restriction — it makes one part of the assignment genuinely elegant: with `prev` references available, `loadTunnelFromDrum`'s two cases (a gap exists nearby vs. the tunnel is completely full) turn out to be *the same operation*. In both cases, you find a target node, detach it from wherever it currently sits, overwrite its value, and reattach it at the mouth you're inserting into. You'll want to discover this yourself rather than have it spelled out further here — but if your solution ends up with two very different-looking branches for those two cases, that's a sign you're fighting the data structure instead of using it.

**Note:** This assignment was developed with the assistance of an AI assistant to facilitate brainstorming and generate portions of the scaffolding and documentation files, thereby accelerating the prototyping process. This was done over multiple iterations, with full manual read-through and edits.

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
│   │       └── PushbackGoalLinkedListImpl.java   # Your implementation goes here
│   └── test/
│       └── java/
│           └── PushbackGoalTest.java             # JUnit 4 test cases
```

---

## **⚙️ Compilation & Running Tests**

```
mvn compile
mvn test
```

GitHub Codespaces works out of the box; locally, you'll need Maven installed (`mvn -v` to check, or see Maven's install docs for your OS).

---

## **🔍 Debugging Test Cases in VS Code with Test Runner**

To debug JUnit test cases in VS Code, follow these steps:

### **Prerequisites:**

* Install the **Java Extension Pack** in VS Code.
* The folder must be imported/opened as a Java project before the green run/debug arrow icons will appear next to test methods — if they're missing, check the Java Projects panel or run "Java: Reload Projects" from the command palette.
* You may need to install version **0.40.0** of the **Test Runner for Java** extension if the green arrows do not appear. You will need to reload your VS Code window and make sure that a Java Project is detected by expanding Java Projects and clicking on Import Project.

#### **Steps:**

1. Open the test file in the editor.
2. Set breakpoints by clicking on the gutter next to the line numbers.
3. Right-click on the gutter next to the line number of the test method name and select **Debug Test**.
4. Use the debug toolbar to step through code, inspect variables, and view call stacks.

Since every operation now involves pointer-chasing instead of array indexing, a debugger that lets you inspect `next`/`prev` references at each step is especially valuable here — print statements showing node addresses are much harder to reason about than watching the actual reference graph in a debugger.

---

## **📘 What's the Same, What's Different**

Every method signature, parameter, return value, and documented edge case in `PushbackGoalInterface.java` is unchanged from Assignment 1 — re-read that assignment's spec for the full behavioral details. Nothing about *what* the class does has changed.

One method deserves special mention: **`removeAllOfValue`'s code does not need to change at all between Assignment 1 and this one.** Look back at what that method is actually built from — a loop that calls `rotateDrum(1)`, `feedDrum()`, and `ejectDrum()`, and nothing else. It never reaches into the drum's storage directly, so it has no idea whether that storage is an array or a linked list, and it doesn't need to know. As long as the three methods it depends on behave correctly, `removeAllOfValue` behaves correctly automatically, regardless of what's underneath them. If you find yourself rewriting or "porting" this method's logic for the new implementation, that's a sign something else is off — the whole point of programming to an interface is that code like this shouldn't have to care what changed underneath it.

What's different is purely internal:

* No array (`T[]`, `Object[]`, etc.) may be used as your storage for the drum or the tunnel.
* No `java.util` container class may be used anywhere (`ArrayList`, `LinkedList`, `HashMap`, `Queue`, `Deque`, `Stack`, etc. — these are examples, not an exhaustive list).
* You must build your own node-based, **doubly-linked** structure (each node has both a `next` and a `prev` reference) and use it for both the drum (a fixed-capacity ring — think about how you represent "the opening" and what rotating actually needs to move) and the tunnel (a fixed-capacity chain with two ends). Use the same node class for both.

A few things worth thinking about as you design your node class and its operations:

* **The drum's `prev` reference won't get much use.** Every drum operation — feeding, ejecting, rotating, even `removeAllOfValue` — can be done by only ever following `next` (rotating "backward" is just rotating "forward" by `capacity - steps`). That's fine and expected; the requirement to build the drum with the same doubly-linked node type as the tunnel is for consistency (one node class, not two), not because the drum's own logic demands `prev`. If you want to think further: a `prev` reference *would* let you rotate in whichever direction is fewer steps (useful for a rotation close to a full loop on a large drum) — that's a nice thing to notice, not something you need to implement.
* **The drum is circular.** A ring is naturally a circularly-linked list — the last node's `next` should point back to the first. Rotating the ring is then just a matter of moving a reference to "which node is currently the opening," not moving any data.
* **`getDrumAt(index)`/`getTunnelAt(index)` require traversal.** Unlike an array, there's no O(1) random access here — walking `index` steps from a known starting point is expected and fine.
* **The tunnel's total node count never changes.** Just like Assignment 1's array had a fixed length, your linked list should always have exactly `capacity` nodes for the lifetime of the tunnel. Insertions and evictions *reposition* nodes; they don't create or destroy them.
* **`sweepAt`'s blocked-compaction case doesn't need any structural relinking at all.** Packing non-empty values toward one end while preserving order can be done by walking two references through the affected range and swapping *values* between nodes — no array, and no need to touch `next`/`prev` for this particular operation.
* **`removeAllOfValue` still may only call `rotateDrum(1)`, `feedDrum()`, and `ejectDrum()`** — same restriction as Assignment 1, and the same algorithm works unchanged, since it never needed to know how the drum stores things in the first place.

---

## **🚫 Restrictions**

* You may only modify `PushbackGoalLinkedListImpl.java`.
* No array may be used as your primary storage for the drum or tunnel.
* No `java.util` container classes anywhere.
* Violating either restriction may result in a **zero** for the implementation portion, regardless of whether the behavioral tests pass — a correct-behaving `ArrayList`-backed (or array-backed) submission is not a correct submission for this assignment.

---

## **🧪 Testing**

```
mvn test
```

`PushbackGoalTest.java` is behaviorally identical to Assignment 1's test suite (same scenarios, same expected values) — it only calls the public interface, so it can't tell whether your storage is an array or a linked list. That's by design: your job is to make the same behavior emerge from a different internal structure.

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
| Code style, comments, and modularity (including no arrays/containers)   | 5       |
| **Total**                                                              | **100** |

### **💡 Grading Guidelines**

* The core features above are graded using Gradescope's autograder, with both visible and hidden test cases.
* If your autograder score is below 60%, your code will be manually reviewed for partial credit — manual grading can award no more than 60% of the total autograder points.
* The `Code style, comments, and modularity` line is anchored by a **hidden automated test** that scans your submitted file for array-type declarations (`T[]`, `Object[]`, etc.) and `java.util` container class usage — if it finds either, this line scores 0 regardless of anything else, since a working array- or container-backed submission is not a correct submission for this assignment. If that check passes, the full 5 points are available, with manual review still able to deduct within that same 5-point budget for genuinely poor style (unclear names, no comments on the pointer-manipulation logic, excessive duplication). This is one combined 5-point line, not two separate ones.