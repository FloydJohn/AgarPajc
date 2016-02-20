package it.unibs.pajc.agar.universe;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public abstract class CircleObject extends GameObject {

    private static final int ANIMATION_SPEED = 1;
    private int animationMass = -1;

    public CircleObject(Point2D.Float position, int mass, Color color, Universe universe) {
        super(position, mass, null, color, universe);
        this.animationMass = mass;
        generateShape();
    }

    protected void generateShape() {
        if (animationMass > 0) generateShape(animationMass);
        else generateShape(mass);
    }

    protected void generateShape(int radius) {
        super.setShape(new Ellipse2D.Float(-radius / 2, -radius / 2, radius, radius));
    }

    @Override
    public void setTarget(Point2D.Float target) {
        super.setTarget(target);
        if (this.target == null) return;

        this.target.setLocation(
                this.target.getX(),
                this.target.getY()
        );
    }

    public IntersectionType intersects(CircleObject otherObject) {
        if (getPosition().distance(otherObject.getPosition()) < getRadius()) {
            if (this.getMass() > otherObject.getMass()) return IntersectionType.THIS_EATS;
            else return IntersectionType.OTHER_EATS;
        }
        return IntersectionType.NO_INTERSECTION;
    }

    public float getRadius() {
        return (float) (getShape(false).getBounds().getWidth() / 2);
    }

    public void updateMass() {
        if (animationMass != mass) {
            if (animationMass > mass) animationMass = Math.max(mass, animationMass - ANIMATION_SPEED);
            else animationMass = Math.min(mass, animationMass + ANIMATION_SPEED);
            generateShape();
        }
    }
}
