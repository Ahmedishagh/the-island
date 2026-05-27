package theisland.view.component;

import theisland.controller.ActionResult;
import theisland.controller.GameController;
import theisland.model.*;
import theisland.model.pion.*;
import theisland.model.tile.TerrainType;
import theisland.util.HexCoord;
import theisland.view.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.List;

/**
 * Composant Swing principal : affiche le plateau hexagonal du jeu.
 *
 * <p>Gère les clics de souris pour sélectionner et déplacer les pions,
 * retirer les tuiles, et déclencher les actions de chaque phase.</p>
 */
public class BoardPanel extends JPanel {

    private static final int HEX_SIZE = 36;
    private static final int ORIGIN_X = 20;
    private static final int ORIGIN_Y = 20;

    private final GameController controller;
    private final GameWindow parentWindow;
    private final HexRenderer renderer;

    // Pion sélectionné par le joueur (explorateur, bateau ou créature)
    private Object selectedPion;
    // Case sélectionnée (pour retrait de tuile)
    private HexCoord selectedCoord;

    // Couleurs de rendu
    private static final Color COLOR_SEA        = new Color(30, 100, 180, 220);
    private static final Color COLOR_BEACH      = new Color(240, 210, 130);
    private static final Color COLOR_FOREST     = new Color(50, 130, 60);
    private static final Color COLOR_MOUNTAIN   = new Color(140, 110, 80);
    private static final Color COLOR_CORNER     = new Color(255, 235, 160);
    private static final Color COLOR_SELECTION  = new Color(255, 255, 0, 160);
    private static final Color COLOR_VALID_MOVE = new Color(100, 255, 100, 120);
    private static final Color COLOR_GRID       = new Color(20, 60, 120, 160);

    /**
     * Crée le panneau du plateau.
     *
     * @param controller  le contrôleur du jeu
     * @param parentWindow la fenêtre principale (pour mises à jour)
     */
    public BoardPanel(GameController controller, GameWindow parentWindow) {
        this.controller = controller;
        this.parentWindow = parentWindow;
        this.renderer = new HexRenderer(HEX_SIZE, ORIGIN_X, ORIGIN_Y);

        setPreferredSize(new Dimension(
                ORIGIN_X * 2 + (int)(Board.COLS * HEX_SIZE * Math.sqrt(3) * 0.75) + HEX_SIZE * 2,
                ORIGIN_Y * 2 + Board.ROWS * HEX_SIZE + HEX_SIZE * 3
        ));
        setBackground(new Color(15, 40, 80));
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY(), e.getButton());
            }
        });
    }

    // ── Rendu ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Board board = controller.getState().getBoard();

        for (Cell cell : board.getAllCells()) {
            drawCell(g2, cell, board);
        }

        // Overlay de sélection
        if (selectedCoord != null) {
            Path2D hex = renderer.hexagonFor(selectedCoord);
            g2.setColor(COLOR_SELECTION);
            g2.fill(hex);
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(hex);
        }
    }

    private void drawCell(Graphics2D g2, Cell cell, Board board) {
        HexCoord coord = cell.getCoord();
        Path2D hex = renderer.hexagonFor(coord);
        Point center = renderer.hexToPixel(coord);

        // Remplissage selon le type
        Color fill = getCellColor(cell);
        g2.setColor(fill);
        g2.fill(hex);

        // Bord
        g2.setColor(COLOR_GRID);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(hex);

        // Contenus
        drawContents(g2, cell, center);
    }

    private Color getCellColor(Cell cell) {
        if (cell.isCornerBeach()) return COLOR_CORNER;
        if (cell.hasTerrain()) {
            return switch (cell.getTile().getType()) {
                case PLAGE   -> COLOR_BEACH;
                case FORET   -> COLOR_FOREST;
                case MONTAGNE -> COLOR_MOUNTAIN;
                default      -> COLOR_SEA;
            };
        }
        return COLOR_SEA;
    }

    private void drawContents(Graphics2D g2, Cell cell, Point center) {
        int x = center.x;
        int y = center.y;

        // Explorateurs sur terre
        List<Explorer> onLand = cell.getExplorersOnLand();
        if (!onLand.isEmpty()) {
            drawExplorerGroup(g2, onLand, x - 8, y - 8);
        }

        // Nageurs
        List<Explorer> swimmers = cell.getSwimmers();
        if (!swimmers.isEmpty()) {
            drawSwimmerGroup(g2, swimmers, x + 4, y);
        }

        // Bateaux
        List<Boat> boats = cell.getBoats();
        if (!boats.isEmpty()) {
            drawBoat(g2, boats.get(0), x, y + 8);
        }

        // Créatures
        for (Creature c : cell.getCreatures()) {
            if (!c.isRemoved()) drawCreature(g2, c, x + 10, y - 10);
        }
    }

    private void drawExplorerGroup(Graphics2D g2, List<Explorer> explorers, int x, int y) {
        for (int i = 0; i < Math.min(explorers.size(), 4); i++) {
            Explorer e = explorers.get(i);
            int dx = (i % 2) * 8;
            int dy = (i / 2) * 8;
            g2.setColor(e.getColor().getAwtColor());
            g2.fillOval(x + dx - 5, y + dy - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1f));
            g2.drawOval(x + dx - 5, y + dy - 5, 10, 10);
        }
        if (explorers.size() > 4) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 7));
            g2.drawString("+" + (explorers.size() - 4), x + 12, y + 12);
        }
    }

    private void drawSwimmerGroup(Graphics2D g2, List<Explorer> swimmers, int x, int y) {
        for (int i = 0; i < Math.min(swimmers.size(), 3); i++) {
            Explorer e = swimmers.get(i);
            g2.setColor(e.getColor().getAwtColor().darker());
            g2.fillRect(x + i * 5 - 3, y - 3, 6, 6);
            g2.setColor(Color.CYAN);
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRect(x + i * 5 - 3, y - 3, 6, 6);
        }
    }

    private void drawBoat(Graphics2D g2, Boat boat, int x, int y) {
        // Corps du bateau (arc)
        g2.setColor(new Color(180, 130, 60));
        g2.fillArc(x - 10, y - 4, 20, 10, 0, -180);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 8));
        g2.drawString(boat.getPassengers().size() + "/" + Boat.MAX_CAPACITY, x - 6, y + 1);

        // Petites couleurs des passagers
        List<Explorer> passengers = boat.getPassengers();
        for (int i = 0; i < passengers.size(); i++) {
            g2.setColor(passengers.get(i).getColor().getAwtColor());
            g2.fillOval(x - 8 + i * 6, y - 12, 5, 5);
        }
    }

    private void drawCreature(Graphics2D g2, Creature creature, int x, int y) {
        Color col = switch (creature.getType()) {
            case SERPENT_DE_MER -> new Color(80, 200, 80);
            case REQUIN         -> new Color(200, 80, 80);
            case BALEINE        -> new Color(80, 150, 220);
        };
        String symbol = switch (creature.getType()) {
            case SERPENT_DE_MER -> "S";
            case REQUIN         -> "R";
            case BALEINE        -> "B";
        };
        g2.setColor(col);
        g2.fillOval(x - 6, y - 6, 12, 12);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 9));
        g2.drawString(symbol, x - 3, y + 4);
    }

    // ── Gestion des clics ─────────────────────────────────────────────────

    private void handleClick(int px, int py, int button) {
        HexCoord coord = renderer.pixelToHex(px, py, Board.COLS, Board.ROWS);
        if (coord == null) return;

        Board board = controller.getState().getBoard();
        Cell cell = board.getCell(coord);
        if (cell == null) return;

        GamePhase phase = controller.getState().getPhase();

        if (button == MouseEvent.BUTTON1) {
            handleLeftClick(cell, coord, phase);
        } else if (button == MouseEvent.BUTTON3) {
            // Clic droit = déselectionner
            selectedPion = null;
            selectedCoord = null;
            repaint();
        }
    }

    private void handleLeftClick(Cell cell, HexCoord coord, GamePhase phase) {
        switch (phase) {
            case SETUP -> handleSetupClick(cell);
            case DEPLACER_PIONS -> handleMoveClick(cell);
            case RETIRER_TUILE -> handleRemoveTileClick(cell);
            case LANCER_DE -> handleCreatureMoveClick(cell);
            default -> parentWindow.showMessage(
                    "Aucune action disponible dans cette phase.", false);
        }
    }

    private void handleSetupClick(Cell cell) {
        // Placement en phase de mise en place — géré par le parent
        parentWindow.onBoardClick(cell);
    }

    private void handleMoveClick(Cell cell) {
        if (selectedPion == null) {
            // Sélectionner un pion sur cette case
            trySelectPion(cell);
        } else {
            // Déplacer le pion sélectionné vers cette case
            applyMove(cell);
        }
    }

    private void trySelectPion(Cell cell) {
        Player current = controller.getState().getCurrentPlayer();

        // Chercher d'abord un explorateur du joueur courant sur cette case
        for (Explorer e : cell.getExplorersOnLand()) {
            if (e.getColor() == current.getColor()) {
                selectedPion = e;
                selectedCoord = cell.getCoord();
                repaint();
                parentWindow.showMessage("Explorateur sélectionné. Cliquez sur la destination.", true);
                return;
            }
        }
        for (Explorer e : cell.getSwimmers()) {
            if (e.getColor() == current.getColor()) {
                selectedPion = e;
                selectedCoord = cell.getCoord();
                repaint();
                parentWindow.showMessage("Nageur sélectionné.", true);
                return;
            }
        }
        for (Boat b : cell.getBoats()) {
            if (b.isEmpty() || b.isControlledBy(current.getColor())) {
                selectedPion = b;
                selectedCoord = cell.getCoord();
                repaint();
                parentWindow.showMessage("Bateau sélectionné.", true);
                return;
            }
        }

        parentWindow.showMessage("Aucun pion à vous sur cette case.", false);
    }

    private void applyMove(Cell target) {
        ActionResult result;

        if (selectedPion instanceof Explorer explorer) {
            result = controller.moveExplorer(explorer, target);
        } else if (selectedPion instanceof Boat boat) {
            result = controller.moveBoat(boat, target);
        } else {
            result = ActionResult.error("Pion inconnu.");
        }

        selectedPion = null;
        selectedCoord = null;
        repaint();

        parentWindow.onActionResult(result);
    }

    private void handleRemoveTileClick(Cell cell) {
        if (!cell.hasTerrain()) {
            parentWindow.showMessage("Cliquez sur une tuile terrain à retirer.", false);
            return;
        }
        ActionResult result = controller.removeTile(cell);
        repaint();
        parentWindow.onActionResult(result);
    }

    private void handleCreatureMoveClick(Cell cell) {
        if (selectedPion instanceof Creature creature) {
            ActionResult result = controller.moveCreature(creature, cell);
            selectedPion = null;
            selectedCoord = null;
            repaint();
            parentWindow.onActionResult(result);
        } else {
            // Sélectionner une créature du bon type
            CreatureType expected = controller.getState().getLastDiceResult();
            if (expected == null) {
                parentWindow.showMessage("Lancez le dé d'abord.", false);
                return;
            }
            for (Creature c : cell.getCreatures()) {
                if (c.getType() == expected && !c.isRemoved()) {
                    selectedPion = c;
                    selectedCoord = cell.getCoord();
                    repaint();
                    parentWindow.showMessage("Créature sélectionnée. Cliquez sur la destination.", true);
                    return;
                }
            }
            parentWindow.showMessage("Aucune créature de type " + expected + " sur cette case.", false);
        }
    }

    /**
     * Réinitialise la sélection courante.
     */
    public void clearSelection() {
        selectedPion = null;
        selectedCoord = null;
        repaint();
    }

    /** @return le pion actuellement sélectionné */
    public Object getSelectedPion() { return selectedPion; }
}
