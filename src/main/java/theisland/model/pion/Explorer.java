package theisland.model.pion;

import theisland.model.PlayerColor;

/**
 * Pion explorateur appartenant à un joueur.
 * Chaque explorateur porte une valeur de trésor cachée (1 à 6)
 * qui n'est révélée qu'en fin de partie.
 *
 * <p>Un explorateur peut être sur terre, sur un bateau ou dans l'eau
 * (auquel cas il devient un nageur).</p>
 */
public class Explorer {

    /** Identifiant unique de cet explorateur */
    private final int id;

    /** Couleur du joueur à qui appartient cet explorateur */
    private final PlayerColor color;

    /** Valeur du trésor (1 à 6), secrète pendant la partie */
    private final int treasureValue;

    /** Mode courant de l'explorateur */
    private ExplorerState state;

    /** Indique si l'explorateur a été sauvé (arrivé sur une île) */
    private boolean saved;

    /** Indique si l'explorateur a été retiré du jeu (mangé / noyé) */
    private boolean dead;

    /**
     * Crée un nouvel explorateur.
     *
     * @param id            identifiant unique dans la liste du joueur
     * @param color         couleur du joueur propriétaire
     * @param treasureValue valeur de trésor (entre 1 et 6)
     */
    public Explorer(int id, PlayerColor color, int treasureValue) {
        this.id = id;
        this.color = color;
        this.treasureValue = treasureValue;
        this.state = ExplorerState.SUR_TERRE;
        this.saved = false;
        this.dead = false;
    }

    /** @return l'identifiant de cet explorateur */
    public int getId() { return id; }

    /** @return la couleur du joueur propriétaire */
    public PlayerColor getColor() { return color; }

    /** @return la valeur trésor (secrète pendant la partie) */
    public int getTreasureValue() { return treasureValue; }

    /** @return l'état courant de l'explorateur */
    public ExplorerState getState() { return state; }

    /** @return vrai si l'explorateur a été mis en sécurité */
    public boolean isSaved() { return saved; }

    /** @return vrai si l'explorateur a été éliminé */
    public boolean isDead() { return dead; }

    /**
     * Change l'état de l'explorateur.
     *
     * @param newState le nouvel état
     */
    public void setState(ExplorerState newState) {
        this.state = newState;
    }

    /**
     * Marque cet explorateur comme sauvé (arrivé sur une île sûre).
     */
    public void setSaved() {
        this.saved = true;
        this.state = ExplorerState.SAUVE;
    }

    /**
     * Retire cet explorateur du jeu.
     */
    public void kill() {
        this.dead = true;
        this.state = ExplorerState.ELIMINE;
    }

    /**
     * Indique si l'explorateur est actuellement un nageur.
     *
     * @return vrai si l'état est NAGEUR
     */
    public boolean isSwimmer() {
        return state == ExplorerState.NAGEUR;
    }

    /**
     * Indique si l'explorateur est en jeu (pas mort, pas sauvé).
     *
     * @return vrai si l'explorateur est encore actif
     */
    public boolean isActive() {
        return !dead && !saved;
    }

    @Override
    public String toString() {
        return color.getLabel() + "#" + id + "(tr=" + treasureValue + "," + state + ")";
    }
}
