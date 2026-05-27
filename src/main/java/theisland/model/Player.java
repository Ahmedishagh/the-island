package theisland.model;

import theisland.model.pion.Explorer;
import theisland.model.tile.TerrainTile;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un joueur humain dans la partie.
 * Un joueur possède ses explorateurs, ses tuiles conservées (contour rouge)
 * et son score final.
 */
public class Player {

    private final String name;
    private final PlayerColor color;

    /** Les 10 explorateurs de ce joueur */
    private final List<Explorer> explorers;

    /**
     * Tuiles conservées (contour rouge) que le joueur peut jouer
     * en début de son tour ou en défense.
     */
    private final List<TerrainTile> savedTiles;

    /**
     * Crée un joueur avec son nom et sa couleur.
     *
     * @param name  le nom affiché
     * @param color la couleur choisie
     */
    public Player(String name, PlayerColor color) {
        this.name = name;
        this.color = color;
        this.explorers = new ArrayList<>();
        this.savedTiles = new ArrayList<>();
    }

    /** @return le nom du joueur */
    public String getName() { return name; }

    /** @return la couleur du joueur */
    public PlayerColor getColor() { return color; }

    /** @return la liste complète des explorateurs */
    public List<Explorer> getExplorers() { return explorers; }

    /** @return les tuiles conservées du joueur */
    public List<TerrainTile> getSavedTiles() { return savedTiles; }

    /**
     * Ajoute un explorateur à la liste du joueur.
     *
     * @param e l'explorateur à ajouter
     */
    public void addExplorer(Explorer e) {
        explorers.add(e);
    }

    /**
     * Ajoute une tuile conservée (contour rouge) à la main du joueur.
     *
     * @param tile la tuile à conserver
     */
    public void addSavedTile(TerrainTile tile) {
        savedTiles.add(tile);
    }

    /**
     * Retire une tuile de la main du joueur (quand elle est jouée).
     *
     * @param tile la tuile à retirer
     * @return vrai si la tuile était bien dans la main
     */
    public boolean removeSavedTile(TerrainTile tile) {
        return savedTiles.remove(tile);
    }

    /**
     * Calcule le score total du joueur en fin de partie.
     * Seuls les explorateurs sauvés comptent.
     *
     * @return la somme des valeurs de trésor des explorateurs sauvés
     */
    public int computeScore() {
        return explorers.stream()
                .filter(Explorer::isSaved)
                .mapToInt(Explorer::getTreasureValue)
                .sum();
    }

    /**
     * Compte le nombre d'explorateurs sauvés (pour départager les égalités).
     *
     * @return le nombre d'explorateurs en lieu sûr
     */
    public long countSaved() {
        return explorers.stream().filter(Explorer::isSaved).count();
    }

    /**
     * Retourne les explorateurs encore actifs (pas morts, pas sauvés).
     *
     * @return liste des explorateurs encore en jeu
     */
    public List<Explorer> getActiveExplorers() {
        return explorers.stream()
                .filter(Explorer::isActive)
                .toList();
    }

    @Override
    public String toString() {
        return name + " (" + color.getLabel() + ")";
    }
}
