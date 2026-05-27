package theisland.model;

import theisland.model.pion.Boat;
import theisland.model.pion.Creature;
import theisland.model.pion.CreatureType;
import theisland.model.pion.Explorer;
import theisland.model.tile.TerrainTile;
import theisland.model.tile.TerrainType;
import theisland.util.HexCoord;

import java.util.*;

/**
 * Plateau de jeu hexagonal de The Island.
 *
 * <p>Disposition : le plateau principal fait environ 11 colonnes × 9 rangées.
 * Les 4 coins accueillent des plages de débarquement (zones sûres).
 * La zone centrale délimitée reçoit les 40 tuiles île.</p>
 *
 * <p>On utilise un offset-column (colonnes paires décalées vers le haut).</p>
 */
public class Board {

    // Dimensions de la grille (inclut les bords de mer)
    public static final int COLS = 13;
    public static final int ROWS = 9;

    private final Map<HexCoord, Cell> cells;

    // Réserve de pions créatures non encore en jeu
    private final List<Creature> reserveSerpents;
    private final List<Creature> reserveRequins;
    private final List<Creature> reserveBaleines;
    private final List<Boat> reserveBoats;

    /**
     * Construit un plateau vide puis initialise sa géographie.
     */
    public Board() {
        cells = new HashMap<>();
        reserveSerpents = new ArrayList<>();
        reserveRequins = new ArrayList<>();
        reserveBaleines = new ArrayList<>();
        reserveBoats = new ArrayList<>();

        initGrid();
    }

    /**
     * Initialise la grille : crée les cases, marque les zones d'île,
     * les coins de plage et les cases de mer.
     */
    private void initGrid() {
        // On crée toutes les cases dans les dimensions de la grille
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                HexCoord coord = new HexCoord(c, r);
                boolean corner = isCornerBeach(c, r);
                boolean island = isIslandZone(c, r);
                cells.put(coord, new Cell(coord, corner, island));
            }
        }
    }

    /**
     * Indique si la position (c, r) est une plage de coin.
     * Les coins sont en haut-gauche, haut-droite, bas-gauche, bas-droite.
     */
    private boolean isCornerBeach(int c, int r) {
        // Plages de coin : 2 colonnes aux extrêmes, 3 premières/dernières rangées
        boolean leftCols = c <= 1;
        boolean rightCols = c >= COLS - 2;
        boolean topRows = r <= 1;
        boolean botRows = r >= ROWS - 2;

        return (leftCols && topRows) || (leftCols && botRows)
                || (rightCols && topRows) || (rightCols && botRows);
    }

    /**
     * Indique si la position (c, r) est dans la zone centrale de l'île
     * (peut recevoir une tuile terrain).
     */
    private boolean isIslandZone(int c, int r) {
        if (isCornerBeach(c, r)) return false;

        // Zone centrale : colonnes 2 à COLS-3, rangées 1 à ROWS-2
        // avec une forme elliptique grossière
        int centerC = COLS / 2;
        int centerR = ROWS / 2;
        double dc = (c - centerC) / ((double)(COLS / 2 - 1));
        double dr = (r - centerR) / ((double)(ROWS / 2 - 0.5));

        // Ellipse légèrement aplatie, on exclut les coins de la zone
        return (dc * dc + dr * dr) < 0.95
                && c >= 2 && c <= COLS - 3
                && r >= 1 && r <= ROWS - 2;
    }

    // ── Accès aux cases ───────────────────────────────────────────────────

    /**
     * Retourne la case à la coordonnée donnée.
     *
     * @param coord la coordonnée hexagonale
     * @return la case, ou null si hors plateau
     */
    public Cell getCell(HexCoord coord) {
        return cells.get(coord);
    }

    /**
     * Retourne toutes les cases du plateau.
     *
     * @return collection de toutes les cases
     */
    public Collection<Cell> getAllCells() {
        return cells.values();
    }

    /**
     * Retourne les cases voisines valides d'une coordonnée.
     *
     * @param coord la coordonnée centrale
     * @return liste des cases voisines présentes sur le plateau
     */
    public List<Cell> getNeighbors(HexCoord coord) {
        List<Cell> result = new ArrayList<>();
        for (HexCoord n : coord.neighbors()) {
            Cell c = cells.get(n);
            if (c != null) result.add(c);
        }
        return result;
    }

    /**
     * Retourne les cases de mer voisines d'une coordonnée.
     *
     * @param coord la coordonnée centrale
     * @return liste des cases mer voisines
     */
    public List<Cell> getSeaNeighbors(HexCoord coord) {
        return getNeighbors(coord).stream()
                .filter(Cell::isSea)
                .toList();
    }

    /**
     * Retourne les cases de terrain voisines d'une coordonnée.
     *
     * @param coord la coordonnée centrale
     * @return liste des cases ayant une tuile terrain voisines
     */
    public List<Cell> getTerrainNeighbors(HexCoord coord) {
        return getNeighbors(coord).stream()
                .filter(Cell::hasTerrain)
                .toList();
    }

    // ── Tuiles ────────────────────────────────────────────────────────────

    /**
     * Retourne toutes les cases avec une tuile terrain, triées par
     * priorité de retrait (plage en premier, puis forêt, puis montagne).
     *
     * @return liste des cases terrain par ordre de retrait
     */
    public List<Cell> getTilesByRemovalOrder() {
        return cells.values().stream()
                .filter(Cell::hasTerrain)
                .sorted(Comparator.comparingInt(cell ->
                        terrainRemovalPriority(cell.getTile().getType())))
                .toList();
    }

    private int terrainRemovalPriority(TerrainType type) {
        return switch (type) {
            case PLAGE -> 0;
            case FORET -> 1;
            case MONTAGNE -> 2;
            default -> 99;
        };
    }

    /**
     * Indique si une tuile peut légalement être retirée du plateau.
     * Les règles : la tuile doit être adjacente à la mer, et on doit
     * respecter l'ordre plage → forêt → montagne.
     *
     * @param cell la case dont on veut retirer la tuile
     * @return vrai si le retrait est autorisé
     */
    public boolean canRemoveTile(Cell cell) {
        if (!cell.hasTerrain()) return false;

        // Doit être adjacent à au moins une case mer
        boolean adjToSea = getNeighbors(cell.getCoord()).stream()
                .anyMatch(Cell::isSea);
        if (!adjToSea) return false;

        // Respecter l'ordre : pas de forêt si une plage existe encore
        // pas de montagne si une forêt existe encore
        TerrainType type = cell.getTile().getType();
        if (type == TerrainType.FORET) {
            boolean beachExists = cells.values().stream()
                    .anyMatch(c -> c.hasTerrain()
                            && c.getTile().getType() == TerrainType.PLAGE
                            && canRemoveTileIgnoreOrder(c));
            if (beachExists) return false;
        } else if (type == TerrainType.MONTAGNE) {
            boolean forestExists = cells.values().stream()
                    .anyMatch(c -> c.hasTerrain()
                            && c.getTile().getType() == TerrainType.FORET
                            && canRemoveTileIgnoreOrder(c));
            if (forestExists) return false;
        }

        return true;
    }

    /**
     * Vérifie seulement la contrainte d'adjacence à la mer,
     * sans tenir compte de l'ordre de retrait.
     */
    private boolean canRemoveTileIgnoreOrder(Cell cell) {
        return getNeighbors(cell.getCoord()).stream().anyMatch(Cell::isSea);
    }

    // ── Réserves ──────────────────────────────────────────────────────────

    /** @return la réserve de serpents de mer non encore en jeu */
    public List<Creature> getReserveSerpents() { return reserveSerpents; }

    /** @return la réserve de requins non encore en jeu */
    public List<Creature> getReserveRequins() { return reserveRequins; }

    /** @return la réserve de baleines non encore en jeu */
    public List<Creature> getReserveBaleines() { return reserveBaleines; }

    /** @return la réserve de bateaux non encore en jeu */
    public List<Boat> getReserveBoats() { return reserveBoats; }

    /**
     * Retourne toutes les créatures d'un type qui sont actuellement sur
     * le plateau (pas en réserve, pas retirées).
     *
     * @param type le type de créature cherché
     * @return liste des créatures de ce type sur le plateau
     */
    public List<Creature> getCreaturesOnBoard(CreatureType type) {
        return cells.values().stream()
                .flatMap(c -> c.getCreatures().stream())
                .filter(cr -> cr.getType() == type && !cr.isRemoved())
                .toList();
    }

    /**
     * Trouve la case sur laquelle se trouve une créature donnée.
     *
     * @param creature la créature à localiser
     * @return la case, ou null si la créature n'est pas sur le plateau
     */
    public Cell findCreatureCell(Creature creature) {
        return cells.values().stream()
                .filter(c -> c.getCreatures().contains(creature))
                .findFirst()
                .orElse(null);
    }

    /**
     * Trouve la case sur laquelle se trouve un bateau.
     *
     * @param boat le bateau à localiser
     * @return la case, ou null si introuvable
     */
    public Cell findBoatCell(Boat boat) {
        return cells.values().stream()
                .filter(c -> c.getBoats().contains(boat))
                .findFirst()
                .orElse(null);
    }

    /**
     * Trouve la case sur laquelle se trouve un explorateur (sur terre).
     *
     * @param explorer l'explorateur à localiser
     * @return la case, ou null si introuvable
     */
    public Cell findExplorerLandCell(Explorer explorer) {
        return cells.values().stream()
                .filter(c -> c.getExplorersOnLand().contains(explorer))
                .findFirst()
                .orElse(null);
    }

    /**
     * Trouve la case sur laquelle nage un explorateur.
     *
     * @param explorer l'explorateur à localiser
     * @return la case, ou null si introuvable
     */
    public Cell findSwimmerCell(Explorer explorer) {
        return cells.values().stream()
                .filter(c -> c.getSwimmers().contains(explorer))
                .findFirst()
                .orElse(null);
    }

    /**
     * Trouve la case et le bateau où se trouve un explorateur embarqué.
     * Retourne null si l'explorateur n'est sur aucun bateau.
     *
     * @param explorer l'explorateur à localiser
     * @return la case contenant le bateau, ou null
     */
    public Cell findExplorerBoatCell(Explorer explorer) {
        return cells.values().stream()
                .filter(c -> c.getBoats().stream()
                        .anyMatch(b -> b.getPassengers().contains(explorer)))
                .findFirst()
                .orElse(null);
    }
}
