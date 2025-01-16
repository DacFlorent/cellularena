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

	Cell cell;
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
	boolean isHarvested;

	Pos closestEnemyOrgan;
	int minDistanceToEnemy;
	Pos farthestEnemyOrgan;
	int maxDistanceToEnemy;

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

	public Cell at(Cell neighbour, Direction direction) {

		int deltaX = 0;
		int deltaY = 0;

		if (direction == Direction.W) {
			deltaX = -1;
		} else if (direction == Direction.E) {
			deltaX = 1;
		} else if (direction == Direction.S) {
			deltaY = +1;
		} else {
			deltaY = -1;
		}
		return getCell(neighbour.pos.x + deltaX, neighbour.pos.y + deltaY);
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
	private static int rootCount;

	Game(int width, int height) {
		grid = new Grid(width, height);
		myProteins = new HashMap<>();
		oppProteins = new HashMap<>();
		myOrgans = new ArrayList<>();
		oppOrgans = new ArrayList<>();
		organMap = new HashMap<>();
		proteinPositions = new HashMap<>();
	}

	public static void setRootCount(int count) {
		rootCount = count;
	}

	public static int getRootCount() {
		return rootCount;
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

	void displayGrid() {
		Map<String, Integer> proteinCache = new HashMap<>();

		for (int y = 0; y < grid.height; y++) {
			for (int x = 0; x < grid.width; x++) {
				Cell cell = grid.getCell(x, y);
				int scoreCell = 0;

				if (cell != null) {
					if (cell.isWall) {
						scoreCell = 0;
						System.err.print(scoreCell + "\t");
					} else if (cell.organ != null) {
						scoreCell = 2;
						System.err.print(cell.organ.organType + scoreCell + "\t");
					} else if (cell.protein != null) {
						scoreCell = 10;
						System.err.print(scoreCell + "\t");

						String positionProtein = x + "," + y;
						proteinCache.put(positionProtein, scoreCell);
					} else {
						scoreCell = 5;
						System.err.print(scoreCell + "\t");
					}
				}
			}
			System.err.println();
		}
	}

	// Check all organs : if organ type = HARVESTER on récupère la direction
	// selon la direction de sa vue on détermine si le voisin dans cette direction est une protéine ou pas
	// Check target cell : if target cell = PROTEIN
	// Si c'est le cas HARVESTED = true
	void checkProteinsHarvested() {
		for (int y = 0; y < grid.height; y++) {
			for (int x = 0; x < grid.width; x++) {
				Cell cell = grid.getCell(x, y);
				if (cell.organ != null && cell.organ.owner == 1 && cell.organ.organType.equals("HARVESTER")) {
					String direction = cell.organ.dir;
					Cell neighbour = null;
					//					System.err.println("" + x + "" + y + "" + direction);

					if ("N".equals(direction)) {
						neighbour = grid.getCell(x, y - 1);
					} else if ("S".equals(direction)) {
						neighbour = grid.getCell(x, y + 1);
					} else if ("E".equals(direction)) {
						neighbour = grid.getCell(x + 1, y);
					} else if ("W".equals(direction)) {
						neighbour = grid.getCell(x - 1, y);
					}
					if (neighbour != null && neighbour.protein != null) {
						neighbour.isHarvested = true;
					}
				}
			}
		}
	}

	List<Pos> enemyOrganPositions() {
		List<Pos> organPositions = new ArrayList<>();

		for (int y = 0; y < grid.height; y++) {
			for (int x = 0; x < grid.width; x++) {
				Cell cell = grid.getCell(x, y);
				if (cell.organ != null && cell.organ.owner == 0) {
					organPositions.add(new Pos(x, y));
				}
			}
		}
		return organPositions;
	}

	int calculateManhattanDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x2 - x1) + Math.abs(y2 - y1);
	}

	public static String getDirection(int deltaX, int deltaY) {
		if (deltaX == 0 && deltaY == 0) {
			return "Same position";
		}

		if (deltaX > 0) {
			return "E";
		} else if (deltaX < 0) {
			return "W";
		}

		if (deltaY > 0) {
			return "S";
		} else if (deltaY < 0) {
			return "N";
		}
		return "Aucune direction";
	}

	void compareDistanceWithEnemy(List<Organ> myOrgans) {
		List<Pos> enemyOrganPositions = enemyOrganPositions();

		for (Organ organ : myOrgans) {
			Pos closestEnemyOrgan = null;
			int minDistance = Integer.MAX_VALUE;
			Pos farthestEnemyOrgan = null;
			int maxDistance = Integer.MIN_VALUE;

			for (Pos enemyPos : enemyOrganPositions) {
				int distance = calculateManhattanDistance(organ.pos.x, organ.pos.y, enemyPos.x, enemyPos.y);

				if (distance < minDistance) {
					minDistance = distance;
					closestEnemyOrgan = enemyPos;
				}
				if (distance > maxDistance) {
					maxDistance = distance;
					farthestEnemyOrgan = enemyPos;
				}
			}

			if (closestEnemyOrgan != null) {
				organ.cell.closestEnemyOrgan = closestEnemyOrgan;
				organ.cell.minDistanceToEnemy = minDistance;
			}

			if (farthestEnemyOrgan != null) {
				organ.cell.farthestEnemyOrgan = farthestEnemyOrgan;
				organ.cell.maxDistanceToEnemy = maxDistance;
			}
		}
	}

	public List<Cell> getNeighbours(Organ organ) {
		List<Cell> resultNeighbours = new ArrayList<>();
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
		return resultNeighbours;
	}

	public void play(int requiredActionsCount) {
		Action action = new Action(this);
		Set<Cell> cellNeighbour = action.checkCellAround();

		// liste des possibilités avec un score estimé
		List<Option> options = action.computeAvailableActions(cellNeighbour);

		// decisionnaire
		options = options.stream()
			//                .filter(o -> o.score > 1000)
			.sorted(
				Comparator.comparingInt( o -> ((Option) o).score).reversed()
					.thenComparingInt(o -> distanceToEnnemy(((Option) o).neighbour))

					//.thenComparingInt((Option) o -> compareDistanceWithEnemy(o.neighbour))
			)
			.toList();

		int rootCount = 1;

		for (Organ organ : myOrgans) {
			if (organ != null && organ.organType.equals("ROOT") && organ.owner == 1) {
				rootCount++;
			}
		}

		Game.setRootCount(rootCount);

		// Sélection des actions à réaliser par ordre pour chaque root
		List<Integer> rootIdProcessed = new ArrayList<>();
		int count = 0;

		while (count < requiredActionsCount) {

			Option bestOption = action.chooseBestAction(options, rootIdProcessed);

			action.doAction(bestOption);
			count++;

			Organ organ = organMap.get(bestOption.organId);
			if (organ != null) {
				rootIdProcessed.add(organ.rootId);
			}
		}

		// gérer multi base :
		// player.numberOfAction, pareil que ton nb de root
		// tant que le player.nbActions (tant que = boucle while)
		// Selection meilleure action pour un rootId non traité
		// doAction (déjà fait)
		// Add rootId dans le Set ID traités
		// Attention gestion des cas de WAIT

	}

	private int distanceToEnnemy(Cell neighbour) {
		return 0;
	}

	// 1 : Affichage de mes organes (owner = 1)
	void displayOrgansOnGrid() {
		for (int y = 0; y < grid.height; y++) {
			for (int x = 0; x < grid.width; x++) {
				Cell cell = grid.getCell(x, y);
				if (cell.organ != null) {
					Organ organ = cell.organ;
					if (organ.owner == 1) {
						//												System.err.println("Organ ID: " + organ.id
						//													+ ", Owner: " + organ.owner
						//													+ ", Organ Parent ID: " + organ.parentId
						//													+ ", Organ Root ID: " + organ.rootId
						//													+ ", Type: " + organ.organType
						//													+ ", Direction: " + organ.dir
						//													+ ", Position: (" + x + ", " + y + ")");
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

	// 2 : Retourne listes cases dispo (4 directions) autour de mes organes
	Set<Cell> checkCellAround() {
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
		for (Cell neighbour : neighbours) {
			if (!neighbour.isWall && neighbour.organ == null) {
				// Crée une nouvelle Option pour chaque cellule voisine
				//				System.err.println(option.neighbour);

				for (Organ organ : game.myOrgans) {
					List<Cell> organNeighbours = game.getNeighbours(organ);
					if (organNeighbours.contains(neighbour)) {
						for (Actions action : Actions.values()) {
							if (action == Actions.ROOT) {
								if (organ.organType.equals("SPORER")) {
									Direction direction = Direction.valueOf(organ.dir);
									Cell target = game.grid.at(organ.cell, direction);
									int i = 1;
									while (target != null && !target.isWall && target.organ == null) {
										if (i >= 3) {
											options.addAll(Actions.ROOT.computeOptions(game, organ, target));
										}
										target = game.grid.at(target, direction);
										i++;
									}

								}
							} else {
								options.addAll(action.computeOptions(game, organ, neighbour));
							}
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
		for (Option option : options) {
			//	System.err.println("choose "  + option);
			if (option != null
				&& !rootIdProcessed.contains(game.organMap.get(option.organId).rootId)
				&& canBuild(option.action, game)) {
				//				System.err.println(options);
				//				System.err.println(option.action);
				System.err.println("Best action : " + option + " with score: "
					+ option.score + " at coordinates X: " + option.neighbour.pos.x
					+ ", Y: " + option.neighbour.pos.y + " " + option.dir);
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
			return false;
		}

		for (Map.Entry<Resources, Integer> entry : requiredResources.entrySet()) {
			Resources resource = entry.getKey();
			int requiredAmount = entry.getValue();

			int availableAmount = game.getMyProteins().getOrDefault(resource.name(), 0);

			if (availableAmount < requiredAmount) {
				//				System.err.println("Pas assez de ressources pour " + resource.name() + ". Disponible: "
				//					+ availableAmount + ", Nécessaire: " + requiredAmount);
				return false;
			}
		}
		return true;
	}

	// 7 Faire l'action
	void doAction(Option bestOption) {
		System.out.println(bestOption);
		// TODO: décrémenter ton stock
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

		int currentTurn = 0;

		// game loop
		while (true) {
			game.reset();
			currentTurn++;
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
					organ.cell = cell;
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

			//			game.displayGrid();
			// game.displayOrgansOnGrid();
			// game.displayProteinsOnGrid();
			// game.displayProteinsInSpecificArea();
			// game.updateProteinPositions();
			// game.reachProt();
			// Action.ActionByProtein(myA, myB, myC, myD);
			// if (currentTurn == 1) {
			// Pos sporerPos = game.checkMaxDistanceFromMyRoot();
			// String sporerDirection = game.sporerDirection(sporerPos);
			// game.putSporer(sporerPos, sporerDirection);
			// System.out.println(game.putSporer(sporerPos, sporerDirection));
			// game.displayGrid();
			// } else if (currentTurn == 2) {
			// Pos sporerPos = game.checkMaxDistanceFromMyRoot();
			// game.shootEmAll(sporerPos);
			// System.out.println(game.shootEmAll(sporerPos));
			// } else if (currentTurn > 2) {
			game.compareDistanceWithEnemy(game.myOrgans);
			game.checkProteinsHarvested();
			game.displayOrgansOnGrid();
			game.play(requiredActionsCount);
			//			}

		}
	}
}

enum Direction {
	N, E, S, W, FIRE;
}

enum Actions {

	WAIT {
		@Override
		public List<Option> computeOptions(Game game, Organ organ, Cell neighbour) {
			return Collections.emptyList();
		}
	},
	BASIC {
		@Override
		public List<Option> computeOptions(Game game, Organ organ, Cell neighbour) {
			int score = 0;

			if (neighbour.isHarvested && game.myProteins.get("A") > 1 && game.myProteins.get("B") > 1 && game.myProteins.get("C") > 1
				&& game.myProteins.get("D") > 1) {
				// System.err.println("Cell at " + neighbour.pos.x + "," + neighbour.pos.y + " is already harvested.");
				score = 1;
			} else if (neighbour.protein != null && (game.myProteins.get("A") < 1 || game.myProteins.get("B") < 1 || game.myProteins.get("C") < 1
				|| game.myProteins.get("D") < 1)) {
				score = 50;
			}
				if (neighbour.protein == null) {
					score = 2;
				} else {
					for (Direction direction : Direction.values()) {
						Cell target = game.grid.at(neighbour, direction);
						if (target != null && target.protein != null) {
							score += 1;
						}

						if (target != null) {
							Cell distantTarget = game.grid.at(target, direction);
							if (distantTarget != null && distantTarget.protein != null) {
								score += 2;
							}
						}
					}
				}
			return List.of(initOption(organ, neighbour, score, this, null));
		}
	},
	HARVESTER {
		@Override
		public List<Option> computeOptions(Game game, Organ organ, Cell neighbour) {

			ArrayList<Option> options = new ArrayList<>();

			int proteinCount = 0;
			int score = 0;
			Set<Cell> countedCells = new HashSet<>();

			for (Direction direction : Direction.values()) {

				Cell target = game.grid.at(neighbour, direction);

				if (target != null && target.protein != null && !target.isHarvested && !countedCells.contains(target)) {

					proteinCount++;
					countedCells.add(target);
					score = 5;
					System.err.println("proteinCount : " + target.pos.x + " " + target.pos.y + " " + proteinCount);
				}

				if (neighbour != null && neighbour.protein != null && proteinCount > 2) {
					score += 10 * proteinCount;
				} else if (neighbour != null && neighbour.protein != null && neighbour.isHarvested) {
					score -= 10;
				}

				options.add(initOption(organ, neighbour, score, this, direction));
			}
			return options;
		}
	},
	ROOT {
		private int rootCount;

		@Override
		public List<Option> computeOptions(Game game, Organ organ, Cell neighbour) {

			int score = Math.abs(organ.pos.x - neighbour.pos.x) + Math.abs(organ.pos.y - neighbour.pos.y);

			// TODO: calculer le score du ROOT en fonction des proteines qui l'entourent.
			// 3 cases autours :
			// proteine ajoute 1 au score
			// si proteine a une distance de 2 du root, on ajoute 2 supplémentaire
			if (rootCount <= 2) {
				for (Direction direction : Direction.values()) {
					Cell target = game.grid.at(neighbour, direction);
					if (target != null && target.protein != null) {
						score += 1;
					}

					if (target != null) {
						Cell distantTarget = game.grid.at(target, direction);
						if (distantTarget != null && distantTarget.protein != null) {
							score += 2;
						}
					}
				}
			}
			//			System.err.println("SPORE SCORE : " + score);
			return List.of(initOption(organ, neighbour, score, this, null));

		}

	}, TENTACLE {
		@Override
		public List<Option> computeOptions(Game game, Organ organ, Cell neighbour) {
			ArrayList<Option> options = new ArrayList<>();
			for (Direction direction : Direction.values()) {
				Cell target = game.grid.at(neighbour, direction);
				int score = 0;
				if (target != null && target.organ != null && target.organ.owner == 0) {
					score = 60;
				} else if (game.myProteins.get("B") > 50 && game.myProteins.get("C") > 50) {
					score = 100;
				} else if (neighbour.isHarvested) {
					score = 0;
				} else {
					score = 1;
				}
				options.add(initOption(organ, neighbour, score, this, direction));
			}
			return options;

		}

	}, SPORER {
		@Override
		public List<Option> computeOptions(Game game, Organ organ, Cell neighbour) {

			ArrayList<Option> options = new ArrayList<>();
			int score = 0;

			int rootCount = Game.getRootCount();
//			System.err.println("Nombre de racines à traiter : " + rootCount);

			if (organ.organType.equals("ROOT") && organ.owner == 1) {
				if (rootCount >= 2) {
					score = 1;
//					System.err.println("Limite atteinte : rootCount = " + rootCount + ", score = " + score);
				}
			}
			if (rootCount <= 2) {
				for (Direction direction : Direction.values()) {
					Cell target = game.grid.at(neighbour, direction);
					int i = 1;

					while (target != null && !target.isWall && target.organ == null) {
						i++;
						System.err.println( i + "target : " + target.pos.x + " " + target.pos.y);

						if (i > 4) {
							if (target != null && target.protein == null && (game.myProteins.get("A") > 2 && game.myProteins.get("B") > 2
								&& game.myProteins.get("C") > 2 && game.myProteins.get("D") > 2)) {
								List<Option> rootOptions = ROOT.computeOptions(game, organ, target);
								for (Option rootOption : rootOptions) {
									score = Math.max(score, rootOption.score);
									System.err.println("target : " + target.pos.x + " " + target.pos.y + " " + score);
								}
							}
						}
						target = game.grid.at(target, direction);
					}
					options.add(initOption(organ, neighbour, score, this, direction));
//					System.err.println("score SPORER : " + neighbour.pos.x + " " + neighbour.pos.y + " " + score);

				}
			}
			return options;
		}

	};

	public abstract List<Option> computeOptions(Game game, Organ organ, Cell neighbour);

	private static Option initOption(Organ organ, Cell neighbour, int score, Actions actions, Direction direction) {
		Option option = new Option();
		option.neighbour = neighbour;
		option.organId = organ.id;
		option.action = actions;
		option.score = score;
		option.dir = direction;
		return option;
	}
}

enum CellType {
	WALL, EMPTY, PROTEIN, ORGAN, HARVESTER
}

enum Resources {
	A, B, C, D
}

class Option {

	public int organId;
	public Direction dir;
	Actions action = Actions.WAIT;
	Cell neighbour;
	int score;

	@Override
	public String toString() {
		//        System.err.println("score : " + score);
		return switch (action) {
			case WAIT -> "WAIT";
			case ROOT -> "SPORE %d %d %d".formatted(organId, neighbour.pos.x, neighbour.pos.y);
			case SPORER -> "GROW %d %d %d SPORER %s".formatted(organId, neighbour.pos.x, neighbour.pos.y, dir.toString());
			case BASIC -> "GROW %d %d %d BASIC".formatted(organId, neighbour.pos.x, neighbour.pos.y);
			case TENTACLE -> "GROW %d %d %d TENTACLE %s".formatted(organId, neighbour.pos.x, neighbour.pos.y, dir.toString());
			case HARVESTER -> "GROW %d %d %d HARVESTER %s".formatted(organId, neighbour.pos.x, neighbour.pos.y, dir.toString());
		};
	}
}