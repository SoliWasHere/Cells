//RECEPTORPROFILE.JAVA (FIXED - 7 FOOD CHANNELS)

package Cells;

/**
 * Represents a cell's receptor sensitivity profile across food signal channels.
 * Note: This only covers FOOD channels (7 total), not the cell repulsion channel.
 */
public class ReceptorProfile {
    // Only food channels (channels 1-7 from MultiChannelGradientField)
    private static final int NUM_FOOD_CHANNELS = MultiChannelGradientField.NUM_CHANNELS - 1;
    private final double[] sensitivities;
    
    public ReceptorProfile() {
        this.sensitivities = new double[NUM_FOOD_CHANNELS];
        
        // Initialize with random sensitivities
        for (int i = 0; i < sensitivities.length; i++) {
            sensitivities[i] = Math.random() * 0.5 + 0.25; // Start 0.25-0.75
        }
    }
    
    /**
     * Copy constructor for reproduction.
     */
    public ReceptorProfile(ReceptorProfile parent) {
        this.sensitivities = parent.sensitivities.clone();
    }
    
    /**
     * Calculate total energy cost of maintaining this receptor profile.
     * Cost scales with sum of squared sensitivities (quadratic cost).
     */
    public double getEnergyCost() {
        double sumSquared = 0;
        for (double sensitivity : sensitivities) {
            sumSquared += sensitivity * sensitivity;
        }
        return sumSquared * 0.05; // Base cost multiplier
    }
    
    /**
     * Calculate weighted movement direction based on food gradient samples.
     * Expects samples from channels 1-7 (food channels only).
     */
    public Vector2D calculateMovementDirection(GradientSample[] foodSamples) {
        if (foodSamples.length != NUM_FOOD_CHANNELS) {
            throw new IllegalArgumentException(
                "Expected " + NUM_FOOD_CHANNELS + " food samples, got " + foodSamples.length
            );
        }
        
        double totalDirX = 0;
        double totalDirY = 0;
        double totalWeight = 0;
        
        for (int i = 0; i < sensitivities.length; i++) {
            double weight = sensitivities[i] * foodSamples[i].strength;
            totalDirX += foodSamples[i].directionX * weight;
            totalDirY += foodSamples[i].directionY * weight;
            totalWeight += weight;
        }
        
        if (totalWeight > 0.001) {
            return new Vector2D(totalDirX / totalWeight, totalDirY / totalWeight);
        }
        
        return new Vector2D(0, 0);
    }
    
    /**
     * Mutate receptor sensitivities during reproduction.
     */
    public void mutate(double evolveRate) {
        for (int i = 0; i < sensitivities.length; i++) {
            double mutation = MathFunctions.evolve(evolveRate) * 0.15;
            sensitivities[i] += mutation;
            sensitivities[i] = Math.max(0.0, Math.min(1.0, sensitivities[i]));
        }
    }
    
    /**
     * Get sensitivity to a specific food channel (0-6 maps to channels 1-7).
     */
    public double getSensitivity(int foodChannelIndex) {
        return sensitivities[foodChannelIndex];
    }
    
    /**
     * Set sensitivity to a specific food channel (0-6 maps to channels 1-7).
     */
    public void setSensitivity(int foodChannelIndex, double value) {
        sensitivities[foodChannelIndex] = Math.max(0.0, Math.min(1.0, value));
    }
    
    /**
     * Get number of food channels.
     */
    public int getNumChannels() {
        return NUM_FOOD_CHANNELS;
    }
    
    /**
     * Get total receptor activity (sum of all sensitivities).
     */
    public double getTotalSensitivity() {
        double total = 0;
        for (double sensitivity : sensitivities) {
            total += sensitivity;
        }
        return total;
    }
    
    /**
     * Get specialization index (0 = generalist, 1 = specialist).
     * Based on variance of sensitivities.
     */
    public double getSpecializationIndex() {
        double mean = getTotalSensitivity() / sensitivities.length;
        double variance = 0;
        
        for (double sensitivity : sensitivities) {
            double diff = sensitivity - mean;
            variance += diff * diff;
        }
        
        variance /= sensitivities.length;
        
        // Normalize: max variance occurs when all sensitivities are 0 or 1
        double maxVariance = 0.25; // Theoretical max for [0,1] range
        return Math.min(1.0, Math.sqrt(variance / maxVariance));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Receptors[");
        for (int i = 0; i < sensitivities.length; i++) {
            sb.append(String.format("%.2f", sensitivities[i]));
            if (i < sensitivities.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}