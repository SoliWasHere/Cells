//CELL.JAVA (WITH STOMACH SYSTEM)

package Cells;

import java.awt.Color;
import java.util.List;

/**
 * Cell with stomach system for waste accumulation and food specialization.
 */
public class Cell extends PhysicsObj {
    private double movementForce;
    private double eatingDistance;
    private double evolveRate;
    private double reproductionThreshold;

    public int lastAte = 0;
    
    private double energy;
    private int age;
    
    // Food preference (what this cell wants to eat)
    private double preferredFoodIdX;
    private double preferredFoodIdY;
    
    // Stomach system for waste
    private double stomachWasteX; // Accumulated waste X component
    private double stomachWasteY; // Accumulated waste Y component
    private double wasteThreshold; // When to expel waste
    
    private GradientSource cellGradientSource;
    
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
        
        // Random food preference
        this.preferredFoodIdX = Math.random();
        this.preferredFoodIdY = Math.random();
        
        // Initialize stomach
        this.stomachWasteX = 0;
        this.stomachWasteY = 0;
        this.wasteThreshold = 200.0;
        
        updateColorFromPreference();
        setSize((int) (eatingDistance * 20));
        setMass(eatingDistance);
        
        this.updateSkipCounter = (int)(Math.random() * UPDATE_SKIP_FREQUENCY);
        
        this.cellGradientSource = new GradientSource(x, y, 50.0, this);
    }

    /**
     * Update cell color based on food preference (red spectrum).
     */
    private void updateColorFromPreference() {
        int red = (int)(150 + preferredFoodIdX * 105); // 150-255
        int magenta = (int)(preferredFoodIdY * 150); // 0-150 for variety
        setColor(new Color(red, magenta, magenta));
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
        
        // Drop food with remaining energy (but limit to prevent explosion)
        if (energy > 10) {
            double foodEnergy = Math.min(energy * 0.5, 200.0);
            Food food = new Food(this.getX(), this.getY());
            food.setNutritionalValue(foodEnergy);
            
            // Waste has opposite food ID
            food.setFoodId(
                MathFunctions.realMod( (1.0 - preferredFoodIdX), 1), 
                MathFunctions.realMod( (1.0 - preferredFoodIdY), 1)
            );
            //world.queueAddition(food);
        }
        
        super.destroy();
    }
    
    @Override
    public void onUpdate() {
        age++;
        energy -= Math.pow(eatingDistance, 1/2.0) / 4.0;
        
        // Update gradient source position
        double oldX = cellGradientSource.x;
        double oldY = cellGradientSource.y;
        cellGradientSource.updatePosition(getX(), getY());
        
        SimulationWorld world = SimulationWorld.getInstance();
        world.getCellGradientField().updateSource(cellGradientSource, oldX, oldY);
        
        if (energy <= 10) {
            destroy();
            return;
        }
        
        // Check if waste threshold reached
        double wasteAmount = Math.sqrt(stomachWasteX * stomachWasteX + stomachWasteY * stomachWasteY);
        if (wasteAmount >= wasteThreshold) {
            expelWaste();
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
        if (energy > Math.max(eatingDistance * 50, reproductionThreshold)) {
            reproduce();
        }
    }

    /**
     * Reproduce and mutate.
     */
    private void reproduce() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        energy -= eatingDistance * 50;
        
        // Create offspring at slightly offset position
        double offsetAngle = Math.random() * 2 * Math.PI;
        double offsetDist = (getSize() + 20) / 2.0; // Spawn outside parent's radius
        
        Cell offspring = new Cell(
            getX() + Math.cos(offsetAngle) * offsetDist, 
            getY() + Math.sin(offsetAngle) * offsetDist
        );
        offspring.setEnergy(eatingDistance * 50);
        
        // Mutate parameters
        offspring.setReproductionThreshold(this.reproductionThreshold * (a()+1));
        offspring.setMovementForce(this.movementForce * ( a() + 1));
        offspring.setEatingDistance(this.eatingDistance * (a() + 1));
        offspring.setWasteThreshold(this.wasteThreshold * (a() + 1));
        
        // Mutate food preference
        offspring.setPreferredFoodId(
            Math.max(0, Math.min(1, this.preferredFoodIdX + MathFunctions.evolve(evolveRate) * 0.1)),
            Math.max(0, Math.min(1, this.preferredFoodIdY + MathFunctions.evolve(evolveRate) * 0.1))
        );
        
        // Give offspring a small velocity away from parent
        offspring.setVelocity(
            getVelocityX() + Math.cos(offsetAngle) * 5,
            getVelocityY() + Math.sin(offsetAngle) * 5
        );
        
        world.queueAddition(offspring);
    }

    /**
     * Expel accumulated waste as food particles.
     */
    private void expelWaste() {
        if (stomachWasteX == 0 && stomachWasteY == 0) return;
        
        // Normalize waste to get food ID
        double wasteMagnitude = Math.sqrt(stomachWasteX * stomachWasteX + stomachWasteY * stomachWasteY);
        double wasteIdX = Math.abs(stomachWasteX / wasteMagnitude);
        double wasteIdY = Math.abs(stomachWasteY / wasteMagnitude);
        
        // Create waste food particle
        Food waste = new Food(getX(), getY());
        waste.setNutritionalValue(wasteMagnitude * 0.5); // Convert waste to nutrition
        waste.setFoodId(wasteIdX, wasteIdY);
        
        // Give waste some velocity away from cell
        double angle = Math.random() * 2 * Math.PI;
        waste.setVelocity(Math.cos(angle) * 5, Math.sin(angle) * 5);
        
        SimulationWorld.getInstance().queueAddition(waste);
        
        // Clear stomach
        stomachWasteX = 0;
        stomachWasteY = 0;
        
        energy -= 5; // Cost of expelling waste
    }
    
    /**
     * Calculate movement direction from gradient fields.
     */
    private Vector2D calculateGradientDirection() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        GradientSample foodGradient = world.getFoodGradientField().sample(getX(), getY());
        GradientSample cellGradient = world.getCellGradientField().sample(getX(), getY());
        
        // Combine gradients
        double dirX = foodGradient.directionX - cellGradient.directionX * 0.5;
        double dirY = foodGradient.directionY - cellGradient.directionY * 0.5;
        
        return new Vector2D(dirX, dirY);
    }
    
    /**
     * Calculate energy efficiency based on food ID matching.
     * Returns value from 0 to 1 based on how well food matches preference.
     */
    private double calculateFoodEfficiency(Food food) {
        double deltaX = food.getFoodIdX() - preferredFoodIdX;
        double deltaY = food.getFoodIdY() - preferredFoodIdY;
        
        // Handle wrapping (toroidal food ID space)
        if (Math.abs(deltaX) > 0.5) deltaX = (deltaX > 0) ? deltaX - 1 : deltaX + 1;
        if (Math.abs(deltaY) > 0.5) deltaY = (deltaY > 0) ? deltaY - 1 : deltaY + 1;
        
        double distance = Math.sqrt( (deltaX * deltaX) + (deltaY * deltaY) );
        double maxDistance = Math.sqrt(2); // Maximum distance in unit square
        
        // Efficiency: 1.0 for perfect match, 0.0 for maximum mismatch
        return (maxDistance - distance) / maxDistance;
    }
    
    /**
     * Try to eat nearby food using spatial hash.
     * IMPORTANT: Only eats Food objects, never other Cells!
     */
    private void tryEatNearbyFood() {
        SimulationWorld world = SimulationWorld.getInstance();
        GradientField foodField = world.getFoodGradientField();
        
        int gridX = (int)(getX() / foodField.getCellSize());
        int gridY = (int)(getY() / foodField.getCellSize());
        
        double eatRadiusSq = Math.pow(eatingDistance * 30, 2);
        
        // Check 3x3 grid around cell
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int checkX = (gridX + dx + foodField.getGridWidth()) % foodField.getGridWidth();
                int checkY = (gridY + dy + foodField.getGridHeight()) % foodField.getGridHeight();
                
                List<PhysicsObj> entities = world.getEntitiesInSpatialCell(checkX, checkY);
                
                for (PhysicsObj obj : entities) {
                    // CRITICAL: Only eat Food, never other Cells or entities!
                    if (!(obj instanceof Food)) continue;
                    
                    // Use wrapped delta for proper distance calculation
                    Vector2D delta = world.getWrappedDelta(getX(), getY(), obj.getX(), obj.getY());
                    double distSq = delta.x * delta.x + delta.y * delta.y;
                    
                    if (distSq < eatRadiusSq) {
                        Food food = (Food) obj;
                        
                        // Calculate efficiency
                        double efficiency = calculateFoodEfficiency(food);
                        double energyGained = food.getNutritionalValue() * efficiency;
                        
                        energy += energyGained;
                        
                        // Accumulate waste (opposite of food ID, scaled by inefficiency)
                        double wasteAmount = food.getNutritionalValue() * (1.0 - efficiency);
                        double wasteIdX = 1.0 - food.getFoodIdX();
                        double wasteIdY = 1.0 - food.getFoodIdY();
                        
                        stomachWasteX += wasteIdX * wasteAmount;
                        stomachWasteY += wasteIdY * wasteAmount;
                        
                        food.destroy();
                        lastAte = SimulationWorld.getInstance().getFrameCount();
                        return; // Only eat one food per frame
                    }
                }
            }
        }
    }

    private double a() {
        return MathFunctions.evolve(evolveRate);
    }
    
    // Setters
    public void setEnergy(double energy) {
        if (energy < 0) {
            throw new IllegalArgumentException("WHYYYY: " + energy);
        }
        this.energy = Math.max(0, energy);
    }
    
    public void setAge(int age) {
        this.age = Math.max(0, age);
    }

    public void setReproductionThreshold(double amount) {
        // Ensure reproduction threshold is reasonable and always higher than starting energy
        this.reproductionThreshold = Math.max(amount, 150.0); // Minimum threshold of 150
        this.reproductionThreshold = Math.min(this.reproductionThreshold, 5000.0); // Maximum of 5000
    }
    
    public void setMovementForce(double force) {
        this.movementForce = Math.max(1.0, force);
    }
    
    public void setWasteThreshold(double threshold) {
        this.wasteThreshold = Math.max(50.0, threshold);
    }
    
    public void setPreferredFoodId(double x, double y) {
        this.preferredFoodIdX = Math.max(0, Math.min(1, x));
        this.preferredFoodIdY = Math.max(0, Math.min(1, y));
        updateColorFromPreference();
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
    public double getPreferredFoodIdX() { return preferredFoodIdX; }
    public double getPreferredFoodIdY() { return preferredFoodIdY; }
    public double getStomachWasteX() { return stomachWasteX; }
    public double getStomachWasteY() { return stomachWasteY; }
    public double getWasteThreshold() { return wasteThreshold; }
    
    @Override
    public String toString() {
        return String.format("Cell[pos=(%.1f, %.1f), energy=%.1f, age=%d, pref=(%.2f, %.2f)]",
            getX(), getY(), energy, age, preferredFoodIdX, preferredFoodIdY);
    }
}