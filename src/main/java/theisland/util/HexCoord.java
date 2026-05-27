package theisland.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Coordonnée dans une grille hexagonale en layout "offset" pair/impair.
 * On utilise la convention "offset column" : les colonnes paires ont
 * un décalage vers le haut.
 *
 * <p>On travaille ici en coordonnées de colonne et de rangée (col, row),
 * et les voisins diffèrent selon la parité de la colonne.</p>
 */
public class HexCoord {

    private final int col;
    private final int row;

    /**
     * Construit une coordonnée hexagonale.
     *
     * @param col la colonne (abscisse)
     * @param row la rangée (ordonnée)
     */
    public HexCoord(int col, int row) {
        this.col = col;
        this.row = row;
    }

    /** @return la colonne */
    public int getCol() { return col; }

    /** @return la rangée */
    public int getRow() { return row; }

    /**
     * Retourne les 6 voisins directs de cette case.
     * Les décalages dépendent de la parité de la colonne.
     *
     * @return liste des 6 coordonnées voisines
     */
    public List<HexCoord> neighbors() {
        List<HexCoord> result = new ArrayList<>();

        // voisins au-dessus et en-dessous sont toujours les mêmes
        result.add(new HexCoord(col, row - 1));
        result.add(new HexCoord(col, row + 1));

        if (col % 2 == 0) {
            // colonne paire
            result.add(new HexCoord(col - 1, row - 1));
            result.add(new HexCoord(col - 1, row));
            result.add(new HexCoord(col + 1, row - 1));
            result.add(new HexCoord(col + 1, row));
        } else {
            // colonne impaire
            result.add(new HexCoord(col - 1, row));
            result.add(new HexCoord(col - 1, row + 1));
            result.add(new HexCoord(col + 1, row));
            result.add(new HexCoord(col + 1, row + 1));
        }

        return result;
    }

    /**
     * Indique si cette coordonnée est adjacente à une autre.
     *
     * @param other l'autre coordonnée
     * @return vrai si elles sont voisines directes
     */
    public boolean isAdjacentTo(HexCoord other) {
        return neighbors().contains(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HexCoord that)) return false;
        return col == that.col && row == that.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row);
    }

    @Override
    public String toString() {
        return "(" + col + ", " + row + ")";
    }
}
