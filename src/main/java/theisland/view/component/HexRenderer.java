package theisland.view.component;

import theisland.util.HexCoord;

import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Utilitaire de rendu hexagonal.
 * Calcule les polygones et coordonnées pixel pour la grille hexagonale.
 *
 * <p>On utilise des hexagones à « flat top » (pointe en haut) avec
 * un offset de colonne.</p>
 */
public class HexRenderer {

    private final int hexSize;    // rayon (centre → sommet)
    private final int hexW;       // largeur totale d'un hexa
    private final int hexH;       // hauteur totale d'un hexa
    private final int originX;    // décalage X du plateau dans le composant
    private final int originY;    // décalage Y du plateau dans le composant

    /**
     * Crée un renderer hexagonal.
     *
     * @param hexSize taille (rayon) de chaque hexagone en pixels
     * @param originX décalage horizontal du coin supérieur-gauche
     * @param originY décalage vertical du coin supérieur-gauche
     */
    public HexRenderer(int hexSize, int originX, int originY) {
        this.hexSize = hexSize;
        this.hexW = (int)(Math.sqrt(3) * hexSize);
        this.hexH = hexSize * 2;
        this.originX = originX;
        this.originY = originY;
    }

    /**
     * Retourne le centre pixel d'une case hexagonale.
     * Layout : pointy-top avec offset pair/impair sur les colonnes.
     *
     * @param coord la coordonnée hexagonale
     * @return le centre en pixels (x, y)
     */
    public Point hexToPixel(HexCoord coord) {
        int col = coord.getCol();
        int row = coord.getRow();

        int x = originX + (int)(col * hexW * 0.75) + hexW / 2;
        // Les colonnes paires sont décalées vers le bas
        int yOffset = (col % 2 == 0) ? 0 : hexH / 2;
        int y = originY + row * hexH / 2 * 2 / 2 + yOffset;
        // Version simplifiée propre :
        y = originY + row * (hexH * 3 / 4) / 2 * 2;
        if (col % 2 != 0) y += hexH / 4;

        // Recalcul net :
        int px = originX + (int)(col * hexW * 0.75 + hexW / 2.0);
        int py = originY + (int)(row * hexH / 2.0 + (col % 2 != 0 ? hexH / 4.0 : 0) + hexH / 2.0);
        return new Point(px, py);
    }

    /**
     * Construit le polygone hexagonal centré en (cx, cy).
     *
     * @param cx centre X
     * @param cy centre Y
     * @return le chemin du polygone hexagonal
     */
    public Path2D hexagon(int cx, int cy) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            // flat-top : angle de départ à 0°
            double angle = Math.PI / 180 * (60 * i);
            double px = cx + hexSize * Math.cos(angle);
            double py = cy + hexSize * Math.sin(angle);
            if (i == 0) path.moveTo(px, py);
            else        path.lineTo(px, py);
        }
        path.closePath();
        return path;
    }

    /**
     * Construit le polygone hexagonal pour une coordonnée de grille.
     *
     * @param coord la coordonnée hexagonale
     * @return le polygone
     */
    public Path2D hexagonFor(HexCoord coord) {
        Point center = hexToPixel(coord);
        return hexagon(center.x, center.y);
    }

    /**
     * Détermine à quelle case hexagonale correspond un point pixel.
     *
     * @param px     position X en pixels
     * @param py     position Y en pixels
     * @param cols   nombre de colonnes du plateau
     * @param rows   nombre de rangées du plateau
     * @return la coordonnée hexagonale, ou null si hors grille
     */
    public HexCoord pixelToHex(int px, int py, int cols, int rows) {
        HexCoord best = null;
        double bestDist = Double.MAX_VALUE;

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                HexCoord coord = new HexCoord(c, r);
                Point center = hexToPixel(coord);
                double dist = Math.hypot(px - center.x, py - center.y);
                if (dist < bestDist && dist < hexSize) {
                    bestDist = dist;
                    best = coord;
                }
            }
        }
        return best;
    }

    /** @return la taille (rayon) des hexagones */
    public int getHexSize() { return hexSize; }

    /** @return la largeur d'un hexagone */
    public int getHexW() { return hexW; }

    /** @return la hauteur d'un hexagone */
    public int getHexH() { return hexH; }
}
