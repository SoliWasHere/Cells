//GRADIENTSOURCE.JAVA (UPDATED - now includes emission bias)

package Cells;

/**
 * Represents a point source with 8D chemical signature.
 * Emission strength can be biased per dimension.
 */
public class GradientSource {
    public double x;
    public double y;
    public double strength;
    public PhysicsObj entity;
    public ChemicalSignature chemistry; // 8D signature
    public double[] emissionBias; // How strongly this source emits in each dimension
    
    public GradientSource(double x, double y, double strength, PhysicsObj entity, ChemicalSignature chemistry) {
        this.x = x;
        this.y = y;
        this.strength = strength;
        this.entity = entity;
        this.chemistry = chemistry;
        
        // Emission bias - sources emit more strongly in dimensions where they're high
        this.emissionBias = new double[ChemicalSignature.DIMENSIONS];
        for (int i = 0; i < ChemicalSignature.DIMENSIONS; i++) {
            // Non-linear: emphasize high values
            this.emissionBias[i] = Math.pow(chemistry.get(i), 1.5);
        }
    }
    
    /**
     * Calculate effective strength as perceived by an observer with given chemistry.
     */
    public double getPerceivedStrength(ChemicalSignature observerChemistry) {
        // Dot product between emission pattern and observer sensitivity
        double dotProduct = 0;
        for (int i = 0; i < ChemicalSignature.DIMENSIONS; i++) {
            dotProduct += emissionBias[i] * observerChemistry.get(i);
        }
        
        // Normalize by maximum possible dot product
        double maxPossible = 0;
        for (int i = 0; i < ChemicalSignature.DIMENSIONS; i++) {
            maxPossible += emissionBias[i];
        }
        
        if (maxPossible < 0.001) return 0;
        
        double compatibility = dotProduct / maxPossible;
        return strength * compatibility;
    }
    
    public void updatePosition(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }
    
    public void updateChemistry(ChemicalSignature newChemistry) {
        this.chemistry = newChemistry;
        for (int i = 0; i < ChemicalSignature.DIMENSIONS; i++) {
            this.emissionBias[i] = Math.pow(newChemistry.get(i), 1.5);
        }
    }
}