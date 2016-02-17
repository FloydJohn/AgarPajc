package it.unibs.pajc.agar.universe;

import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;

public class Food extends CircleObject{

    public static final Color[] possibleColors = new Color[]{Color.CYAN, Color.BLUE, Color.RED, Color.GREEN, Color.PINK, Color.ORANGE};
    private static final int FOOD_SIZE = 10;
    private int id;

    public Food(Universe universe, Point2D.Float pos, int id) {
        super(pos, FOOD_SIZE, possibleColors[new Random().nextInt(possibleColors.length)], universe);
        this.id = id;
        this.mass = 1;
    }

    public Food(Universe universe, JSONObject json) {
        this(universe, new Point2D.Float(), -1);
        super.fromJSON(json);
        this.id = json.getInt("id");
        generateShape(FOOD_SIZE);
    }

    public int getId() {
        return id;
    }

    @Override
    protected void prepareUpdate() {

    }

    @Override
    public JSONObject toJSON() {
        return super.toJSON().put("id", id);
    }

}
