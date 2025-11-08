//FOOD>JAVA

package Cells;

import java.awt.Color;

/**
 * A food particle that cells can consume for energy.
 * Static by default but can drift in space.
 */
public class Food extends PhysicsObj {
    private static final double DEFAULT_NUTRITIONAL_VALUE = 50.0;
    
    private double nutritionalValue;
    
    /**
     * Create a new food particle at the specified position.
     */
    public Food(double x, double y) {
        super(x, y);
        this.nutritionalValue = DEFAULT_NUTRITIONAL_VALUE;
        setColor(new Color(100, 150, 255));
        setSize(3);
    }
    
    /**
     * Create food with custom nutritional value.
     */
    public Food(double x, double y, double nutritionalValue) {
        this(x, y);
        this.nutritionalValue = nutritionalValue;
    }
    
    public double getNutritionalValue() {
        return nutritionalValue;
    }
    
    public void setNutritionalValue(double value) {
        this.nutritionalValue = Math.max(0, value);
    }
}