//GRADIENTSOURCE.JAVA

package Cells;

/**
 * Represents a point source in a gradient field.
 */
public class GradientSource {
    public double x;
    public double y;
    public double strength;
    public PhysicsObj entity; // Reference to the entity creating this source
    
    public GradientSource(double x, double y, double strength, PhysicsObj entity) {
        this.x = x;
        this.y = y;
        this.strength = strength;
        this.entity = entity;
    }
    
    public void updatePosition(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }
}