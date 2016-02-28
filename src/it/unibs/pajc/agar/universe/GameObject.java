package it.unibs.pajc.agar.universe;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public abstract class GameObject {

    final Universe universe;
    private final Point2D.Float position;
    private final Color color;
    private final AffineTransform shapeTransform = new AffineTransform();
    int mass;
    Point2D.Float target;
    private Shape shape;
    private float speed = 0f;
    private State currentState = State.ADDED;

    GameObject(Point2D.Float position, int mass, Shape shape, Color color, Universe universe) {
        this.shape = shape;
        setMass(mass);
        this.position = new Point2D.Float();
        this.setPosition(position.x, position.y);
        this.color = color;
        this.universe = universe;
        this.target = (Point2D.Float) this.position.clone();
    }

    Point2D.Float getPosition() {
        return position;
    }

    private void setPosition(float x, float y) {
        this.position.x = x;
        this.position.y = y;
    }

    public Shape getShape(boolean translated) {
        if (translated) {
            shapeTransform.setToTranslation(getPosition().getX(), getPosition().getY());
            return shapeTransform.createTransformedShape(shape);
        } else return shape;
    }

    void setShape(Shape shape) {
        this.shape = shape;
    }

    public Color getColor() {
        return color;
    }

    private void setSpeed(float speed) {
        this.speed = speed;
    }

    void setTarget(Point2D.Float target) {

        if (target == null) {
            this.target = null;
        } else {
            if (this.target == null) this.target = new Point2D.Float();
            this.target.x = target.x;
            this.target.y = target.y;
        }
    }

    public void update() {
        if (target == null || speed == 0f) return;
        moveToTarget();
    }

    private void moveToTarget() {

        if (target.distance(position) < speed) {
            position.setLocation(target);
            target = null;
            return;
        }

        float direction = (float) Math.atan2(target.y - position.y, target.x - position.x);
        move(
                position.getX() + speed * Math.cos(direction),
                position.getY() + speed * Math.sin(direction)
        );
    }

    private void move(double x, double y) {
        setPosition(
                (float) Math.max(0, Math.min(x, universe.getBounds().width)),
                (float) Math.max(0, Math.min(y, universe.getBounds().height))
        );
    }

    public boolean isOutside(Rectangle viewWindow) {
        return !getShape(true).intersects(viewWindow);
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public int getMass() {
        return mass;
    }

    void setMass(int mass) {
        this.mass = mass;
        setSpeed(Math.max(0.1f, (float) (4 - 0.001 * (float) mass)));
    }

    public void eat(GameObject object) {
        setMass(getMass() + object.getMass());
    }

    JSONObject toJSON() {
        JSONObject out = new JSONObject();
        out.put("x", (int) position.getX());
        out.put("y", (int) position.getY());
        out.put("m", mass);
        return out;
    }

    void fromJSON(JSONObject in) throws IllegalArgumentException {
        try {
            setPosition(in.getInt("x"), in.getInt("y"));
            setMass(in.getInt("m"));
        } catch (JSONException e) {
            throw new IllegalArgumentException("Couldn't parse GameObject", e);
        }
    }


    public enum State {
        ADDED, REMOVING
    }
}
