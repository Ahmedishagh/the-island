package theisland.model.pion;

/**
 * Pion créature marine (serpent de mer, requin ou baleine).
 * Les créatures se déplacent en mer et peuvent attaquer explorateurs ou bateaux.
 */
public class Creature {

    private static int idCounter = 0;

    private final int id;
    private final CreatureType type;

    /** Vrai quand la créature a été retirée du plateau */
    private boolean removed;

    /**
     * Crée une nouvelle créature du type donné.
     *
     * @param type le type de créature
     */
    public Creature(CreatureType type) {
        this.id = ++idCounter;
        this.type = type;
        this.removed = false;
    }

    /** @return le type de cette créature */
    public CreatureType getType() { return type; }

    /** @return l'identifiant interne */
    public int getId() { return id; }

    /** @return vrai si la créature a été retirée du jeu */
    public boolean isRemoved() { return removed; }

    /**
     * Retire la créature du plateau.
     */
    public void remove() { this.removed = true; }

    /**
     * Nombre maximum de cases que peut parcourir cette créature
     * lors d'un déplacement de dé.
     *
     * @return distance maximale de déplacement
     */
    public int maxMoveDistance() {
        return switch (type) {
            case SERPENT_DE_MER -> 1;
            case REQUIN -> 2;
            case BALEINE -> 3;
        };
    }

    @Override
    public String toString() {
        return type.name() + "#" + id;
    }
}
