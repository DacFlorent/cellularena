import java.util.*;
import java.io.*;
import java.math.*;

class Pos {

	int x;
	int y;

	private Pos organPos;
	private Pos proteinPos;

	// Constructeur de la classe Pos
	Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	// Méthode pour calculer la distance entre deux positions
	public int calculateDistance(Pos other) {
		return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
	}

	// Getter et Setter pour organPos
	public Pos getOrganPos() {
		return organPos;
	}

	public void setOrganPos(Pos organPos) {
		this.organPos = organPos;
	}

	// Getter et Setter pour proteinPos
	public Pos getProteinPos() {
		return proteinPos;
	}

	public void setProteinPos(Pos proteinPos) {
		this.proteinPos = proteinPos;
	}

	// Méthode pour obtenir la position de l'organe ou de la protéine la plus proche
	public Pos findClosestProtein(Game game) {
		int minDistance = Integer.MAX_VALUE;
		Pos closestPos = null;

		// Parcours de la grille pour trouver la protéine la plus proche
		for (int y = 0; y < game.grid.height; y++) {
			for (int x = 0; x < game.grid.width; x++) {
				Cell cell = game.grid.getCell(x, y);
				if (cell != null && cell.protein != null) {
					Pos proteinPos = new Pos(x, y);
					int distance = this.calculateDistance(proteinPos); // Calcul de la distance
					if (distance < minDistance) {
						minDistance = distance;
						closestPos = proteinPos;
					}
				}
			}
		}
		return closestPos; // Retourne la position de la protéine la plus proche
	}
}

class Organ {

	int id;
	int owner;
	int parentId;
	int rootId;
	Pos pos;  // Position de l'organe
	String organType;
	String dir;

	// Constructeur d'organe
	Organ(int id, int owner, int parentId, int rootId, Pos pos, String organType, String dir) {
		this.id = id;
		this.owner = owner;
		this.parentId = parentId;
		this.rootId = rootId;
		this.pos = pos;
		this.organType = organType;
		this.dir = dir;
	}

	// Accès direct à la position de l'organe
	public Pos getPosition() {
		return pos;
	}

	// Mise à jour de la position de l'organe
	public void setPosition(Pos newPosition) {
		this.pos = newPosition;
	}
}

class Cell {

	Pos pos;
	boolean isWall;
	String protein;
	Organ organ;

	Cell(Pos pos) {
		this(pos, false, null, null);
	}

	Cell(Pos pos, boolean isWall, String protein, Organ organ) {
		this.pos = pos;
		this.isWall = isWall;
		this.protein = protein;
		this.organ = organ;
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
				cells[x + width * y] = new Cell(new Pos(x, y), false, null, null);
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

	// Détermine le type d'action à effectuer pour un organe
	public static ActionType decideActionType(Organ organ, Game game) {
		Pos organPos = organ.pos;
		Pos closestProteinPos = organPos.findClosestProtein(game);

		if (closestProteinPos != null) {
			// Calcul de la distance entre l'organe et la protéine la plus proche
			int distance = organPos.calculateDistance(closestProteinPos);

			System.err.println("Distance entre l'organe et la protéine la plus proche : " + distance);

			// Si la distance est égale à 2, l'action devient immédiatement HARVESTER
			if (distance == 2) {
				return ActionType.HARVESTER;
			} else {
				// Sinon, l'action reste BASIC
				return ActionType.BASIC;
			}
		}

		// Si aucune protéine n'est trouvée, revenir à l'action par défaut
		return ActionType.BASIC;
	}

	public static String generateAction(Organ organ, Game game, ActionType actionType) {
		switch (actionType) {
			case HARVESTER:
				return handleHarvesterAction(organ, game);
			case BASIC:
			default:
				return handleBasicAction(organ, game);
		}
	}

	public static String handleBasicAction(Organ organ, Game game) {
		Pos organPos = organ.pos;
		Pos closestProteinPos = organPos.findClosestProtein(game);

		if (closestProteinPos != null) {
			moveOrgan(organ, "BASIC"); // Déplacement simplifié basé sur l'axe X
			return "GROW " + organ.id + " " + closestProteinPos.x + " " + closestProteinPos.y + " BASIC " + organ.dir;
		}

		moveOrgan(organ, "BASIC");  // Action par défaut si aucune protéine trouvée
		return "GROW " + organ.id + " 16 3 BASIC " + organ.dir;
	}

	public static String handleHarvesterAction(Organ organ, Game game) {
		Pos organPos = organ.pos;
		Pos closestProteinPos = organPos.findClosestProtein(game);

		if (closestProteinPos != null) {
			// Actions spécifiques à HARVESTER quand l'organe est proche d'une protéine
			moveOrgan(organ, "HARVESTER");
			return "GROW " + (organ.id - 1) + " " + closestProteinPos.x + " " + closestProteinPos.y + " HARVESTER " + organ.dir;
		}

		// Si aucune protéine n'est trouvée, revenir à l'action de base
		return "GROW " + organ.id + " 16 3 BASIC " + organ.dir;
	}

	private static void moveOrgan(Organ organ, String actionType) {
		int deltaX = 0, deltaY = 0;

		if (actionType.equals("BASIC")) {
			// Si le type d'action est BASIC, on déplace l'organe uniquement sur l'axe x
			deltaX = 1;  // Déplacement de 1 sur l'axe x
			// deltaY reste 0, donc l'organe ne bouge pas sur l'axe y
		} else if (actionType.equals("HARVESTER")) {
			// Si le type d'action est HARVESTER, on déplace l'organe en fonction de sa direction
			switch (organ.dir) {
				case "N":
					deltaY = 1;  // Déplacement vers le haut (Nord)
					break;
				case "S":
					deltaY = -1;  // Déplacement vers le bas (Sud)
					break;
				case "E":
					deltaX = 1;  // Déplacement vers la droite (Est)
					break;
				case "W":
					deltaX = -1;  // Déplacement vers la gauche (Ouest)
					break;
				default:
					// Si aucune direction n'est spécifiée, on ne bouge pas
					break;
			}
		}

		// Mise à jour de la position de l'organe
		organ.pos = new Pos(organ.pos.x + deltaX, organ.pos.y + deltaY);

		// Vérification de la mise à jour de la position
		System.err.println("Organe " + organ.id + " déplacé vers : (" + organ.pos.x + ", " + organ.pos.y + ")");
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
		myProteins.clear();
		oppProteins.clear();
	}

	void addMyOrgan(Organ organ) {
		myOrgans.add(organ);  // Ajouter un organe à l'équipe du joueur
		organMap.put(organ.id, organ);  // Ajouter l'organe à la map d'organes
	}

	// Ajoute un organe à l'équipe adverse
	void addOppOrgan(Organ organ) {
		oppOrgans.add(organ);  // Ajouter un organe à l'équipe adverse
		organMap.put(organ.id, organ);  // Ajouter l'organe à la map d'organes
	}

	// Ajoute une protéine à l'équipe du joueur
	void addMyProtein(String protein, int count) {
		myProteins.put(protein, count);  // Ajouter une protéine avec un nombre donné
	}

	// Ajoute une protéine à l'équipe adverse
	void addOppProtein(String protein, int count) {
		oppProteins.put(protein, count);  // Ajouter une protéine avec un nombre donné
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

			int myProteinCount = in.nextInt();
			for (int i = 0; i < myProteinCount; i++) {
				String protein = in.next();
				int count = in.nextInt();
				game.addMyProtein(protein, count);
			}

			int oppProteinCount = in.nextInt();
			for (int i = 0; i < oppProteinCount; i++) {
				String protein = in.next();
				int count = in.nextInt();
				game.addOppProtein(protein, count);
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
				for (Organ organ : game.myOrgans) {
					ActionType actionType = Action.decideActionType(organ, game);
					String action = Action.generateAction(organ, game, actionType);
					System.out.println(action);
				}
			}
		}
	}
}

enum Direction {
	N, E, S, W;
}