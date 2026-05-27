package theisland.model;

import java.awt.Color;

/**
 * Les quatre couleurs de joueurs disponibles dans The Island.
 */
public enum PlayerColor {

    ROUGE("Rouge", new Color(220, 50, 50)),
    BLEU("Bleu", new Color(50, 100, 210)),
    VERT("Vert", new Color(40, 160, 60)),
    JAUNE("Jaune", new Color(230, 200, 30));

    private final String label;
    private final Color awtColor;

    PlayerColor(String label, Color awtColor) {
        this.label = label;
        this.awtColor = awtColor;
    }

    /**
     * @return le nom affiché dans l'interface
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return la couleur Java2D correspondante
     */
    public Color getAwtColor() {
        return awtColor;
    }

    @Override
    public String toString() {
        return label;
    }
}
