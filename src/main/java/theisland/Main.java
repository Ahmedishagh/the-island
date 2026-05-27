package theisland;

import theisland.view.SetupDialog;

import javax.swing.*;

/**
 * Point d'entrée du jeu The Island.
 * Lance l'interface de configuration puis démarre la partie.
 */
public class Main {

    public static void main(String[] args) {
        // On s'assure de tourner sur l'EDT pour tout ce qui touche à Swing
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // On reste avec le look par défaut si ça plante
            }

            SetupDialog setup = new SetupDialog(null);
            setup.setVisible(true);
        });
    }
}
