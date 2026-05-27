package theisland.view;

import javax.swing.*;
import java.awt.*;

/**
 * Boîte de dialogue d'aide présentant les règles du jeu
 * de façon résumée et accessible aux débutants.
 *
 * <p>La fenêtre est organisée en onglets thématiques pour
 * retrouver facilement l'information cherchée.</p>
 */
public class HelpDialog extends JDialog {

    /**
     * Crée et affiche la boîte d'aide.
     *
     * @param parent la fenêtre parente
     */
    public HelpDialog(Frame parent) {
        super(parent, "Aide – Règles du jeu The Island", false);
        setSize(620, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(25, 50, 90));
        tabs.setForeground(Color.WHITE);

        tabs.addTab("Objectif", makeTab(RULES_OBJECTIF));
        tabs.addTab("Tour de jeu", makeTab(RULES_TOUR));
        tabs.addTab("Déplacements", makeTab(RULES_DEPLACEMENT));
        tabs.addTab("Créatures", makeTab(RULES_CREATURES));
        tabs.addTab("Tuiles", makeTab(RULES_TUILES));
        tabs.addTab("Fin de partie", makeTab(RULES_FIN));

        add(tabs, BorderLayout.CENTER);

        JButton btnClose = new JButton("Fermer");
        btnClose.setBackground(new Color(60, 60, 80));
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dispose());
        JPanel south = new JPanel();
        south.setBackground(new Color(20, 40, 75));
        south.add(btnClose);
        add(south, BorderLayout.SOUTH);
    }

    private JScrollPane makeTab(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Arial", Font.PLAIN, 12));
        area.setBackground(new Color(20, 45, 85));
        area.setForeground(new Color(220, 235, 255));
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        return new JScrollPane(area);
    }

    // ── Textes d'aide ─────────────────────────────────────────────────────

    private static final String RULES_OBJECTIF =
        "OBJECTIF\n\n" +
        "Vous êtes des explorateurs pris au piège sur une île qui s'enfonce dans la mer suite" +
        " à une éruption volcanique. Il faut évacuer vos 10 explorateurs en les embarquant " +
        "dans des bateaux et en les débarquant sur les plages sûres dans les coins du plateau.\n\n" +
        "Chaque explorateur porte une valeur de trésor secrète (1 à 6) inscrite sous son pion. " +
        "Le joueur qui totalise le plus de points de trésor avec ses explorateurs sauvés gagne la partie.\n\n" +
        "En cas d'égalité de score, le joueur ayant sauvé le plus grand nombre d'explorateurs l'emporte.";

    private static final String RULES_TOUR =
        "DÉROULEMENT D'UN TOUR\n\n" +
        "Chaque joueur joue son tour dans l'ordre. Un tour se décompose en 4 étapes :\n\n" +
        "1. JOUER UNE TUILE CONSERVÉE (optionnel)\n" +
        "   Si vous possédez une tuile de terrain à contour rouge, vous pouvez la jouer " +
        "en début de tour (dauphin, vent, déplacement de créature).\n\n" +
        "2. DÉPLACER LES PIONS\n" +
        "   Vous disposez de 3 cases de déplacement au total, répartissables librement " +
        "entre vos explorateurs et vos bateaux.\n\n" +
        "3. RETIRER UNE TUILE DE TERRAIN (obligatoire)\n" +
        "   Vous devez retirer une tuile de l'île. L'ordre est impératif : d'abord les " +
        "tuiles Plage, puis les tuiles Forêt, puis les tuiles Montagne.\n" +
        "   La tuile retirée doit être adjacente à la mer.\n\n" +
        "4. LANCER LE DÉ ET DÉPLACER UNE CRÉATURE\n" +
        "   Le dé indique quel type de créature déplacer (serpent, requin ou baleine). " +
        "Si aucune créature de ce type n'est en jeu, il ne se passe rien.";

    private static final String RULES_DEPLACEMENT =
        "DÉPLACEMENTS\n\n" +
        "EXPLORATEURS SUR TERRE\n" +
        "• Peuvent aller sur n'importe quelle tuile adjacente (même occupée).\n" +
        "• Peuvent sauter sur un bateau adjacent (si une place est libre).\n" +
        "• Tombent à la mer (nageur) si leur tuile est retirée.\n\n" +
        "NAGEURS\n" +
        "• Ne peuvent se déplacer que d'UNE SEULE case de mer par tour.\n" +
        "• Peuvent monter sur un bateau s'ils partagent la même case.\n" +
        "• Tués immédiatement s'ils rencontrent un requin ou un serpent de mer.\n\n" +
        "BATEAUX\n" +
        "• Capacité maximale : 3 explorateurs.\n" +
        "• Un bateau vide peut être déplacé par n'importe quel joueur.\n" +
        "• Un bateau occupé est contrôlé par le joueur ayant le plus de passagers.\n" +
        "• Débarquer sur une plage de coin sauve les explorateurs à bord.\n\n" +
        "CONSEIL : Essayez de regrouper vos explorateurs sur des bateaux " +
        "puis naviguez vers les plages de coin pour les mettre en sécurité.";

    private static final String RULES_CREATURES =
        "CRÉATURES MARINES\n\n" +
        "SERPENT DE MER (vert S)\n" +
        "• Déplacement : 1 case de mer.\n" +
        "• Coule les bateaux avec passagers (ils sont tués).\n" +
        "• Élimine tous les nageurs sur sa case.\n" +
        "• N'affecte pas les bateaux vides.\n\n" +
        "REQUIN (rouge R)\n" +
        "• Déplacement : 1 à 2 cases de mer.\n" +
        "• Élimine tous les nageurs sur sa case.\n" +
        "• N'affecte pas les bateaux (chargés ou vides).\n\n" +
        "BALEINE (bleu B)\n" +
        "• Déplacement : 1 à 3 cases de mer.\n" +
        "• Fait chavirer les bateaux chargés (les passagers deviennent nageurs).\n" +
        "• Si un requin est sur la même case, les nageurs sont tués.\n" +
        "• N'affecte pas les bateaux vides ni les nageurs seuls.\n\n" +
        "Les créatures n'ont aucun effet les unes sur les autres.";

    private static final String RULES_TUILES =
        "TUILES DE TERRAIN\n\n" +
        "Chaque tuile a une face cachée révélée au moment du retrait.\n\n" +
        "TUILES À EFFET IMMÉDIAT (contour vert)\n" +
        "• REQUIN : apparition d'un requin sur la case libérée (nageurs tués).\n" +
        "• BALEINE : apparition d'une baleine sur la case libérée.\n" +
        "• BATEAU : apparition d'un bateau (nageurs embarquent si possible).\n" +
        "• TOURBILLON : tout est retiré du jeu dans la zone (case + voisins mer).\n" +
        "• VOLCAN : FIN DE PARTIE IMMÉDIATE !\n\n" +
        "TUILES CONSERVÉES (contour rouge)\n" +
        "La tuile est gardée en main et peut être jouée plus tard.\n" +
        "• DAUPHIN : déplace un de vos nageurs de 1 à 3 cases (début de tour).\n" +
        "• VENT : déplace un de vos bateaux de 1 à 3 cases (début de tour).\n" +
        "• DÉPLACEMENT CRÉATURE : téléporte une créature sur n'importe quelle case mer.\n" +
        "• CHASSE REQUIN : retire un requin qui attaque vos nageurs (en défense).\n" +
        "• CHASSE BALEINE : retire une baleine qui attaque vos bateaux (en défense).";

    private static final String RULES_FIN =
        "FIN DE PARTIE\n\n" +
        "La partie se termine dès que la tuile VOLCAN est révélée.\n\n" +
        "• Tous les explorateurs encore en mer ou sur l'île sont perdus.\n" +
        "• Seuls les explorateurs débarqués sur les plages de coin comptent.\n\n" +
        "DÉCOMPTE DES POINTS\n" +
        "On retourne les pions sauvés pour révéler leur valeur de trésor (1 à 6).\n" +
        "Le joueur avec le total le plus élevé gagne.\n\n" +
        "ÉGALITÉ\n" +
        "En cas d'égalité de score, c'est le joueur ayant sauvé le plus " +
        "d'explorateurs qui remporte la victoire.\n\n" +
        "RAPPEL DES VALEURS\n" +
        "Chaque joueur a 10 explorateurs avec les valeurs suivantes :\n" +
        "1, 1, 1, 2, 2, 3, 3, 4, 5, 6 (Total maximum : 28 points)";
}
