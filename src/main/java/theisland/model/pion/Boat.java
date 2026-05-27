package theisland.model.pion;

import theisland.model.PlayerColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Pion bateau pouvant transporter jusqu'à 3 explorateurs.
 * Un bateau est contrôlé par le joueur qui y a le plus d'explorateurs.
 * En cas d'égalité, chaque joueur majoritaire le contrôle.
 */
public class Boat {

    /** Capacité maximale d'un bateau */
    public static final int MAX_CAPACITY = 3;

    private static int idCounter = 0;

    private final int id;

    /** Passagers actuellement à bord */
    private final List<Explorer> passengers;

    /** Indique si ce bateau a été retiré du jeu */
    private boolean sunk;

    /**
     * Crée un bateau vide.
     */
    public Boat() {
        this.id = ++idCounter;
        this.passengers = new ArrayList<>();
        this.sunk = false;
    }

    /** @return l'identifiant interne du bateau */
    public int getId() { return id; }

    /** @return les passagers à bord */
    public List<Explorer> getPassengers() { return passengers; }

    /** @return vrai si le bateau a été coulé ou retiré du jeu */
    public boolean isSunk() { return sunk; }

    /**
     * Marque le bateau comme coulé/retiré du jeu.
     */
    public void sink() { this.sunk = true; }

    /**
     * @return vrai si le bateau peut encore embarquer au moins un passager
     */
    public boolean hasRoom() {
        return passengers.size() < MAX_CAPACITY;
    }

    /**
     * Indique si le bateau est vide.
     *
     * @return vrai si aucun explorateur à bord
     */
    public boolean isEmpty() {
        return passengers.isEmpty();
    }

    /**
     * Ajoute un explorateur à bord.
     *
     * @param explorer l'explorateur à embarquer
     * @throws IllegalStateException si le bateau est plein
     */
    public void embark(Explorer explorer) {
        if (!hasRoom()) {
            throw new IllegalStateException("Le bateau " + id + " est plein !");
        }
        passengers.add(explorer);
        explorer.setState(ExplorerState.SUR_BATEAU);
    }

    /**
     * Retire un explorateur du bateau.
     *
     * @param explorer l'explorateur à débarquer
     * @return vrai si l'explorateur était bien à bord
     */
    public boolean disembark(Explorer explorer) {
        return passengers.remove(explorer);
    }

    /**
     * Détermine la ou les couleurs qui contrôlent ce bateau.
     * Le contrôle appartient au joueur ayant le plus d'explorateurs à bord.
     * En cas d'égalité, tous les joueurs à égalité contrôlent le bateau.
     *
     * @return liste des couleurs de joueurs qui contrôlent le bateau,
     *         vide si le bateau est vide
     */
    public List<PlayerColor> getControllers() {
        if (passengers.isEmpty()) return List.of();

        // Comptage par couleur
        java.util.Map<PlayerColor, Long> counts = passengers.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Explorer::getColor,
                        java.util.stream.Collectors.counting()));

        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);

        return counts.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(java.util.Map.Entry::getKey)
                .toList();
    }

    /**
     * Indique si un joueur donné contrôle ce bateau.
     *
     * @param color la couleur du joueur
     * @return vrai si ce joueur contrôle le bateau
     */
    public boolean isControlledBy(PlayerColor color) {
        return getControllers().contains(color);
    }

    @Override
    public String toString() {
        return "Bateau#" + id + "(" + passengers.size() + "/" + MAX_CAPACITY + ")";
    }
}
