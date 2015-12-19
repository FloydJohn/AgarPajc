package it.unibs.pajc.agar;

import it.unibs.pajc.agar.network.NetworkController;
import it.unibs.pajc.agar.universe.Food;
import it.unibs.pajc.agar.universe.Player;
import it.unibs.pajc.agar.universe.Universe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class GameController extends JPanel implements KeyListener, MouseMotionListener {

    private static final String CONNECTING = "Connecting...";

    private static GameController instance;
    public static GameController getInstance() {
        if (instance == null) instance = new GameController();
        return instance;
    }

    private GameController() {
        super();
        StartDialog dialog = new StartDialog();
        dialog.setVisible(true);
        this.setMinimumSize(new Dimension(800,600));
        new Timer(20, e -> this.repaint()).start();
        offset = new Point2D.Float(0,0);
        mouse = new Point2D.Float(0,0);

        NetworkController.getInstance().connect();
    }


    Universe universe;
    Point2D.Float offset;
    Point2D.Float mouse;

    private void initUniverse() {
        universe = new Universe("PlayerName", new Dimension(5000, 3000));
        universe.generateRandomFood(500);
    }

    @Override
    protected void paintComponent(Graphics gOrig) {
        Graphics2D g = (Graphics2D) gOrig;

        switch (NetworkController.getInstance().getCurrentState()) {
            case CONNECTED:
                paintGame(g);
                break;
            case LOGIN:
                paintLogin(g);
                break;
            case DEAD:
                break;
        }
    }

    Font loginFont = new Font("Arial", Font.BOLD, 50);
    private void paintLogin(Graphics2D g) {
        clearScreen(g, Color.BLACK);
        g.setFont(loginFont);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(CONNECTING, g);
        int x = (this.getWidth() - (int) r.getWidth()) / 2;
        int y = (this.getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
        g.drawString(CONNECTING, x, y);
    }

    private void paintGame(Graphics2D g) {
        Point2D mousePosition = MouseInfo.getPointerInfo().getLocation();
        mouse.setLocation(mousePosition.getX() + offset.x, this.getSize().getHeight() - mousePosition.getY() + offset.y);

        updateOffset();
        initScreen(g);

        if (mouse.getX() != 0 || mouse.getY() != 0) {
            universe.getPlayer().setTarget(mouse);
        }
        universe.update();

        for (Food f : universe.getFoods().values()) {
            if (!f.isInside(offset, this.getSize())) continue;
            g.setColor(f.getColor());
            g.fill(f.getShape(true));
        }

        for (Player p : universe.getPlayers().values()) {
            g.setColor(p.getColor());
            for (Player.Piece piece : p.getPieces()) {
                if (!piece.isInside(offset, this.getSize())) continue;
                g.fill(piece.getShape(true));
            }
        }
    }

    private void updateOffset() {
        Point2D.Float pPos = universe.getPlayer().getPosition();
        Dimension pSize = universe.getPlayer().getDimension().getSize();
        offset.setLocation(
                Math.min(Math.max(0, pPos.x + pSize.width - this.getWidth()/2), universe.getBounds().getWidth() - this.getWidth() + pSize.width),
                Math.min(Math.max(0, pPos.y + pSize.height - this.getHeight()/2), universe.getBounds().getHeight() - this.getHeight() + pSize.height)
        );
    }

    private void initScreen(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(1, -1);
        g.translate(0, -this.getSize().height);
        g.translate(-offset.x, -offset.y);
        clearScreen(g, Color.WHITE);
    }

    private void clearScreen(Graphics2D g, Color color) {
        g.setColor(color);
        g.fillRect((int)offset.x, (int)offset.y, this.getSize().width + (int)offset.x, (int) (this.getSize().height + offset.y));
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //mouse.setLocation(e.getPoint().getX() + offset.x, this.getSize().getHeight() - e.getPoint().getY() + offset.y);
    }
}
