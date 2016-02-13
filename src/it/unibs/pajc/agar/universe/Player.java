package it.unibs.pajc.agar.universe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Player {

    public static final Color[] possibleColors = new Color[]{Color.CYAN, Color.BLUE, Color.BLACK, Color.RED, Color.GREEN};
    private ArrayList<Piece> pieces = new ArrayList<>();
    private int color;
    private Universe universe;
    private String name;
    private GameObject.State currentState = GameObject.State.ADDED;

    public Player(String name, boolean isReal, Point2D.Float position, int mass, int color, Universe universe) {
        this.color = color;
        this.name = name;

        this.universe = universe;
        pieces.add(new Piece(null, isReal, position, mass, getColor(), universe));
    }

    public Player(Universe universe, JSONObject playerJson) {
        this.universe = universe;
        fromJSON(playerJson);
    }

    public void setTarget(Point2D.Float target) {
        pieces.get(0).setTarget(target);
    }

    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    public Color getColor() {
        return possibleColors[color];
    }

    public void update() {
        pieces.forEach(Piece::update);
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

    public JSONObject toJSON() {
        JSONObject out = new JSONObject();
        out.put("n", name);
        out.put("c", color);
        JSONArray piecesJson = new JSONArray();
        for (Piece p : pieces) piecesJson.put(p.toJSON());
        out.put("i", piecesJson);
        return out;
    }

    public void fromJSON(JSONObject in) throws IllegalArgumentException {
        try {
            name = in.getString("n");
            color = in.getInt("c");
            JSONArray piecesJson = in.getJSONArray("i");
            for (int i = 0; i < piecesJson.length(); i++) {
                if (pieces.size() > i) {
                    pieces.get(i).fromJSON((JSONObject) piecesJson.get(i));
                } else {
                    Piece newPiece = new Piece(null, false, new Point2D.Float(), 0, getColor(), universe);
                    newPiece.fromJSON((JSONObject) piecesJson.get(i));
                    pieces.add(newPiece);
                }
            }
            for (int i = piecesJson.length(); i < pieces.size(); i++) pieces.remove(i);
            if (in.has("e")) universe.eatFoods(in.getJSONArray("e"));
        } catch (JSONException | ClassCastException e) {
            throw new IllegalArgumentException("Could not parse Player JSON!", e);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isInside(Point2D.Float point) {
        for (Piece p : pieces)
            if (p.getShape(true).contains(point)) return true;
        return false;
    }

    public void eat(int mass) {
        pieces.get(0).setMass(pieces.get(0).getMass() + mass);
    }

    public Point2D.Float getCenter() {
        return pieces.get(0).getCenter();
    }

    public float getRadius() {
        return pieces.get(0).getRadius();
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
