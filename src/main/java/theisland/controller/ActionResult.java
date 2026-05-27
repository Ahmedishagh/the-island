package theisland.controller;

import java.util.List;

/**
 * Résultat d'une action dans le contrôleur de jeu.
 * Permet à la vue de savoir si l'action a réussi, et
 * de récupérer les effets déclenchés pour les afficher.
 */
public class ActionResult {

    private final boolean success;
    private final String message;
    private final List<String> effects;

    private ActionResult(boolean success, String message, List<String> effects) {
        this.success = success;
        this.message = message;
        this.effects = effects;
    }

    /**
     * Crée un résultat de succès sans effets supplémentaires.
     *
     * @param message le message de confirmation
     * @return le résultat
     */
    public static ActionResult ok(String message) {
        return new ActionResult(true, message, List.of());
    }

    /**
     * Crée un résultat de succès avec effets.
     *
     * @param message le message principal
     * @param effects les effets supplémentaires déclenchés
     * @return le résultat
     */
    public static ActionResult ok(String message, List<String> effects) {
        return new ActionResult(true, message, effects);
    }

    /**
     * Crée un résultat d'erreur.
     *
     * @param reason la raison de l'échec
     * @return le résultat
     */
    public static ActionResult error(String reason) {
        return new ActionResult(false, reason, List.of());
    }

    /** @return vrai si l'action a réussi */
    public boolean isSuccess() { return success; }

    /** @return le message principal */
    public String getMessage() { return message; }

    /** @return les effets secondaires de l'action */
    public List<String> getEffects() { return effects; }

    /** @return true si l'action a échoué */
    public boolean isError() { return !success; }

    @Override
    public String toString() {
        return (success ? "✓ " : "✗ ") + message
                + (effects.isEmpty() ? "" : " | " + String.join(", ", effects));
    }
}
