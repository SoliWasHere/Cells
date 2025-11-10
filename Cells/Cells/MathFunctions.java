//MATHFUNCTIONS.JAVA

package Cells;

/**
 * Utility math functions for the simulation.
 */
public class MathFunctions {
    
    /**
     * Generate a non-uniform random evolution factor with bias toward smaller changes.
     * Uses arc-cosine distribution for more realistic mutations.
     * 
     * @param evolveRate Controls mutation intensity (higher = more extreme mutations)
     * @return Mutation multiplier, typically between -1 and 1
     */
    public static double evolve(double evolveRate) {
        double rand = (Math.random() * 2) - 1; // [-1, 1]
        double multiplier = 1;
        
        if (rand < 0) {
            multiplier = -1;
            rand = -rand;
        }
        
        double a = Math.acos((2 * rand) - 1) / Math.PI; // [0, 1]
        double b = Math.pow(1 - a, evolveRate);
        
        return b * multiplier;
    }

    public static double realMod(double x, double i) {
        return (
            (((x % i) + i) % i)
        );
    }
    
    /**
     * Normalize a vector to unit length.
     */
    public static double[] normalizeVector(double x, double y) {
        double magnitude = Math.sqrt(x * x + y * y);
        if (magnitude < 0.0001) {
            return new double[]{0, 0};
        }
        return new double[]{x / magnitude, y / magnitude};
    }
    
    /**
     * Clamp a value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}