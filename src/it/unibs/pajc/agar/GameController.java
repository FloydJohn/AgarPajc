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
    private int dotsTimer = 0;

    private GameController() {
        super();
        StartDialog dialog = new StartDialog();
        //noinspection PointlessBooleanExpression, ConstantConditions
        if (!debugging) dialog.setVisible(true);
        else dialog.closeDialog(true);
        this.setMinimumSize(new Dimension(800,600));
        new Timer(20, e -> this.repaint()).start();
        mouse = new Point2D.Float(0,0);
        universe = new Universe(dialog.getPlayerName(), new Dimension(5000, 3000), dialog.isServer());
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
        setSize(800, 600);
        if (!universe.getPlayer().isAlive()) deadLoop(g);

        else switch (NetworkController.getInstance().getCurrentState()) {
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
        oldTransform = g.getTransform();
        updateViewWindow();
        initScreen(g);
        clearScreen(g, Color.BLACK);
        g.setTransform(oldTransform);
        g.setFont(loginFont);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        if (++dotsTimer >= 200) dotsTimer = 0;
        String message = CONNECTING.substring(0, CONNECTING.length() - 3 + dotsTimer / 50);
        Rectangle2D r = fm.getStringBounds(CONNECTING, g);
        int x = (int) ((viewWindow.getWidth() - (int) r.getWidth()) / 2);
        int y = (int) ((viewWindow.getHeight() - (int) r.getHeight()) / 2);
        g.drawString(message, x, y);
    }

    private void gameLoop(Graphics2D g) {
        oldTransform = g.getTransform();
        updateViewWindow();
        initScreen(g);
        newTransform = g.getTransform();
        mouse.setLocation(eventMousePosition.x * viewWindow.getWidth() / this.getWidth() + viewWindow.getX(),
                eventMousePosition.y * viewWindow.getHeight() / this.getHeight() + viewWindow.getY());

        g.setColor(Color.RED);
        g.drawOval((int) mouse.x - 5, (int) mouse.y - 5, 10, 10);

        if ((mouse.getX() != 0 || mouse.getY() != 0) && !universe.getPlayer().isInside(mouse)) {
            universe.getPlayer().setTarget(mouse);
        } else universe.getPlayer().setTarget(null);

        universe.update();

        for (Food f : universe.getFoods().values()) {
            if (!f.isInside(viewWindow) || f.getCurrentState().equals(GameObject.State.REMOVING)) continue;
            g.setColor(f.getColor());
            g.fill(f.getShape(true));
        }

        g.setFont(massFont);
        for (Player p : universe.getPlayers().values()) {
            if (!p.isAlive()) continue;
            g.setColor(p.getColor());
            for (Player.Piece piece : p.getPieces()) {
                if (!piece.isInside(viewWindow)) continue;
                g.setTransform(newTransform);
                g.fill(piece.getShape(true));
                g.setTransform(oldTransform);
                g.setColor(Color.BLACK);
                g.drawString(String.format("x=%d   y=%d   m=%d   r=%d", (int) piece.getPosition().x, (int) piece.getPosition().y, piece.getMass(), (int) p.getRadius()), 0, 12);
            }
        }
    }

    private void deadLoop(Graphics2D g) {

    }

    private void updateViewWindow() {

        Point2D.Float pPos = universe.getPlayer().getPosition();
        float pRadius = universe.getPlayer().getRadius();
        Dimension uSize = universe.getBounds();

        int h = (int) Math.min(uSize.height, Math.max(this.getHeight(), 4 * pRadius));
        int w = h * 4 / 3;
        int x = (int) Math.min(uSize.width - w, Math.max(0, pPos.getX() - w / 2));
        int y = (int) Math.min(uSize.height - h, Math.max(0, pPos.getY() - h / 2));

        viewWindow.setBounds(x, y, w, h);
    }

    private void initScreen(Graphics2D g) {
        g.scale(1, -1);
        g.translate(0, -this.getSize().height);
        g.scale(this.getWidth() / viewWindow.getWidth(), this.getHeight() / viewWindow.getHeight());
        g.translate(-viewWindow.x, -viewWindow.y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        clearScreen(g, Color.WHITE);
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
            case 'r':
                if (!universe.getPlayer().isAlive()) universe.restartGame();
                break;
            case 'q':
                if (!universe.getPlayer().isAlive()) System.exit(0);
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
