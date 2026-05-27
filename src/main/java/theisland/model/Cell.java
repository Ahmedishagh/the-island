package theisland.model;

import theisland.model.pion.Boat;
import theisland.model.pion.Creature;
import theisland.model.pion.Explorer;
import theisland.model.tile.TerrainTile;
import theisland.util.HexCoord;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une case hexagonale du plateau de jeu.
 *
 * <p>Une case peut contenir :
 * <ul>
 *   <li>une tuile de terrain (si c'est une case île)</li>
 *   <li>des explorateurs sur terre</li>
 *   <li>des nageurs</li>
 *   <li>des bateaux</li>
 *   <li>des créatures marines</li>
 * </ul>
 * </p>
 */
public class Cell {

    private final HexCoord coord;

    /** La tuile posée sur cette case (null si mer libre) */
    private TerrainTile tile;

    /** Vrai si cette case fait partie des plages de coin (zones de départ) */
    private final boolean cornerBeach;

    /** Vrai si cette case fait partie de la zone délimitée pour les tuiles */
    private final boolean islandZone;

    /** Explorateurs sur terre (sur la tuile) */
    private final List<Explorer> explorersOnLand;

    /** Nageurs dans l'eau */
    private final List<Explorer> swimmers;

    /** Bateaux présents sur cette case de mer */
    private final List<Boat> boats;

    /** Créatures marines sur cette case */
    private final List<Creature> creatures;

    /**
     * Crée une nouvelle case.
     *
     * @param coord       la coordonnée hexagonale
     * @param cornerBeach vrai si c'est une plage de coin (sauvegarde)
     * @param islandZone  vrai si la case peut recevoir une tuile île
     */
    public Cell(HexCoord coord, boolean cornerBeach, boolean islandZone) {
        this.coord = coord;
        this.cornerBeach = cornerBeach;
        this.islandZone = islandZone;
        this.tile = null;
        this.explorersOnLand = new ArrayList<>();
        this.swimmers = new ArrayList<>();
        this.boats = new ArrayList<>();
        this.creatures = new ArrayList<>();
    }

    // ── Accesseurs basiques ────────────────────────────────────────────────

    /** @return la coordonnée de cette case */
    public HexCoord getCoord() { return coord; }

    /** @return la tuile terrain posée, ou null */
    public TerrainTile getTile() { return tile; }

    /** Pose une tuile terrain sur cette case */
    public void setTile(TerrainTile tile) { this.tile = tile; }

    /** @return vrai si c'est une plage de coin (zone de sauvegarde) */
    public boolean isCornerBeach() { return cornerBeach; }

    /** @return vrai si cette case est dans la zone d'île */
    public boolean isIslandZone() { return islandZone; }

    // ── État de la case ────────────────────────────────────────────────────

    /**
     * Indique si cette case est une case de mer navigable.
     * Une case est de la mer si elle n'a pas de tuile terrain ET n'est pas
     * une plage de coin.
     *
     * @return vrai si la case est une case mer
     */
    public boolean isSea() {
        return tile == null && !cornerBeach;
    }

    /**
     * Indique si cette case est une case de terrain (île).
     *
     * @return vrai si une tuile terrain est posée dessus
     */
    public boolean hasTerrain() {
        return tile != null;
    }

    // ── Explorateurs ───────────────────────────────────────────────────────

    /** @return la liste des explorateurs sur la tuile terrain */
    public List<Explorer> getExplorersOnLand() { return explorersOnLand; }

    /** @return la liste des nageurs dans l'eau */
    public List<Explorer> getSwimmers() { return swimmers; }

    /**
     * Ajoute un explorateur sur la tuile terrain.
     *
     * @param e l'explorateur à placer sur terre
     */
    public void addExplorerOnLand(Explorer e) {
        explorersOnLand.add(e);
    }

    /**
     * Retire un explorateur de la tuile terrain.
     *
     * @param e l'explorateur à retirer
     * @return vrai si l'explorateur était bien sur cette case
     */
    public boolean removeExplorerFromLand(Explorer e) {
        return explorersOnLand.remove(e);
    }

    /**
     * Ajoute un nageur dans l'eau sur cette case.
     *
     * @param e l'explorateur qui nage
     */
    public void addSwimmer(Explorer e) {
        swimmers.add(e);
    }

    /**
     * Retire un nageur de cette case.
     *
     * @param e le nageur à retirer
     * @return vrai s'il était bien là
     */
    public boolean removeSwimmer(Explorer e) {
        return swimmers.remove(e);
    }

    // ── Bateaux ────────────────────────────────────────────────────────────

    /** @return les bateaux présents sur cette case */
    public List<Boat> getBoats() { return boats; }

    /**
     * Ajoute un bateau sur cette case.
     *
     * @param b le bateau
     */
    public void addBoat(Boat b) { boats.add(b); }

    /**
     * Retire un bateau de cette case.
     *
     * @param b le bateau à retirer
     * @return vrai si le bateau était bien là
     */
    public boolean removeBoat(Boat b) { return boats.remove(b); }

    // ── Créatures ──────────────────────────────────────────────────────────

    /** @return les créatures marines sur cette case */
    public List<Creature> getCreatures() { return creatures; }

    /**
     * Ajoute une créature sur cette case.
     *
     * @param c la créature
     */
    public void addCreature(Creature c) { creatures.add(c); }

    /**
     * Retire une créature de cette case.
     *
     * @param c la créature à retirer
     * @return vrai si la créature était bien là
     */
    public boolean removeCreature(Creature c) { return creatures.remove(c); }

    @Override
    public String toString() {
        return "Case" + coord + (tile != null ? "[" + tile.getType() + "]"
                : cornerBeach ? "[PLAGE_COIN]" : "[MER]");
    }
}
