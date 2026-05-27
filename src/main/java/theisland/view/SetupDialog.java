package theisland.view;

import theisland.model.Player;
import theisland.model.PlayerColor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Boîte de dialogue de configuration de la partie.
 * Permet de choisir le nombre de joueurs (2 à 4) et leur nom.
 *
 * <p>Une fois validé, lance la fenêtre principale du jeu.</p>
 */
public class SetupDialog extends JDialog {

    private JSpinner spnPlayerCount;
    private final JTextField[] nameFields = new JTextField[4];
    private final JLabel[] colorLabels   = new JLabel[4];
    private JPanel playerPanel;

    private boolean confirmed = false;
    private List<Player> players;

    /**
     * Crée la boîte de dialogue de configuration.
     *
     * @param parent la fenêtre parente (peut être null)
     */
    public SetupDialog(Frame parent) {
        super(parent, "The Island – Nouvelle partie", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        getContentPane().setBackground(new Color(20, 45, 85));
        setLayout(new BorderLayout(0, 0));

        // ── Titre ──────────────────────────────────────────────────────────
        JLabel title = new JLabel("THE ISLAND", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(new Color(255, 210, 80));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 4, 0));
        add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Configuration de la partie", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.ITALIC, 13));
        subtitle.setForeground(new Color(160, 200, 255));

        // ── Centre ─────────────────────────────────────────────────────────
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(new Color(20, 45, 85));
        center.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        center.add(subtitle);
        center.add(Box.createVerticalStrut(16));

        // Nombre de joueurs
        JPanel nbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nbPanel.setBackground(new Color(20, 45, 85));
        JLabel lblNb = new JLabel("Nombre de joueurs : ");
        lblNb.setForeground(Color.WHITE);
        lblNb.setFont(new Font("Arial", Font.PLAIN, 13));
        SpinnerNumberModel model = new SpinnerNumberModel(2, 2, 4, 1);
        spnPlayerCount = new JSpinner(model);
        spnPlayerCount.setPreferredSize(new Dimension(55, 26));
        nbPanel.add(lblNb);
        nbPanel.add(spnPlayerCount);
        center.add(nbPanel);
        center.add(Box.createVerticalStrut(10));

        // Noms des joueurs
        playerPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        playerPanel.setBackground(new Color(20, 45, 85));
        center.add(playerPanel);

        // Initialiser avec 2 joueurs
        buildPlayerFields(2);

        spnPlayerCount.addChangeListener(e -> {
            int count = (int) spnPlayerCount.getValue();
            buildPlayerFields(count);
            pack();
        });

        add(center, BorderLayout.CENTER);

        // ── Boutons ────────────────────────────────────────────────────────
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        south.setBackground(new Color(15, 35, 70));

        JButton btnStart = new JButton("Démarrer la partie");
        btnStart.setBackground(new Color(60, 160, 60));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFont(new Font("Arial", Font.BOLD, 13));
        btnStart.setFocusPainted(false);
        btnStart.setBorderPainted(false);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.addActionListener(e -> onStart());

        JButton btnCancel = new JButton("Quitter");
        btnCancel.setBackground(new Color(160, 50, 50));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Arial", Font.BOLD, 13));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> System.exit(0));

        south.add(btnStart);
        south.add(btnCancel);
        add(south, BorderLayout.SOUTH);
    }

    /**
     * Reconstruit les champs de saisie des noms selon le nombre de joueurs.
     */
    private void buildPlayerFields(int count) {
        playerPanel.removeAll();
        PlayerColor[] colors = PlayerColor.values();
        String[] defaultNames = {"Alice", "Bob", "Charlie", "Diana"};

        for (int i = 0; i < count; i++) {
            // Label couleur
            JLabel lbl = new JLabel("Joueur " + (i + 1) + " (" + colors[i].getLabel() + ") :");
            lbl.setForeground(colors[i].getAwtColor());
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            colorLabels[i] = lbl;

            // Champ de nom
            if (nameFields[i] == null) {
                nameFields[i] = new JTextField(defaultNames[i], 12);
            }
            nameFields[i].setBackground(new Color(35, 65, 110));
            nameFields[i].setForeground(Color.WHITE);
            nameFields[i].setCaretColor(Color.WHITE);
            nameFields[i].setFont(new Font("Arial", Font.PLAIN, 12));
            nameFields[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(colors[i].getAwtColor()),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));

            playerPanel.add(lbl);
            playerPanel.add(nameFields[i]);
        }

        playerPanel.revalidate();
        playerPanel.repaint();
    }

    /**
     * Valide la configuration et lance la fenêtre de jeu.
     */
    private void onStart() {
        int count = (int) spnPlayerCount.getValue();
        players = new ArrayList<>();
        PlayerColor[] colors = PlayerColor.values();

        for (int i = 0; i < count; i++) {
            String name = nameFields[i].getText().trim();
            if (name.isEmpty()) name = "Joueur " + (i + 1);
            players.add(new Player(name, colors[i]));
        }

        confirmed = true;
        dispose();

        // Lancer la fenêtre principale
        GameWindow gw = new GameWindow(players);
        gw.setVisible(true);
    }

    /** @return vrai si la configuration a été validée */
    public boolean isConfirmed() { return confirmed; }

    /** @return la liste des joueurs configurés */
    public List<Player> getPlayers() { return players; }
}
