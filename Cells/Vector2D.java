package Cells;

/**
 * Simple 2D vector for physics calculations.
 */
public class Vector2D {
    public final double x;
    public final double y;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Get the magnitude (length) of this vector.
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
    
    /**
     * Get the squared magnitude (faster, no sqrt).
     */
    public double magnitudeSquared() {
        return x * x + y * y;
    }
    
    /**
     * Return a normalized version of this vector (length = 1).
     */
    public Vector2D normalize() {
        double mag = magnitude();
        if (mag < 0.0001) return new Vector2D(0, 0);
        return new Vector2D(x / mag, y / mag);
    }
    
    /**
     * Scale this vector by a scalar.
     */
    public Vector2D scale(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }
    
    /**
     * Add another vector to this one.
     */
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }
    
    /**
     * Subtract another vector from this one.
     */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }
    
    /**
     * Calculate dot product with another vector.
     */
    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }
    
    /**
     * Get the angle of this vector in radians.
     */
    public double angle() {
        return Math.atan2(y, x);
    }
    
    /**
     * Create a vector from angle and magnitude.
     */
    public static Vector2D fromPolar(double angle, double magnitude) {
        return new Vector2D(
            Math.cos(angle) * magnitude,
            Math.sin(angle) * magnitude
        );
    }
    
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}