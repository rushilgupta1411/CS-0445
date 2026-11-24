import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Solves a Meowdoku board using recursive backtracking.
 * Usage: java Meowdoku <puzzle-file>
 */
public class Meowdoku {
    private final int n;
    private final int[][] regions;
    private final int regionMask;
    private final int[] cats;

    private Meowdoku(int n, int[][] regions, int regionMask) {
        this.n = n;
        this.regions = regions;
        this.regionMask = regionMask;
        cats = new int[n];
        Arrays.fill(cats, -1);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Meowdoku <puzzle-file>");
            return;
        }
        try {
            Meowdoku puzzle = readPuzzle(args[0]);
            if (puzzle.solve(0, 0, 0)) {
                puzzle.printSolution();
            } else {
                System.out.println("NO SOLUTION");
            }
        } catch (IOException | IllegalArgumentException exception) {
            // Invalid input is not a valid puzzle with no solution.
            System.err.println("Cannot read puzzle: " + exception.getMessage());
        }
    }

    private static Meowdoku readPuzzle(String filename) throws IOException {
        // Read the named file directly; standard input is never consulted.
        try (BufferedReader input = Files.newBufferedReader(Path.of(filename))) {
            String line = input.readLine();
            if (line == null) {
                throw new IllegalArgumentException("Missing board size.");
            }
            int n = Integer.parseInt(line.trim());
            if (n < 1 || n > 15) {
                throw new IllegalArgumentException("Board size must be between 1 and 15.");
            }
            int[][] regions = new int[n][n];
            int regionMask = 0;
            for (int row = 0; row < n; row++) {
                line = input.readLine();
                if (line == null || line.trim().length() != n) {
                    throw new IllegalArgumentException("Incorrect row length.");
                }
                line = line.trim();
                for (int col = 0; col < n; col++) {
                    char letter = line.charAt(col);
                    if (letter < 'A' || letter > 'Z') {
                        throw new IllegalArgumentException("Region names must be uppercase letters.");
                    }
                    regions[row][col] = letter - 'A';
                    regionMask |= 1 << regions[row][col];
                }
            }
            if (Integer.bitCount(regionMask) != n) {
                throw new IllegalArgumentException("Expected exactly N regions.");
            }
            return new Meowdoku(n, regions, regionMask);
        }
    }

    /**
     * Each recursive step adds one cat and undoes it if the branch fails.
     * A bit represents a used column or region; cats[row] tracks used rows.
     */
    private boolean solve(int placed, int usedColumns, int usedRegions) {
        if (placed == n) {
            return true;
        }

        int[] candidates = new int[n];
        int[] possibleRegions = new int[n];
        int[] columnChoices = new int[n];
        int[] regionChoices = new int[26];
        int fewest = Integer.MAX_VALUE;
        int choiceType = 0; // 0: row, 1: column, 2: region
        int choice = -1;

        for (int row = 0; row < n; row++) {
            if (cats[row] >= 0) {
                continue;
            }
            int allowedColumns = ((1 << n) - 1) & ~usedColumns;
            // Only immediately adjacent rows can touch this row. Rows may be
            // selected out of order, so check both neighbours when assigned.
            if (row > 0 && cats[row - 1] >= 0) {
                int neighbour = 1 << cats[row - 1];
                allowedColumns &= ~(neighbour | (neighbour << 1) | (neighbour >>> 1));
            }
            if (row + 1 < n && cats[row + 1] >= 0) {
                int neighbour = 1 << cats[row + 1];
                allowedColumns &= ~(neighbour | (neighbour << 1) | (neighbour >>> 1));
            }
            for (int col = 0; col < n; col++) {
                int columnBit = 1 << col;
                int regionBit = 1 << regions[row][col];
                if ((allowedColumns & columnBit) != 0 && (usedRegions & regionBit) == 0) {
                    candidates[row] |= columnBit;
                    possibleRegions[row] |= regionBit;
                    columnChoices[col]++;
                    regionChoices[regions[row][col]]++;
                }
            }
            int count = Integer.bitCount(candidates[row]);
            if (count == 0) {
                return false;
            }
            if (count < fewest) {
                fewest = count;
                choiceType = 0;
                choice = row;
            }
        }

        // Every unused column and region must also have at least one cell.
        // Branch on the row, column or region having the fewest legal cells.
        for (int col = 0; col < n; col++) {
            if ((usedColumns & (1 << col)) == 0) {
                if (columnChoices[col] == 0) {
                    return false;
                }
                if (columnChoices[col] < fewest) {
                    fewest = columnChoices[col];
                    choiceType = 1;
                    choice = col;
                }
            }
        }
        for (int region = 0; region < 26; region++) {
            int bit = 1 << region;
            if ((regionMask & bit) != 0 && (usedRegions & bit) == 0) {
                if (regionChoices[region] == 0) {
                    return false;
                }
                if (regionChoices[region] < fewest) {
                    fewest = regionChoices[region];
                    choiceType = 2;
                    choice = region;
                }
            }
        }

        // Distinct rows must be able to claim distinct columns AND regions.
        // These matching checks are necessary conditions, not replacements for
        // backtracking: future adjacency conflicts are still checked by solve.
        if (n - placed >= 3
                && (!hasMatching(candidates) || !hasMatching(possibleRegions))) {
            return false;
        }

        for (int row = 0; row < n; row++) {
            if (cats[row] >= 0 || (choiceType == 0 && row != choice)) {
                continue;
            }
            int remaining = candidates[row];
            while (remaining != 0) {
                int col = Integer.numberOfTrailingZeros(remaining);
                remaining &= remaining - 1;
                if ((choiceType == 1 && col != choice)
                        || (choiceType == 2 && regions[row][col] != choice)) {
                    continue;
                }
                cats[row] = col;
                if (solve(placed + 1, usedColumns | (1 << col),
                        usedRegions | (1 << regions[row][col]))) {
                    return true;
                }
                cats[row] = -1;
            }
        }
        return false;
    }

    /** Finds a distinct available column/region for each unassigned row. */
    private boolean hasMatching(int[] domains) {
        int[] matchedRow = new int[26];
        Arrays.fill(matchedRow, -1);
        int[] seen = new int[26];
        for (int row = 0; row < n; row++) {
            if (cats[row] < 0 && !augment(row, domains, matchedRow, seen, row + 1)) {
                return false;
            }
        }
        return true;
    }

    private boolean augment(int row, int[] domains, int[] matchedRow, int[] seen, int stamp) {
        int options = domains[row];
        while (options != 0) {
            int value = Integer.numberOfTrailingZeros(options);
            options &= options - 1;
            if (seen[value] == stamp) {
                continue;
            }
            seen[value] = stamp;
            if (matchedRow[value] < 0
                    || augment(matchedRow[value], domains, matchedRow, seen, stamp)) {
                matchedRow[value] = row;
                return true;
            }
        }
        return false;
    }

    private void printSolution() {
        StringBuilder answer = new StringBuilder();
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                answer.append(cats[row] == col ? '*' : '.');
            }
            answer.append('\n');
        }
        System.out.print(answer);
    }
}
