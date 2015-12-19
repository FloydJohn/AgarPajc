package it.unibs.pajc.agar.universe;

import java.awt.*;
import java.awt.geom.Point2D;

public class Food extends CircleObject{

    public Food(Universe universe, Point2D.Float pos) {
        super(pos, 10, Color.GREEN, universe);
    }

    @Override
    public int getMass() {
        return 1;
    }

    @Override
    protected void prepareUpdate() {

    }
}
