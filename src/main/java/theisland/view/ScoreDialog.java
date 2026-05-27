package theisland.view;

import theisland.model.GameState;
import theisland.model.Player;
import theisland.model.pion.Explorer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * Boîte de dialogue de fin de partie affichant le classement des joueurs.
 * Révèle les valeurs des explorateurs sauvés et affiche le vainqueur.
 */
public class ScoreDialog extends JDialog {

    /**
     * Crée la boîte de dialogue de scores.
     *
     * @param parent la fenêtre parente
     * @param state  l'état final du jeu
     */
    public ScoreDialog(Frame parent, GameState state) {
        super(parent, "Fin de partie – Résultats", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 460);
        setLocationRelativeTo(parent);
        buildUI(state);
    }

    private void buildUI(GameState state) {
        getContentPane().setBackground(new Color(20, 45, 85));
        setLayout(new BorderLayout(0, 8));

        // ── Titre ──────────────────────────────────────────────────────────
        Player winner = state.computeWinner();
        JLabel lblTitle = new JLabel("🏆 " + winner.getName() + " remporte la partie !", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 20));
        lblTitle.setForeground(new Color(255, 210, 60));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(16, 0, 4, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ── Tableau des scores ─────────────────────────────────────────────
        String[] columns = {"Joueur", "Explorateurs sauvés", "Score total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Trier par score décroissant
        List<Player> sorted = state.getPlayers().stream()
                .sorted(Comparator.comparingInt(Player::computeScore).reversed())
                .toList();

        for (Player p : sorted) {
            model.addRow(new Object[]{
                    p.getName() + " (" + p.getColor().getLabel() + ")",
                    p.countSaved() + " / 10",
                    p.computeScore() + " pts"
            });
        }

        JTable table = new JTable(model);
        table.setBackground(new Color(25, 55, 100));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 100, 160));
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.getTableHeader().setBackground(new Color(15, 35, 70));
        table.getTableHeader().setForeground(new Color(160, 200, 255));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Surligner le gagnant en or
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (row == 0) {
                    c.setBackground(new Color(80, 65, 10));
                    c.setForeground(new Color(255, 220, 60));
                } else {
                    c.setBackground(new Color(25, 55, 100));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        scroll.getViewport().setBackground(new Color(25, 55, 100));
        add(scroll, BorderLayout.CENTER);

        // ── Détail des explorateurs sauvés ────────────────────────────────
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBackground(new Color(18, 40, 75));
        detailPanel.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));

        JLabel lblDetail = new JLabel("Détail des explorateurs sauvés");
        lblDetail.setForeground(new Color(140, 180, 240));
        lblDetail.setFont(new Font("Arial", Font.BOLD, 11));
        lblDetail.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(lblDetail);

        for (Player p : sorted) {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getName()).append(" → ");
            boolean first = true;
            for (Explorer e : p.getExplorers()) {
                if (e.isSaved()) {
                    if (!first) sb.append(", ");
                    sb.append(e.getTreasureValue());
                    first = false;
                }
            }
            if (first) sb.append("(aucun)");
            JLabel lbl = new JLabel(sb.toString());
            lbl.setForeground(p.getColor().getAwtColor());
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailPanel.add(lbl);
        }

        add(detailPanel, BorderLayout.EAST);

        // ── Boutons ────────────────────────────────────────────────────────
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        south.setBackground(new Color(15, 35, 70));

        JButton btnNew = new JButton("Nouvelle partie");
        btnNew.setBackground(new Color(60, 140, 60));
        btnNew.setForeground(Color.WHITE);
        btnNew.setFont(new Font("Arial", Font.BOLD, 12));
        btnNew.setFocusPainted(false);
        btnNew.setBorderPainted(false);
        btnNew.addActionListener(e -> {
            dispose();
            // Ouvrir une nouvelle config
            SetupDialog setup = new SetupDialog(null);
            setup.setVisible(true);
        });

        JButton btnQuit = new JButton("Quitter");
        btnQuit.setBackground(new Color(150, 50, 50));
        btnQuit.setForeground(Color.WHITE);
        btnQuit.setFont(new Font("Arial", Font.BOLD, 12));
        btnQuit.setFocusPainted(false);
        btnQuit.setBorderPainted(false);
        btnQuit.addActionListener(e -> System.exit(0));

        south.add(btnNew);
        south.add(btnQuit);
        add(south, BorderLayout.SOUTH);
    }
}
