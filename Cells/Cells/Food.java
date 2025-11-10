//FOOD.JAVA (WITH FOOD ID)

package Cells;

import java.awt.Color;

/**
 * Food particle with a 2D food ID vector for specialization.
 */
public class Food extends PhysicsObj {
    private static final double DEFAULT_NUTRITIONAL_VALUE = 50.0;
    
    private double nutritionalValue;
    private GradientSource foodGradientSource;
    
    // Food ID: 2D vector from (0,0) to (1,1) representing food type
    private double foodIdX;
    private double foodIdY;
    
    public Food(double x, double y) {
        super(x, y);
        this.nutritionalValue = DEFAULT_NUTRITIONAL_VALUE;
        
        // Random food ID
        this.foodIdX = Math.random();
        this.foodIdY = Math.random();
        
        updateColorFromFoodId();
        setSize(3);
        
        // Create gradient source
        this.foodGradientSource = new GradientSource(x, y, nutritionalValue, this);
    }
    
    public Food(double x, double y, double nutritionalValue) {
        this(x, y);
        this.nutritionalValue = nutritionalValue;
        this.foodGradientSource.strength = nutritionalValue;
    }
    
    public Food(double x, double y, double nutritionalValue, double foodIdX, double foodIdY) {
        this(x, y, nutritionalValue);
        this.foodIdX = Math.max(0, Math.min(1, foodIdX));
        this.foodIdY = Math.max(0, Math.min(1, foodIdY));
        updateColorFromFoodId();
    }
    
    /**
     * Update food color based on its food ID.
     * foodIdX maps to green-yellow spectrum
     * foodIdY maps to brightness/intensity
     */
    private void updateColorFromFoodId() {
        // Map food ID to green-yellow color spectrum
        int green = (int)(100 + foodIdX * 155); // 100-255
        int yellow = (int)(100 + foodIdY * 155); // 100-255
        int red = (int)(foodIdY * 200); // 0-200 for yellow tint
        
        setColor(new Color(red, green, Math.max(50, 255 - yellow)));
    }
    
    @Override
    protected void onAddedToWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getFoodGradientField().addSource(foodGradientSource);
    }
    
    @Override
    protected void onRemovedFromWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getFoodGradientField().removeSource(foodGradientSource);
    }
    
    @Override
    protected void onUpdate() {
        // Update gradient source if food moves
        double oldX = foodGradientSource.x;
        double oldY = foodGradientSource.y;
        
        if (Math.abs(oldX - getX()) > 0.1 || Math.abs(oldY - getY()) > 0.1) {
            foodGradientSource.updatePosition(getX(), getY());
            
            SimulationWorld world = SimulationWorld.getInstance();
            world.getFoodGradientField().updateSource(foodGradientSource, oldX, oldY);
        }
    }
    
    // Getters
    public double getNutritionalValue() {
        return nutritionalValue;
    }
    
    public double getFoodIdX() {
        return foodIdX;
    }
    
    public double getFoodIdY() {
        return foodIdY;
    }
    
    // Setters
    public void setNutritionalValue(double value) {
        this.nutritionalValue = Math.max(0, value);
        this.foodGradientSource.strength = this.nutritionalValue;
    }
    
    public void setFoodId(double foodIdX, double foodIdY) {
        this.foodIdX = Math.max(0, Math.min(1, foodIdX));
        this.foodIdY = Math.max(0, Math.min(1, foodIdY));
        updateColorFromFoodId();
    }
    
    @Override
    public String toString() {
        return String.format("Food[pos=(%.1f, %.1f), nutrition=%.1f, id=(%.2f, %.2f)]",
            getX(), getY(), nutritionalValue, foodIdX, foodIdY);
    }
}