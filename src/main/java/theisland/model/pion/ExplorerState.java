package theisland.model.pion;

/**
 * État courant d'un pion explorateur.
 */
public enum ExplorerState {

    /** L'explorateur est posé sur une tuile de terrain */
    SUR_TERRE,

    /** L'explorateur est à bord d'un bateau */
    SUR_BATEAU,

    /** L'explorateur nage dans une case de mer */
    NAGEUR,

    /** L'explorateur a atteint une île de débarquement – il est sauvé */
    SAUVE,

    /** L'explorateur a été retiré du jeu (mangé ou noyé) */
    ELIMINE
}
