// Very Basic Text-Based Pushback Goal Simulator (Bonus)

import java.util.Scanner;

public class PushbackGame {

    private static final int DRUM_CAPACITY = 4;
    private static final int TUNNEL_CAPACITY = 8;
    private static final char EMPTY = '.';

    public static void main(String[] args) {
        PushbackGoalInterface<Character> goal = new PushbackGoalArrayImpl<>();
        goal.initializeDrum(DRUM_CAPACITY, EMPTY);
        goal.initializeTunnel(TUNNEL_CAPACITY, EMPTY);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Commands:");
        System.out.println("  FEED <value>       - feed a cube into the drum's opening (e.g. FEED R)");
        System.out.println("  EJECT              - eject whatever is at the drum's opening");
        System.out.println("  ROTATE <steps>     - rotate the drum by steps (can be negative)");
        System.out.println("  LOAD <L|R>         - transfer one cube from the drum into that tunnel end");
        System.out.println("  DUMP <L|R>         - empty the whole drum into that tunnel end");
        System.out.println("  SWEEP <index> <L|R> - arm sweep from index toward that end");
        System.out.println("  BLOCK <L|R>        - block that tunnel end");
        System.out.println("  UNBLOCK <L|R>      - unblock that tunnel end");
        System.out.println("  CONTROL <start> <end> - report which color controls that zone");
        System.out.println("  Q                  - quit");
        printState(goal);

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toUpperCase();

            try {
                switch (cmd) {
                    case "Q":
                        scanner.close();
                        return;
                    case "FEED": {
                        boolean ok = goal.feedDrum(parts[1].toUpperCase().charAt(0));
                        System.out.println(ok ? "Fed." : "Opening already occupied.");
                        break;
                    }
                    case "EJECT": {
                        Character c = goal.ejectDrum();
                        System.out.println("Ejected: " + c);
                        break;
                    }
                    case "ROTATE": {
                        goal.rotateDrum(Integer.parseInt(parts[1]));
                        break;
                    }
                    case "LOAD": {
                        TunnelEnd end = parseEnd(parts[1]);
                        boolean ok = goal.loadTunnelFromDrum(end);
                        System.out.println(ok ? "Loaded into tunnel." : "Load failed.");
                        break;
                    }
                    case "DUMP": {
                        TunnelEnd end = parseEnd(parts[1]);
                        int count = goal.dumpDrumIntoTunnel(end);
                        System.out.println("Transferred " + count + " cube(s).");
                        break;
                    }
                    case "SWEEP": {
                        int index = Integer.parseInt(parts[1]);
                        TunnelEnd end = parseEnd(parts[2]);
                        int cleared = goal.sweepAt(index, end);
                        System.out.println("Cleared " + cleared + " cube(s).");
                        break;
                    }
                    case "BLOCK": {
                        goal.setBlocked(parseEnd(parts[1]), true);
                        break;
                    }
                    case "UNBLOCK": {
                        goal.setBlocked(parseEnd(parts[1]), false);
                        break;
                    }
                    case "CONTROL": {
                        int start = Integer.parseInt(parts[1]);
                        int end = Integer.parseInt(parts[2]);
                        Character control = goal.determineControl(start, end);
                        System.out.println("Control: " + (control == null ? "none/tied" : control));
                        break;
                    }
                    default:
                        System.out.println("Unknown command.");
                        continue;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                continue;
            }

            printState(goal);
        }
        scanner.close();
    }

    private static TunnelEnd parseEnd(String token) {
        return token.toUpperCase().startsWith("L") ? TunnelEnd.LEFT : TunnelEnd.RIGHT;
    }

    private static void printState(PushbackGoalInterface<Character> goal) {
        StringBuilder drum = new StringBuilder("Drum [opening->]: ");
        for (int i = 0; i < goal.drumCapacity(); i++) {
            drum.append(goal.getDrumAt(i)).append(' ');
        }
        System.out.println(drum);

        StringBuilder tunnel = new StringBuilder("Tunnel (L");
        tunnel.append(goal.isBlocked(TunnelEnd.LEFT) ? "*" : "").append("->");
        for (int i = 0; i < goal.tunnelCapacity(); i++) {
            tunnel.append(goal.getTunnelAt(i)).append(' ');
        }
        tunnel.append("<-R").append(goal.isBlocked(TunnelEnd.RIGHT) ? "*" : "");
        System.out.println(tunnel + "   (* = blocked)");
    }
}