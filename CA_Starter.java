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

	// Affichage de la grille complète  ( toutes les entités )
//	void displayGrid() {
//		for (int y = 0; y < height; y++) {
//			for (int x = 0; x < width; x++) {
//				Cell cell = cells[x + width * y];
//				StringBuilder cellInfo = new StringBuilder();
//
//				// Pos Cell
//				cellInfo.append("Cell(").append(x).append(", ").append(y).append("): ");
//
//				// check WALL
//				if (cell.isWall) {
//					cellInfo.append("WALL ");
//				}
//
//				// Check PROT
//				if (cell.protein != null) {
//					cellInfo.append("Protein: ").append(cell.protein).append(" ");
//				}
//
//				// CHECK ORGAN
//				if (cell.organ != null) {
//					cellInfo.append("Organ ID: ").append(cell.organ.id)
//						.append(", Type: ").append(cell.organ.organType)
//						.append(", Owner: ").append(cell.organ.owner)
//						.append(", Direction: ").append(cell.organ.dir);
//				}
//
//				System.err.println(cellInfo.toString());
//			}
//		}
//	}
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

	// MaJ de la position des proteines
//	void updateProteinPositions() {
//		proteinPositions.clear();
//		for (int y = 0; y < grid.height; y++) {
//			for (int x = 0; x < grid.width; x++) {
//				Cell cell = grid.getCell(x, y);
//				if (cell.protein != null) {
//					proteinPositions.put(cell.protein, cell.pos);
//				}
//			}
//		}
//	}

	// Affichage des proteines sur la grille
//	void displayProteinsOnGrid() {
//		for (int y = 0; y < grid.height; y++) {
//			for (int x = 0; x < grid.width; x++) {
//				Cell cell = grid.getCell(x, y);
//				if (cell.protein != null) {
//					System.err.println("Protein " + cell.protein + " is at position " + x + ", " + y);
//				}
//			}
//		}
//	}

	// Affichage des organes sur la grille
//	void displayOrgansOnGrid() {
//		for (int y = 0; y < grid.height; y++) {
//			for (int x = 0; x < grid.width; x++) {
//				Cell cell = grid.getCell(x, y);
//				if (cell.organ != null) {
//					Organ organ = cell.organ;
//					//					System.err.println("Organ ID: " + organ.id
//					//						+ ", Owner: " + organ.owner
//					//						+ ", Organ Parent ID: " + organ.parentId
//					//						+ ", Organ Root ID: " + organ.rootId
//					//						+ ", Type: " + organ.organType
//					//						+ ", Direction: " + organ.dir
//					//						+ ", Position: (" + x + ", " + y + ")");
//				}
//			}
//		}
//	}

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
			return "Same position"; // L'organe et la protéine sont au même endroit
		}

		if (Math.abs(deltaX) > Math.abs(deltaY)) {
			return deltaX > 0 ? "E" : "W"; // Protéine à droite ou à gauche
		} else {
			return deltaY > 0 ? "S" : "N"; // Protéine en bas ou en haut
		}
	}

	List<Pos> protPositionOnGrid() {
		List<Pos> proteinPositions = new ArrayList<>();

		// Parcourir toute la grille pour trouver les protéines
		for (int y = 0; y < grid.height; y++) {
			for (int x = 0; x < grid.width; x++) {
				Cell cell = grid.getCell(x, y);
				if (cell.protein != null && cell.protein.equals("A")) { // Vérifier si c'est une protéine de type "A"
					proteinPositions.add(new Pos(x, y)); // Ajouter la position de la protéine
				}
			}
		}

		return proteinPositions; // Retourner la liste des positions des protéines "A"
	}

	// affichage des proteines uniquement sur la zone de jeu de mon Organe (area restreinte)
	//	void displayProteinsInSpecificArea() {
	//		List<Pos> proteinsInArea = new ArrayList<>();
	//		// Parcourt les lignes entre Y = 1 et Y = 3 inclusivement
	//		for (int y = 1; y <= 3; y++) {
	//			// Parcourt les colonnes entre X = 1 et X = 16 inclusivement
	//			for (int x = 1; x <= 16; x++) {
	//				Cell cell = grid.getCell(x, y);
	//				if (cell != null && cell.protein != null) {
	//					proteinsInArea.add(cell.pos);
	//					System.err.println("(Specific AREA) Protein " + cell.protein + " is at position " + x + ", " + y);
	//				}
	//			}
	//		}
	//	}

//	Pos findClosestProteinInArea(Organ organ) {
//		List<Pos> proteinsInArea = getProteinsInSpecificArea();
//		Pos closestProteinPos = null;
//		int minDistance = Integer.MAX_VALUE;
//
//		for (Pos proteinPos : proteinsInArea) {
//			// Calculer la distance de Manhattan entre l'organe et la protéine
//			int deltaX = Math.abs(proteinPos.x - organ.pos.x);
//			int deltaY = Math.abs(proteinPos.y - organ.pos.y);
//			int distance = deltaX + deltaY;  // Distance de Manhattan
//
//			// Si la protéine est plus proche, mettre à jour la protéine la plus proche
//			if (distance < minDistance) {
//				minDistance = distance;
//				closestProteinPos = proteinPos;
//			}
//		}
//
//		return closestProteinPos;
//	}

	// Recherche autour de mon organe
//	List<CellType> checkCellAround(Pos pos, int radius) {
//		List<CellType> cellTypes = new ArrayList<>();
//		// Parcourt les cellules autour de la position dans le rayon donné
//		for (int x = pos.x - radius; x <= pos.x + radius; x++) {
//			for (int y = pos.y - radius; y <= pos.y + radius; y++) {
//				// Manhattan pour extraire les diagonales
//				if (Math.abs(pos.x - x) + Math.abs(pos.y - y) == radius) {
//					// Récupérer la cellule correspondante
//					Cell cell = grid.getCell(x, y);
//
//					// Déterminer le type de la cellule et l'ajouter à la liste
//					//                if (cell != null) {
//					//                    if (cell.protein != null) {
//					//                        cellTypes.add(CellType.PROTEIN);
//					//                    } else if (cell.organ != null) {
//					//                        cellTypes.add(CellType.ORGAN);
//					//                    } else {
//					//                        cellTypes.add(CellType.EMPTY);
//					//                    }
//					//
//					//                    // Affichage pour déboguer
//					//                    System.err.println("Cellule à (" + x + ", " + y + ") : " + (cell.protein != null ? "PROTEIN" : (cell.organ != null ? "ORGAN" : "EMPTY")));
//					//                }
//					if (cell != null && cell.organ != null && cell.organ.owner == 0) {
//						cellTypes.add(CellType.ORGAN);
//
//						// Affichage uniquement si la cellule contient un organe avec ownerId = 0
//						System.err.println("Cellule à (" + x + ", " + y + ") : ORGAN avec ownerId = 0");
//					}
//				}
//			}
//		}
//
//		// Retourner la liste des types de cellules trouvés
//		return cellTypes;
//	}
//
//	List<Pos> getProteinsInSpecificArea() {
//		List<Pos> proteinsInArea = new ArrayList<>();
//
//		// Parcourt les lignes entre Y = 1 et Y = 3 inclusivement
//		for (int y = 1; y <= 3; y++) {
//			// Parcourt les colonnes entre X = 1 et X = 16 inclusivement
//			for (int x = 1; x <= 16; x++) {
//				Cell cell = grid.getCell(x, y);
//				if (cell != null && cell.protein != null) {
//					proteinsInArea.add(cell.pos); // Ajout de la position de la protéine
//				}
//			}
//		}
//		return proteinsInArea;
//	}
//
//	static boolean hasSporer = false;
//	static boolean hasSpore = false;
//
//	// Rejoindre la position de la proteine la plus proche
//	void reachProt() {
//		if (!myOrgans.isEmpty()) {
//			// Récupérez le dernier organe de la liste
//			Organ lastOrgan = myOrgans.get(myOrgans.size() - 1);
//			System.err.println("lastOrgan ID : " + lastOrgan.id);
//			Organ organ = myOrgans.get(0);
//			System.err.println("Organ ID : " + organ.id);
//
//			Pos closestProteinPos = findClosestProteinInArea(lastOrgan);
//			String direction = "";
//			String actionType;
//
//			grid.width = 16;
//			grid.height = 7;
//
//			if (closestProteinPos != null) {
//
//				int deltaX = closestProteinPos.x - lastOrgan.pos.x;
//				int deltaY = closestProteinPos.y - lastOrgan.pos.y;
//
//				if (deltaY > 0) {
//					direction = "S"; // Sud
//				} else if (deltaY < 0) {
//					direction = "N"; // Nord
//				} else if (deltaX > 0) {
//					direction = "E"; // Est
//				}
//
//				// affichage du delta Y :
//				System.err.println("delta Y : " + deltaY);
//				// affichage du delta X :
//				System.err.println("delta X : " + deltaX);
//				// si delta X = 2, HARVESTER, sinon BASIC
//
//				if (deltaX != 0 && deltaY != 0) {
//					// Gestion des diagonales
//					if (deltaX == 1) {
//						actionType = "GROW " + lastOrgan.id + " " + closestProteinPos.x + " " + closestProteinPos.y + " " + "HARVESTER " + direction;
//						System.out.println(actionType);
//
//						int numActions = 3;
//						// Appeler la méthode de mouvement HARVESTER et sortir pendant 3 tours
//						for (int i = 0; i < numActions; i++) {
//							reachNextProt(i); // Passer l'itération pour personnaliser l'action
//						}
//					} else if (deltaX > 2) {
//						putSporer(organ, lastOrgan, deltaY, closestProteinPos, direction, organ.pos);
//						hasSporer = true;
//						if (hasSporer == true) {
//							shootSpore(closestProteinPos, direction);
//							hasSpore = true;
//							if (hasSpore == true) {
//								actionType = "GROW 5 " + closestProteinPos.x + " " + closestProteinPos.y + " " + "HARVESTER E";
//								System.out.println(actionType);
//							}
//						}
//					} else {
//						// Si c'est une diagonale avec une plus grande distance, on peut attendre ou gérer autrement
//						actionType = "GROW " + lastOrgan.id + " " + closestProteinPos.x + " " + closestProteinPos.y + " " + "BASIC " + direction;
//						System.out.println(actionType);
//					}
//				} else {
//					if (deltaX == 2) {
//						actionType = "GROW " + lastOrgan.id + " " + closestProteinPos.x + " " + closestProteinPos.y + " " + "HARVESTER " + direction;
//						System.out.println(actionType);
//						// sortir 3 tours
//						int numActions = 3;
//						for (int i = 0; i < numActions; i++) {
//							reachNextProt(i); // Passer l'itération pour personnaliser l'action
//						}
//					} else {
//						if (deltaX > 2) {
//							putSporer(organ, lastOrgan, deltaY, closestProteinPos, direction, organ.pos);
//							hasSporer = true;
//							if (hasSporer == true) {
//								shootSpore(closestProteinPos, direction);
//								hasSpore = true;
//								if (hasSpore == true) {
//									actionType = "GROW 5 " + closestProteinPos.x + " " + closestProteinPos.y + " " + "HARVESTER E";
//									System.out.println(actionType);
//								}
//							}
//						}
//					}
//				}
//			} else {
//				// Aucune protéine trouvée : TENTACLE ATTAQUE APRES HARVESTER
//				List<CellType> cellTypes = checkCellAround(lastOrgan.pos, 2);  // Rayon de 2
//
//				// Affichage de ce qui a été trouvé
//				System.err.println("Cellules trouvées autour de la position : " + cellTypes);
//
//				if (cellTypes.contains(CellType.ORGAN)) {
//					actionType = "GROW " + lastOrgan.id + " 16 5 TENTACLE " + direction;
//					System.out.println(actionType);
//					int numActions = 3; // Nombre d'actions à exécuter
//					for (int i = 0; i < numActions; i++) {
//						afterTentacle(i); // Passer l'itération pour personnaliser l'action
//					}
//
//				} else {
//					String actionType1 = "GROW " + organ.id + " " + "14 3 BASIC " + direction;
//					String actionType2 = "GROW " + lastOrgan.id + " " + "14 3 BASIC " + direction;
//
//					System.out.println(actionType1);
//					System.out.println(actionType2);
//
//				}
//			}
//		} else {
//			String actionType = "WAIT";
//			System.out.println(actionType);
//		}
//	}
//
//	void reachNextProt(int iteration) {
//		// faire un mouvement depuis le root Organ vers une position infinie entre " "
//		String direction = "N";
//
//		String actionType = "GROW 1 11 5 BASIC";
//		System.out.println(actionType);
//	}
//
//	void afterTentacle(int iteration) {
//		// continué la croissance apres TENTACLE
//		String direction = "S";
//
//		String actionType = "GROW 1 11 5 BASIC " + direction;
//		System.out.println(actionType);
//	}
//
//	void putSporer(Organ organ, Organ lastOrgan, int deltaY, Pos closestProteinPos, String direction, Pos pos) {
//		direction = "E";
//		int sporeX = pos.x;
//		System.err.println("sporeX : " + sporeX);
//		int sporeY = pos.y;
//		System.err.println("spore Y : " + sporeY);
//
//		if (deltaY < 0) {
//			sporeX = 1;
//			sporeY = 1;
//		} else if (deltaY > 0) {
//			sporeX = 1;
//			sporeY = 3;
//		} else {
//			sporeX = 2;
//			sporeY = 2;
//		}
//		String actionType = "GROW " + organ.id + " " + sporeX + " " + sporeY + " " + "SPORER " + direction;
//		System.out.println(actionType);
//
//		//		String actionSpore = "SPORE 3 " + closestProteinPos.x + " " + closestProteinPos.y + " ";
//		//		System.out.println(actionSpore);
//
//		//		String actionType1 = "GROW " + organ.id + " " + "9 3 BASIC " + direction;
//		//		String actionType2 = "GROW " + lastOrgan.id + " " + "9 3 BASIC " + direction;
//		//
//		//		String actionType9 = "GROW " + lastOrgan.id + " " +  "16 3 HARVESTER";
//		//		String actionType10 = "WAIT";
//		//
//		//		System.out.println(actionType1);
//		//		System.out.println(actionType2);
//		//		System.out.println(actionType9);
//		//		System.out.println(actionType10);
//	}
//
//	void shootSpore(Pos closestProteinPos, String direction) {
//		direction = "E";
//		int shootX = (closestProteinPos.x - 2);
//		int shootY = closestProteinPos.y;
//
//		String actionSpore = "SPORE 3 " + shootX + " " + shootY + " " + direction;
//		System.out.println(actionSpore);
//	}

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

	// Liste des actions possibles
//	public List<Actions> availableActions() {
//		// Création de la liste des actions
//		List<Actions> availableAction = new ArrayList<>();
//		// Ajout dans la liste des actions possibles
//		availableAction.add(Actions.BASIC);
//		availableAction.add(Actions.SPORER);
//		availableAction.add(Actions.TENTACLE);
//		availableAction.add(Actions.WAIT);
//
//		return availableAction;
//	}

	//  4 association Actions avec Scores
	//	public Map<Actions, Integer> actionScores() {
	//		Map<Actions, Integer> scores = new HashMap<>();
	//		scores.put(Actions.BASIC, 30);      // Score pour BASIC
	//		scores.put(Actions.SPORER, 20);   // Score pour SPORER
	//		scores.put(Actions.TENTACLE, 10); // Score pour TENTACLE
	//		scores.put(Actions.WAIT, 0);      // Score pour WAIT

	/// /		System.err.println("score Grow : " + scores.get(Actions.GROW));
	/// /		System.err.println("score Sporer : " + scores.get(Actions.SPORER));
	/// /		System.err.println("score Tentacle : " + scores.get(Actions.TENTACLE));
	/// /		System.err.println("score Wait : " + scores.get(Actions.WAIT));
	//
	//		return scores;
	//	}
	// association Actions avec Ressources

	public int computeScoreForAction(Actions action, Cell neighbour) {
		int score = 0;

		if (action == Actions.BASIC) {
			if (neighbour.protein == null) {
				score += 5; // Si la cellule n'a pas de protéine
			} else {
				score += 20; // Si la cellule a une protéine
			}
		} if (action == Actions.HARVESTER) {
			if (neighbour.protein == null) {
				score += 10; //
			} else {
				score += 20;
			}

		}
		return score;
	}
}

class Action {

	// BASIC = Prot A
	// HARVESTER = Prot C + D
	// TENTACLE = B + C
	// SPORER = B + D
	// ROOT = A + B + C + D


	public static Map<Actions, Map<Resources, Integer>> actionRessources() {
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
		rootResources.put(Resources.A,1);
		rootResources.put(Resources.B,1);
		rootResources.put(Resources.C,1);
		rootResources.put(Resources.D,1);
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

	// 1 : Affichage de mes organes (owner = 1)
	static void displayOrgansOnGrid(Grid grid) {
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

	// Méthode pour calculer la distance de Manhattan entre deux points
	int calculateManhattanDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x2 - x1) + Math.abs(y2 - y1);
	}

	// 2 : Retourne listes cases dispo (4 directions) autour de mes organes
	static Set<Cell> checkCellAround(List<Organ> myOrgans, Game game) {
		// Cases dispo autour de mes organes
		Set<Cell> cells = new HashSet<>();

		for (Organ organ : myOrgans) {
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
	static List<Option> computeAvailableActions(Set<Cell> neighbours, Game game) {
		List<Option> options = new ArrayList<>();
		// liste des actions possibles selon les cases dispo
		for (Cell neighbour : neighbours) {
			// Vérifie les conditions de base pour ajouter les actions
			if (!neighbour.isWall && neighbour.organ == null) {
				// Crée une nouvelle Option pour chaque cellule voisine
				Option option = new Option();
				option.neighbour = neighbour;
				// Ajoute toutes les actions disponibles pour cette cellule
				for (Actions action : Actions.values()) {
					option.action = action;
					option.score = game.computeScoreForAction(action, neighbour);
					options.add(option); // Ajoute l'option à la liste
				}
				//				System.err.println("Available actions : " + game.availableActions());
			}
		}
		return options;
	}

	static class Option {

		//	TODO:	Organ organ;
		Organ myOrgan;
		Actions action = Actions.WAIT;
		Cell neighbour;
		int score;

		@Override
		public String toString() {
			// écrit l'action pour le system out .println !
			if (myOrgan != null) {
			return "GROW " + myOrgan.id + " " + neighbour.pos.x + " " + neighbour.pos.y + " " + action;
			} else {
				return "GROW 1 " + neighbour.pos.x + " " + neighbour.pos.y + " " + action;
			}

		}
	}

	private static final Option WAIT = new Option();

	// 4 (dans la class game)
	// 5 choisir parmis les actions possibles celle avec le score le plus haut
	static Option chooseBestAction(List<Option> options,Game game) {

		for (Option option : options) {
			if (canBuild(option.action, game)) {
				System.err.println("Best action : " + option + " with score: "
					+ option.score + " at coordinates X: " + option.neighbour.pos.x
					+ ", Y: " + option.neighbour.pos.y);
				return option;
			}
		}

		return WAIT;
	}

	private static boolean canBuild(Actions action, Game game) {
		// je dois vérifier que j'ai suffisamment de protéine pour construire l'extension
		Map<Resources, Integer> requiredResources = actionRessources().get(action);
		Map<String, Integer> availableResources = game.getMyProteins();


		for (Map.Entry<Resources, Integer> entry : requiredResources.entrySet()) {
			Resources resource = entry.getKey();
			int requiredAmount = entry.getValue();

			if (requiredAmount == 0) {
				continue;
			}
			System.err.println("Quantité nécéssaire : " + requiredAmount);

			String resourceKey = resource.name();


			// Vérifier la quantité disponible
			int availableAmount = availableResources.get(resourceKey);


			System.err.println("Quantité disponible de " + resource.name() + " : " + availableAmount);

			// Si la quantité disponible est inférieure à la quantité requise, retourner false
			if (availableAmount < requiredAmount) {

				return false;
			}
		}
		return true;
	}

	// si plusieurs actions avec le score identiques choisir celle avec une protéine

	// 6 Selon les ressources voir si l'action est faisable
	static void displayResourcesForAction(Actions bestAction) {
		// Récupère les ressources nécessaires pour l'action choisie
		Map<Resources, Integer> resourcesRequired = actionRessources().get(bestAction);

		// Affiche les ressources nécessaires pour l'action choisie
		System.err.println("Resources needed for action (" + bestAction + "):");
		for (Map.Entry<Resources, Integer> entry : resourcesRequired.entrySet()) {
			System.err.println(entry.getKey() + ": " + entry.getValue());
		}
	}

	// 7 Faire l'action
	static void doAction(Option bestOption) {
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
			for (int i = 0; i < requiredActionsCount; i++) {
				System.err.println("Actions count : " + requiredActionsCount);
				// Write an action using System.out.println()
				// To debug: System.err.println("Debug messages...");
				// game.grid.displayGrid();
			}
			//			game.displayOrgansOnGrid();
			//			game.displayProteinsOnGrid();
			//			game.displayProteinsInSpecificArea();
			//			game.updateProteinPositions();
			//			game.reachProt();
			//            Action.ActionByProtein(myA, myB, myC, myD);
			game.compareDistanceWithProteins(game.myOrgans, game);
			Action.displayOrgansOnGrid(game.grid);
			Set<Cell> cellNeighbour = Action.checkCellAround(game.myOrgans, game);
			List<Action.Option> options = Action.computeAvailableActions(cellNeighbour, game);
			options.sort(Comparator.comparingInt(o -> -o.score));
			Action.Option bestOption = Action.chooseBestAction(options, game);
			//            Action.displayResourcesForAction(bestAction, game);
			Action.doAction(bestOption);
		}
	}
}

enum Direction {
	N, E, S, W;
}

enum Actions {
	ROOT, WAIT, TENTACLE, SPORER, HARVESTER, BASIC;
}

enum CellType {
	WALL, EMPTY, PROTEIN, ORGAN
}

enum Resources {
	A, B, C, D
}