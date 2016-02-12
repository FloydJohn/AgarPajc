package it.unibs.pajc.agar.universe;

import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.Point2D;

public class Food extends CircleObject{

    private int id;

    public Food(Universe universe, Point2D.Float pos, int id) {
        super(pos, 10, Color.GREEN, universe);
        this.id = id;
        this.mass = 1;
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
