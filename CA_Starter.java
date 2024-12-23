import java.util.*;

class Pos {

	int x;
	int y;

	Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int calculateDistance(Pos other) {
		return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
	}

	public Pos findClosestProtein(Game game) {
		int minDistance = Integer.MAX_VALUE;
		Pos closestPos = null;

		// On parcourt la grille pour trouver toutes les protéines
		for (int y = 0; y < game.grid.height; y++) {
			for (int x = 0; x < game.grid.width; x++) {
				Cell cell = game.grid.getCell(x, y);
				if (cell != null && cell.protein != null) {
					Pos proteinPos = new Pos(x, y);
					int distance = this.calculateDistance(proteinPos);
					// On garde la protéine la plus proche
					if (distance < minDistance) {
						minDistance = distance;
						closestPos = proteinPos;
					}
				}
			}
		}
		return closestPos;
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

	public Pos getPosition() {
		return pos;
	}

	public void setPosition(Pos newPosition) {
		this.pos = newPosition;
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
}

class Grid {

	Cell[] cells;
	int width;
	int height;

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

	void setCell(Pos pos, Cell cell) {
		cells[pos.x + width * pos.y] = cell;
	}
}

class Action {
	private static StringBuilder actionLogs = new StringBuilder();

	public static void measureSurroundingEntities(Organ organ, Game game) {
		Pos organPos = organ.getPosition();
		boolean proteinFoundFirstLoop = false;

		for (Direction dir : Direction.values()) {
			Pos nextPos = calculateNewPosition(organPos, dir);
			Cell nextCell = game.grid.getCell(nextPos.x, nextPos.y);

			if (nextCell != null) {
				actionLogs.append("Direction : ").append(dir).append(" à (")
					.append(nextPos.x).append(", ").append(nextPos.y).append(")\n");

				// Vérifications pour la cellule
				if (nextCell.isWall) {
					actionLogs.append("WALL\n");
				} else if (nextCell.organ != null) {
					actionLogs.append("Organ find (ID: ").append(nextCell.organ.id).append(").\n");
				} else if (nextCell.protein != null) {
					actionLogs.append("Protéine find : ").append(nextCell.protein).append(".\n");
					if (!proteinFoundFirstLoop) {
						proteinFoundFirstLoop = true;
					}
				} else {
					actionLogs.append("No entity here.\n");
				}
			}
		}

		ActionType actionType = decideActionType(proteinFoundFirstLoop, false);

		displayActionLogs();
	}

	public static ActionType decideActionType(boolean proteinFoundFirstLoop, boolean proteinFoundSecondLoop) {
		if (proteinFoundFirstLoop) {
			return ActionType.BASIC;
		}
		if (proteinFoundSecondLoop) {
			return ActionType.HARVESTER;
		}
		return ActionType.BASIC;
	}

	public static String generateAction(Organ organ, Game game) {
		// Récupérer la position actuelle de l'organe
		Pos organPos = organ.getPosition();

		// Trouver la position de la première protéine autour de l'organe
		Pos proteinPos = findProteinPosition(organ, game);

		// Si aucune protéine n'est trouvée, on utilise la méthode findClosestProtein
		if (proteinPos == null) {
			proteinPos = organ.pos.findClosestProtein(game);
		}

		// Déterminer l'action à effectuer en fonction de la présence ou non d'une protéine
		String actionType = (proteinPos != null) ? "BASIC" : "HARVESTER";  // Si une protéine est trouvée, "BASIC", sinon "HARVESTER"

		// Déterminer la direction vers la position de la protéine ou utiliser la position actuelle si aucune protéine n'est trouvée
		Pos targetPos = (proteinPos != null) ? proteinPos : organPos;  // Utiliser la position actuelle si aucune protéine
		Direction direction = findDirectionToGrow(organ, targetPos, game);

		// Générer la ligne d'action au format "GROW organId x y ACTION direction"
		String actionResult = "GROW " + organ.id + " " + targetPos.x + " " + targetPos.y + " " + actionType + " " + direction;

		return actionResult;
	}

	// Méthode pour trouver la position de la première protéine (dynamique)
	private static Pos findProteinPosition(Organ organ, Game game) {
		Pos organPos = organ.getPosition(); // Position actuelle de l'organisme

		// Recherche de la première protéine autour de l'organisme
		for (Direction dir : Direction.values()) {
			Pos nextPos = calculateNewPosition(organPos, dir);
			Cell nextCell = game.grid.getCell(nextPos.x, nextPos.y);

			// Vérification si une protéine est présente dans cette cellule
			if (nextCell != null && nextCell.protein != null) {
				return nextPos;  // Retourner la position de la protéine trouvée
			}
		}

		return null;  // Retourner null si aucune protéine n'est trouvée
	}

	private static Direction findDirectionToGrow(Organ organ, Pos targetPos, Game game) {
		Pos organPos = organ.getPosition();

		if (Math.abs(organPos.x - targetPos.x) <= 1 && Math.abs(organPos.y - targetPos.y) <= 1) {
			if (organPos.x < targetPos.x) return Direction.E;
			if (organPos.x > targetPos.x) return Direction.W;
			if (organPos.y < targetPos.y) return Direction.S;
			if (organPos.y > targetPos.y) return Direction.N;
		}

		if (organPos.x < targetPos.x) {
			return Direction.E;
		} else if (organPos.x > targetPos.x) {
			return Direction.W;
		} else if (organPos.y < targetPos.y) {
			return Direction.S;
		} else {
			return Direction.N;
		}
	}

	private static String determineTargetType(Organ organ, Game game) {
		Pos organPos = organ.getPosition();

		for (Direction dir : Direction.values()) {
			Pos nextPos = calculateNewPosition(organPos, dir);
			Cell nextCell = game.grid.getCell(nextPos.x, nextPos.y);

			if (nextCell != null) {
				if (nextCell.isWall) {
					return "WALL";
				}
				if (nextCell.organ != null && nextCell.organ.organType.equals("ROOT")) {
					return "ROOT";
				}
				if (nextCell.organ != null && nextCell.organ.organType.equals("BASIC")) {
					return "BASIC";
				}
				if (nextCell.organ != null && nextCell.organ.organType.equals("HARVESTER")) {
					return "HARVESTER";
				}
				if (nextCell.protein != null && nextCell.protein.equals("A")) {
					return "A";
				}
			}
		}

		return "BASIC";
	}

	private static Pos calculateNewPosition(Pos currentPos, Direction direction) {
		int newX = currentPos.x;
		int newY = currentPos.y;

		switch (direction) {
			case N:
				newY--;
				break;
			case S:
				newY++;
				break;
			case E:
				newX++;
				break;
			case W:
				newX--;
				break;
		}

		return new Pos(newX, newY);
	}

	public static void displayActionLogs() {
		if (actionLogs.length() > 0) {
			System.err.println(actionLogs.toString());
			actionLogs.setLength(0);  // Réinitialiser les logs après affichage
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
					game.organMap.put(organ.id, organ);
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
					Action.measureSurroundingEntities(organ, game);
					String action = Action.generateAction(organ, game);
					System.out.println(action);
					Action.displayActionLogs();
				}
			}
		}
	}
}

enum Direction {
	N, E, S, W;
}

enum ActionType {
	BASIC,
	HARVESTER,
}