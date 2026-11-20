# CS 1501 — Assignment 5: PittGuard: Hardening the Network

> ⚙️ *Note:* This assignment was developed with the assistance of an AI assistant to facilitate brainstorming and generate portions of the scaffolding and documentation files, thereby accelerating the prototyping process. This was done over multiple iterations, with full manual read-through and edits.

## 🔐 Security Context

After an incident, a security team asks two questions about the network:

1. **Which links would an attacker use?** An attacker does not need every link, only the cheapest set that reaches every machine.
2. **Which vulnerable machines should we patch first?** Patching takes a machine offline, and each night there is only a limited maintenance window.

In this assignment you will answer both, one step at a time, and the first step feeds the second:

* 🌲 **A minimum spanning tree** finds the attacker's cheapest campaign (the *backbone*).
* 🎒 **Dynamic programming (0/1 knapsack)** picks the patches that remove the most exposure within the maintenance window.

## 📘 Objective

Implement a command-line tool, **PittGuard**, that reads a network and a maintenance budget and prints two lines, one per step:

```
backbone: ...
best: ...
```

**The first line is graded on its own**, so a mistake in Step 2 does not cost you the points for Step 1. Step 2 builds on Step 1, so it can only be correct if the backbone is.

---

# 🧱 Graph File Format

```
<num_nodes>
node_id vulnerable          (num_nodes lines)
...
node1 node2 latency encryption_level      (any number of lines, may be zero)
...
```

* `node_id` — a string with no spaces. IDs are **case-sensitive** (`a` and `A` are different machines).
* `vulnerable` — exactly `true` or `false`.
* `latency` — a **positive integer**.
* `encryption_level` — `1`, `2` or `3` (3 is the strongest).
* Blank lines and lines that start with `#` are ignored. Fields are separated by any amount of spaces or tabs. Files may use Windows (`\r\n`) line endings.
* Links are **undirected**: the link `A B 3 3` can be used in both directions.

> ⚠️ **Node names can be longer than one letter.** The examples mostly use single letters like `A` and `C`, but the tests use multi-character IDs such as `Gateway`, `db-server`, `10.0.0.1`, `web_01` and `N0042`. Do not store IDs as a `char`, do not compute an index with `id.charAt(0) - 'A'`, and do not assume an ID is one character long. Keep IDs as `String`s and map them to array indices (for example with a `HashMap<String, Integer>`).

**You may assume every input file is well formed**, so you do not need to validate it. In addition, every test file has **no self-loops and no duplicate links**, and the **backbone (Step 1) is unique**, so any correct spanning-tree algorithm gives the same tree.

### 📄 Example file used throughout: `TestFiles/campus.txt`

```
10
# Nodes (format: node vulnerable?)
P false
A true
B true
C true
D true
E true
F true
G false
H true
Z true

# Edges (format: node1 node2 latency encryption_level)
A B 3 3
B C 2 3
C D 2 3
C E 4 2
D E 1 3
P C 4 3
P F 6 1
P G 2 3
G H 5 2
F H 1 3
```

Vulnerable machines: A B C D E F H Z. Safe machines: P and G. Machine Z has no links at all.

---

# 📏 Two Measures of a Link

Every link has two numbers computed from its latency and encryption level. They answer different questions, so do not mix them up.

| Measure | Formula | Used in | What it means |
| ------- | ------- | ------- | ------------- |
| **Attacker effort** | `latency × encryption_level` | Step 1 (backbone) | How hard the link is for an attacker to break and cross. Slow, strongly encrypted links take longer. Fast, weakly encrypted links are cheap targets. |
| **Downtime** (hours) | `latency × (1 + (3 − encryption_level) / 10)` | Step 2 (patch cost) | How long it takes to re-establish and re-verify the link after a machine is patched. Slow and weakly encrypted links take longer. |

For downtime, level 3 → ×1.0, level 2 → ×1.1, level 1 → ×1.2.

> ⚠️ **Precision.** Attacker effort is already a whole number. Downtime is always a whole number of tenths of an hour (`latency × (13 − level)` tenths), so **store every downtime and the budget as integers in tenths**. Do not use `double` for the budget comparison: `cost <= budget` must be exact.

---

# 🧩 The Two Steps

## 🌲 Step 1 — The backbone (minimum spanning tree)

The **backbone** is the cheapest set of links, by **attacker effort**, that keeps every group of machines connected: the attacker's cheapest campaign. If the network is split into several groups, the backbone is a minimum spanning *forest*, one tree per group. With `n` machines in `c` groups it always has `n − c` links.

* This step uses the **whole network**: every machine and every link, vulnerable or not.
* Any link that is not in the backbone is redundant: it closes a loop, and it is the most expensive link in that loop.
* A machine's **backbone degree** is the number of backbone links that touch it. You need this in Step 2.

Output line:

```
backbone: <total attacker effort of the backbone> <number of backbone links>
```

## 🎒 Step 2 — The patch plan (0/1 knapsack)

You have a maintenance window of `--budget` hours. Choose which machines to patch.

* **Only vulnerable machines can be patched.** Safe machines are never candidates.
* The **downtime** of a machine is the sum of the **downtime of all of its links**, including links to safe machines. A machine with no links has downtime 0.
* The **value** of a machine is its **exposure**:

```
value = 1 + backbone degree
```

  The backbone degree counts the routes an attacker would use through the machine, so a machine that sits on many of them is worth more to protect. The `1 +` makes every machine worth something.
* Choose the set of machines whose **total downtime is at most the budget** and whose **total value is as large as possible**. Each machine can be chosen at most once.

Output line:

```
best: <the largest total value>
```

You print only the best total value, not the set of machines (several different sets can reach the same value).

> 💡 **Why each machine counts on its own.** Machines are patched one after another: a machine is taken offline, patched, brought back and its links are re-verified, then the next one is done. So each machine's downtime and value do not depend on which other machines you choose, and the totals are simple sums. A link between two machines you both patch is re-verified once from each end, so its downtime counts in both machines' downtime. This independence is what makes the plan a 0/1 knapsack.

---

# 💻 Command-Line Interface

```bash
java PittGuard --input <file> --budget <hours>
```

* `--budget` — a non-negative number of hours with **at most one decimal place** (`0`, `5`, `12.5`, `20.0` are fine; `-1`, `1.25`, `abc` are errors).

## 🧾 Output Format

Exactly two lines, in this order, with single spaces between items and no trailing spaces:

```
backbone: <total effort> <link count>
best: <value>
```

If the network has no machines, print `backbone: 0 0` and `best: 0`.

---

# 🧮 Worked Example (`campus.txt`)

## Step 1

Attacker effort of each link (`latency × level`):

| Link | A–B | B–C | C–D | C–E | D–E | P–C | P–F | P–G | G–H | F–H |
| ---- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Effort | 9 | 6 | 6 | 8 | 3 | 12 | 6 | 6 | 10 | 3 |

Two links are redundant:

* `C–E` (8) closes the triangle C–D–E, where `C–D` (6) and `D–E` (3) are cheaper.
* `G–H` (10) closes the loop P–F–H–G, where the other three links cost 6, 3 and 6.

The other 8 links form the backbone, with total effort 9 + 6 + 6 + 3 + 12 + 6 + 6 + 3 = 51. (`Z` is its own group with no links, so there are 10 machines in 2 groups and 8 backbone links.)

```
backbone: 51 8
```

Backbone degrees: P 3, C 3, B 2, D 2, F 2, A 1, E 1, G 1, H 1, Z 0.

## Step 2

Downtime of a link (hours): A–B 3.0, B–C 2.0, C–D 2.0, C–E 4.4, D–E 1.0, P–C 4.0, P–F 7.2, P–G 2.0, G–H 5.5, F–H 1.0.

The candidates are the vulnerable machines:

| Machine | Links | Downtime | Backbone degree | Value |
| ------- | ----- | -------- | :-------------: | :---: |
| A | A–B | 3.0 | 1 | 2 |
| B | A–B, B–C | 5.0 | 2 | 3 |
| C | B–C, C–D, C–E, P–C | 12.4 | 3 | 4 |
| D | C–D, D–E | 3.0 | 2 | 3 |
| E | C–E, D–E | 5.4 | 1 | 2 |
| F | P–F, F–H | 8.2 | 2 | 3 |
| H | G–H, F–H | 6.5 | 1 | 2 |
| Z | none | 0.0 | 0 | 1 |

Results for different maintenance windows:

| Budget (hours) | 0 | 3.0 | 8.0 | 12.0 | 20.0 | 30.0 | 60.0 |
| -------------- | :-: | :-: | :-: | :--: | :--: | :--: | :--: |
| `best` | 1 | 4 | 7 | 9 | 12 | 15 | 20 |

For example, with 10.0 hours the best plan is B, D and Z: 5.0 + 3.0 + 0.0 = 8.0 hours for a value of 3 + 3 + 1 = 7, and nothing else fits in the remaining 2.0 hours. With 0 hours you can still patch Z, because it has no downtime. With 60.0 hours everything fits (the total downtime is 43.5), so `best` is the sum of all values (20).

```bash
java PittGuard --input TestFiles/campus.txt --budget 10.0
```
```
backbone: 51 8
best: 7
```

---

# ⚠️ Errors

For any of the following, print **one clear message to `stderr`**, print **nothing to `stdout`**, and exit with a **non-zero** status. For every valid input exit with status **0**, including when the budget is 0. Your program must catch the problem itself: a crash with an uncaught exception or stack trace does not count.

* `--input` or `--budget` is missing, an unknown flag is given, or a flag has no value.
* `--budget` is not a non-negative number with at most one decimal place.
* The `--input` file cannot be read.

---

# 🧪 Testing Your Work

```
TestFiles/            # graphs (campus.txt is the README example)
tests/cases.tsv       # test cases: name, visibility, command-line arguments
tests/expected/       # expected output of every case
run_tests.sh          # test runner
PittGuard.java        # the only file you edit and submit
```

```bash
bash run_tests.sh                  # compile and run the provided tests
bash run_tests.sh --show           # also show a diff for each failure
bash run_tests.sh --only campus    # run only tests whose name contains "campus"
```

(On Windows use Git Bash or WSL.) The provided tests are only part of what Gradescope runs; the rest are hidden until grades are published. On Gradescope, the `backbone:` line and the `best:` line of every graph are checked by **separate** tests. **Write your own small graphs** and trace them by hand, because the hidden tests are built around these situations:

* a network with no machines, with a single machine, or with no links at all;
* a network that is already a tree (every link is in the backbone) and one with many loops;
* a network split into several groups (the backbone is a forest with `n − c` links);
* a loop where a link is redundant, and weakly encrypted links, which cost more downtime but less attacker effort;
* a budget of 0, a budget exactly equal to a machine's downtime, and a budget larger than everything;
* machines with no links (downtime 0), and safe machines that have many backbone links but cannot be patched;
* multi-character and case-sensitive node IDs.

Each test graph has at most about 500 machines and budgets of at most 200.0 hours, and each test must finish in **10 seconds**.

## 💡 Hints

* **Step 1:** sort the links by attacker effort and add each one that does not join two machines already connected (a union-find structure makes this fast).
* **Step 2:** this is the standard 0/1 knapsack, with the budget counted in tenths of an hour. Use a one-dimensional array indexed by budget and loop over the budget **downward** for each machine, so a machine is used at most once.

---

## 🚀 Deliverable

Submit your completed Java program to **Gradescope**. Only this file is submitted:

```
PittGuard.java
```

It must compile with standard Java (17 or newer), with no external libraries and no package declaration.

## 📊 Grading Rubric

| Item | Points |
| ---- | :----: |
| ✅ Autograder: Step 1, the `backbone:` line | 40 |
| ✅ Autograder: Step 2, the `best:` line | 55 |
| ✅ Autograder: command-line errors | 5 |

### 💡 Grading Guidelines

* Each output line is checked by its **own** tests, so a bug in Step 2 does not cost you the Step 1 points. The exception is that Step 2 uses the backbone from Step 1: it can only be correct if the backbone is.
* Test cases include both visible and hidden scenarios to assess correctness and edge handling.
* If the autograder score is below 60%, the code is reviewed manually for partial credit (max 60% of the available points).