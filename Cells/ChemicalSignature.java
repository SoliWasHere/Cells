//CHEMICALSPACE.JAVA - Central 8D chemical space representation

package Cells;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents an 8-dimensional chemical signature.
 * Used for both food types and cell preferences.
 */
public class ChemicalSignature {
    public static final int DIMENSIONS = 8;
    public static final double MAX_DISTANCE = Math.sqrt(DIMENSIONS); // ~2.828
    
    private final double[] components;
    
    public ChemicalSignature() {
        this.components = new double[DIMENSIONS];
    }
    
    public ChemicalSignature(double[] components) {
        if (components.length != DIMENSIONS) {
            throw new IllegalArgumentException("Must have " + DIMENSIONS + " components");
        }
        this.components = components.clone();
        clamp();
    }
    
    public static ChemicalSignature random() {
        double[] comps = new double[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            comps[i] = Math.random();
        }
        return new ChemicalSignature(comps);
    }
    
    public static ChemicalSignature zeros() {
        return new ChemicalSignature(new double[DIMENSIONS]);
    }
    
    /**
     * Calculate Euclidean distance to another signature.
     */
    public double distanceTo(ChemicalSignature other) {
        double sumSquares = 0;
        for (int i = 0; i < DIMENSIONS; i++) {
            double diff = this.components[i] - other.components[i];
            sumSquares += diff * diff;
        }
        return Math.sqrt(sumSquares);
    }
    
    /**
     * Calculate compatibility based on distance.
     * Returns value in [0, 1] where 1 = perfect match, 0 = incompatible.
     */
    public double compatibilityWith(ChemicalSignature other) {
        double distance = distanceTo(other);
        double normalized = distance / MAX_DISTANCE;
        return Math.max(0, 1.0 - normalized);
    }
    
    /**
     * Mutate this signature.
     */
    public ChemicalSignature mutate(double rate) {
        double[] newComps = components.clone();
        for (int i = 0; i < DIMENSIONS; i++) {
            double mutation = MathFunctions.evolve(rate) * 0.2;
            newComps[i] += mutation;
        }
        return new ChemicalSignature(newComps);
    }
    
    /**
     * Linear interpolation between this and another signature.
     */
    public ChemicalSignature lerp(ChemicalSignature other, double t) {
        double[] newComps = new double[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            newComps[i] = this.components[i] * (1 - t) + other.components[i] * t;
        }
        return new ChemicalSignature(newComps);
    }
    
    /**
     * Calculate "opposite" signature in chemical space.
     */
    public ChemicalSignature opposite() {
        double[] newComps = new double[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            newComps[i] = 1.0 - this.components[i];
        }
        return new ChemicalSignature(newComps);
    }
    
    /**
     * Add two signatures (for accumulation, like waste).
     */
    public ChemicalSignature add(ChemicalSignature other) {
        double[] newComps = new double[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            newComps[i] = this.components[i] + other.components[i];
        }
        return new ChemicalSignature(newComps);
    }
    
    /**
     * Scale signature by scalar.
     */
    public ChemicalSignature scale(double scalar) {
        double[] newComps = new double[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            newComps[i] = this.components[i] * scalar;
        }
        return new ChemicalSignature(newComps);
    }
    
    /**
     * Get magnitude (length) of this vector.
     */
    public double magnitude() {
        double sumSquares = 0;
        for (int i = 0; i < DIMENSIONS; i++) {
            sumSquares += components[i] * components[i];
        }
        return Math.sqrt(sumSquares);
    }
    
    /**
     * Normalize to unit vector.
     */
    public ChemicalSignature normalize() {
        double mag = magnitude();
        if (mag < 0.0001) return ChemicalSignature.zeros();
        return scale(1.0 / mag);
    }
    
    private void clamp() {
        for (int i = 0; i < DIMENSIONS; i++) {
            components[i] = Math.max(0, Math.min(1, components[i]));
        }
    }
    
    public double get(int index) {
        return components[index];
    }
    
    public void set(int index, double value) {
        components[index] = Math.max(0, Math.min(1, value));
    }
    
    public double[] toArray() {
        return components.clone();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < DIMENSIONS; i++) {
            sb.append(String.format("%.2f", components[i]));
            if (i < DIMENSIONS - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}