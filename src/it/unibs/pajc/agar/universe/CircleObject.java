package it.unibs.pajc.agar.universe;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public abstract class CircleObject extends GameObject {

    private Point2D.Float center;

    public Point2D.Float getCenter() {
        return center;
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        if (center == null) center = new Point2D.Float();
        center.x = (float) (getPosition().x + getRadius());
        center.y = (float) (getPosition().y + getShape(false).getBounds().getHeight()/2);
    }

    public CircleObject(Point2D.Float position, int mass, Color color, Universe universe) {
        super(position, mass, null, color, universe);
        generateShape();
    }

    @Override
    public void setMass(int mass) {
        super.setMass(mass);
        generateShape();
    }

    protected void generateShape() {
        super.setShape(new Ellipse2D.Float(0, 0, mass, mass));
    }

    @Override
    public void setTarget(Point2D.Float target) {
        super.setTarget(target);
        if (this.target == null) return;

        this.target.setLocation(
                this.target.getX() - shape.getBounds().getWidth()/2,
                this.target.getY() - shape.getBounds().getHeight()/2
        );
    }

    public boolean intersects(CircleObject otherObject) {
        return getCenter().distance(otherObject.getCenter()) < getRadius() + otherObject.getRadius();
    }

    private double getRadius() {
        return getShape(false).getBounds().getWidth()/2;
    }
}
