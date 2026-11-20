import java.util.*;

/**
 * PittGuard -- Assignment 5: Hardening the Network
 *
 * Submit ONLY this file. It must compile with no external libraries.
 *
 *   java PittGuard --input <file> --budget <hours>
 *
 * See README.md for the full specification (file format, the two steps, output format).
 *
 * Suggested structure (you may add as many helper methods/classes as you like):
 *   1. parse the command line and read the graph file   -> main / loadNetwork
 *   2. Step 1: the backbone (minimum spanning forest by attacker effort)
 *        -> total effort, number of links, and every machine's backbone degree
 *   3. Step 2: the patch plan (0/1 knapsack over the budget, counted in tenths of an hour)
 *        -> cost  = downtime of all of the machine's links
 *        -> value = 1 + backbone degree
 *
 * Errors: print ONE helpful line to System.err and exit with a non-zero status
 *         (System.exit(1)). Print nothing to System.out in that case.
 */
public class PittGuard {

    public static void main(String[] args) {
        // TODO: parse args (--input, --budget) and reject anything the README says is an error
        // TODO: read the network (machines, vulnerable flags, links)
        // TODO: Step 1 -- compute the backbone and every machine's backbone degree
        // TODO: Step 2 -- 0/1 knapsack over the vulnerable machines
        // TODO: print exactly two lines:  "backbone: <total effort> <links>"  and  "best: <value>"
        System.out.println("TODO: PittGuard is not implemented yet");
    }
}
