//FOOD.JAVA (WITH MULTI-CHANNEL EMISSION AND WASTE DAMAGE)

package Cells;

import java.awt.Color;
import java.util.List;

/**
 * Food particle that emits multi-channel signals.
 * Waste particles damage nearby cells.
 */
public class Food extends PhysicsObj {
    private static final double DEFAULT_NUTRITIONAL_VALUE = 50.0;
    
    private double nutritionalValue;
    private GradientSource[] channelSources;
    
    private double foodIdX;
    private double foodIdY;
    
    private double lastX;
    private double lastY;
    
    private boolean isWaste = false;
    private static final double WASTE_DAMAGE_RADIUS = 100.0;
    private static final double WASTE_DAMAGE_PER_FRAME = 0.5;
    
    public Food(double x, double y) {
        super(x, y);
        this.nutritionalValue = DEFAULT_NUTRITIONAL_VALUE;
        
        this.foodIdX = Math.random();
        this.foodIdY = Math.random();
        
        updateColorFromFoodId();
        setSize(3);

        this.lastX = x;
        this.lastY = y;

        
        this.channelSources = new GradientSource[MultiChannelGradientField.NUM_CHANNELS];
    }
    
    public Food(double x, double y, double nutritionalValue) {
        this(x, y);
        this.nutritionalValue = nutritionalValue;

        this.lastX = x;
        this.lastY = y;

    }
    
    public Food(double x, double y, double nutritionalValue, double foodIdX, double foodIdY) {
        this(x, y, nutritionalValue);
        this.foodIdX = Math.max(0, Math.min(1, foodIdX));
        this.foodIdY = Math.max(0, Math.min(1, foodIdY));
        this.lastX = x;
        this.lastY = y;


        updateColorFromFoodId();
    }
    
    private void updateColorFromFoodId() {
        if (isWaste) {
            // Waste is dark brownish
            setColor(new Color(80, 60, 40));
        } else {
            int green = (int)(100 + foodIdX * 155);
            int yellow = (int)(100 + foodIdY * 155);
            int red = (int)(foodIdY * 200);
            setColor(new Color(red, green, Math.max(50, 255 - yellow)));
        }
    }
    
    @Override
    protected void onAddedToWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        MultiChannelGradientField multiField = world.getMultiChannelField();
        
        // Get emission pattern for this food (7 values for channels 1-7)
        double[] channelStrengths = multiField.foodIdToChannelStrengths(foodIdX, foodIdY);
        
        // Add sources to food channels (1-7)
        // channelStrengths[0] maps to channel 1, channelStrengths[1] maps to channel 2, etc.
        for (int i = 0; i < channelStrengths.length; i++) {
            double strength = channelStrengths[i] * nutritionalValue;
            int channelIndex = i + 1; // Channels 1-7
            channelSources[channelIndex] = new GradientSource(getX(), getY(), strength, this);
            multiField.getChannel(channelIndex).addSource(channelSources[channelIndex]);
        }
    }
    
    @Override
    protected void onRemovedFromWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        MultiChannelGradientField multiField = world.getMultiChannelField();
        
        for (int i = 1; i < MultiChannelGradientField.NUM_CHANNELS; i++) {
            if (channelSources[i] != null) {
                multiField.getChannel(i).removeSource(channelSources[i]);
            }
        }
    }
    
    @Override
    protected void onUpdate() {
        // Update gradient sources if food moves
        double oldX = getX();
        double oldY = getY();
        
        SimulationWorld world = SimulationWorld.getInstance();
        
        // If this is waste, damage nearby cells
        if (isWaste) {
            damageNearbyCells();
        }
        
        // Update gradient source positions
        if (Math.abs(getX() - lastX) > 0.1 || Math.abs(getY() - lastY) > 0.1) {
            MultiChannelGradientField multiField = world.getMultiChannelField();
            
            for (int i = 1; i < MultiChannelGradientField.NUM_CHANNELS; i++) {
                if (channelSources[i] != null) {
                    channelSources[i].updatePosition(getX(), getY());
                    multiField.getChannel(i).updateSource(channelSources[i], lastX, lastY);
                }
            }
            
            lastX = getX();
            lastY = getY();
        }

    }
    
    /**
     * Waste particles damage nearby cells.
     */
    private void damageNearbyCells() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        int gridX = (int)(getX() / 50);
        int gridY = (int)(getY() / 50);
        
        double damageRadiusSq = WASTE_DAMAGE_RADIUS * WASTE_DAMAGE_RADIUS;
        
        // Check nearby grid cells
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int checkX = (gridX + dx + 200) % 200;
                int checkY = (gridY + dy + 200) % 200;
                
                List<PhysicsObj> entities = world.getEntitiesInSpatialCell(checkX, checkY);
                
                for (PhysicsObj obj : entities) {
                    if (!(obj instanceof Cell)) continue;
                    
                    Cell cell = (Cell) obj;
                    
                    Vector2D delta = world.getWrappedDelta(getX(), getY(), cell.getX(), cell.getY());
                    double distSq = delta.magnitudeSquared();
                    
                    if (distSq < damageRadiusSq) {
                        double distance = Math.sqrt(distSq);
                        double damageMultiplier = 1.0 - (distance / WASTE_DAMAGE_RADIUS);
                        double damage = WASTE_DAMAGE_PER_FRAME * damageMultiplier;
                        
                        cell.damageFromWaste(damage);
                    }
                }
            }
        }
    }
    
    // Getters
    public double getNutritionalValue() { return nutritionalValue; }
    public double getFoodIdX() { return foodIdX; }
    public double getFoodIdY() { return foodIdY; }
    public boolean isWaste() { return isWaste; }
    
    // Setters
    public void setNutritionalValue(double value) {
        this.nutritionalValue = Math.max(0, value);
        
        // Update gradient source strengths
        if (channelSources[1] != null) { // Check channel 1 instead of 0
            SimulationWorld world = SimulationWorld.getInstance();
            MultiChannelGradientField multiField = world.getMultiChannelField();
            double[] channelStrengths = multiField.foodIdToChannelStrengths(foodIdX, foodIdY);
            
            for (int i = 0; i < channelStrengths.length; i++) {
                int channelIndex = i + 1; // Channels 1-7
                if (channelSources[channelIndex] != null) {
                    channelSources[channelIndex].strength = channelStrengths[i] * nutritionalValue;
                }
            }
        }
    }
    
    public void setFoodId(double foodIdX, double foodIdY) {
        this.foodIdX = Math.max(0, Math.min(1, foodIdX));
        this.foodIdY = Math.max(0, Math.min(1, foodIdY));
        updateColorFromFoodId();
    }
    
    public void setIsWaste(boolean isWaste) {
        this.isWaste = isWaste;
        updateColorFromFoodId();
        
        if (isWaste) {
            setSize(5); // Waste is slightly larger
        }
    }
    
    @Override
    public String toString() {
        return String.format("Food[pos=(%.1f, %.1f), nutrition=%.1f, id=(%.2f, %.2f), waste=%s]",
            getX(), getY(), nutritionalValue, foodIdX, foodIdY, isWaste);
    }
}