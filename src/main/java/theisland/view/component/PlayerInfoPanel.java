package theisland.view.component;

import theisland.model.GamePhase;
import theisland.model.GameState;
import theisland.model.Player;
import theisland.model.pion.Explorer;
import theisland.model.tile.TerrainTile;

import javax.swing.*;
import java.awt.*;

/**
 * Panneau latéral affichant les informations du joueur courant :
 * nom, score, phase, déplacements restants, tuiles conservées.
 */
public class PlayerInfoPanel extends JPanel {

    private final JLabel lblPlayer;
    private final JLabel lblPhase;
    private final JLabel lblMoves;
    private final JLabel lblScore;
    private final JLabel lblSaved;
    private final JList<String> lstTiles;
    private final DefaultListModel<String> tileModel;
    private final JTextArea taLog;

    /**
     * Construit le panneau latéral.
     */
    public PlayerInfoPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(200, 600));
        setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        setBackground(new Color(25, 50, 90));

        lblPlayer = makeLabel("---", 15, Font.BOLD);
        lblPhase  = makeLabel("", 11, Font.PLAIN);
        lblMoves  = makeLabel("", 11, Font.PLAIN);
        lblScore  = makeLabel("", 11, Font.PLAIN);
        lblSaved  = makeLabel("", 11, Font.PLAIN);

        add(sectionTitle("Joueur courant"));
        add(lblPlayer);
        add(Box.createVerticalStrut(6));
        add(sectionTitle("Phase"));
        add(lblPhase);
        add(Box.createVerticalStrut(6));
        add(lblMoves);
        add(Box.createVerticalStrut(12));
        add(sectionTitle("Score / Sauvés"));
        add(lblScore);
        add(lblSaved);
        add(Box.createVerticalStrut(12));
        add(sectionTitle("Tuiles conservées"));

        tileModel = new DefaultListModel<>();
        lstTiles = new JList<>(tileModel);
        lstTiles.setBackground(new Color(20, 45, 80));
        lstTiles.setForeground(Color.WHITE);
        lstTiles.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lstTiles.setSelectionBackground(new Color(60, 120, 200));
        JScrollPane tileScroll = new JScrollPane(lstTiles);
        tileScroll.setPreferredSize(new Dimension(190, 120));
        tileScroll.setMaximumSize(new Dimension(190, 120));
        tileScroll.setBorder(BorderFactory.createLineBorder(new Color(80, 120, 180)));
        add(tileScroll);

        add(Box.createVerticalStrut(12));
        add(sectionTitle("Journal"));

        taLog = new JTextArea(8, 18);
        taLog.setEditable(false);
        taLog.setBackground(new Color(15, 35, 65));
        taLog.setForeground(new Color(180, 210, 255));
        taLog.setFont(new Font("Monospaced", Font.PLAIN, 10));
        taLog.setLineWrap(true);
        taLog.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(taLog);
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 100, 160)));
        add(logScroll);
    }

    /**
     * Met à jour l'affichage en fonction de l'état de jeu courant.
     *
     * @param state l'état du jeu
     */
    public void refresh(GameState state) {
        Player p = state.getCurrentPlayer();

        lblPlayer.setText(p.getName());
        lblPlayer.setForeground(p.getColor().getAwtColor());

        lblPhase.setText(phaseLabel(state.getPhase()));
        lblMoves.setText("Déplacements restants : " + state.getMovesRemaining());
        lblScore.setText("Score actuel : " + p.computeScore() + " pts");
        lblSaved.setText("Sauvés : " + p.countSaved() + "/10");

        tileModel.clear();
        for (TerrainTile tile : p.getSavedTiles()) {
            tileModel.addElement("● " + tileActionLabel(tile));
        }
    }

    /**
     * Ajoute un message dans le journal des actions.
     *
     * @param msg     le message à ajouter
     * @param success vrai si l'action a réussi (vert), faux si erreur (orange)
     */
    public void log(String msg, boolean success) {
        String prefix = success ? "✓ " : "✗ ";
        taLog.append(prefix + msg + "\n");
        // Auto-scroll vers le bas
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }

    // ── Utilitaires UI ────────────────────────────────────────────────────

    private JLabel makeLabel(String text, int fontSize, int style) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", style, fontSize));
        lbl.setForeground(Color.WHITE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Arial", Font.BOLD, 9));
        lbl.setForeground(new Color(140, 180, 240));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private String phaseLabel(GamePhase phase) {
        return switch (phase) {
            case SETUP                  -> "Mise en place";
            case JOUER_TUILE_CONSERVEE  -> "Jouer une tuile ?";
            case DEPLACER_PIONS        -> "Déplacer les pions";
            case RETIRER_TUILE         -> "Retirer une tuile";
            case LANCER_DE             -> "Créature / Dé";
            case FIN_DE_PARTIE         -> "FIN DE PARTIE";
        };
    }

    private String tileActionLabel(TerrainTile tile) {
        return switch (tile.getAction()) {
            case DAUPHIN        -> "Dauphin (nageur +3)";
            case VENT           -> "Vent (bateau +3)";
            case CHASSER_REQUIN -> "Chasse requin";
            case CHASSER_BALEINE -> "Chasse baleine";
            case DEPLACER_SERPENT -> "Dépl. serpent";
            case DEPLACER_REQUIN  -> "Dépl. requin";
            case DEPLACER_BALEINE -> "Dépl. baleine";
            default -> tile.getAction().name();
        };
    }

    /** @return la liste des tuiles conservées (pour récupérer la sélection) */
    public JList<String> getTileList() { return lstTiles; }
}
