import java.util.*;
import java.util.List;

class Pos {

	final int x;
	final int y;

	Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		return x == ((Pos)obj).x && y == ((Pos)obj).y;
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

	public Organ(int id, int owner, int rootId, Pos pos) {
		this.id = id;
		this.owner = owner;
		this.rootId = rootId;
		this.pos = pos;
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

	@Override
	public boolean equals(Object obj) {
		return pos.equals(((Cell)obj).pos);
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

	public Map<String, Integer> getMyProteins() {
		return myProteins;
	}

	void reset() {
		grid.reset();
		myOrgans.clear();
		oppOrgans.clear();
		organMap.clear();
		proteinPositions.clear();
	}

	void compareDistanceWithProteins(List<Organ> myOrgans, Game game) {
		List<Pos> proteinPositions = protPositionOnGrid();

		for (Organ organ : myOrgans) {

			for (Pos proteinPos : proteinPositions) {
				int deltaX = proteinPos.x - organ.pos.x; // Positif : protéine en E, négatif : protéine en W
				int deltaY = proteinPos.y - organ.pos.y; // Positif : protéine en S, négatif : protéine en N
				int distance = calculateManhattanDistance(organ.pos.x, organ.pos.y, proteinPos.x, proteinPos.y);
				String direction = getDirection(deltaX, deltaY);

				//				System.err.println("Distance from organ " + organ.id + " at position (" + organ.pos.x + ", " + organ.pos.y +
				//					") to protein A at position (" + proteinPos.x + ", " + proteinPos.y + ") is: " + distance + " Direction : " + direction);
			}
		}

	}

	int calculateManhattanDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x2 - x1) + Math.abs(y2 - y1);
	}

	private String getDirection(int deltaX, int deltaY) {
		if (deltaX == 0 && deltaY == 0) {
			return "Same position";
		}

		if (Math.abs(deltaX) > Math.abs(deltaY)) {
			return deltaX > 0 ? "E" : "W";
		} else {
			return deltaY > 0 ? "S" : "N";
		}
	}

	List<Pos> protPositionOnGrid() {
		List<Pos> proteinPositions = new ArrayList<>();

		// Parcourir toute la grille pour trouver les protéines
		for (int y = 0; y < grid.height; y++) {
			for (int x = 0; x < grid.width; x++) {
				Cell cell = grid.getCell(x, y);
				if (cell.protein != null && cell.protein.equals("A")) {
					proteinPositions.add(new Pos(x, y));
				}
			}
		}

		return proteinPositions;
	}

	// Récuperation des cellules autour de mon organe
	public List<Cell> getNeighbours(Organ organ) {
		// création de la liste de ces cellules
		List<Cell> resultNeighbours = new ArrayList<>();
		// ajout dans la liste des cellules voisines à mon organe
		Cell cellE = grid.getCell((organ.pos.x + 1), organ.pos.y);
		Cell cellW = grid.getCell((organ.pos.x - 1), organ.pos.y);
		Cell cellN = grid.getCell(organ.pos.x, (organ.pos.y - 1));
		Cell cellS = grid.getCell(organ.pos.x, (organ.pos.y + 1));
		if (cellE != null) {
			resultNeighbours.add(cellE);
		}
		if (cellW != null) {
			resultNeighbours.add(cellW);
		}
		if (cellN != null) {
			resultNeighbours.add(cellN);
		}
		if (cellS != null) {
			resultNeighbours.add(cellS);
		}
		// retourne cette liste
		return resultNeighbours;
	}

	public int computeScoreForAction(Actions action, Cell neighbour) {
		int score = 0;

		if (action == Actions.BASIC) {
			if (neighbour.protein == null) {
				score += 5; // Si la cellule n'a pas de protéine
			} else {
				score += 20; // Si la cellule a une protéine
			}
		}
		if (action == Actions.HARVESTER) {
			// TODO : pour chaque direction
			// -> récupère la cellule de la direction
			// si protein...

			if (neighbour.protein == null) {
				score += 10; //
			} else {
				score += 25;
			}

		}
		if (action == Actions.TENTACLE) {
			if (neighbour.protein == null) {
				score += 15;
			} else {
				score += 20;
			}
		}
		if (action == Actions.SPORER) {

			// IDEM TODO: score sporer = MAX(score ROOT) sur la ligne de mire
			//
			if (neighbour.protein == null) {
				score += 20;
			} else {
				score += 25;
			}
		}
		return score;
	}

	public void play(int requiredActionsCount) {
		Action action = new Action(this);
		Set<Cell> cellNeighbour = action.checkCellAround();
		List<Option> options = action.computeAvailableActions(cellNeighbour);
		options.sort(Comparator.comparingInt(o -> -o.score));

		// WHILE
		List<Integer> rootIdProcessed = new ArrayList<>();
		int count = 0;
		while (count < requiredActionsCount) {
			Option bestOption = action.chooseBestAction(options, rootIdProcessed); // Set après options
			action.doAction(bestOption);
			rootIdProcessed.add(myOrgans.get(bestOption.organId).rootId);
		}

		// gérer multi base :
		// player.numberOfAction, pareil que ton nb de root
		// tant que le player.nbActions (tant que = boucle while)
		// Selection meilleure action pour un rootId non traité
		// doAction (déjà fait)
		// Add rootId dans le Set ID traités
		// Attention gestion des cas de WAIT

	}

	// 1 : Affichage de mes organes (owner = 1)
	void displayOrgansOnGrid() {
		for (int y = 0; y < grid.height; y++) {
			for (int x = 0; x < grid.width; x++) {
				Cell cell = grid.getCell(x, y);
				if (cell.organ != null) {
					Organ organ = cell.organ;
					if (organ.owner == 1) {
						//						System.err.println("Organ ID: " + organ.id
						//							+ ", Owner: " + organ.owner
						//							+ ", Organ Parent ID: " + organ.parentId
						//							+ ", Organ Root ID: " + organ.rootId
						//							+ ", Type: " + organ.organType
						//							+ ", Direction: " + organ.dir
						//							+ ", Position: (" + x + ", " + y + ")");
					}
				}
			}
		}
	}
}

class Action {

	private final Game game;

	Action(Game game) {
		this.game = game;
	}

	// BASIC = Prot A
	// HARVESTER = Prot C + D
	// TENTACLE = B + C
	// SPORER = B + D
	// ROOT = A + B + C + D

	static Map<Actions, Map<Resources, Integer>> actionRessources() {
		Map<Actions, Map<Resources, Integer>> spendForAction = new HashMap<>();
		// For GROW (BASIC)
		Map<Resources, Integer> basicResources = new HashMap<>();
		basicResources.put(Resources.A, 1);
		spendForAction.put(Actions.BASIC, basicResources);
		// For HARVESTER
		Map<Resources, Integer> harvesterResources = new HashMap<>();
		harvesterResources.put(Resources.C, 1);
		harvesterResources.put(Resources.D, 1);
		spendForAction.put(Actions.HARVESTER, harvesterResources);
		// For TENTACLE
		Map<Resources, Integer> tentacleResources = new HashMap<>();
		tentacleResources.put(Resources.B, 1);
		tentacleResources.put(Resources.C, 1);
		spendForAction.put(Actions.TENTACLE, tentacleResources);
		// For SPORER
		Map<Resources, Integer> sporerResources = new HashMap<>();
		sporerResources.put(Resources.B, 1);
		sporerResources.put(Resources.D, 1);
		spendForAction.put(Actions.SPORER, sporerResources);
		// For ROOT
		Map<Resources, Integer> rootResources = new HashMap<>();
		rootResources.put(Resources.A, 1);
		rootResources.put(Resources.B, 1);
		rootResources.put(Resources.C, 1);
		rootResources.put(Resources.D, 1);
		spendForAction.put(Actions.ROOT, rootResources);

		return spendForAction;
	}

	// 1 : Définir mes organes
	// 2 : Recherche autour de mes organes en n+1
	// 3 : Selon les cases autour => Définir les actions possibles Parmis les actions possibles définir un score par action (méthode rouge)
	// 4 : Classement des actions les plus rentables (gestion des égalités) -> SORT
	// 5 : Selon les ressources voir si l'action est faisable (PRENDRE LA PREMIERE REALISABLE)
	// 6 (Selon les ressources voir si l'action est faisable)
	// 7 Faire l'action
	// 8 Premier mouvement = SPORER + SPORE SHOOT vers une protein A
	// - Recherche de la protein available pour le sporer
	// - Cree le sporer
	// - Shoot la spore en n-2 de la protein
	// - Cree un HARVESTER avec la bonne direction

	// Méthode pour calculer la distance de Manhattan entre deux points
	int calculateManhattanDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x2 - x1) + Math.abs(y2 - y1);
	}

	// 2 : Retourne listes cases dispo (4 directions) autour de mes organes
	Set<Cell> checkCellAround() {
		// Cases dispo autour de mes organes
		Set<Cell> cells = new HashSet<>();

		for (Organ organ : game.myOrgans) {
			//			System.err.println("Checking organ ID: " + organ.id); // Traçabilité
			List<Cell> neighbours = game.getNeighbours(organ);
			for (Cell neighbour : neighbours) {
				if (!neighbour.isWall && neighbour.organ == null) {
					cells.add(neighbour);
					//	System.err.println("Available for grow at X : " + neighbour.pos.x + ", Y : " + neighbour.pos.y);
				}
			}
		}
		return cells;
	}

	// 3 : définir les actions possibles
	List<Option> computeAvailableActions(Set<Cell> neighbours) {
		List<Option> options = new ArrayList<>();
		// liste des actions possibles selon les cases dispo
		for (Cell neighbour : neighbours) {
			// Vérifie les conditions de base pour ajouter les actions
			if (!neighbour.isWall && neighbour.organ == null) {
				// Crée une nouvelle Option pour chaque cellule voisine
				//				System.err.println(option.neighbour);

				for (Organ organ : game.myOrgans) {
					List<Cell> organNeighbours = game.getNeighbours(organ);
					if (organNeighbours.contains(neighbour)) {
						for (Actions action : Actions.values()) {
							Option option = new Option();
							option.neighbour = neighbour;
							option.organId = organ.id;
							option.action = action;
							option.score = game.computeScoreForAction(action, neighbour);
							options.add(option);

							options.addAll(action.computeOptions(game, neighbour));

							// Pour le debug : affichage des détails de l'option
							//							System.err.println(option);
							//							System.err.println(option.score);
							//							System.err.println(option.action);
						}
						break;
					}
				}
				//				System.err.println("Available actions : " + game.availableActions());
			}
		}

		// TODO : score ROOT à partir de sporer
		// si j'ai des sporer : alors je calcule pour chacune des cases de la ligne de mire
		// le score d'aparition d'un root :
		// B-R-S-> - - -
		// génère 3 options (3 root possible)
		//		System.err.println(options);
		return options;
	}



	private static final Option WAIT = new Option();

	// 4 (dans la class game)
	// 5 choisir parmis les actions possibles celle avec le score le plus haut
	Option chooseBestAction(List<Option> options, List<Integer> rootIdProcessed) {
		//		System.err.println(options);

		for (Option option : options) {
			//			System.err.println("choose "  + option);
			if (option != null
				&& !rootIdProcessed.contains(game.myOrgans.get(option.organId).rootId)
				&& canBuild(option.action, game)) {
				//				System.err.println(options);
				//				System.err.println(option.action);
				System.err.println("Best action : " + option + " with score: "
					+ option.score + " at coordinates X: " + option.neighbour.pos.x
					+ ", Y: " + option.neighbour.pos.y);
				return option;
			}

		}
		return WAIT;
	}

	private boolean canBuild(Actions action, Game game) {
		// Récupérer les ressources nécessaires pour l'action
		Map<Resources, Integer> requiredResources = actionRessources().get(action);
		//		requiredResources
		//			.forEach((r,v) -> System.err.println(action + "required  " + r + " v " + v));
		//		Map<String, Integer> availableResources = game.getMyProteins();
		//		availableResources
		//			.forEach((r,v) -> System.err.println("available  " + r + " v " + v));
		//		System.err.println("myprot" + game.getMyProteins());
		//		System.err.println("After retrieving: " + availableResources);
		if (requiredResources == null) {
			System.err.println("Aucune ressources restante");
			return false; // Pas de ressources, impossible de construire
		}
		// Parcourir les ressources nécessaires pour l'action
		for (Map.Entry<Resources, Integer> entry : requiredResources.entrySet()) {
			Resources resource = entry.getKey();
			int requiredAmount = entry.getValue();

			// Vérifier si nous avons la quantité nécessaire de chaque ressource
			int availableAmount = game.getMyProteins().getOrDefault(resource.name(), 0);

			// Si la quantité disponible est inférieure à la quantité requise, l'action échoue
			if (availableAmount < requiredAmount) {
				//				System.err.println("Pas assez de ressources pour " + resource.name() + ". Disponible: "
				//					+ availableAmount + ", Nécessaire: " + requiredAmount);
				return false;  // Pas assez de ressources
			}
		}
		return true;  // Si toutes les ressources sont suffisantes, on peut construire
	}

	// si plusieurs actions avec le score identiques choisir celle avec une protéine

	// 7 Faire l'action
	void doAction(Option bestOption) {
		System.out.println(bestOption.toString());
	}

	// 8 Chosir la protein pour le sporer :

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

			int numberOfActions = 0;
			for (Organ organ : game.myOrgans) {
				if (organ.organType.equals("ROOT")) {
					numberOfActions++;
				}
			}
			System.err.println("Actions count : " + numberOfActions);

			for (int i = 0; i < requiredActionsCount; i++) {
				// Write an action using System.out.println()
				// To debug: System.err.println("Debug messages...");
			}
			// 			game.grid.displayGrid();
			//			game.displayOrgansOnGrid();
			//			game.displayProteinsOnGrid();
			//			game.displayProteinsInSpecificArea();
			//			game.updateProteinPositions();
			//			game.reachProt();
			//            Action.ActionByProtein(myA, myB, myC, myD);
			game.compareDistanceWithProteins(game.myOrgans, game);
			game.displayOrgansOnGrid();
			game.play(requiredActionsCount);

		}
	}
}

enum Direction {
	N, E, S, W;
}

enum Actions {
	WAIT {
		@Override
		public List<Option> computeOptions(Game game, Cell neighbour) {
			return Collections.emptyList();
		}
	}, ROOT {
		@Override
		public List<Option> computeOptions(Game game, Cell neighbour) {
			return Collections.emptyList();
		}
	}, TENTACLE {
		@Override
		public List<Option> computeOptions(Game game, Cell neighbour) {
			return Collections.emptyList();
		}
	}, SPORER {
		@Override
		public List<Option> computeOptions(Game game, Cell neighbour) {
			return Collections.emptyList();
		}
	}, HARVESTER {
		@Override
		public List<Option> computeOptions(Game game, Cell neighbour) {
			return Collections.emptyList();
		}
	},
	BASIC {
		@Override
		public List<Option> computeOptions(Game game, Cell neighbour) {
			return Collections.emptyList();
		}
	};

	public abstract List<Option> computeOptions(Game game, Cell neighbour);
}

enum CellType {
	WALL, EMPTY, PROTEIN, ORGAN
}

enum Resources {
	A, B, C, D
}

class Option {

	//	TODO:	Organ organ;
	public int organId;
	Actions action = Actions.WAIT;
	Cell neighbour;
	int score;

	@Override
	public String toString() {
		// écrit l'action pour le system out .println !
		if (neighbour != null && neighbour.pos != null) {
			return "GROW " + organId + " " + neighbour.pos.x + " " + neighbour.pos.y + " " + action;
		} else {
			return "WAIT";
		}
	}
}