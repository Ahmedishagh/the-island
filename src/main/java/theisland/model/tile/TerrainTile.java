package theisland.model.tile;

/**
 * Représente une tuile de terrain posée sur le plateau.
 * Chaque tuile a un type visible (plage, forêt, montagne) et une action
 * cachée au dos qui est révélée lors de son retrait.
 */
public class TerrainTile {

    private final TerrainType type;
    private final TileAction action;

    // true dès que le dos de la tuile a été regardé par un joueur
    private boolean revealed;

    /**
     * Crée une tuile de terrain.
     *
     * @param type   le type de terrain visible
     * @param action l'action cachée au dos de la tuile
     */
    public TerrainTile(TerrainType type, TileAction action) {
        this.type = type;
        this.action = action;
        this.revealed = false;
    }

    /**
     * @return le type de terrain de cette tuile
     */
    public TerrainType getType() {
        return type;
    }

    /**
     * @return l'action cachée de cette tuile
     */
    public TileAction getAction() {
        return action;
    }

    /**
     * Indique si le dos de la tuile a déjà été révélé.
     *
     * @return vrai si la tuile a été retournée au moins une fois
     */
    public boolean isRevealed() {
        return revealed;
    }

    /**
     * Marque la tuile comme révélée (le joueur a retourné la tuile).
     */
    public void reveal() {
        this.revealed = true;
    }

    /**
     * Indique si l'action doit être jouée immédiatement (contour vert)
     * ou conservée pour plus tard (contour rouge).
     *
     * @return vrai si l'action est à effet immédiat
     */
    public boolean isImmediateAction() {
        return switch (action) {
            case REQUIN_IMMEDIAT, BALEINE_IMMEDIATE,
                    BATEAU_IMMEDIAT, TOURBILLON, VOLCAN -> true;
            default -> false;
        };
    }

    @Override
    public String toString() {
        return type + "[" + action + (revealed ? ",révélé" : "") + "]";
    }
}
