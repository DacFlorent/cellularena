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
    List<Organ> children;

    Organ(int id, int owner, int parentId, int rootId, Pos pos, String organType, String dir) {
        this.id = id;
        this.owner = owner;
        this.parentId = parentId;
        this.rootId = rootId;
        this.pos = pos;
        this.organType = organType;
        this.dir = dir;
        this.children = new ArrayList<>();
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

    // Affichage de la grille complète  ( toutes les entités )
    void displayGrid() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = cells[x + width * y];
                StringBuilder cellInfo = new StringBuilder();

                // Pos Cell
                cellInfo.append("Cell(").append(x).append(", ").append(y).append("): ");

                // check WALL
                if (cell.isWall) {
                    cellInfo.append("WALL ");
                }

                // Check PROT
                if (cell.protein != null) {
                    cellInfo.append("Protein: ").append(cell.protein).append(" ");
                }

                // CHECK ORGAN
                if (cell.organ != null) {
                    cellInfo.append("Organ ID: ").append(cell.organ.id)
                            .append(", Type: ").append(cell.organ.organType)
                            .append(", Owner: ").append(cell.organ.owner)
                            .append(", Direction: ").append(cell.organ.dir);
                }

                System.err.println(cellInfo.toString());
            }
        }
    }
}

class Game {

    Grid grid;
    Map<String, Integer> myProteins;
    Map<String, Integer> oppProteins;
    List<Organ> myOrgans;
    List<Organ> oppOrgans;
    Map<Integer, Organ> organMap;
    Map<String, Pos> proteinPositions;

    Game(int width, int height) {
        grid = new Grid(width, height);
        myProteins = new HashMap<>();
        oppProteins = new HashMap<>();
        myOrgans = new ArrayList<>();
        oppOrgans = new ArrayList<>();
        organMap = new HashMap<>();
        proteinPositions = new HashMap<>();
    }

    void reset() {
        grid.reset();
        myOrgans.clear();
        oppOrgans.clear();
        organMap.clear();
        proteinPositions.clear();
    }

    // MaJ de la position des proteines
    void updateProteinPositions() {
        proteinPositions.clear();
        for (int y = 0; y < grid.height; y++) {
            for (int x = 0; x < grid.width; x++) {
                Cell cell = grid.getCell(x, y);
                if (cell.protein != null) {
                    proteinPositions.put(cell.protein, cell.pos);
                }
            }
        }
    }

    // Affichage des proteines sur la grille
    void displayProteinsOnGrid() {
        for (int y = 0; y < grid.height; y++) {
            for (int x = 0; x < grid.width; x++) {
                Cell cell = grid.getCell(x, y);
                if (cell.protein != null) {
                    System.err.println("Protein " + cell.protein + " is at position " + x + ", " + y);
                }
            }
        }
    }

    // Affichage des organes sur la grille
    void displayOrgansOnGrid() {
        for (int y = 0; y < grid.height; y++) {
            for (int x = 0; x < grid.width; x++) {
                Cell cell = grid.getCell(x, y);
                if (cell.organ != null) {
                    Organ organ = cell.organ;
                    System.err.println("Organ ID: " + organ.id
                            + ", Owner: " + organ.owner
                            + ", Organ Parent ID: " + organ.parentId
                            + ", Organ Root ID: " + organ.rootId
                            + ", Type: " + organ.organType
                            + ", Direction: " + organ.dir
                            + ", Position: (" + x + ", " + y + ")");
                }
            }
        }
    }

    // affichage des proteines uniquement sur la zone de jeu de mon Organe (area restreinte)
    void displayProteinsInSpecificArea() {
        List<Pos> proteinsInArea = new ArrayList<>();
        // Parcourt les lignes entre Y = 1 et Y = 3 inclusivement
        for (int y = 1; y <= 3; y++) {
            // Parcourt les colonnes entre X = 1 et X = 16 inclusivement
            for (int x = 1; x <= 16; x++) {
                Cell cell = grid.getCell(x, y);
                if (cell != null && cell.protein != null) {
                    proteinsInArea.add(cell.pos);
                    System.err.println("(Specific AREA) Protein " + cell.protein + " is at position " + x + ", " + y);
                }
            }
        }
    }

    Pos findClosestProteinInArea(Organ organ) {
        List<Pos> proteinsInArea = getProteinsInSpecificArea();
        Pos closestProteinPos = null;
        int minDistance = Integer.MAX_VALUE;

        for (Pos proteinPos : proteinsInArea) {
            // Calculer la distance de Manhattan entre l'organe et la protéine
            int deltaX = Math.abs(proteinPos.x - organ.pos.x);
            int deltaY = Math.abs(proteinPos.y - organ.pos.y);
            int distance = deltaX + deltaY;  // Distance de Manhattan

            // Si la protéine est plus proche, mettre à jour la protéine la plus proche
            if (distance < minDistance) {
                minDistance = distance;
                closestProteinPos = proteinPos;
            }
        }

        return closestProteinPos;
    }

    List<Pos> getProteinsInSpecificArea() {
        List<Pos> proteinsInArea = new ArrayList<>();

        // Parcourt les lignes entre Y = 1 et Y = 3 inclusivement
        for (int y = 1; y <= 3; y++) {
            // Parcourt les colonnes entre X = 1 et X = 16 inclusivement
            for (int x = 1; x <= 16; x++) {
                Cell cell = grid.getCell(x, y);
                if (cell != null && cell.protein != null) {
                    proteinsInArea.add(cell.pos); // Ajout de la position de la protéine
                }
            }
        }
        return proteinsInArea;
    }

    // Rejoindre la position de la proteine la plus proche
    void reachProt() {
        if (!myOrgans.isEmpty()) {
            // Récupérez le dernier organe de la liste
            Organ lastOrgan = myOrgans.get(myOrgans.size() - 1);

            Pos closestProteinPos = findClosestProteinInArea(lastOrgan);
            String direction="";
            String actionType;

           grid.width = 16;
           grid.height = 3;

            if (closestProteinPos != null) {
                int deltaX = closestProteinPos.x - lastOrgan.pos.x;
                int deltaY = closestProteinPos.y - lastOrgan.pos.y;

                if (deltaY > 0) {
                    direction = "S"; // Sud
                } else if (deltaY < 0) {
                    direction = "N"; // Nord
                } else if (deltaX > 0) {
                    direction = "E"; // Est
                }

                // affichage du delta Y :
                System.err.println("delta Y : " + deltaY);
                // affichage du delta X :
                System.err.println("delta X : " + deltaX);
                // ID last Organ
                System.err.println("Dernier organe ID: " + lastOrgan.id);
                // si delta X = 2, HARVESTER, sinon BASIC
                if (deltaX == 2) {
                    actionType = "GROW " + lastOrgan.id + " " + closestProteinPos.x + " " + closestProteinPos.y + " " + "HARVESTER " + direction;
                } else {
                    actionType = "GROW " + lastOrgan.id + " " + closestProteinPos.x + " " + closestProteinPos.y + " " + "BASIC " + direction;
                }
            } else {
                // Aucune protéine trouvée : mouvement par défaut
                direction = "N"; // Choisir une direction par défaut
                actionType = "GROW " + lastOrgan.id + " " + grid.width + " " + grid.height + " " + "BASIC " + direction;
            }
            System.out.println(actionType);
        } else {
            System.err.println("Liste des organes vide !");
        }
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
                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");
                // game.grid.displayGrid();
                game.displayOrgansOnGrid();
                game.displayProteinsOnGrid();
                game.displayProteinsInSpecificArea();
            }
            game.updateProteinPositions();
            game.reachProt();
            //            List<String> actions = new ArrayList<>();
            //            for (Organ organ : game.myOrgans) {
            //                game.compareOrganToProteinsOnGrid(organ, actions);
            //            }
            //            for (String action : actions) {
            //                System.out.println(action);
            //            }
        }
    }
}

enum Direction {
    N, E, S, W;
}

enum OrganType {
    ROOT, HARVESTER, BASIC;
}

enum ActionType {
    GROW, WAIT;
}