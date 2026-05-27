package theisland.controller;

import theisland.model.*;
import theisland.model.pion.*;
import theisland.model.tile.TerrainTile;
import theisland.model.tile.TileAction;
import theisland.util.HexCoord;

import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur principal du jeu The Island.
 *
 * <p>Cette classe fait le lien entre le modèle ({@link GameState}) et les
 * vues. Elle valide chaque action avant de l'appliquer, et met à jour
 * l'état du jeu en conséquence.</p>
 *
 * <p>Toutes les méthodes qui modifient l'état du jeu renvoient un
 * {@link ActionResult} décrivant ce qui s'est passé (succès, erreur,
 * effets déclenchés).</p>
 */
public class GameController {

    private final GameState state;

    /**
     * Crée un contrôleur pour la partie donnée.
     *
     * @param state l'état de jeu à piloter
     */
    public GameController(GameState state) {
        this.state = state;
    }

    /** @return l'état de jeu courant */
    public GameState getState() { return state; }

    // ══════════════════════════════════════════════════════════════════════
    //  PHASE SETUP
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Place un explorateur sur une case de terrain vierge lors de la phase
     * de mise en place.
     *
     * @param explorer l'explorateur à placer
     * @param target   la case de destination
     * @return le résultat de l'action
     */
    public ActionResult setupPlaceExplorer(Explorer explorer, Cell target) {
        if (state.getPhase() != GamePhase.SETUP) {
            return ActionResult.error("Pas en phase de mise en place.");
        }
        if (!target.hasTerrain()) {
            return ActionResult.error("Choisissez une case de terrain.");
        }
        if (!target.getExplorersOnLand().isEmpty()) {
            // La règle dit « tuile déserte » – plusieurs explorateurs
            // sur la même tuile sont autorisés dans certaines variantes,
            // on reste strict ici
        }

        target.addExplorerOnLand(explorer);
        return ActionResult.ok("Explorateur placé sur " + target.getCoord());
    }

    /**
     * Place un bateau sur une case de mer voisine d'une tuile terrain.
     *
     * @param boat   le bateau à placer
     * @param target la case de mer cible
     * @return le résultat de l'action
     */
    public ActionResult setupPlaceBoat(Boat boat, Cell target) {
        if (state.getPhase() != GamePhase.SETUP) {
            return ActionResult.error("Pas en phase de mise en place.");
        }
        if (!target.isSea()) {
            return ActionResult.error("Le bateau doit être placé sur une case de mer.");
        }
        boolean adjToTerrain = state.getBoard().getTerrainNeighbors(target.getCoord())
                .size() > 0;
        if (!adjToTerrain) {
            return ActionResult.error("Le bateau doit être adjacent à une tuile terrain.");
        }
        if (!target.getBoats().isEmpty()) {
            return ActionResult.error("Cette case de mer est déjà occupée par un bateau.");
        }

        board().getReserveBoats().remove(boat);
        target.addBoat(boat);
        return ActionResult.ok("Bateau placé sur " + target.getCoord());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DÉPLACEMENT DES PIONS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Déplace un explorateur d'une case à une autre.
     * Gère les transitions : terre→terre, terre→bateau, bateau→bateau,
     * terre→mer (devient nageur), bateau→mer (saute).
     *
     * @param explorer l'explorateur à déplacer
     * @param target   la case de destination
     * @return le résultat de l'action
     */
    public ActionResult moveExplorer(Explorer explorer, Cell target) {
        if (state.getPhase() != GamePhase.DEPLACER_PIONS) {
            return ActionResult.error("Ce n'est pas le moment de déplacer des pions.");
        }
        if (state.getMovesRemaining() <= 0) {
            return ActionResult.error("Plus de déplacements disponibles ce tour.");
        }
        if (!explorer.isActive()) {
            return ActionResult.error("Cet explorateur n'est plus en jeu.");
        }

        Cell from = state.findExplorerCell(explorer);
        if (from == null) {
            return ActionResult.error("Impossible de localiser l'explorateur.");
        }
        if (!from.getCoord().isAdjacentTo(target.getCoord())) {
            return ActionResult.error("La destination doit être une case adjacente.");
        }

        // Un explorateur sauvé ne peut pas repartir
        if (explorer.isSaved()) {
            return ActionResult.error("Un explorateur sauvé ne peut pas être déplacé.");
        }

        return applyExplorerMove(explorer, from, target);
    }

    private ActionResult applyExplorerMove(Explorer explorer, Cell from, Cell target) {
        List<String> effects = new ArrayList<>();

        // ── Cas : sur terre ───────────────────────────────────────────────
        if (explorer.getState() == ExplorerState.SUR_TERRE) {
            from.removeExplorerFromLand(explorer);

            if (target.hasTerrain() || target.isCornerBeach()) {
                // Terre → Terre ou Plage de coin (sauvé)
                if (target.isCornerBeach()) {
                    target.addExplorerOnLand(explorer);
                    explorer.setSaved();
                    effects.add("Explorateur sauvé sur la plage !");
                } else {
                    target.addExplorerOnLand(explorer);
                    explorer.setState(ExplorerState.SUR_TERRE);
                }
            } else if (target.isSea()) {
                // Terre → Mer : monte sur un bateau ou devient nageur
                if (!target.getBoats().isEmpty()) {
                    Boat boat = target.getBoats().get(0);
                    if (boat.hasRoom()) {
                        boat.embark(explorer);
                        effects.add("Explorateur embarqué sur " + boat);
                    } else {
                        // Bateau plein, il tombe à la mer
                        target.addSwimmer(explorer);
                        explorer.setState(ExplorerState.NAGEUR);
                        effects.add("Bateau plein, l'explorateur nage.");
                    }
                } else {
                    target.addSwimmer(explorer);
                    explorer.setState(ExplorerState.NAGEUR);
                }
            }
        }
        // ── Cas : sur un bateau ───────────────────────────────────────────
        else if (explorer.getState() == ExplorerState.SUR_BATEAU) {
            Boat fromBoat = from.getBoats().stream()
                    .filter(b -> b.getPassengers().contains(explorer))
                    .findFirst().orElse(null);
            if (fromBoat == null) {
                return ActionResult.error("L'explorateur n'est sur aucun bateau.");
            }
            // Vérifier que c'est bien le bon joueur qui contrôle
            if (!fromBoat.isControlledBy(state.getCurrentPlayer().getColor())
                    && !fromBoat.isEmpty()) {
                // on laisse passer si c'est son propre explorateur
                if (!explorer.getColor().equals(state.getCurrentPlayer().getColor())) {
                    return ActionResult.error("Vous ne contrôlez pas ce bateau.");
                }
            }

            fromBoat.disembark(explorer);

            if (target.isCornerBeach()) {
                // Débarquement sur plage de coin = sauvé
                target.addExplorerOnLand(explorer);
                explorer.setSaved();
                effects.add("Explorateur débarqué et sauvé !");
            } else if (target.isSea() && !target.getBoats().isEmpty()) {
                // Bateau → autre bateau adjacent
                Boat otherBoat = target.getBoats().get(0);
                if (otherBoat.hasRoom()) {
                    otherBoat.embark(explorer);
                } else {
                    target.addSwimmer(explorer);
                    explorer.setState(ExplorerState.NAGEUR);
                    effects.add("Autre bateau plein, il nage.");
                }
            } else if (target.isSea()) {
                // Saute du bateau
                target.addSwimmer(explorer);
                explorer.setState(ExplorerState.NAGEUR);
            }
        }
        // ── Cas : nageur ──────────────────────────────────────────────────
        else if (explorer.getState() == ExplorerState.NAGEUR) {
            from.removeSwimmer(explorer);

            if (target.isCornerBeach()) {
                target.addExplorerOnLand(explorer);
                explorer.setSaved();
                effects.add("Nageur sauvé sur la plage !");
            } else if (target.isSea() && !target.getBoats().isEmpty()) {
                // Monte sur un bateau dans la même case (1 déplacement)
                Boat boat = target.getBoats().get(0);
                if (boat.hasRoom()) {
                    boat.embark(explorer);
                    effects.add("Nageur embarqué.");
                } else {
                    target.addSwimmer(explorer);
                    effects.add("Bateau plein, reste nageur.");
                }
            } else if (target.isSea()) {
                target.addSwimmer(explorer);
            }

            // Vérifier les créatures sur la case destination
            effects.addAll(checkCreatureEffectsOnSwimmer(explorer, target));
        }

        state.useMove();
        return ActionResult.ok("Déplacement effectué.", effects);
    }

    /**
     * Déplace un bateau d'une case à une autre (le bateau doit être contrôlé
     * par le joueur courant, ou être vide).
     *
     * @param boat   le bateau à déplacer
     * @param target la case de mer cible
     * @return le résultat de l'action
     */
    public ActionResult moveBoat(Boat boat, Cell target) {
        if (state.getPhase() != GamePhase.DEPLACER_PIONS) {
            return ActionResult.error("Ce n'est pas le moment de déplacer les bateaux.");
        }
        if (state.getMovesRemaining() <= 0) {
            return ActionResult.error("Plus de déplacements disponibles.");
        }
        if (!target.isSea()) {
            return ActionResult.error("Un bateau ne peut naviguer que sur la mer.");
        }

        Cell from = board().findBoatCell(boat);
        if (from == null) return ActionResult.error("Bateau introuvable sur le plateau.");

        if (!from.getCoord().isAdjacentTo(target.getCoord())) {
            return ActionResult.error("La destination doit être adjacente.");
        }

        // Vérification du contrôle : bateau vide = n'importe qui peut le déplacer
        if (!boat.isEmpty()) {
            if (!boat.isControlledBy(state.getCurrentPlayer().getColor())) {
                return ActionResult.error("Vous ne contrôlez pas ce bateau.");
            }
        }

        // Arrivée sur une plage de coin : les passagers sont sauvés
        List<String> effects = new ArrayList<>();
        if (target.isCornerBeach()) {
            for (Explorer e : new ArrayList<>(boat.getPassengers())) {
                boat.disembark(e);
                target.addExplorerOnLand(e);
                e.setSaved();
                effects.add(e.getColor().getLabel() + " sauvé !");
            }
        }

        from.removeBoat(boat);
        target.addBoat(boat);
        state.useMove();

        // Vérifier créatures sur la case destination
        effects.addAll(checkCreatureEffectsOnBoat(boat, target));

        return ActionResult.ok("Bateau déplacé.", effects);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  RETRAIT D'UNE TUILE TERRAIN
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Retire une tuile terrain du plateau. Déclenche l'effet du verso.
     *
     * @param cell la case dont on retire la tuile
     * @return le résultat de l'action, incluant les effets déclenchés
     */
    public ActionResult removeTile(Cell cell) {
        if (state.getPhase() != GamePhase.RETIRER_TUILE) {
            return ActionResult.error("Ce n'est pas le moment de retirer une tuile.");
        }
        if (!board().canRemoveTile(cell)) {
            return ActionResult.error("Cette tuile ne peut pas être retirée maintenant "
                    + "(vérifiez l'ordre plage → forêt → montagne, "
                    + "et qu'elle est bien adjacente à la mer).");
        }

        TerrainTile tile = cell.getTile();
        tile.reveal();
        cell.setTile(null);

        List<String> effects = new ArrayList<>();

        // Les explorateurs sur cette tuile tombent à la mer
        for (Explorer e : new ArrayList<>(cell.getExplorersOnLand())) {
            cell.removeExplorerFromLand(e);
            cell.addSwimmer(e);
            e.setState(ExplorerState.NAGEUR);
            effects.add(e.getColor().getLabel() + " tombe à l'eau !");
        }

        // Appliquer l'effet de la tuile
        if (tile.isImmediateAction()) {
            effects.addAll(applyImmediateAction(tile, cell));
        } else {
            // Tuile conservée (contour rouge)
            state.getCurrentPlayer().addSavedTile(tile);
            effects.add("Tuile " + tile.getAction() + " conservée pour plus tard.");
        }

        state.setPhase(GamePhase.LANCER_DE);
        return ActionResult.ok("Tuile retirée : " + tile.getAction(), effects);
    }

    private List<String> applyImmediateAction(TerrainTile tile, Cell cell) {
        List<String> effects = new ArrayList<>();

        switch (tile.getAction()) {
            case VOLCAN -> {
                state.triggerVolcano();
                effects.add("🌋 VOLCAN ! Fin de la partie !");
            }
            case REQUIN_IMMEDIAT -> {
                if (!board().getReserveRequins().isEmpty()) {
                    Creature requin = board().getReserveRequins().remove(0);
                    cell.addCreature(requin);
                    // Éliminer les nageurs présents
                    for (Explorer e : new ArrayList<>(cell.getSwimmers())) {
                        cell.removeSwimmer(e);
                        e.kill();
                        effects.add(e + " dévoré par un requin !");
                    }
                    effects.add("Un requin apparaît !");
                }
            }
            case BALEINE_IMMEDIATE -> {
                if (!board().getReserveBaleines().isEmpty()) {
                    Creature baleine = board().getReserveBaleines().remove(0);
                    cell.addCreature(baleine);
                    effects.add("Une baleine apparaît !");
                    effects.addAll(applyWhaleEffect(baleine, cell));
                }
            }
            case BATEAU_IMMEDIAT -> {
                if (!board().getReserveBoats().isEmpty()) {
                    Boat boat = board().getReserveBoats().remove(0);
                    cell.addBoat(boat);
                    // Embarquer les nageurs (max 3)
                    List<Explorer> swimmers = new ArrayList<>(cell.getSwimmers());
                    int boarded = 0;
                    for (Explorer e : swimmers) {
                        if (boarded >= Boat.MAX_CAPACITY) break;
                        cell.removeSwimmer(e);
                        boat.embark(e);
                        boarded++;
                        effects.add(e + " monte à bord !");
                    }
                    effects.add("Un bateau apparaît !");
                }
            }
            case TOURBILLON -> {
                effects.addAll(applyTourbillon(cell));
            }
        }
        return effects;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DÉ ET DÉPLACEMENT DES CRÉATURES
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Lance le dé de créature et passe en phase de déplacement de créature.
     *
     * @return le résultat du dé
     */
    public ActionResult rollDice() {
        if (state.getPhase() != GamePhase.LANCER_DE) {
            return ActionResult.error("Ce n'est pas le moment de lancer le dé.");
        }
        CreatureType result = state.rollDice();
        state.setPhase(GamePhase.LANCER_DE); // on reste dans la phase, la vue gère
        return ActionResult.ok("Dé lancé : " + result.name(), List.of());
    }

    /**
     * Déplace une créature vers une case de mer cible.
     * La distance maximale dépend du type de créature.
     *
     * @param creature la créature à déplacer
     * @param target   la case de mer cible
     * @return le résultat de l'action
     */
    public ActionResult moveCreature(Creature creature, Cell target) {
        if (state.getPhase() != GamePhase.LANCER_DE) {
            return ActionResult.error("Ce n'est pas le moment de déplacer une créature.");
        }
        if (state.getLastDiceResult() != creature.getType()) {
            return ActionResult.error("Le dé indique " + state.getLastDiceResult()
                    + ", pas " + creature.getType());
        }
        if (!target.isSea()) {
            return ActionResult.error("Les créatures ne se déplacent que sur la mer.");
        }

        Cell from = board().findCreatureCell(creature);
        if (from == null) return ActionResult.error("Créature introuvable.");

        // Vérifier la distance (BFS à distance max)
        int dist = hexDistance(from.getCoord(), target.getCoord());
        if (dist < 1 || dist > creature.maxMoveDistance()) {
            return ActionResult.error("Distance invalide pour ce type de créature "
                    + "(max " + creature.maxMoveDistance() + ").");
        }

        from.removeCreature(creature);
        target.addCreature(creature);

        List<String> effects = new ArrayList<>();
        effects.addAll(applyCreatureArrival(creature, target));

        // Fin du tour après déplacement de créature
        endTurn();

        return ActionResult.ok("Créature déplacée.", effects);
    }

    /**
     * Saute le déplacement de créature (si aucune créature du type n'est
     * en jeu, il ne se passe rien).
     *
     * @return résultat de l'action
     */
    public ActionResult skipCreatureMove() {
        if (state.getPhase() != GamePhase.LANCER_DE) {
            return ActionResult.error("Pas en phase créature.");
        }
        endTurn();
        return ActionResult.ok("Déplacement de créature ignoré.", List.of());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TUILES CONSERVÉES
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Joue une tuile conservée (contour rouge) en début de tour
     * ou en défense.
     *
     * @param tile       la tuile à jouer
     * @param targetCell la case cible éventuelle (peut être null pour certaines actions)
     * @param targetPion le pion ciblé (explorateur, nageur, créature…)
     * @return le résultat de l'action
     */
    public ActionResult playSavedTile(TerrainTile tile, Cell targetCell, Object targetPion) {
        Player current = state.getCurrentPlayer();
        if (!current.getSavedTiles().contains(tile)) {
            return ActionResult.error("Vous ne possédez pas cette tuile.");
        }

        List<String> effects = new ArrayList<>();

        switch (tile.getAction()) {
            case DAUPHIN -> {
                if (!(targetPion instanceof Explorer nageur)) {
                    return ActionResult.error("Choisissez un de vos nageurs.");
                }
                if (nageur.getColor() != current.getColor() || !nageur.isSwimmer()) {
                    return ActionResult.error("Vous devez choisir l'un de vos nageurs.");
                }
                // Le dauphin permet de bouger jusqu'à 3 cases – géré par la vue
                effects.add("Dauphin : nageur boosté jusqu'à 3 cases.");
            }
            case VENT -> {
                if (!(targetPion instanceof Boat boat)) {
                    return ActionResult.error("Choisissez un bateau que vous contrôlez.");
                }
                if (!boat.isControlledBy(current.getColor())) {
                    return ActionResult.error("Vous ne contrôlez pas ce bateau.");
                }
                effects.add("Vent : bateau propulsé jusqu'à 3 cases.");
            }
            case CHASSER_REQUIN -> {
                if (!(targetPion instanceof Creature requin)
                        || requin.getType() != CreatureType.REQUIN) {
                    return ActionResult.error("Choisissez un requin à chasser.");
                }
                Cell c = board().findCreatureCell(requin);
                if (c != null) c.removeCreature(requin);
                requin.remove();
                effects.add("Requin retiré du jeu !");
            }
            case CHASSER_BALEINE -> {
                if (!(targetPion instanceof Creature baleine)
                        || baleine.getType() != CreatureType.BALEINE) {
                    return ActionResult.error("Choisissez une baleine à chasser.");
                }
                Cell c = board().findCreatureCell(baleine);
                if (c != null) c.removeCreature(baleine);
                baleine.remove();
                effects.add("Baleine retirée du jeu !");
            }
            case DEPLACER_SERPENT, DEPLACER_REQUIN, DEPLACER_BALEINE -> {
                if (!(targetPion instanceof Creature creature)) {
                    return ActionResult.error("Choisissez une créature à déplacer.");
                }
                if (targetCell == null || !targetCell.isSea()) {
                    return ActionResult.error("Choisissez une case de mer valide.");
                }
                Cell from = board().findCreatureCell(creature);
                if (from != null) from.removeCreature(creature);
                targetCell.addCreature(creature);
                effects.add("Créature déplacée vers " + targetCell.getCoord());
            }
            default -> {
                return ActionResult.error("Cette tuile ne peut pas être jouée ainsi.");
            }
        }

        current.removeSavedTile(tile);
        return ActionResult.ok("Tuile jouée : " + tile.getAction(), effects);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Méthodes internes – effets des créatures
    // ══════════════════════════════════════════════════════════════════════

    private List<String> applyCreatureArrival(Creature creature, Cell cell) {
        return switch (creature.getType()) {
            case SERPENT_DE_MER -> applySeaSnakeEffect(creature, cell);
            case REQUIN -> applySharkEffect(creature, cell);
            case BALEINE -> applyWhaleEffect(creature, cell);
        };
    }

    private List<String> applySeaSnakeEffect(Creature snake, Cell cell) {
        List<String> effects = new ArrayList<>();
        // Coule les bateaux avec passagers, tue les nageurs
        for (Boat boat : new ArrayList<>(cell.getBoats())) {
            if (!boat.isEmpty()) {
                for (Explorer e : new ArrayList<>(boat.getPassengers())) {
                    boat.disembark(e);
                    e.kill();
                    effects.add(e + " tué par le Serpent de Mer !");
                }
                cell.removeBoat(boat);
                boat.sink();
            }
        }
        for (Explorer swimmer : new ArrayList<>(cell.getSwimmers())) {
            cell.removeSwimmer(swimmer);
            swimmer.kill();
            effects.add(swimmer + " englouti par le Serpent de Mer !");
        }
        return effects;
    }

    private List<String> applySharkEffect(Creature shark, Cell cell) {
        List<String> effects = new ArrayList<>();
        // Tue tous les nageurs, n'affecte pas les bateaux
        for (Explorer swimmer : new ArrayList<>(cell.getSwimmers())) {
            cell.removeSwimmer(swimmer);
            swimmer.kill();
            effects.add(swimmer + " dévoré par le requin !");
        }
        return effects;
    }

    private List<String> applyWhaleEffect(Creature whale, Cell cell) {
        List<String> effects = new ArrayList<>();
        // Fait chavirer les bateaux avec passagers
        boolean sharkPresent = cell.getCreatures().stream()
                .anyMatch(c -> c.getType() == CreatureType.REQUIN && !c.isRemoved());

        for (Boat boat : new ArrayList<>(cell.getBoats())) {
            if (!boat.isEmpty()) {
                for (Explorer e : new ArrayList<>(boat.getPassengers())) {
                    boat.disembark(e);
                    if (sharkPresent) {
                        e.kill();
                        effects.add(e + " jeté à la mer et dévoré par un requin !");
                    } else {
                        cell.addSwimmer(e);
                        e.setState(ExplorerState.NAGEUR);
                        effects.add(e + " tombe à la mer (baleine) !");
                    }
                }
                cell.removeBoat(boat);
                boat.sink();
                effects.add("Bateau chaviré !");
            }
        }
        return effects;
    }

    private List<String> applyTourbillon(Cell cell) {
        List<String> effects = new ArrayList<>();
        List<Cell> zone = new ArrayList<>();
        zone.add(cell);
        zone.addAll(board().getSeaNeighbors(cell.getCoord()));

        for (Cell c : zone) {
            for (Explorer e : new ArrayList<>(c.getSwimmers())) {
                c.removeSwimmer(e);
                e.kill();
            }
            for (Boat b : new ArrayList<>(c.getBoats())) {
                for (Explorer e : new ArrayList<>(b.getPassengers())) {
                    b.disembark(e);
                    e.kill();
                }
                c.removeBoat(b);
                b.sink();
            }
            for (Creature cr : new ArrayList<>(c.getCreatures())) {
                c.removeCreature(cr);
                cr.remove();
            }
        }
        effects.add("Tourbillon ! Tout est englouti dans la zone !");
        return effects;
    }

    private List<String> checkCreatureEffectsOnSwimmer(Explorer explorer, Cell cell) {
        List<String> effects = new ArrayList<>();
        boolean hasShark = cell.getCreatures().stream()
                .anyMatch(c -> c.getType() == CreatureType.REQUIN && !c.isRemoved());
        boolean hasSnake = cell.getCreatures().stream()
                .anyMatch(c -> c.getType() == CreatureType.SERPENT_DE_MER && !c.isRemoved());

        if ((hasShark || hasSnake) && explorer.isSwimmer()) {
            cell.removeSwimmer(explorer);
            explorer.kill();
            effects.add(explorer + " immédiatement tué par une créature !");
        }
        return effects;
    }

    private List<String> checkCreatureEffectsOnBoat(Boat boat, Cell cell) {
        List<String> effects = new ArrayList<>();
        boolean hasSnake = cell.getCreatures().stream()
                .anyMatch(c -> c.getType() == CreatureType.SERPENT_DE_MER && !c.isRemoved());
        boolean hasWhale = cell.getCreatures().stream()
                .anyMatch(c -> c.getType() == CreatureType.BALEINE && !c.isRemoved());

        if (hasSnake && !boat.isEmpty()) {
            effects.addAll(applySeaSnakeEffect(null, cell));
        } else if (hasWhale && !boat.isEmpty()) {
            Creature whale = cell.getCreatures().stream()
                    .filter(c -> c.getType() == CreatureType.BALEINE)
                    .findFirst().orElse(null);
            if (whale != null) effects.addAll(applyWhaleEffect(whale, cell));
        }
        return effects;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Gestion des tours
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Termine le tour courant et passe au joueur suivant.
     */
    private void endTurn() {
        if (!state.isGameOver()) {
            state.nextTurn();
        }
    }

    /**
     * Avance la phase de déplacement des pions vers le retrait de tuile
     * quand le joueur a fini de déplacer.
     */
    public void finishMoving() {
        if (state.getPhase() == GamePhase.DEPLACER_PIONS) {
            state.setPhase(GamePhase.RETIRER_TUILE);
        }
    }

    /**
     * Passe de la phase de tuile conservée à la phase de déplacement.
     */
    public void skipSavedTile() {
        if (state.getPhase() == GamePhase.JOUER_TUILE_CONSERVEE) {
            state.setPhase(GamePhase.DEPLACER_PIONS);
        }
    }

    // ── Utilitaires ────────────────────────────────────────────────────────

    private Board board() { return state.getBoard(); }

    /**
     * Calcule la distance hexagonale entre deux coordonnées.
     * Conversion en coordonnées cubiques pour le calcul.
     */
    private int hexDistance(HexCoord a, HexCoord b) {
        int[] ca = toCube(a);
        int[] cb = toCube(b);
        return Math.max(Math.abs(ca[0] - cb[0]),
                Math.max(Math.abs(ca[1] - cb[1]), Math.abs(ca[2] - cb[2])));
    }

    private int[] toCube(HexCoord h) {
        int x = h.getCol() - (h.getRow() - (h.getRow() & 1)) / 2;
        int z = h.getRow();
        int y = -x - z;
        return new int[]{x, y, z};
    }
}
