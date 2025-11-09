//FOOD.JAVA

package Cells;

import java.awt.Color;

/**
 * Food particle that contributes to the food gradient field.
 */
public class Food extends PhysicsObj {
    private static final double DEFAULT_NUTRITIONAL_VALUE = 50.0;
    
    private double nutritionalValue;
    private GradientSource foodGradientSource;
    
    public Food(double x, double y) {
        super(x, y);
        this.nutritionalValue = DEFAULT_NUTRITIONAL_VALUE;
        setColor(new Color(100, 150, 255));
        setSize(3);
        
        // Create gradient source
        this.foodGradientSource = new GradientSource(x, y, nutritionalValue, this);
    }
    
    public Food(double x, double y, double nutritionalValue) {
        this(x, y);
        this.nutritionalValue = nutritionalValue;
        this.foodGradientSource.strength = nutritionalValue;
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
    
    public double getNutritionalValue() {
        return nutritionalValue;
    }
    
    public void setNutritionalValue(double value) {
        this.nutritionalValue = Math.max(0, value);
        this.foodGradientSource.strength = this.nutritionalValue;
    }
}