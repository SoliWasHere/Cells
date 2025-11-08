package Cells;

import java.awt.Color;
import java.util.List;

/**
 * A living cell that can move, eat food, and eventually reproduce.
 * Demonstrates complex behavior built on the PhysicsObj foundation.
 */
public class Cell extends PhysicsObj {
    // Behavioral parameters (future: make these evolvable)
    private double FOOD_DETECTION_RADIUS = 500.0;
    private double MOVEMENT_FORCE = 10.0;
    private double EATING_DISTANCE = 1.0; // Distance threshold for eating
    private double evolveRate = Math.random() * 10;
    
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
        energy -= (Math.pow(EATING_DISTANCE,1/2))/2; // Baseline energy consumption
        
        // Search for food
        findAndChaseFood();
        
        // Try to eat if close to target
        if (targetFood != null) {
            tryEatFood(targetFood);
        }
        
        // Future: reproduction, death, etc.
        if (energy <= 0) {
            destroy(); // Cell dies
        }

        if (energy > 200) {
            energy -= 100;
            Cell offspring = new Cell(getX() + 10, getY() + 10);

            offspring.setMovementForce(
                this.MOVEMENT_FORCE * ( MathFunction.evolve(evolveRate) + 1)
            );

            offspring.setSize((int) (EATING_DISTANCE * 20) );

            offspring.setEatingDistance(
                EATING_DISTANCE * (MathFunction.evolve(evolveRate)+1)
            );

            offspring.setColor(
                new Color(
                    (int) Math.min(255, getColor().getRed() * (MathFunction.evolve(evolveRate) + 1)),
                    (int) Math.min(255, getColor().getGreen() * (MathFunction.evolve(evolveRate) + 1)),
                    (int) Math.min(255, getColor().getBlue() * (MathFunction.evolve(evolveRate) + 1))
                )
            );

            SimulationWorld.getInstance().addEntity(offspring);
        }
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
        energy -= MOVEMENT_FORCE * 0.1; // Energy cost for moving
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
            
            // Reset color after a moment (in real implementation, use a timer)
            // For now, we'll just keep it simple
        } else if (getDistanceTo(food) > FOOD_DETECTION_RADIUS) {
            // Lost track of food
            targetFood = null;
        }
    }

    // === Setters ===

    public void setEnergy(double energy) {
        this.energy = Math.max(0, energy);
    }

    public void setAge(int age) {
        this.age = Math.max(0, age);
    }

    public void setFoodDetectionRadius(double radius) {
        this.FOOD_DETECTION_RADIUS = radius;
    }

    public void setMovementForce(double force) {
        this.MOVEMENT_FORCE = force;
    }

    public void setEatingDistance(double distance) {
        if (distance > 200) {
            this.destroy();
        }
        this.EATING_DISTANCE = distance;
    }
    
    // === Getters ===
    
    public double getEnergy() {
        return energy;
    }
    
    public int getAge() {
        return age;
    }

    public double getMovementForce() {
        return MOVEMENT_FORCE;
    }
    
    @Override
    public String toString() {
        return String.format("Cell[pos=(%.1f, %.1f), energy=%.1f, age=%d]",
            getX(), getY(), energy, age);
    }
}