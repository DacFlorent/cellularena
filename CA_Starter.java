import java.util.*;
import java.io.*;
import java.math.*;

class Pos {
    final int x;
    final int y;

    Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Organ {
    int id;
    int owner;
    int parentId;
    int rootId;
    Pos pos;
    String organType;
    String dir;

    Organ(int id, int owner, int parentId, int rootId, Pos pos, String organType, String dir) {
        this.id = id;
        this.owner = owner;
        this.parentId = parentId;
        this.rootId = rootId;
        this.pos = pos;
        this.organType = organType;
        this.dir = dir;
    }
}

class Cell {
    Pos pos;
    boolean isWall;
    String protein;
    Organ organ;

    Cell(Pos pos, boolean isWall, String protein, Organ organ) {
        this.pos = pos;
        this.isWall = isWall;
        this.protein = protein;
        this.organ = organ;
    }

    Cell(Pos pos) {
        this(pos, false, null, null);
    }
}

class Grid {
    Cell[] cells;
    int width, height;

    Grid(int width, int height) {
        this.width = width;
        this.height = height;
        cells = new Cell[width * height];
        reset();
    }

    void reset() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[x + width * y] = new Cell(new Pos(x, y));
            }
        }
    }

    Cell getCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return cells[x + width * y];
        }
        return null;
    }

    Cell getCell(Pos pos) {
        return getCell(pos.x, pos.y);
    }

    void setCell(Pos pos, Cell cell) {
        cells[pos.x + width * pos.y] = cell;
    }
}

class Game {
    Grid grid;
    Map<String, Integer> myProteins;
    Map<String, Integer> oppProteins;
    List<Organ> myOrgans;
    List<Organ> oppOrgans;
    Map<Integer, Organ> organMap;

    Game(int width, int height) {
        grid = new Grid(width, height);
        myProteins = new HashMap<>();
        oppProteins = new HashMap<>();
        myOrgans = new ArrayList<>();
        oppOrgans = new ArrayList<>();
        organMap = new HashMap<>();
    }

    void reset() {
        grid.reset();
        myOrgans.clear();
        oppOrgans.clear();
        organMap.clear();
    }
    void printGrid() {
        Organ currentOrgan = null;
        if (!myOrgans.isEmpty()) {
            currentOrgan = myOrgans.get(myOrgans.size() - 1);  // Dernier organe créé
        }

        if (currentOrgan != null) {
            int organX = currentOrgan.pos.x;
            int organY = currentOrgan.pos.y;

            for (int y = organY - 2; y <= organY + 2; y++) {
                for (int x = organX - 2; x <= organX + 2; x++) {
                    if (x >= 0 && x < grid.width && y >= 0 && y < grid.height) {
                        Cell cell = grid.getCell(x, y);
                        String entityType = "";

                        if (cell.isWall) {
                            entityType = "WALL";
                        } else if (cell.protein != null) {
                            entityType = "Protein: " + cell.protein;
                        } else if (cell.organ != null) {
                            entityType = "Organ: " + cell.organ.organType + " (ID: " + cell.organ.id + ")";
                        } else entityType = "EMPTY";

                        System.err.print("[" + x + "," + y + "]: " + entityType + "   ");
                    }
                }
                System.err.println();
            }
        } else {
            System.err.println("No organ found.");
        }
    }
}

class Action {
    String performAction(Game game) {
        Organ currentOrgan = null;
        if (!game.myOrgans.isEmpty()) {
            currentOrgan = game.myOrgans.get(game.myOrgans.size() - 1);
        }

        if (currentOrgan != null) {
            int organX = currentOrgan.pos.x;
            int organY = currentOrgan.pos.y;
            boolean proteinFound = false;

            int radius = 2; // n + 2

            for (int y = organY - radius; y <= organY + radius; y++) {
                for (int x = organX - radius; x <= organX + radius; x++) {
                    if (x >= 0 && x < game.grid.width && y >= 0 && y < game.grid.height) {
                        Cell cell = game.grid.getCell(x, y);


                        if (cell.protein != null) {
                            proteinFound = true;
                            System.err.println("Protein found at position: [" + x + ", " + y + "] - Type: " + cell.protein);

                            return "GROW 1 17 2 HARVESTER E"; // Action de collecte de la protéine
                        }
                    }
                }
            }

            if (!proteinFound) {
                return "GROW 1 17 2 BASIC";
            }
        } else {
            return "GROW 1 17 2 BASIC";
        }

        return "GROW 1 17 2 BASIC";
    }
}

/**
 * Grow and multiply your organisms to end up larger than your opponent.
 **/
class Player {
    // Protein types
    static final String A = "A";
    static final String B = "B";
    static final String C = "C";
    static final String D = "D";

    static final String WALL = "WALL";

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt(); // columns in the game grid
        int height = in.nextInt(); // rows in the game grid

        Game game = new Game(width, height);
        Action action = new Action();

        // game loop
        while (true) {
            game.reset();

            int entityCount = in.nextInt();
            for (int i = 0; i < entityCount; i++) {
                int x = in.nextInt();
                int y = in.nextInt(); // grid coordinate
                String type = in.next(); // WALL, ROOT, BASIC, TENTACLE, HARVESTER, SPORER, A, B, C, D
                int owner = in.nextInt(); // 1 if your organ, 0 if enemy organ, -1 if neither
                int organId = in.nextInt(); // id of this entity if it's an organ, 0 otherwise
                String organDir = in.next(); // N,E,S,W or X if not an organ
                int organParentId = in.nextInt();
                int organRootId = in.nextInt();

                Pos pos = new Pos(x, y);
                Cell cell = null;
                if (type.equals(WALL)) {
                    cell = new Cell(pos, true, null, null);
                } else if (Arrays.asList(A, B, C, D).contains(type)) {
                    cell = new Cell(pos, false, type, null);
                } else {
                    Organ organ = new Organ(organId, owner, organParentId, organRootId, pos, type, organDir);
                    cell = new Cell(pos, false, null, organ);
                    if (owner == 1) {
                        game.myOrgans.add(organ);
                    } else {
                        game.oppOrgans.add(organ);
                    }
                    game.organMap.put(organId, organ);
                }

                if (cell != null) {
                    game.grid.setCell(pos, cell);
                }
            }
            int myA = in.nextInt();
            int myB = in.nextInt();
            int myC = in.nextInt();
            int myD = in.nextInt(); // your protein stock
            int oppA = in.nextInt();
            int oppB = in.nextInt();
            int oppC = in.nextInt();
            int oppD = in.nextInt(); // opponent's protein stock
            game.myProteins.put(A, myA);
            game.myProteins.put(B, myB);
            game.myProteins.put(C, myC);
            game.myProteins.put(D, myD);
            game.oppProteins.put(A, oppA);
            game.oppProteins.put(B, oppB);
            game.oppProteins.put(C, oppC);
            game.oppProteins.put(D, oppD);
            int requiredActionsCount = in.nextInt();
            for (int i = 0; i < requiredActionsCount; i++) {
                game.printGrid();


                String actionToPerform = action.performAction(game);
                System.out.println(actionToPerform);
            }
        }
    }
}
enum Direction {
    N, E, S, W;
}