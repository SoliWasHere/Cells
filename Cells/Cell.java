package Cells;

import java.awt.Color;
import java.util.List;

/**
 * A living cell that can move, eat food, and eventually reproduce.
 * Demonstrates complex behavior built on the PhysicsObj foundation.
 */
public class Cell extends PhysicsObj {
    // Behavioral parameters (future: make these evolvable)
    private static final double FOOD_DETECTION_RADIUS = 500.0;
    private static final double MOVEMENT_FORCE = 10.0;
    private static final double EATING_DISTANCE = 1.0; // Distance threshold for eating
    
    // Cell state
    private double energy;
    private int age;
    private Food targetFood;
    
    /**
     * Create a new cell at the specified position.
     */
    public Cell(double x, double y) {
        super(x, y);
        this.dampingFactor = 0.95; // Cells move through "fluid"
        this.energy = 100.0;
        this.age = 0;
        this.targetFood = null;
        setColor(Color.MAGENTA);
    }
    
    @Override
    protected void onUpdate() {
        age++;
        
        // Search for food
        findAndChaseFood();
        
        // Try to eat if close to target
        if (targetFood != null) {
            tryEatFood(targetFood);
        }
        
        // Future: reproduction, death, etc.
    }
    
    /**
     * Find the closest food and move towards it.
     */
    private void findAndChaseFood() {
        targetFood = findClosestFood();
        
        if (targetFood != null) {
            moveTowards(targetFood);
        }
    }
    
    /**
     * Find the closest food within detection radius.
     */
    private Food findClosestFood() {
        List<PhysicsObj> nearby = getCellsInRadius(FOOD_DETECTION_RADIUS);
        
        Food closest = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (PhysicsObj obj : nearby) {
            if (obj instanceof Food) {
                double distance = getDistanceTo(obj);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = (Food) obj;
                }
            }
        }
        
        return closest;
    }
    
    /**
     * Move towards a target entity.
     */
    private void moveTowards(PhysicsObj target) {
        Vector2D direction = getDirectionTo(target);
        applyForce(direction.scale(MOVEMENT_FORCE));
    }
    
    /**
     * Attempt to eat food if close enough.
     */
    private void tryEatFood(Food food) {
        if (isCollidingWith(food)) {
            // Consume the food
            energy += food.getNutritionalValue();
            food.destroy();
            targetFood = null;
            
            // Visual feedback
            setColor(Color.GREEN);
            
            // Reset color after a moment (in real implementation, use a timer)
            // For now, we'll just keep it simple
        } else if (getDistanceTo(food) > FOOD_DETECTION_RADIUS) {
            // Lost track of food
            targetFood = null;
        }
    }
    
    // === Getters ===
    
    public double getEnergy() {
        return energy;
    }
    
    public int getAge() {
        return age;
    }
    
    @Override
    public String toString() {
        return String.format("Cell[pos=(%.1f, %.1f), energy=%.1f, age=%d]",
            getX(), getY(), energy, age);
    }
}