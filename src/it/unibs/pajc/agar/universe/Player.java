package it.unibs.pajc.agar.universe;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class Player {

    private ArrayList<Piece> pieces = new ArrayList<>();
    private final boolean isReal;
    private Color color;
    private Universe universe;
    private String name;

    public Player(String name, boolean isReal, Point2D.Float position, int mass, Color color, Universe universe) {
        this.isReal = isReal;
        this.color = color;
        this.name = name;
        this.universe = universe;
        pieces.add(new Piece(null, isReal, position, mass, color, universe));
    }

    public void setTarget(Point2D.Float target) {
        pieces.get(0).setTarget(target);
    }

    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    public Color getColor() {
        return color;
    }

    public void update() {
        pieces.forEach(Piece::update);
    }

    public Point2D.Float getPosition() {
        return pieces.get(0).getPosition();
    }

    public Rectangle getDimension() {
        return pieces.get(0).getShape(false).getBounds();
    }

    public boolean intersects(CircleObject circleObject) {
        for (Piece p : pieces) {
            if (p.intersects(circleObject))
                return true;
        }
        return false;
    }

    public void eat(Food food) {
        pieces.stream().filter(p -> p.intersects(food)).forEach(p -> p.eat(food));
    }

    public class Piece extends CircleObject{

        private Piece parent;
        private boolean isReal;

        public Piece(Piece parent, boolean isReal, Point2D.Float position, int mass, Color color, Universe universe) {
            super(position, mass, color, universe);
            this.parent = parent;
            this.isReal = isReal;
        }

        @Override
        protected void prepareUpdate() {
            if (!isReal || parent == null) return;
            super.setSpeed(parent.getSpeed()/2);
            super.setTarget(parent.getPosition());
        }
    }
}
