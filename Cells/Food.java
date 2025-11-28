//FOOD.JAVA (DISCRETE TYPES)

package Cells;

import java.awt.Color;

/**
 * Food with discrete color-based types.
 * Type 0 = Red, Type 1 = Green, Type 2 = Blue, Type 3 = Gray (dead matter)
 */
public class Food extends PhysicsObj {
    private double nutritionalValue;
    private ChemicalSignature chemistry;
    private GradientSource gradientSource;
    
    private double lastX;
    private double lastY;
    
    private int foodType = 0; // 0=red, 1=green, 2=blue, 3=gray
    
    public Food(double x, double y, ChemicalSignature chemistry, double nutritionalValue) {
        super(x, y);
        this.chemistry = chemistry;
        this.nutritionalValue = nutritionalValue;
        
        setSize(4);
        this.lastX = x;
        this.lastY = y;
        
        this.gradientSource = new GradientSource(x, y, nutritionalValue, this, chemistry);
    }
    
    @Override
    protected void onAddedToWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getMultiChannelField().addSource(gradientSource);
    }
    
    @Override
    protected void onRemovedFromWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getMultiChannelField().removeSource(gradientSource);
    }
    
    @Override
    protected void onUpdate() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        // Update gradient source position if moved
        if (Math.abs(getX() - lastX) > 0.1 || Math.abs(getY() - lastY) > 0.1) {
            gradientSource.updatePosition(getX(), getY());
            world.getMultiChannelField().updateSource(gradientSource, lastX, lastY);
            
            lastX = getX();
            lastY = getY();
        }
    }
    
    // Getters
    public double getNutritionalValue() { return nutritionalValue; }
    public ChemicalSignature getChemistry() { return chemistry; }
    public boolean isWaste() { return foodType == 3; }
    public int getFoodType() { return foodType; }
    
    // Setters
    public void setNutritionalValue(double value) {
        this.nutritionalValue = Math.max(0, value);
        gradientSource.strength = nutritionalValue;
    }
    
    public void setIsWaste(boolean isWaste) {
        if (isWaste) {
            this.foodType = 3;
            setSize(5);
        }
    }
    
    public void setFoodType(int type) {
        this.foodType = type;
    }
    
    @Override
    public String toString() {
        String[] typeNames = {"Red", "Green", "Blue", "Gray"};
        return String.format("Food[type=%s, nutrition=%.1f]", typeNames[foodType], nutritionalValue);
    }

    public GradientSource getGradientSource() {
        return this.gradientSource;
    }
}