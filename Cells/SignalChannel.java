//SIGNALCHANNEL.JAVA

package Cells;

/**
 * Represents a single gradient signal channel.
 * Mimics a specific chemical signal type (like pheromones, nutrients, etc.)
 */
public class SignalChannel {
    private final GradientField gradientField;
    private final int channelIndex;
    
    public SignalChannel(int cellSize, int gridWidth, int gridHeight, 
                        double maxInfluenceRadius, double falloffExponent, 
                        int channelIndex) {
        this.gradientField = new GradientField(cellSize, gridWidth, gridHeight, 
                                              maxInfluenceRadius, falloffExponent);
        this.channelIndex = channelIndex;
    }
    
    public void addSource(GradientSource source) {
        gradientField.addSource(source);
    }
    
    public void removeSource(GradientSource source) {
        gradientField.removeSource(source);
    }
    
    public void updateSource(GradientSource source, double oldX, double oldY) {
        gradientField.updateSource(source, oldX, oldY);
    }
    
    public GradientSample sample(double x, double y) {
        return gradientField.sample(x, y);
    }
    
    public void clear() {
        gradientField.clear();
    }
    
    public int getChannelIndex() {
        return channelIndex;
    }
    
    public GradientField getGradientField() {
        return gradientField;
    }
}