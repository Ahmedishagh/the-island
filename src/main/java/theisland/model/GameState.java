package theisland.model;

import theisland.model.pion.*;
import theisland.model.tile.TerrainTile;
import theisland.model.tile.TerrainType;
import theisland.model.tile.TileAction;
import theisland.util.HexCoord;

import java.util.*;

/**
 * État global d'une partie de The Island.
 * Cette classe centralise les données de jeu et expose les méthodes
 * pour vérifier et appliquer les actions des joueurs.
 *
 * <p>Le contrôleur ({@link theisland.controller.GameController}) est
 * responsable d'appeler ces méthodes dans le bon ordre.</p>
 */
public class GameState {

    private final List<Player> players;
    private final Board board;

    private int currentPlayerIndex;
    private GamePhase phase;

    /** Résultat du dernier lancer de dé (type de créature à déplacer) */
    private CreatureType lastDiceResult;

    /** Nombre de déplacements restants dans la phase DEPLACER_PIONS */
    private int movesRemaining;

    private boolean gameOver;
    private boolean volcanoRevealed;

    // ── Initialisation ────────────────────────────────────────────────────

    /**
     * Crée un nouvel état de partie.
     *
     * @param players la liste des joueurs (2 à 4)
     */
    public GameState(List<Player> players) {
        if (players.size() < 2 || players.size() > 4) {
            throw new IllegalArgumentException("Il faut entre 2 et 4 joueurs.");
        }
        this.players = new ArrayList<>(players);
        this.board = new Board();
        this.currentPlayerIndex = 0;
        this.phase = GamePhase.SETUP;
        this.movesRemaining = 3;
        this.gameOver = false;
        this.volcanoRevealed = false;

        initPieces();
        placeTilesOnBoard();
    }

    /**
     * Initialise les pions : crée les explorateurs de chaque joueur
     * et remplit les réserves de créatures et de bateaux.
     */
    private void initPieces() {
        // Valeurs de trésor par joueur : 1,1,1,2,2,3,3,4,5,6
        int[] treasureValues = {1, 1, 1, 2, 2, 3, 3, 4, 5, 6};

        for (Player player : players) {
            List<Integer> values = new ArrayList<>();
            for (int v : treasureValues) values.add(v);
            Collections.shuffle(values); // ordre aléatoire sous les pions

            for (int i = 0; i < 10; i++) {
                Explorer e = new Explorer(i + 1, player.getColor(), values.get(i));
                player.addExplorer(e);
            }
        }

        // 5 serpents de mer en réserve (les 5 sont placés sur le plateau au début)
        for (int i = 0; i < 5; i++) {
            board.getReserveSerpents().add(new Creature(CreatureType.SERPENT_DE_MER));
        }
        // 6 requins
        for (int i = 0; i < 6; i++) {
            board.getReserveRequins().add(new Creature(CreatureType.REQUIN));
        }
        // 5 baleines
        for (int i = 0; i < 5; i++) {
            board.getReserveBaleines().add(new Creature(CreatureType.BALEINE));
        }
        // 12 bateaux
        for (int i = 0; i < 12; i++) {
            board.getReserveBoats().add(new Boat());
        }
    }

    /**
     * Mélange les 40 tuiles terrain et les pose aléatoirement
     * sur les cases de la zone île.
     * Place également 5 serpents de mer sur les cases mer du plateau
     * qui ont le symbole serpent (ici simulé par tirage aléatoire).
     */
    private void placeTilesOnBoard() {
        // Créer les 40 tuiles
        List<TerrainTile> tiles = buildTilePool();
        Collections.shuffle(tiles);

        // Récupérer les cases de la zone île
        List<Cell> islandCells = board.getAllCells().stream()
                .filter(Cell::isIslandZone)
                .toList();

        // Placer les tuiles (on prend autant de tuiles que de cases île)
        int count = Math.min(tiles.size(), islandCells.size());
        for (int i = 0; i < count; i++) {
            islandCells.get(i).setTile(tiles.get(i));
        }

        // Placer les 5 serpents de mer sur des cases mer aléatoires
        // (celles non isle et non coin)
        List<Cell> seaCells = board.getAllCells().stream()
                .filter(Cell::isSea)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        Collections.shuffle(seaCells);

        int serpentsPlaced = 0;
        for (Cell c : seaCells) {
            if (serpentsPlaced >= 5) break;
            if (!board.getReserveSerpents().isEmpty()) {
                Creature s = board.getReserveSerpents().remove(0);
                c.addCreature(s);
                serpentsPlaced++;
            }
        }
    }

    /**
     * Construit le pool de 40 tuiles terrain avec leurs actions cachées.
     *
     * <p>Distribution des tuiles terrain (d'après le sujet) :</p>
     * <ul>
     *   <li>16 Plage : baleine×3, dauphin×3, bateau×1, vent×2, dauphin×3,
     *       serpent de mer×1, requin×1, baleine×1, tourbillon×1</li>
     *   <li>16 Forêt : baleine×2, dauphin×2, bateau×3, tourbillon×2,
     *       requin×1, vent×1, serpent de mer×1, baleine×1, chasser requin×1,
     *       chasser baleine×2</li>
     *   <li>8 Montagne : dauphin×1, tourbillon×4, volcan×1, chasser requin×1,
     *       chasser baleine×1</li>
     * </ul>
     *
     * @return la liste des 40 tuiles
     */
    private List<TerrainTile> buildTilePool() {
        List<TerrainTile> pool = new ArrayList<>();

        // ── Plage (16 tuiles) ─────────────────────────────────────────────
        addTiles(pool, TerrainType.PLAGE, TileAction.BALEINE_IMMEDIATE,  3);
        addTiles(pool, TerrainType.PLAGE, TileAction.DAUPHIN,            3);
        addTiles(pool, TerrainType.PLAGE, TileAction.BATEAU_IMMEDIAT,    1);
        addTiles(pool, TerrainType.PLAGE, TileAction.VENT,               2);
        addTiles(pool, TerrainType.PLAGE, TileAction.DEPLACER_SERPENT,   1);
        addTiles(pool, TerrainType.PLAGE, TileAction.REQUIN_IMMEDIAT,    1);
        addTiles(pool, TerrainType.PLAGE, TileAction.TOURBILLON,         1);
        addTiles(pool, TerrainType.PLAGE, TileAction.CHASSER_REQUIN,     1);
        addTiles(pool, TerrainType.PLAGE, TileAction.CHASSER_BALEINE,    1);
        addTiles(pool, TerrainType.PLAGE, TileAction.DEPLACER_REQUIN,    1);
        addTiles(pool, TerrainType.PLAGE, TileAction.DEPLACER_BALEINE,   1);
        // total = 16

        // ── Forêt (16 tuiles) ─────────────────────────────────────────────
        addTiles(pool, TerrainType.FORET, TileAction.BALEINE_IMMEDIATE,  2);
        addTiles(pool, TerrainType.FORET, TileAction.DAUPHIN,            2);
        addTiles(pool, TerrainType.FORET, TileAction.BATEAU_IMMEDIAT,    3);
        addTiles(pool, TerrainType.FORET, TileAction.TOURBILLON,         2);
        addTiles(pool, TerrainType.FORET, TileAction.REQUIN_IMMEDIAT,    1);
        addTiles(pool, TerrainType.FORET, TileAction.VENT,               1);
        addTiles(pool, TerrainType.FORET, TileAction.DEPLACER_SERPENT,   1);
        addTiles(pool, TerrainType.FORET, TileAction.CHASSER_REQUIN,     1);
        addTiles(pool, TerrainType.FORET, TileAction.CHASSER_BALEINE,    2);
        addTiles(pool, TerrainType.FORET, TileAction.DEPLACER_REQUIN,    1);
        // total = 16

        // ── Montagne (8 tuiles) ───────────────────────────────────────────
        addTiles(pool, TerrainType.MONTAGNE, TileAction.DAUPHIN,          1);
        addTiles(pool, TerrainType.MONTAGNE, TileAction.TOURBILLON,       4);
        addTiles(pool, TerrainType.MONTAGNE, TileAction.VOLCAN,           1);
        addTiles(pool, TerrainType.MONTAGNE, TileAction.CHASSER_REQUIN,   1);
        addTiles(pool, TerrainType.MONTAGNE, TileAction.CHASSER_BALEINE,  1);
        // total = 8

        return pool;  // 40 tuiles au total
    }

    private void addTiles(List<TerrainTile> pool, TerrainType type,
                          TileAction action, int count) {
        for (int i = 0; i < count; i++) {
            pool.add(new TerrainTile(type, action));
        }
    }

    // ── Accesseurs ────────────────────────────────────────────────────────

    /** @return la liste des joueurs */
    public List<Player> getPlayers() { return players; }

    /** @return le plateau de jeu */
    public Board getBoard() { return board; }

    /** @return le joueur dont c'est actuellement le tour */
    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }

    /** @return l'index du joueur courant */
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }

    /** @return la phase de jeu courante */
    public GamePhase getPhase() { return phase; }

    /** Change la phase de jeu */
    public void setPhase(GamePhase phase) { this.phase = phase; }

    /** @return le résultat du dernier lancer de dé */
    public CreatureType getLastDiceResult() { return lastDiceResult; }

    /** @return le nombre de déplacements restants ce tour */
    public int getMovesRemaining() { return movesRemaining; }

    /** @return vrai si la partie est terminée */
    public boolean isGameOver() { return gameOver; }

    /** @return vrai si le volcan a été révélé */
    public boolean isVolcanoRevealed() { return volcanoRevealed; }

    // ── Logique de tour ───────────────────────────────────────────────────

    /**
     * Décrémente le compteur de déplacements.
     */
    public void useMove() {
        if (movesRemaining > 0) movesRemaining--;
    }

    /**
     * Passe au joueur suivant et réinitialise les compteurs du tour.
     */
    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        movesRemaining = 3;
        lastDiceResult = null;
        phase = GamePhase.JOUER_TUILE_CONSERVEE;
    }

    /**
     * Lance le dé de créature.
     * Le dé a 6 faces : 2 serpents, 2 requins, 2 baleines.
     *
     * @return le type de créature tiré
     */
    public CreatureType rollDice() {
        int face = new Random().nextInt(6);
        lastDiceResult = switch (face) {
            case 0, 1 -> CreatureType.SERPENT_DE_MER;
            case 2, 3 -> CreatureType.REQUIN;
            default   -> CreatureType.BALEINE;
        };
        return lastDiceResult;
    }

    /**
     * Déclenche la fin de partie (volcan révélé).
     * Tous les explorateurs encore en mer sont perdus.
     */
    public void triggerVolcano() {
        volcanoRevealed = true;
        gameOver = true;
        phase = GamePhase.FIN_DE_PARTIE;

        // Retirer tous les explorateurs encore actifs (pas sauvés)
        for (Player p : players) {
            for (Explorer e : p.getExplorers()) {
                if (e.isActive()) {
                    e.kill();
                }
            }
        }
    }

    /**
     * Retourne le joueur gagnant de la partie.
     * Critère principal : score le plus élevé.
     * Critère secondaire (égalité) : plus d'explorateurs sauvés.
     *
     * @return le joueur gagnant
     */
    public Player computeWinner() {
        return players.stream()
                .max(Comparator.comparingInt(Player::computeScore)
                        .thenComparingLong(Player::countSaved))
                .orElse(players.get(0));
    }

    /**
     * Trouve la case sur laquelle se trouve un explorateur,
     * qu'il soit sur terre, dans l'eau ou sur un bateau.
     *
     * @param explorer l'explorateur à localiser
     * @return la case, ou null
     */
    public Cell findExplorerCell(Explorer explorer) {
        Cell c = board.findExplorerLandCell(explorer);
        if (c != null) return c;
        c = board.findSwimmerCell(explorer);
        if (c != null) return c;
        return board.findExplorerBoatCell(explorer);
    }
}
