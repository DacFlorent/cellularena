import java.util.*;
import java.util.List;

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
	private int myA;

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
					//					System.err.println("Organ ID: " + organ.id
					//						+ ", Owner: " + organ.owner
					//						+ ", Organ Parent ID: " + organ.parentId
					//						+ ", Organ Root ID: " + organ.rootId
					//						+ ", Type: " + organ.organType
					//						+ ", Direction: " + organ.dir
					//						+ ", Position: (" + x + ", " + y + ")");
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

	// Recherche autour de mon organe
	List<CellType> checkCellAround(Pos pos, int radius) {
		List<CellType> cellTypes = new ArrayList<>();
		// Parcourt les cellules autour de la position dans le rayon donné
		for (int x = pos.x - radius; x <= pos.x + radius; x++) {
			for (int y = pos.y - radius; y <= pos.y + radius; y++) {
				// Manhattan pour extraire les diagonales
				if (Math.abs(pos.x - x) + Math.abs(pos.y - y) == radius) {
					// Récupérer la cellule correspondante
					Cell cell = grid.getCell(x, y);

					// Déterminer le type de la cellule et l'ajouter à la liste
					//                if (cell != null) {
					//                    if (cell.protein != null) {
					//                        cellTypes.add(CellType.PROTEIN);
					//                    } else if (cell.organ != null) {
					//                        cellTypes.add(CellType.ORGAN);
					//                    } else {
					//                        cellTypes.add(CellType.EMPTY);
					//                    }
					//
					//                    // Affichage pour déboguer
					//                    System.err.println("Cellule à (" + x + ", " + y + ") : " + (cell.protein != null ? "PROTEIN" : (cell.organ != null ? "ORGAN" : "EMPTY")));
					//                }
					if (cell != null && cell.organ != null && cell.organ.owner == 0) {
						cellTypes.add(CellType.ORGAN);

						// Affichage uniquement si la cellule contient un organe avec ownerId = 0
						System.err.println("Cellule à (" + x + ", " + y + ") : ORGAN avec ownerId = 0");
					}
				}
			}
		}

		// Retourner la liste des types de cellules trouvés
		return cellTypes;
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
	private boolean putSporerCalled = false;

	private boolean alreadyCalled() {
		return putSporerCalled;
	}

	void reachProt() {

		if (!myOrgans.isEmpty()) {
			// Récupérez le dernier organe de la liste
			Organ lastOrgan = myOrgans.get(myOrgans.size() - 1);
			System.err.println("lastOrgan ID : " + lastOrgan.id);
			Organ organ = myOrgans.get(0);
			System.err.println("Organ ID : " + organ.id);

			Pos closestProteinPos = findClosestProteinInArea(lastOrgan);
			String direction = "";
			String actionType;

			grid.width = 16;
			grid.height = 7;


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
				// si delta X = 2, HARVESTER, sinon BASIC

				if (deltaX != 0 && deltaY != 0) {
					// Gestion des diagonales
					if (deltaX == 1) {
						actionType = "GROW " + lastOrgan.id + " " + closestProteinPos.x + " " + closestProteinPos.y + " " + "HARVESTER " + direction;
						System.out.println(actionType);

//						int numActions = 3;
//						// Appeler la méthode de mouvement HARVESTER et sortir pendant 3 tours
//						for (int i = 0; i < numActions; i++) {
//							reachNextProt(i); // Passer l'itération pour personnaliser l'action
//						}
					} else if (deltaX > 2) {
						if (!alreadyCalled()) {
							putSporer(organ, lastOrgan, deltaY, closestProteinPos, direction, organ.pos);
						}

					}
				} else {
					if (deltaX == 2) {
						actionType = "GROW " + lastOrgan.id + " " + closestProteinPos.x + " " + closestProteinPos.y + " " + "HARVESTER " + direction;
						System.out.println(actionType);
						// sortir 3 tours
//						int numActions = 3;
//						for (int i = 0; i < numActions; i++) {
//							reachNextProt(i); // Passer l'itération pour personnaliser l'action
//						}
					} else {
						if (deltaX > 2) {
							if (!alreadyCalled()) {
								putSporer(organ, lastOrgan, deltaY, closestProteinPos, direction, organ.pos);
							}
						}
					}
				}
			} else {
				// Aucune protéine trouvée : TENTACLE ATTAQUE APRES HARVESTER
				List<CellType> cellTypes = checkCellAround(lastOrgan.pos, 2);  // Rayon de 2
				// Affichage de ce qui a été trouvé
				System.err.println("Cellules trouvées autour de la position : " + cellTypes);

				if (cellTypes.contains(CellType.ORGAN)) {
					actionType = "GROW " + lastOrgan.id + " 16 5 TENTACLE " + direction;
					System.out.println(actionType);
//					int numActions = 3; // Nombre d'actions à exécuter
//					for (int i = 0; i < numActions; i++) {
//						afterTentacle(i); // Passer l'itération pour personnaliser l'action
//					}
				} else {
//					String[] actions = {
//							"GROW 7 9 3 BASIC", "GROW 8 3 3 BASIC",
//							"GROW 11 9 3 BASIC", "GROW 12 9 3 BASIC",
//							"GROW 18 9 3 BASIC", "GROW 16 9 3 BASIC",
//							"GROW 20 16 3 HARVESTER", "WAIT"
//					};
//
//					for (int i = 0; i < actions.length; i += 2) {
//						if (i + 1 < actions.length) {  // Vérifier si une deuxième action existe
//							// Afficher les deux actions successivement
//							System.out.println(actions[i]);
//							System.out.println(actions[i + 1]);
//						} else {
//							// Afficher l'action restante si elle existe
//							System.out.println(actions[i]);
//						}
//					}

					int numActions = 0;
					for (int i = 0; ; i++) {
						noProtA(i);
						if (i >= 100) {  // Après 10 itérations, sortir de la boucle
							break;
						}
					}
				}
			}
		} else {
			String wait = "WAIT";
			System.out.println(wait);
		}
	}

	void reachNextProt(int iteration) {
		// faire un mouvement depuis le root Organ vers une position infinie entre " "
		String direction = "N";

		String actionType = "GROW 1 11 5 BASIC";
		System.out.println(actionType);
	}

	void noProtA(int iteration) {
		String actionType = "WAIT";
		System.out.println(actionType);
	}

	void afterTentacle(int iteration) {
		// continué la croissance apres TENTACLE
		String direction = "S";

		String actionType = "GROW 1 11 5 BASIC " + direction;
		System.out.println(actionType);
	}

	void putSporer(Organ organ, Organ lastOrgan, int deltaY, Pos closestProteinPos, String direction, Pos pos) {
		direction = "E";
		int sporeX = pos.x;
		System.err.println("sporeX : " + sporeX);
		int sporeY = pos.y;
		System.err.println("spore Y : " + sporeY);

		if (deltaY < 0) {
			sporeX = 1;
			sporeY = 1;
		} else if (deltaY > 0) {
			sporeX = 1;
			sporeY = 3;
		} else {
			sporeX = 2;
			sporeY = 2;
		}
		String actionType = "GROW " + organ.id + " " + sporeX + " " + sporeY + " SPORER " + direction;
		System.out.println(actionType);

		// Simuler validation de SPORER
		boolean sporerOk = validateAction("SPORER");

		if (sporerOk) {
			// Action 2 : SPORE
			String actionSpore = "SPORE 3 " + closestProteinPos.x + " " + closestProteinPos.y;
			System.out.println(actionSpore);

			// Simuler validation de SPORE
			boolean sporeOk = validateAction("SPORE");

			if (sporeOk) {
				// Action 3 : Débogage
				Organ sporeOrgan = new Organ(organ.id, 1, 1, new Pos(sporeX, sporeY));
				System.err.println("Spore ID : " + sporeOrgan.id);
				String actionGrow1 = "GROW 1 9 3 BASIC";
				String actionGrow2 = "GROW 5 9 3 BASIC";
				System.out.println(actionGrow1);
				System.out.println(actionGrow2);
				putSporerCalled = true;
			}

		}
	}

	// Méthode simulant la validation d'une action
	boolean validateAction(String actionType) {
		// Logique pour valider si l'action a réussi
		// Par exemple, lire une confirmation ou simuler un succès
		System.err.println("Validating action: " + actionType);
		return true; // Simuler que l'action est réussie
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
	GROW, WAIT, TENTACLE, SPORER;
}

enum CellType {
	WALL, EMPTY, PROTEIN, ORGAN
}