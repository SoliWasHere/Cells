//CELL.JAVA (GRADIENT-BASED)

package Cells;

import java.awt.Color;
import java.util.List;

/**
 * Gradient-based Cell: Follows food gradient field.
 */
public class Cell extends PhysicsObj {
    private double movementForce;
    private double eatingDistance;
    private double evolveRate;
    private double reproductionThreshold;
    
    private double energy;
    private int age;
    
    private GradientSource cellGradientSource; // This cell's contribution to cell gradient
    
    // Update every N frames for performance
    private int updateSkipCounter = 0;
    private static final int UPDATE_SKIP_FREQUENCY = 2;
    
    private Vector2D cachedGradientDirection = new Vector2D(0, 0);
    
    public Cell(double x, double y) {
        super(x, y);
        this.dampingFactor = 0.95;
        
        this.movementForce = 10.0;
        this.eatingDistance = 1.0;
        this.evolveRate = Math.max(Math.random() * 10, 0.1);
        this.reproductionThreshold = eatingDistance * 100;
        
        this.energy = 100.0;
        this.age = 0;
        
        setColor(Color.MAGENTA);
        setSize((int) (eatingDistance * 20));
        setMass(eatingDistance);
        
        this.updateSkipCounter = (int)(Math.random() * UPDATE_SKIP_FREQUENCY);
        
        // Create gradient source for cell repulsion
        this.cellGradientSource = new GradientSource(x, y, 50.0, this);
    }

    @Override
    protected void onAddedToWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getCellGradientField().addSource(cellGradientSource);
    }
    
    @Override
    protected void onRemovedFromWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getCellGradientField().removeSource(cellGradientSource);
    }

    @Override
    public void destroy() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        // Drop food with remaining energy
        Food food = new Food(this.getX(), this.getY());
        food.setNutritionalValue(Math.max(0, energy - 10));
        food.setColor(new Color(
            (int) (255 - Math.min(255, food.getNutritionalValue() * 2)),
            255,
            255
        ));
        world.queueAddition(food);
        
        super.destroy();
    }
    
    @Override
    protected void onUpdate() {
        age++;
        energy -= Math.pow(eatingDistance, 1/2.0) / 4.0;
        
        // Update gradient source position
        double oldX = cellGradientSource.x;
        double oldY = cellGradientSource.y;
        cellGradientSource.updatePosition(getX(), getY());
        
        SimulationWorld world = SimulationWorld.getInstance();
        world.getCellGradientField().updateSource(cellGradientSource, oldX, oldY);
        
        if (energy <= 0) {
            destroy();
            return;
        }
        
        // Sample gradients periodically
        updateSkipCounter++;
        if (updateSkipCounter >= UPDATE_SKIP_FREQUENCY) {
            updateSkipCounter = 0;
            cachedGradientDirection = calculateGradientDirection();
        }
        
        // Apply movement based on gradient
        if (cachedGradientDirection.magnitudeSquared() > 0.000001) {
            Vector2D direction = cachedGradientDirection.normalize();
            applyForce(direction.scale(movementForce));
            energy -= movementForce * 0.01;
        }
        
        // Try to eat nearby food
        tryEatNearbyFood();
        
        // Reproduction
        if (energy > reproductionThreshold) {
            reproduce();
        }
    }
    
    /**
     * Calculate movement direction from gradient fields.
     */
    private Vector2D calculateGradientDirection() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        // Sample food gradient (attraction)
        GradientSample foodGradient = world.getFoodGradientField().sample(getX(), getY());
        
        // Sample cell gradient (repulsion)
        GradientSample cellGradient = world.getCellGradientField().sample(getX(), getY());
        
        // Combine gradients: move toward food, away from cells
        double dirX = foodGradient.directionX - cellGradient.directionX * 0.5;
        double dirY = foodGradient.directionY - cellGradient.directionY * 0.5;
        
        return new Vector2D(dirX, dirY);
    }
    
    /**
     * Try to eat nearby food using spatial hash.
     */
    private void tryEatNearbyFood() {
        SimulationWorld world = SimulationWorld.getInstance();
        GradientField foodField = world.getFoodGradientField();
        
        // Check nearby cells in spatial hash
        int gridX = (int)(getX() / foodField.getCellSize());
        int gridY = (int)(getY() / foodField.getCellSize());
        
        double eatRadiusSq = Math.pow(eatingDistance * 30, 2);
        
        // Check 3x3 grid around cell
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int checkX = (gridX + dx + foodField.getGridWidth()) % foodField.getGridWidth();
                int checkY = (gridY + dy + foodField.getGridHeight()) % foodField.getGridHeight();
                
                // Get food sources in this cell (would need to add accessor method)
                List<PhysicsObj> entities = world.getEntitiesInSpatialCell(checkX, checkY);
                
                for (PhysicsObj obj : entities) {
                    if (obj instanceof Food) {
                        double dx_world = obj.getX() - getX();
                        double dy_world = obj.getY() - getY();
                        double distSq = dx_world * dx_world + dy_world * dy_world;
                        
                        if (distSq < eatRadiusSq) {
                            Food food = (Food) obj;
                            energy += food.getNutritionalValue();
                            food.destroy();
                            return; // Only eat one per frame
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Reproduce and mutate.
     */
    private void reproduce() {
        energy -= eatingDistance * 50;
        
        Cell offspring = new Cell(getX() + 10, getY() + 10);
        offspring.setEnergy(eatingDistance * 50);
        
        // Mutate parameters
        double mutate = MathFunctions.evolve(evolveRate) + 1;
        
        offspring.setReproductionThreshold(this.reproductionThreshold * mutate);
        offspring.setMovementForce(this.movementForce * mutate);
        offspring.setEatingDistance(this.eatingDistance * mutate);
        
        // Mutate color
        offspring.setColor(new Color(
            Math.min(255, Math.max(0, (int)(getColor().getRed() * mutate))),
            Math.min(255, Math.max(0, (int)(getColor().getGreen() * mutate))),
            Math.min(255, Math.max(0, (int)(getColor().getBlue() * mutate)))
        ));
        
        SimulationWorld.getInstance().queueAddition(offspring);
    }
    
    // Setters
    public void setEnergy(double energy) {
        this.energy = Math.max(0, energy);
    }
    
    public void setAge(int age) {
        this.age = Math.max(0, age);
    }

    public void setReproductionThreshold(double amount) {
        this.reproductionThreshold = Math.max(amount, this.energy);
    }
    
    public void setMovementForce(double force) {
        this.movementForce = Math.max(1.0, force);
    }
    
    public void setEatingDistance(double distance) {
        if (distance > 10.0 || distance < 0.1) {
            destroy();
            return;
        }
        
        this.eatingDistance = distance;
        
        int newSize = (int) (eatingDistance * 20);
        if (newSize > 200 || newSize < 2) {
            destroy();
            return;
        }
        
        super.setSize(newSize);
        setMass(eatingDistance);
        
        // Update gradient source strength
        cellGradientSource.strength = eatingDistance * 50;
    }
    
    @Override
    public void setSize(int size) {
        if (size > 200 || size < 2) {
            destroy();
            return;
        }
        super.setSize(size);
    }
    
    // Getters
    public double getEnergy() { return energy; }
    public int getAge() { return age; }
    public double getMovementForce() { return movementForce; }
    public double getEatingDistance() { return eatingDistance; }
    public double getEvolveRate() { return evolveRate; }
    public double getReproductionThreshold() { return reproductionThreshold; }
    
    @Override
    public String toString() {
        return String.format("Cell[pos=(%.1f, %.1f), energy=%.1f, age=%d]",
            getX(), getY(), energy, age);
    }
}