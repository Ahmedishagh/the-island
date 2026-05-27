package theisland.model;

/**
 * Les grandes phases du tour d'un joueur dans The Island.
 * Les actions doivent être effectuées dans cet ordre exact.
 */
public enum GamePhase {

    /**
     * Phase de configuration initiale : placement des explorateurs et bateaux.
     */
    SETUP,

    /**
     * (Optionnel) Le joueur peut jouer une tuile conservée en début de tour.
     */
    JOUER_TUILE_CONSERVEE,

    /**
     * Le joueur déplace des explorateurs et/ou des bateaux (3 déplacements max).
     */
    DEPLACER_PIONS,

    /**
     * Le joueur doit retirer une tuile de terrain (obligatoire).
     */
    RETIRER_TUILE,

    /**
     * Le joueur lance le dé et déplace une créature marine.
     */
    LANCER_DE,

    /**
     * La partie est terminée (volcan révélé ou plus de tuiles).
     */
    FIN_DE_PARTIE
}
