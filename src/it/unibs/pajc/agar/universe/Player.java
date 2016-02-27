package it.unibs.pajc.agar.universe;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.Point2D;

public class Player extends CircleObject {

    public static final Color[] possibleColors = new Color[]{Color.CYAN, Color.BLUE, Color.BLACK, Color.RED, Color.GREEN};
    private int color;
    private String name;
    private boolean updateLocked = false;

    public Player(String name, Point2D.Float position, int mass, int color, Universe universe) {
        super(position, mass, possibleColors[color], universe);
        this.name = name;
    }

    public Player(Universe universe, JSONObject playerJson) {
        super(new Point2D.Float(), playerJson.getInt("m"), Color.RED, universe);
        fromJSON(playerJson);
    }

    @Override
    public Color getColor() {
        return possibleColors[color];
    }

    @Override
    public JSONObject toJSON() {
        JSONObject out = super.toJSON();
        out.put("n", name);
        out.put("c", color);
        return out;
    }

    @Override
    public void fromJSON(JSONObject in) throws IllegalArgumentException {
        try {
            if (updateLocked) {
                if (in.getInt("m") < 0) {
                    updateLocked = false;
                    System.out.println("[Player#fromJSON] Unlocked player " + name);
                } else return;
            }
            name = in.getString("n");
            color = in.getInt("c");
            super.fromJSON(in);
            if (in.has("e")) universe.eatFoods(in.getJSONArray("e"));
        } catch (JSONException | ClassCastException e) {
            throw new IllegalArgumentException("Could not parse Player JSON!", e);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isInside(Point2D.Float point) {
        return getShape(true).contains(point);
    }

    public void eat(int mass) {
        setMass(getMass() + mass);
    }

    public float getRadius() {
        if (!isAlive()) return 0;
        return super.getRadius();
    }

    public Point2D.Float getPosition() {
        if (!isAlive()) return new Point2D.Float();
        return super.getPosition();
    }

    public boolean isAlive() {
        return mass > 0;
    }

    public void die() {
        this.mass = -1;
        System.out.println("[Player#die] Updates locked for " + name);
        this.updateLocked = true;
    }
}
