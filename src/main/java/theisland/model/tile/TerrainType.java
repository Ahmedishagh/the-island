package theisland.model.tile;

/**
 * Types de terrain possibles pour les cases du plateau et les tuiles.
 */
public enum TerrainType {

    /** Case de mer ordinaire (pas de tuile terrain) */
    MER,

    /** Tuile de plage – retirée en premier */
    PLAGE,

    /** Tuile de forêt – retirée en deuxième */
    FORET,

    /** Tuile de montagne – retirée en dernier */
    MONTAGNE,

    /** Zone de plage de départ dans les coins (non retirable) */
    PLAGE_DEPART
}
