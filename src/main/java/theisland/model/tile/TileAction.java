package theisland.model.tile;

/**
 * Action cachée inscrite au dos d'une tuile de terrain.
 * Ces actions sont révélées quand la tuile est retirée du jeu.
 *
 * <p>Les actions à contour VERT se jouent immédiatement à la révélation.
 * Les actions à contour ROUGE se conservent pour être utilisées plus tard
 * (en début de tour ou en défense).</p>
 */
public enum TileAction {

    // ──────────────────────────────────────────────────────────────────────
    //  Actions VERTES – jouées immédiatement lors du retrait de la tuile
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Ajouter un requin sur la case libérée.
     * Tout nageur présent est retiré du jeu.
     */
    REQUIN_IMMEDIAT,

    /**
     * Ajouter une baleine sur la case libérée.
     */
    BALEINE_IMMEDIATE,

    /**
     * Ajouter un bateau sur la case libérée.
     * Les nageurs présents montent à bord (3 max au choix du joueur).
     */
    BATEAU_IMMEDIAT,

    /**
     * Tourbillon : retire tous les pions de la case libérée
     * et de toutes ses cases mer adjacentes.
     */
    TOURBILLON,

    /**
     * Éruption volcanique : fin immédiate du jeu !
     */
    VOLCAN,

    // ──────────────────────────────────────────────────────────────────────
    //  Actions ROUGES – conservées, jouées en début de tour ou en défense
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Dauphin : déplace l'un de vos nageurs de 1 à 3 cases mer.
     * Utilisable en début de votre tour.
     */
    DAUPHIN,

    /**
     * Vent : déplace l'un de vos bateaux de 1 à 3 cases mer.
     * Utilisable en début de votre tour.
     */
    VENT,

    /**
     * Déplace un serpent de mer déjà sur le plateau vers n'importe
     * quelle case mer libre.
     * Utilisable en début de votre tour.
     */
    DEPLACER_SERPENT,

    /**
     * Déplace un requin déjà sur le plateau vers n'importe
     * quelle case mer libre.
     * Utilisable en début de votre tour.
     */
    DEPLACER_REQUIN,

    /**
     * Déplace une baleine déjà sur le plateau vers n'importe
     * quelle case mer libre.
     * Utilisable en début de votre tour.
     */
    DEPLACER_BALEINE,

    /**
     * Défense : retire un requin du jeu quand il menace un de vos nageurs.
     * Utilisable hors de votre tour.
     */
    CHASSER_REQUIN,

    /**
     * Défense : retire une baleine du jeu quand elle menace un de vos bateaux.
     * Utilisable hors de votre tour.
     */
    CHASSER_BALEINE
}
