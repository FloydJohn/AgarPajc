package it.unibs.pajc.agar;

import it.unibs.pajc.agar.network.NetworkController;
import it.unibs.pajc.agar.universe.Food;
import it.unibs.pajc.agar.universe.GameObject;
import it.unibs.pajc.agar.universe.Player;
import it.unibs.pajc.agar.universe.Universe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class GameController extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

    private static final String CONNECTING = "Connecting...";

    private static GameController instance;
    private final boolean debugging = false;
    Universe universe;
    Point2D.Float mouse, eventMousePosition = new Point2D.Float();
    Font loginFont = new Font("Arial", Font.BOLD, 50),
            massFont = new Font("Arial", Font.BOLD, 12);

    Rectangle viewWindow;
    AffineTransform oldTransform, newTransform;

    private GameController() {
        super();
        StartDialog dialog = new StartDialog();
        //noinspection PointlessBooleanExpression, ConstantConditions
        if (!debugging) dialog.setVisible(true);
        else dialog.closeDialog(true);
        this.setMinimumSize(new Dimension(800,600));
        new Timer(20, e -> this.repaint()).start();
        mouse = new Point2D.Float(0,0);
        universe = new Universe(dialog.getPlayerName(), new Dimension(5000, 3000));
        viewWindow = new Rectangle(0, 0, 0, 0);
        updateViewWindow();
        if (dialog.isServer()) universe.generateRandomFood(500);
        NetworkController.getInstance().connect(dialog.isServer(), dialog.getIpAddress(), 1234, universe);
    }

    public static GameController getInstance() {
        if (instance == null) instance = new GameController();
        return instance;
    }

    @Override
    protected void paintComponent(Graphics gOrig) {
        Graphics2D g = (Graphics2D) gOrig;

        switch (NetworkController.getInstance().getCurrentState()) {
            case CONNECTED:
                gameLoop(g);
                break;
            case LOGIN:
                loginLoop(g);
                break;
            case DEAD:
                System.exit(1);
                break;
        }
    }

    private void loginLoop(Graphics2D g) {
        clearScreen(g, Color.BLACK);
        g.setFont(loginFont);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(CONNECTING, g);
        int x = (int) ((viewWindow.getWidth() - (int) r.getWidth()) / 2);
        int y = (int) ((viewWindow.getHeight() - (int) r.getHeight()) / 2 + fm.getAscent());
        g.drawString(CONNECTING, x, y);
    }

    private void gameLoop(Graphics2D g) {
        this.setSize(800, 600);
        oldTransform = g.getTransform();
        updateViewWindow();
        initScreen(g);
        newTransform = g.getTransform();
        mouse.setLocation((eventMousePosition.x + viewWindow.x) / newTransform.getScaleX(), (eventMousePosition.y + viewWindow.y) / newTransform.getScaleX());

        g.setColor(Color.RED);
        g.drawOval((int) mouse.x - 5, (int) mouse.y - 5, 10, 10);

        if ((mouse.getX() != 0 || mouse.getY() != 0) && !universe.getPlayer().isInside(mouse)) {
            universe.getPlayer().setTarget(mouse);
        } else universe.getPlayer().setTarget(null);

        universe.update();

        for (Food f : universe.getFoods().values()) {
            if (//!f.isInside(viewWindow) ||//TODO Restore
                    f.getCurrentState().equals(GameObject.State.TO_REMOVE) ||
                            f.getCurrentState().equals(GameObject.State.REMOVED)) continue;
            g.setColor(f.getColor());
            g.fill(f.getShape(true));
        }

        g.setFont(massFont);
        for (Player p : universe.getPlayers().values()) {
            g.setColor(p.getColor());
            for (Player.Piece piece : p.getPieces()) {
                if (!piece.isInside(viewWindow)) continue;
                g.setTransform(newTransform);
                g.fill(piece.getShape(true));
                g.setTransform(oldTransform);
                g.setColor(Color.WHITE);
                String weight = String.valueOf(piece.getMass());
                FontMetrics fm = g.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(weight, g);
                int x = (int) ((int) ((piece.getCenter().x - (int) r.getWidth() / 2)) - viewWindow.getX());
                int y = (int) ((viewWindow.getHeight() - piece.getCenter().y - (int) r.getHeight() / 2) + fm.getAscent() + viewWindow.getY());
                g.drawString(weight, x, y);
            }
        }
    }

    private void updateViewWindow() {

        Point2D.Float pCenter = universe.getPlayer().getCenter();
        float pRadius = universe.getPlayer().getRadius();
        Dimension uSize = universe.getBounds();

        int h = (int) Math.min(uSize.height, Math.max(this.getHeight(), 4 * pRadius));
        int w = h * 4 / 3;
        int x = (int) Math.min(uSize.width - w, Math.max(0, pCenter.x - w / 2));
        int y = (int) Math.min(uSize.height - h, Math.max(0, pCenter.y - h / 2));

        viewWindow.setBounds(x, y, w, h);
    }

    private void initScreen(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(1, -1);
        g.translate(0, -this.getSize().height);
        g.translate(-viewWindow.x, -viewWindow.y);
        clearScreen(g, Color.WHITE);
        double scaleFactor = this.getHeight() / viewWindow.getHeight();
        g.scale(scaleFactor, scaleFactor);
    }

    private void clearScreen(Graphics2D g, Color color) {
        g.setColor(color);
        g.fill(viewWindow);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'm':
                universe.getPlayer().eat(20);
                break;
            case 'a':
                universe.generateRandomFood(1);
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.eventMousePosition.setLocation(e.getX(), this.getHeight() - e.getY());
    }
}
