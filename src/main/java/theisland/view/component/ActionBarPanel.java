package theisland.view.component;

import theisland.model.GamePhase;
import theisland.model.pion.CreatureType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Barre d'actions en bas de la fenêtre.
 * Affiche les boutons disponibles selon la phase de jeu courante.
 */
public class ActionBarPanel extends JPanel {

    private JButton btnPlayTile;
    private JButton btnSkipTile;
    private JButton btnFinishMove;
    private JButton btnRollDice;
    private JButton btnSkipCreature;
    private JButton btnHelp;

    private JLabel lblDiceResult;

    /**
     * Construit la barre d'actions.
     */
    public ActionBarPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        setBackground(new Color(18, 40, 75));
        setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(60, 100, 160)));

        btnPlayTile    = makeButton("Jouer tuile conservée", new Color(80, 160, 80));
        btnSkipTile    = makeButton("Passer", new Color(100, 100, 120));
        btnFinishMove  = makeButton("Fin des déplacements", new Color(60, 120, 200));
        btnRollDice    = makeButton("Lancer le dé", new Color(200, 130, 30));
        btnSkipCreature= makeButton("Aucune créature → fin de tour", new Color(100, 80, 120));
        btnHelp        = makeButton("? Aide", new Color(60, 60, 80));

        lblDiceResult  = new JLabel();
        lblDiceResult.setFont(new Font("Arial", Font.BOLD, 14));
        lblDiceResult.setForeground(Color.WHITE);

        add(btnPlayTile);
        add(btnSkipTile);
        add(btnFinishMove);
        add(btnRollDice);
        add(btnSkipCreature);
        add(lblDiceResult);
        add(Box.createHorizontalGlue());
        add(btnHelp);
    }

    /**
     * Met à jour la visibilité des boutons selon la phase courante.
     *
     * @param phase la phase de jeu active
     */
    public void updateForPhase(GamePhase phase) {
        btnPlayTile.setVisible(phase == GamePhase.JOUER_TUILE_CONSERVEE);
        btnSkipTile.setVisible(phase == GamePhase.JOUER_TUILE_CONSERVEE);
        btnFinishMove.setVisible(phase == GamePhase.DEPLACER_PIONS);
        btnRollDice.setVisible(phase == GamePhase.LANCER_DE);
        btnSkipCreature.setVisible(phase == GamePhase.LANCER_DE);
        lblDiceResult.setVisible(phase == GamePhase.LANCER_DE);
        revalidate();
        repaint();
    }

    /**
     * Affiche le résultat du dé.
     *
     * @param type le type de créature tirée, ou null pour masquer
     */
    public void showDiceResult(CreatureType type) {
        if (type == null) {
            lblDiceResult.setText("");
            return;
        }
        String label = switch (type) {
            case SERPENT_DE_MER -> "🐍 Serpent de Mer";
            case REQUIN         -> "🦈 Requin";
            case BALEINE        -> "🐋 Baleine";
        };
        lblDiceResult.setText(label);
    }

    // ── Enregistrement des listeners ──────────────────────────────────────

    /** @param l listener pour le bouton "Jouer tuile" */
    public void onPlayTile(ActionListener l)     { btnPlayTile.addActionListener(l); }

    /** @param l listener pour le bouton "Passer" */
    public void onSkipTile(ActionListener l)     { btnSkipTile.addActionListener(l); }

    /** @param l listener pour "Fin des déplacements" */
    public void onFinishMove(ActionListener l)   { btnFinishMove.addActionListener(l); }

    /** @param l listener pour "Lancer le dé" */
    public void onRollDice(ActionListener l)     { btnRollDice.addActionListener(l); }

    /** @param l listener pour "Aucune créature" */
    public void onSkipCreature(ActionListener l) { btnSkipCreature.addActionListener(l); }

    /** @param l listener pour le bouton Aide */
    public void onHelp(ActionListener l)         { btnHelp.addActionListener(l); }

    // ── Utilitaire ────────────────────────────────────────────────────────

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
