package it.unibs.pajc.agar.universe;

import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.Point2D;

public class Food extends CircleObject{

    private State currentState = State.TO_ADD;
    private int id;

    public Food(Universe universe, Point2D.Float pos, int id) {
        super(pos, 10, Color.GREEN, universe);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    @Override
    public int getMass() {
        return 1;
    }

    @Override
    protected void prepareUpdate() {

    }

    @Override
    public JSONObject toJSON() {
        return super.toJSON().put("id", id);
    }

    public enum State {
        TO_ADD, TO_REMOVE, ADDED, REMOVED
    }
}
