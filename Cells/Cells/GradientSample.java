//GRADIENTSAMPLE.JAVA

package Cells;

/**
 * Result of sampling a gradient field at a point.
 */
public class GradientSample {
    public final double strength;
    public final double directionX;
    public final double directionY;
    
    public GradientSample(double strength, double directionX, double directionY) {
        this.strength = strength;
        this.directionX = directionX;
        this.directionY = directionY;
    }
    
    public Vector2D getDirection() {
        return new Vector2D(directionX, directionY);
    }
    
    @Override
    public String toString() {
        return String.format("GradientSample[strength=%.2f, dir=(%.2f, %.2f)]", 
                           strength, directionX, directionY);
    }
}