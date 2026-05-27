package theisland.model.pion;

/**
 * Type de créature marine pouvant apparaître sur le plateau.
 * Chaque créature a des effets distincts sur les explorateurs et les bateaux.
 */
public enum CreatureType {

    /**
     * Serpent de mer : se déplace d'une case par tour de dé.
     * Coule un bateau avec ses passagers. Élimine les nageurs sur sa case.
     */
    SERPENT_DE_MER,

    /**
     * Requin : se déplace d'une ou deux cases par tour de dé.
     * Élimine tous les nageurs sur sa case. N'affecte pas les bateaux.
     */
    REQUIN,

    /**
     * Baleine : se déplace d'une à trois cases par tour de dé.
     * Fait chavirer les bateaux (les occupants deviennent nageurs).
     * Si un requin est sur la même case, les nageurs sont éliminés.
     * N'affecte pas les bateaux vides ni les nageurs seuls.
     */
    BALEINE
}
