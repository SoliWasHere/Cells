//RECEPTORSENSITIVITY.JAVA (NEW)

package Cells;

/**
 * Represents sensitivity/gain for each dimension of chemical space.
 * Allows cells to "focus" on certain chemical dimensions.
 */
public class ReceptorSensitivity {
    private final double[] gains;
    
    public ReceptorSensitivity() {
        this.gains = new double[ChemicalSignature.DIMENSIONS];
        // Start with equal sensitivity
        for (int i = 0; i < gains.length; i++) {
            gains[i] = 1.0;
        }
    }
    
    public ReceptorSensitivity(double[] gains) {
        this.gains = gains.clone();
    }
    
    /**
     * Apply receptor gains to a chemical signature (for perception).
     */
    public ChemicalSignature applyGains(ChemicalSignature input) {
        double[] modified = new double[ChemicalSignature.DIMENSIONS];
        for (int i = 0; i < ChemicalSignature.DIMENSIONS; i++) {
            modified[i] = input.get(i) * gains[i];
        }
        // Renormalize to [0,1]
        double max = 0;
        for (double v : modified) {
            max = Math.max(max, v);
        }
        if (max > 1.0) {
            for (int i = 0; i < modified.length; i++) {
                modified[i] /= max;
            }
        }
        return new ChemicalSignature(modified);
    }
    
    /**
     * Mutate receptor gains.
     */
    public ReceptorSensitivity mutate(double rate) {
        double[] newGains = gains.clone();
        for (int i = 0; i < gains.length; i++) {
            double mutation = MathFunctions.evolve(rate) * 0.2;
            newGains[i] = Math.max(0.1, Math.min(2.0, newGains[i] + mutation));
        }
        return new ReceptorSensitivity(newGains);
    }
    
    /**
     * Get energy cost of maintaining these receptors.
     * Higher gains cost more energy.
     */
    public double getEnergyCost() {
        double totalGain = 0;
        for (double gain : gains) {
            totalGain += gain;
        }
        return totalGain * 0.02; // Cost per unit gain
    }
    
    public double getGain(int dimension) {
        return gains[dimension];
    }
    
    public ReceptorSensitivity copy() {
        return new ReceptorSensitivity(gains);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < gains.length; i++) {
            sb.append(String.format("%.2f", gains[i]));
            if (i < gains.length - 1) sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}