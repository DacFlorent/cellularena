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
class Action {

    public static ActionType decideActionType(Organ organ, Game game) {
        // Vérifie si l'organ est sur une protéine
        Cell currentCell = game.grid.getCell(organ.pos);
        if (currentCell != null && currentCell.protein != null) {
            return ActionType.HARVESTER;
        }

        // Vérifie si l'organ est adjacent à une protéine
        if (isAdjacentToProtein(organ.pos, game)) {
            return ActionType.HARVESTER;
        }

        // Logique par défaut : action BASIC
        return ActionType.BASIC;
    }
    private static Pos findAdjacentProtein(Pos pos, Game game) {
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] dir : directions) {
            int newX = pos.x + dir[0];
            int newY = pos.y + dir[1];
            if (newX >= 0 && newY >= 0 && newX < game.grid.width && newY < game.grid.height) {
                Cell adjacentCell = game.grid.getCell(newX, newY);
                if (adjacentCell != null && adjacentCell.protein != null) {
                    return new Pos(newX, newY);
                }
            }
        }
        return null; // Aucun voisin contenant une protéine
    }
    private static String calculateDirection(Pos from, Pos to) {
        if (to.x > from.x) return "E"; // Vers l'est
        if (to.x < from.x) return "W"; // Vers l'ouest
        if (to.y > from.y) return "S"; // Vers le sud
        if (to.y < from.y) return "N"; // Vers le nord
        throw new IllegalArgumentException("Invalid positions for direction calculation");
    }
    private static boolean isAdjacentToProtein(Pos pos, Game game) {
        // Vérifie les positions adjacentes : haut, bas, gauche, droite
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            int newX = pos.x + dir[0];
            int newY = pos.y + dir[1];
            if (newX >= 0 && newY >= 0 && newX < game.grid.width && newY < game.grid.height) {
                Cell adjacentCell = game.grid.getCell(newX, newY);
                if (adjacentCell != null && adjacentCell.protein != null) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String generateAction(Organ organ, Game game, ActionType actionType) {
        switch (actionType) {
            case HARVESTER:
                return handleHarvesterAction(organ, game);
            case BASIC:
                return handleBasicAction(organ, game);
            default:
                throw new IllegalArgumentException("Unknown action type: " + actionType);
        }
    }
    private static Pos getNextStepTowardsProtein(Pos currentPos, Pos targetPos) {
        int dx = targetPos.x - currentPos.x;
        int dy = targetPos.y - currentPos.y;

        if (Math.abs(dx) > Math.abs(dy)) {
            return new Pos(currentPos.x + Integer.signum(dx), currentPos.y);
        } else {
            return new Pos(currentPos.x, currentPos.y + Integer.signum(dy));
        }
    }
    private static String handleHarvesterAction(Organ organ, Game game) {
        Pos proteinPos = findAdjacentProtein(organ.pos, game);

        if (proteinPos != null) {
            // Récolte en direction de la protéine adjacente
            String direction = calculateDirection(organ.pos, proteinPos);
            return "HARVEST " + organ.id + " " + direction;
        }

        // Si aucune protéine adjacente n'est trouvée (devrait être rare)
        throw new IllegalStateException("No adjacent protein found for HARVESTER action");
    }

    private static String handleBasicAction(Organ organ, Game game) {
        Pos targetPos = findClosestProtein(organ.pos, game);

        if (targetPos != null) {
            // Calculer la prochaine étape
            Pos nextStep = getNextStepTowardsProtein(organ.pos, targetPos);

            if (isAdjacentToProtein(nextStep, game)) {
                // Si la prochaine étape est adjacente, basculer en mode HARVESTER
                return "HARVEST " + organ.id + " " + calculateDirection(organ.pos, targetPos);
            }

            // Sinon, continuer le mouvement étape par étape
            return "GROW " + organ.id + " " + nextStep.x + " " + nextStep.y + " BASIC";
        }

        // Si aucune protéine n'est trouvée, continuer par défaut
        return "GROW " + organ.id + " 17 8 BASIC";
    }

    private static Pos findClosestProtein(Pos organPos, Game game) {
        Pos closestProteinPos = null;
        int minDistance = Integer.MAX_VALUE;

        for (int y = 0; y < game.grid.height; y++) {
            for (int x = 0; x < game.grid.width; x++) {
                Cell cell = game.grid.getCell(x, y);
                if (cell != null && cell.protein != null) {
                    int distance = Math.abs(organPos.x - x) + Math.abs(organPos.y - y);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestProteinPos = new Pos(x, y);
                    }
                }
            }
        }
        return closestProteinPos;
    }
}



enum ActionType {
    BASIC,
    HARVESTER,
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
            int requiredActionsCount = in.nextInt(); // your number of organisms, output an action for each one in any order
            for (int i = 0; i < requiredActionsCount; i++) {
                Organ organ = game.myOrgans.get(i);

                // Appel à la méthode statique dans Action
                ActionType actionType = Action.decideActionType(organ, game);

                String action = Action.generateAction(organ, game, actionType);
                System.out.println(action);
            }
        }
    }
}