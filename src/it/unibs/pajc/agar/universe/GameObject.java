package it.unibs.pajc.agar.universe;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public abstract class GameObject {

    protected Point2D.Float position;
    protected int mass;
    protected Shape shape;
    protected Color color;
    protected float speed = 0f;
    protected Point2D.Float target;
    protected Universe universe;

    private AffineTransform shapeTransform = new AffineTransform();

    public Point2D.Float getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        this.position.x = x;
        this.position.y = y;
    }

    public void setMass(int mass) {
        this.mass = mass;
        setSpeed((float) (4 - 0.001* (float) mass));
    }

    public Shape getShape(boolean translated) {
        if (translated) {
            shapeTransform.setToTranslation(getPosition().getX(), getPosition().getY());
            return shapeTransform.createTransformedShape(shape);
        } else return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public Color getColor() {
        return color;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setTarget(Point2D.Float target) {

        if (target == null) {
            this.target = null;
        } else {
            if (this.target == null) this.target = new Point2D.Float();
            this.target.x = target.x;
            this.target.y = target.y;
        }
    }

    public GameObject(Point2D.Float position, int mass, Shape shape, Color color, Universe universe) {
        this.shape = shape;
        setMass(mass);
        this.position = new Point2D.Float();
        setPosition(position.x, position.y);
        this.color = color;
        this.universe = universe;
        this.target = (Point2D.Float) this.position.clone();
    }

    protected abstract void prepareUpdate();

    public void update() {
        prepareUpdate();
        if (target == null || speed == 0f) return;
        moveToTarget();
    }

    private void moveToTarget() {
        //System.out.println("Moving to target "+target+" from position "+position);

        if (target.distance(position) < speed) {
            position.setLocation(target);
            target = null;
            //System.out.println("DESTINATION REACHED!!!");
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
                (float)Math.max(0, Math.min(x, universe.getBounds().width)),
                (float)Math.max(0, Math.min(y, universe.getBounds().height))
        );//System.out.println("Moving! New pos = "+position);
    }

    public boolean isInside(Point2D.Float bottomLeftVertex, Dimension size) {

        return  position.getX() > bottomLeftVertex.getX() + shape.getBounds2D().getWidth() &&
                position.getX() < bottomLeftVertex.getX() + size.getWidth() &&
                position.getY() > bottomLeftVertex.getY() + shape.getBounds2D().getHeight() &&
                position.getY() < bottomLeftVertex.getY() + size.getHeight();
    }

    public int getMass() {
        return mass;
    }

    public void eat(GameObject object) {
        setMass(getMass() + object.getMass());
    }
}
