//MULTICHANNELGRADIENTFIELD.JAVA

package Cells;

/**
 * Multi-channel gradient field system.
 * Simulates multiple distinct chemical signal types that entities can emit and sense.
 */
public class MultiChannelGradientField {
    public static final int NUM_CHANNELS = 8;
    
    private final SignalChannel[] channels;
    private final int cellSize;
    private final int gridWidth;
    private final int gridHeight;
    
    public MultiChannelGradientField(int cellSize, int gridWidth, int gridHeight) {
        this.cellSize = cellSize;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.channels = new SignalChannel[NUM_CHANNELS];
        
        // Create channels with different parameters
        for (int i = 0; i < NUM_CHANNELS; i++) {
            // Vary influence radius slightly for each channel
            double influenceRadius = 250.0 + (i * 10.0);
            channels[i] = new SignalChannel(cellSize, gridWidth, gridHeight, 
                                           influenceRadius, 2.0, i);
        }
    }

    /**
     * Map a food ID (2D) to channel emission strengths (7D for food channels 1-7).
     * Channel 0 (cell repulsion) is not included.
     */
    public double[] foodIdToChannelStrengths(double foodIdX, double foodIdY) {
        double[] strengths = new double[NUM_CHANNELS - 1]; // 7 food channels
        
        for (int i = 0; i < strengths.length; i++) {
            // Channel index is i+1 (channels 1-7)
            double angle = ((i + 1) * Math.PI / 4.0); // 45° increments
            
            // Project food ID onto this channel's "sensitivity axis"
            double projection = Math.cos(angle) * foodIdX + Math.sin(angle) * foodIdY;
            
            // Add nonlinearity to create more distinct patterns
            double activation = Math.pow(Math.cos(projection * Math.PI * 2), 2);
            
            // Add phase shift based on channel index for variety
            double phaseShift = Math.sin((foodIdX + foodIdY + (i + 1) * 0.3) * Math.PI);
            activation = (activation + phaseShift + 1.0) / 2.5;
            
            strengths[i] = Math.max(0, Math.min(1, activation));
        }
        
        return strengths;
    }
    
    /**
     * Sample all channels at a point and return as array.
     */
    public GradientSample[] sampleAll(double x, double y) {
        GradientSample[] samples = new GradientSample[NUM_CHANNELS];
        for (int i = 0; i < NUM_CHANNELS; i++) {
            samples[i] = channels[i].sample(x, y);
        }
        return samples;
    }
    
    /**
     * Get a specific channel.
     */
    public SignalChannel getChannel(int index) {
        if (index < 0 || index >= NUM_CHANNELS) {
            throw new IllegalArgumentException("Channel index out of bounds: " + index);
        }
        return channels[index];
    }
    
    public void clear() {
        for (SignalChannel channel : channels) {
            channel.clear();
        }
    }
    
    public int getNumChannels() {
        return NUM_CHANNELS;
    }
}