package it.unibs.pajc.agar.universe;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public abstract class CircleObject extends GameObject {

    private Point2D.Float center;

    public CircleObject(Point2D.Float position, int mass, Color color, Universe universe) {
        super(position, mass, null, color, universe);
        generateShape();
    }

    public Point2D.Float getCenter() {
        return center;
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        updateCenter();
    }

    private void updateCenter() {
        if (position == null) return;
        if (center == null) center = new Point2D.Float();
        center.x = getPosition().x + getRadius();
        center.y = getPosition().y + getRadius();
    }

    @Override
    public void setMass(int mass) {
        super.setMass(mass);
        generateShape();
        updateCenter();
    }

    protected void generateShape() {
        generateShape(mass);
    }

    protected void generateShape(int radius) {
        super.setShape(new Ellipse2D.Float(0, 0, radius, radius));
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

    public IntersectionType intersects(CircleObject otherObject) {
        if (getCenter().distance(otherObject.getCenter()) < (getRadius() + otherObject.getRadius()) / 2) {
            if (this.getMass() > otherObject.getMass()) return IntersectionType.THIS_EATS;
            else return IntersectionType.OTHER_EATS;
        }
        return IntersectionType.NO_INTERSECTION;
    }

    public float getRadius() {
        return (float) (getShape(false).getBounds().getWidth() / 2);
    }
}
