package theisland.view;

import theisland.controller.ActionResult;
import theisland.controller.GameController;
import theisland.model.*;
import theisland.model.pion.*;
import theisland.model.tile.TerrainTile;
import theisland.view.component.ActionBarPanel;
import theisland.view.component.BoardPanel;
import theisland.view.component.PlayerInfoPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Fenêtre principale du jeu The Island.
 *
 * <p>Assemble le plateau ({@link BoardPanel}), le panneau d'info joueur
 * ({@link PlayerInfoPanel}) et la barre d'actions ({@link ActionBarPanel})
 * dans une mise en page cohérente.</p>
 *
 * <p>Orchestre également la phase de mise en place (setup) et transitionne
 * vers les phases de jeu normales une fois tous les pions placés.</p>
 */
public class GameWindow extends JFrame {

    private final GameController controller;
    private final BoardPanel boardPanel;
    private final PlayerInfoPanel infoPanel;
    private final ActionBarPanel actionBar;

    // État de la mise en place
    private int setupExplorerIndex;   // index de l'explorateur à placer
    private int setupPlayerIndex;     // joueur courant pendant le setup
    private int setupBoatCount;       // bateaux placés par le joueur courant
    private boolean setupBoatPhase;   // true quand on place les bateaux

    /**
     * Construit et affiche la fenêtre principale.
     *
     * @param players la liste des joueurs configurés
     */
    public GameWindow(List<Player> players) {
        super("The Island");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        GameState state = new GameState(players);
        controller = new GameController(state);

        boardPanel  = new BoardPanel(controller, this);
        infoPanel   = new PlayerInfoPanel();
        actionBar   = new ActionBarPanel();

        buildLayout();
        wireButtons();

        setMinimumSize(new Dimension(900, 620));
        pack();
        setLocationRelativeTo(null);

        // Démarrer la mise en place
        startSetupPhase();
    }

    // ── Construction de l'interface ───────────────────────────────────────

    private void buildLayout() {
        getContentPane().setBackground(new Color(15, 35, 70));
        setLayout(new BorderLayout(4, 4));

        // Plateau avec scrollpane au centre
        JScrollPane boardScroll = new JScrollPane(boardPanel);
        boardScroll.setBackground(new Color(15, 35, 70));
        boardScroll.getViewport().setBackground(new Color(15, 40, 80));
        boardScroll.setBorder(BorderFactory.createLineBorder(new Color(40, 80, 140)));
        add(boardScroll, BorderLayout.CENTER);

        add(infoPanel, BorderLayout.EAST);
        add(actionBar, BorderLayout.SOUTH);

        // Barre de titre avec info joueur courant
        JPanel topBar = buildTopBar();
        add(topBar, BorderLayout.NORTH);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(10, 25, 55));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JLabel title = new JLabel("THE ISLAND");
        title.setFont(new Font("Georgia", Font.BOLD, 18));
        title.setForeground(new Color(255, 200, 50));
        bar.add(title, BorderLayout.WEST);

        return bar;
    }

    private void wireButtons() {
        actionBar.onPlayTile(e -> onPlaySavedTile());
        actionBar.onSkipTile(e -> {
            controller.skipSavedTile();
            refresh();
        });
        actionBar.onFinishMove(e -> {
            controller.finishMoving();
            refresh();
        });
        actionBar.onRollDice(e -> onRollDice());
        actionBar.onSkipCreature(e -> {
            controller.skipCreatureMove();
            refresh();
        });
        actionBar.onHelp(e -> new HelpDialog(this).setVisible(true));
    }

    // ── Phase de mise en place ────────────────────────────────────────────

    /**
     * Initialise la phase de placement des explorateurs.
     * Chaque joueur place ses 10 explorateurs un par un, puis ses 2 bateaux.
     */
    private void startSetupPhase() {
        setupPlayerIndex  = 0;
        setupExplorerIndex = 0;
        setupBoatCount    = 0;
        setupBoatPhase    = false;

        controller.getState().setPhase(GamePhase.SETUP);
        refresh();
        showMessage("Mise en place — " + currentSetupPlayer().getName()
                + " : placez vos explorateurs sur les tuiles terrain.", true);
    }

    /**
     * Callback appelé quand l'utilisateur clique sur une case pendant la mise en place.
     *
     * @param cell la case cliquée
     */
    public void onBoardClick(Cell cell) {
        if (controller.getState().getPhase() != GamePhase.SETUP) return;

        Player p = currentSetupPlayer();

        if (!setupBoatPhase) {
            // Placement des explorateurs
            if (setupExplorerIndex >= p.getExplorers().size()) return;
            Explorer explorer = p.getExplorers().get(setupExplorerIndex);
            ActionResult result = controller.setupPlaceExplorer(explorer, cell);
            if (result.isSuccess()) {
                setupExplorerIndex++;
                boardPanel.repaint();

                if (setupExplorerIndex >= p.getExplorers().size()) {
                    // Passer aux bateaux pour ce joueur
                    setupBoatPhase = true;
                    setupBoatCount = 0;
                    showMessage(p.getName() + " : placez vos 2 bateaux sur la mer (adjacents à une tuile).", true);
                } else {
                    showMessage(p.getName() + " : placez l'explorateur "
                            + (setupExplorerIndex + 1) + "/10", true);
                }
            } else {
                showMessage(result.getMessage(), false);
            }
        } else {
            // Placement des bateaux
            if (setupBoatCount >= 2) return;

            // Trouver un bateau disponible en réserve
            var reserveBoats = controller.getState().getBoard().getReserveBoats();
            if (reserveBoats.isEmpty()) {
                showMessage("Plus de bateaux disponibles.", false);
                return;
            }
            Boat boat = reserveBoats.get(0);
            ActionResult result = controller.setupPlaceBoat(boat, cell);
            if (result.isSuccess()) {
                setupBoatCount++;
                boardPanel.repaint();

                if (setupBoatCount >= 2) {
                    advanceSetupPlayer();
                } else {
                    showMessage(p.getName() + " : placez votre 2e bateau.", true);
                }
            } else {
                showMessage(result.getMessage(), false);
            }
        }
    }

    /**
     * Passe au joueur suivant pendant la mise en place,
     * ou démarre la partie si tous ont placé.
     */
    private void advanceSetupPlayer() {
        setupPlayerIndex++;
        setupExplorerIndex = 0;
        setupBoatPhase = false;
        setupBoatCount = 0;

        List<Player> players = controller.getState().getPlayers();
        if (setupPlayerIndex >= players.size()) {
            // Mise en place terminée
            controller.getState().nextTurn(); // initialise le premier vrai tour
            refresh();
            showMessage("La partie commence ! C'est à "
                    + controller.getState().getCurrentPlayer().getName() + " de jouer.", true);
        } else {
            boardPanel.repaint();
            showMessage("Mise en place — " + currentSetupPlayer().getName()
                    + " : placez vos explorateurs.", true);
        }
    }

    private Player currentSetupPlayer() {
        return controller.getState().getPlayers().get(setupPlayerIndex);
    }

    // ── Actions de la barre de boutons ────────────────────────────────────

    private void onRollDice() {
        ActionResult r = controller.rollDice();
        if (r.isSuccess()) {
            actionBar.showDiceResult(controller.getState().getLastDiceResult());
            infoPanel.log("Dé : " + controller.getState().getLastDiceResult(), true);

            // Vérifier si des créatures du type tiré sont en jeu
            var creaturesInPlay = controller.getState().getBoard()
                    .getCreaturesOnBoard(controller.getState().getLastDiceResult());
            if (creaturesInPlay.isEmpty()) {
                showMessage("Aucune créature de ce type en jeu. Tour suivant.", true);
                controller.skipCreatureMove();
                refresh();
            } else {
                showMessage("Cliquez sur une créature (" + controller.getState().getLastDiceResult()
                        + ") puis sur sa destination.", true);
            }
        }
    }

    private void onPlaySavedTile() {
        Player p = controller.getState().getCurrentPlayer();
        List<TerrainTile> savedTiles = p.getSavedTiles();

        if (savedTiles.isEmpty()) {
            showMessage("Vous n'avez pas de tuile à jouer.", false);
            controller.skipSavedTile();
            refresh();
            return;
        }

        // Construire un menu de sélection
        String[] options = savedTiles.stream()
                .map(t -> t.getAction().name())
                .toArray(String[]::new);

        String choice = (String) JOptionPane.showInputDialog(
                this, "Choisissez la tuile à jouer :",
                "Jouer une tuile", JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (choice == null) return; // annulation

        int idx = -1;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(choice)) { idx = i; break; }
        }
        if (idx < 0) return;

        TerrainTile tile = savedTiles.get(idx);
        // Pour simplifier, les tuiles DAUPHIN et VENT nécessitent une cible
        // → on laisse le joueur cliquer sur le plateau ensuite
        // (le boardPanel gère la sélection de créature)
        ActionResult result = controller.playSavedTile(tile, null, null);
        onActionResult(result);
    }

    // ── Callbacks depuis BoardPanel ───────────────────────────────────────

    /**
     * Traite le résultat d'une action : log + message + refresh.
     *
     * @param result le résultat de l'action
     */
    public void onActionResult(ActionResult result) {
        infoPanel.log(result.getMessage(), result.isSuccess());
        for (String effect : result.getEffects()) {
            infoPanel.log(effect, result.isSuccess());
        }

        if (!result.isSuccess()) {
            showMessage(result.getMessage(), false);
        } else {
            // Afficher les effets dans la barre de statut
            if (!result.getEffects().isEmpty()) {
                String allEffects = String.join(" | ", result.getEffects());
                showMessage(allEffects, true);
            }
        }

        // Vérifier fin de partie
        if (controller.getState().isGameOver()) {
            showEndGame();
            return;
        }

        refresh();
    }

    /**
     * Affiche un message dans la barre d'état (titre de la fenêtre).
     *
     * @param msg     le message
     * @param isInfo  vrai pour info, faux pour erreur
     */
    public void showMessage(String msg, boolean isInfo) {
        // On réutilise la barre de titre de la fenêtre pour les messages courts
        String prefix = isInfo ? "ℹ " : "⚠ ";
        setTitle("The Island — " + prefix + msg);
    }

    // ── Rafraîchissement ──────────────────────────────────────────────────

    /**
     * Rafraîchit tous les composants de la vue.
     */
    public void refresh() {
        GameState state = controller.getState();
        infoPanel.refresh(state);
        actionBar.updateForPhase(state.getPhase());
        actionBar.showDiceResult(state.getLastDiceResult());
        boardPanel.clearSelection();
        boardPanel.repaint();

        // Mettre à jour le titre avec le joueur courant
        if (state.getPhase() != GamePhase.FIN_DE_PARTIE
                && state.getPhase() != GamePhase.SETUP) {
            setTitle("The Island — Tour de " + state.getCurrentPlayer().getName()
                    + " | " + phaseLabel(state.getPhase()));
        }
    }

    private void showEndGame() {
        SwingUtilities.invokeLater(() -> {
            setTitle("The Island — FIN DE PARTIE");
            boardPanel.repaint();
            new ScoreDialog(this, controller.getState()).setVisible(true);
        });
    }

    private String phaseLabel(GamePhase phase) {
        return switch (phase) {
            case JOUER_TUILE_CONSERVEE -> "Jouer une tuile ?";
            case DEPLACER_PIONS       -> "Déplacer (" + controller.getState().getMovesRemaining() + " restants)";
            case RETIRER_TUILE        -> "Retirer une tuile";
            case LANCER_DE            -> "Dé créature";
            default -> phase.name();
        };
    }
}
