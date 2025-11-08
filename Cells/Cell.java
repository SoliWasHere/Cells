//CELL.JAVA

package Cells;

import java.awt.Color;
import java.util.List;

/**
 * A living cell that can move, eat food, and reproduce.
 * Uses vector-based steering: attracted to food, repelled by other cells.
 */
public class Cell extends PhysicsObj {
    // === Behavioral Parameters (Each cell has its own!) ===
    private double foodDetectionRadius;
    private double cellDetectionRadius;
    private double movementForce;
    private double foodAttractionWeight;    // Your constant A
    private double cellRepulsionWeight;     // Your constant B
    private double eatingDistance;
    private double evolveRate;
    private double reproductionThreshold;
    
    // === Cell State ===
    private double energy;
    private int age;
    
    /**
     * Create a new cell at the specified position.
     */
    public Cell(double x, double y) {
        super(x, y);
        this.dampingFactor = 0.95;
        
        // Default values
        this.foodDetectionRadius = 500.0;
        this.cellDetectionRadius = 300.0;
        this.movementForce = 10.0;
        this.foodAttractionWeight = 1.0;    // Your constant A
        this.cellRepulsionWeight = 10.0;     // Your constant B
        this.eatingDistance = 1.0;
        this.evolveRate = Math.max(Math.random()*10,this.age/10);
        this.reproductionThreshold = eatingDistance * 100;
        
        // Initial state
        this.energy = 100.0;
        this.age = 0;
        
        // Visual properties
        setColor(Color.MAGENTA);
        setSize((int) (eatingDistance * 20));
        setMass(eatingDistance);
    }
    
    @Override
    protected void onUpdate() {
        age++;
        
        // Energy consumption (your formula)
        energy -= Math.pow(eatingDistance, 2) / 4.0;
        
        // === NEW: Vector-based steering ===
        Vector2D steeringVector = calculateSteeringVector();
        
        // Apply movement if there's a direction to go
        if (steeringVector.magnitude() > 0.001) {
            Vector2D direction = steeringVector.normalize();
            applyForce(direction.scale(movementForce));
            energy -= movementForce * 0.01; // Energy cost for moving
        }
        
        // Try to eat nearby food
        tryEatNearbyFood();
        
        // Death check
        if (energy <= 0) {
            destroy();
            return;
        }
        
        // Reproduction (your formula)
        if (energy > reproductionThreshold) {
            reproduce();
        }
    }
    
    /**
     * Calculate steering vector based on food (attractive) and cells (repulsive).
     * Uses inverse distance weighting - closer objects have stronger influence.
     */
    private Vector2D calculateSteeringVector() {
        Vector2D totalVector = new Vector2D(0, 0);
        
        // Attraction to food (weighted by constant A and inverse distance)
        List<PhysicsObj> nearbyFood = getCellsInRadius(foodDetectionRadius);
        for (PhysicsObj obj : nearbyFood) {
            if (obj instanceof Food) {
                Vector2D directionToFood = getDirectionTo(obj);
                double distance = getDistanceTo(obj);
                
                // Inverse distance weighting (closer = stronger attraction)
                double weight = foodAttractionWeight / Math.max(distance, 1.0);
                
                totalVector = totalVector.add(directionToFood.scale(weight));
            }
        }
        
        // Repulsion from cells (weighted by constant B and inverse distance)
        List<PhysicsObj> nearbyCells = getCellsInRadius(cellDetectionRadius);
        for (PhysicsObj obj : nearbyCells) {
            if (obj instanceof Cell && obj != this) {
                Vector2D directionToCell = getDirectionTo(obj);
                double distance = getDistanceTo(obj);
                
                // Inverse distance weighting (closer = stronger repulsion)
                double weight = cellRepulsionWeight / Math.max(distance, 1.0);
                
                // Invert direction for repulsion
                Vector2D directionAway = new Vector2D(-directionToCell.x, -directionToCell.y);
                totalVector = totalVector.add(directionAway.scale(weight));
            }
        }
        
        return totalVector;
    }
    
    /**
     * Try to eat any food the cell is touching.
     */
    private void tryEatNearbyFood() {
        List<PhysicsObj> nearby = getCellsInRadius(eatingDistance * 30);
        
        for (PhysicsObj obj : nearby) {
            if (obj instanceof Food && isCollidingWith(obj)) {
                Food food = (Food) obj;
                energy += food.getNutritionalValue();
                food.destroy();
            }
        }
    }
    
    /**
     * Reproduce with mutation (your reproduction logic).
     */
    private void reproduce() {
        // Energy cost (your formula)
        energy -= eatingDistance * 50;
        
        // Create offspring at offset position
        Cell offspring = new Cell(getX() + 10, getY() + 10);
        
        // Give offspring starting energy (your formula)
        offspring.setEnergy(eatingDistance * 50);

        offspring.setReproductionThreshold(
            (MathFunctions.evolve(evolveRate) + 1) * this.reproductionThreshold
        );
        
        // Mutate movement force (your mutation)
        offspring.setMovementForce(
            this.movementForce * (MathFunctions.evolve(evolveRate) + 1)
        );
        
        // Mutate eating distance and update size/mass accordingly (your mutation)
        double newEatingDistance = this.eatingDistance * (MathFunctions.evolve(evolveRate) + 1);
        offspring.setEatingDistance(newEatingDistance);
        
        // NEW: Also mutate the steering weights
        offspring.setFoodAttractionWeight(
            this.foodAttractionWeight * (MathFunctions.evolve(evolveRate) + 1)
        );
        offspring.setCellRepulsionWeight(
            this.cellRepulsionWeight * (MathFunctions.evolve(evolveRate) + 1)
        );
        
        // Mutate color (your color mutation)
        offspring.setColor(new Color(
            (int) Math.min(255, Math.max(0, getColor().getRed() * (MathFunctions.evolve(evolveRate) + 1))),
            (int) Math.min(255, Math.max(0, getColor().getGreen() * (MathFunctions.evolve(evolveRate) + 1))),
            (int) Math.min(255, Math.max(0, getColor().getBlue() * (MathFunctions.evolve(evolveRate) + 1)))
        ));
        
        // Add to world (using queue to avoid concurrent modification)
        SimulationWorld.getInstance().queueAddition(offspring);
    }
    
    // === Setters ===
    
    public void setEnergy(double energy) {
        this.energy = Math.max(0, energy);
    }
    
    public void setAge(int age) {
        this.age = Math.max(0, age);
    }
    
    public void setFoodDetectionRadius(double radius) {
        this.foodDetectionRadius = Math.max(10.0, radius);
    }

    public void setReproductionThreshold(double amount) {
        this.reproductionThreshold = Math.max(amount, this.energy);
    }
    
    public void setCellDetectionRadius(double radius) {
        this.cellDetectionRadius = Math.max(10.0, radius);
    }
    
    public void setMovementForce(double force) {
        this.movementForce = Math.max(1.0, force);
    }
    
    public void setFoodAttractionWeight(double weight) {
        this.foodAttractionWeight = Math.max(0.1, weight);
    }
    
    public void setCellRepulsionWeight(double weight) {
        this.cellRepulsionWeight = Math.max(0.1, weight);
    }
    
    public void setEatingDistance(double distance) {
        // Kill if size becomes too extreme (your size limit logic)
        if (distance > 10.0 || distance < 0.1) {
            destroy();
            return;
        }
        
        this.eatingDistance = distance;
        
        // Update size and mass based on eating distance
        int newSize = (int) (eatingDistance * 20);
        if (newSize > 200 || newSize < 2) {
            destroy();
            return;
        }
        
        super.setSize(newSize);
        setMass(eatingDistance);
    }
    
    @Override
    public void setSize(int size) {
        // Kill if size is out of bounds (your size check)
        if (size > 200 || size < 2) {
            destroy();
            return;
        }
        super.setSize(size);
    }
    
    // === Getters ===
    
    public double getEnergy() {
        return energy;
    }
    
    public int getAge() {
        return age;
    }
    
    public double getMovementForce() {
        return movementForce;
    }
    
    public double getFoodDetectionRadius() {
        return foodDetectionRadius;
    }
    
    public double getCellDetectionRadius() {
        return cellDetectionRadius;
    }
    
    public double getFoodAttractionWeight() {
        return foodAttractionWeight;
    }
    
    public double getCellRepulsionWeight() {
        return cellRepulsionWeight;
    }
    
    public double getEatingDistance() {
        return eatingDistance;
    }
    
    public double getEvolveRate() {
        return evolveRate;
    }

    public double getReproductionThreshold() {
        return reproductionThreshold;
    }
    
    @Override
    public String toString() {
        return String.format("Cell[pos=(%.1f, %.1f), energy=%.1f, age=%d]",
            getX(), getY(), energy, age);
    }
}