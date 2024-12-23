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
		int distance = Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
		return distance;
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

		for (int y = 0; y < game.grid.height; y++) {
			for (int x = 0; x < game.grid.width; x++) {
				Cell cell = game.grid.getCell(x, y);
				if (cell != null && cell.protein != null) {
					Pos proteinPos = new Pos(x, y);
					int distance = this.calculateDistance(proteinPos);
					if (distance < minDistance) {
						minDistance = distance;
						closestPos = proteinPos;
					}
				}
			}
		}

		// Après avoir trouvé la protéine la plus proche, déterminer la direction
		if (closestPos != null) {
			String direction = findDirectionToProtein(closestPos);
		}

		return closestPos;
	}

	public String findDirectionToProtein(Pos proteinPos) {
		if (proteinPos == null) {
			return "UNKNOWN";  // Aucun déplacement possible si aucune protéine n'est trouvée
		}

		// Calculer la direction par rapport à l'organe
		if (proteinPos.x < this.x) {
			return "W"; // OUEST
		} else if (proteinPos.x > this.x) {
			return "E"; // EST
		} else if (proteinPos.y < this.y) {
			return "N"; // NORD
		} else if (proteinPos.y > this.y) {
			return "S"; // SUD
		}
		return "UNKNOWN"; // En cas d'égalité (même position)
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
			System.err.println("Protéine la plus proche trouvée à : (" + closestProteinPos.x + ", " + closestProteinPos.y + ")");
			System.err.println("Position de l'organe: (" + organPos.x + ", " + organPos.y + ")");
			System.err.println("Distance calculée: " + distance);

			// Si la distance est égale à 2, l'action devient immédiatement HARVESTER
			if (distance == 2) {
				System.err.println("Action HARVESTER pour l'organe " + organ.id);
				return ActionType.HARVESTER;
			} else {
				// Sinon, l'action reste BASIC
				System.err.println("Action BASIC pour l'organe " + organ.id);
				return ActionType.BASIC;
			}
		}

		// Si aucune protéine n'est trouvée, revenir à l'action par défaut
		return ActionType.BASIC;
	}

	public static String generateAction(Organ organ, Game game, ActionType actionType) {
		System.err.println("Action type générée pour l'organe " + organ.id + ": " + actionType);
		switch (actionType) {
			case HARVESTER:
				return handleHarvesterAction(organ, game);
			case BASIC:
			default:
				return handleBasicAction(organ, game);
		}
	}

	public static String handleHarvesterAction(Organ organ, Game game) {
		Pos organPos = organ.pos;
		Pos closestProteinPos = organPos.findClosestProtein(game);

		if (closestProteinPos != null) {
			// Récupérer la direction vers la protéine la plus proche
			String direction = organPos.findDirectionToProtein(closestProteinPos);
			System.err.println("Direction calculée pour HARVESTER: " + direction);

			// Utiliser la direction obtenue pour l'action HARVESTER
			return "GROW " + organ.id + " " + closestProteinPos.x + " " + closestProteinPos.y + " HARVESTER " + direction;
		}

		// Si aucune protéine n'est trouvée, utiliser l'action de base avec la direction de l'organe
		return "GROW " + organ.id + " 16 3 BASIC " + organ.dir;  // Action par défaut si aucune protéine trouvée
	}

	public static String handleBasicAction(Organ organ, Game game) {
		Pos organPos = organ.pos;
		Pos closestProteinPos = organPos.findClosestProtein(game);

		if (closestProteinPos != null) {
			String direction = "N";
			return "GROW " + organ.id + " " + closestProteinPos.x + " " + closestProteinPos.y + " BASIC " + direction;
		}
		return "GROW " + organ.id + " 16 2 BASIC " + organ.dir;  // Action par défaut si aucune protéine trouvée
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
	static final String A = "A";
	static final String B = "B";
	static final String C = "C";
	static final String D = "D";
	static final String WALL = "WALL";

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int width = in.nextInt();
		int height = in.nextInt();

		Game game = new Game(width, height);

		while (true) {
			game.reset();

			int entityCount = in.nextInt();
			for (int i = 0; i < entityCount; i++) {
				int x = in.nextInt();
				int y = in.nextInt();
				String type = in.next();
				int owner = in.nextInt();
				int organId = in.nextInt();
				String organDir = in.next();
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
			int myD = in.nextInt();
			int oppA = in.nextInt();
			int oppB = in.nextInt();
			int oppC = in.nextInt();
			int oppD = in.nextInt();

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