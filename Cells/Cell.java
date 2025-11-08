//CELL.JAVA (ULTRA-OPTIMIZED)

package Cells;

import java.awt.Color;
import java.util.List;

/**
 * ULTRA-OPTIMIZED Cell: Minimal per-frame calculations.
 * Key changes:
 * - Reduced detection radii
 * - Skip updates on alternate frames
 * - Early exit conditions
 * - Simplified steering math
 */
public class Cell extends PhysicsObj {
    private double foodDetectionRadius;
    private double cellDetectionRadius;
    private double movementForce;
    private double foodAttractionWeight;
    private double cellRepulsionWeight;
    private double eatingDistance;
    private double evolveRate;
    private double reproductionThreshold;
    
    private double energy;
    private int age;
    
    // OPTIMIZATION: Skip expensive calculations on alternate frames
    private int updateSkipCounter = 0;
    private static final int UPDATE_SKIP_FREQUENCY = 2; // Update steering every N frames
    
    // OPTIMIZATION: Cached steering vector
    private Vector2D cachedSteeringVector = new Vector2D(0, 0);
    
    public Cell(double x, double y) {
        super(x, y);
        this.dampingFactor = 0.95;
        
        // OPTIMIZATION: Much smaller detection radii
        this.foodDetectionRadius = 150.0;  // Was 500
        this.cellDetectionRadius = 100.0;  // Was 300
        this.movementForce = 10.0;
        this.foodAttractionWeight = 1.0;
        this.cellRepulsionWeight = 10.0;
        this.eatingDistance = 1.0;
        this.evolveRate = Math.max(Math.random()*10, 0.1);
        this.reproductionThreshold = eatingDistance * 100;
        
        this.energy = 100.0;
        this.age = 0;
        
        setColor(Color.MAGENTA);
        setSize((int) (eatingDistance * 20));
        setMass(eatingDistance);
        
        // OPTIMIZATION: Randomize update offset to distribute load
        this.updateSkipCounter = (int)(Math.random() * UPDATE_SKIP_FREQUENCY);
    }
    
    @Override
    protected void onUpdate() {
        age++;
        energy -= Math.pow(eatingDistance, 1/2) / 4.0;
        
        // OPTIMIZATION: Early exit if dead
        if (energy <= 0) {
            destroy();
            return;
        }
        
        // OPTIMIZATION: Update steering vector only every N frames
        updateSkipCounter++;
        if (updateSkipCounter >= UPDATE_SKIP_FREQUENCY) {
            updateSkipCounter = 0;
            cachedSteeringVector = calculateSteeringVectorOptimized();
        }
        
        // Apply cached steering
        if (cachedSteeringVector.magnitudeSquared() > 0.000001) {
            Vector2D direction = cachedSteeringVector.normalize();
            applyForce(direction.scale(movementForce));
            energy -= movementForce * 0.01;
        }
        
        // OPTIMIZATION: Only check for food in immediate vicinity
        tryEatNearbyFoodOptimized();
        
        // Reproduction
        if (energy > reproductionThreshold) {
            reproduce();
        }
    }
    
    /**
     * ULTRA-OPTIMIZED steering calculation.
     * - Limit number of entities processed
     * - Use squared distances to avoid sqrt
     * - Early exit when force is sufficient
     */
    private Vector2D calculateSteeringVectorOptimized() {
        double totalX = 0;
        double totalY = 0;
        
        // OPTIMIZATION: Get entities from current grid cell and immediate neighbors only
        List<PhysicsObj> nearby = getCellsInSameGridAndNeighbors();
        
        int foodProcessed = 0;
        int cellsProcessed = 0;
        final int MAX_FOOD_CHECKS = 5;    // Only check closest 5 food
        final int MAX_CELL_CHECKS = 10;   // Only check closest 10 cells
        
        for (PhysicsObj obj : nearby) {
            // OPTIMIZATION: Quick type check and counter limits
            if (obj instanceof Food && foodProcessed < MAX_FOOD_CHECKS) {
                double dx = obj.getX() - getX();
                double dy = obj.getY() - getY();
                double distSq = dx * dx + dy * dy;
                
                // OPTIMIZATION: Use squared distance comparison (no sqrt!)
                double radiusSq = foodDetectionRadius * foodDetectionRadius;
                if (distSq < radiusSq && distSq > 1.0) {
                    double dist = Math.sqrt(distSq); // Only sqrt if needed
                    double weight = foodAttractionWeight / dist;
                    totalX += (dx / dist) * weight;
                    totalY += (dy / dist) * weight;
                    foodProcessed++;
                }
            }
            else if (obj instanceof Cell && obj != this && cellsProcessed < MAX_CELL_CHECKS) {
                double dx = obj.getX() - getX();
                double dy = obj.getY() - getY();
                double distSq = dx * dx + dy * dy;
                
                double radiusSq = cellDetectionRadius * cellDetectionRadius;
                if (distSq < radiusSq && distSq > 1.0) {
                    double dist = Math.sqrt(distSq);
                    double weight = cellRepulsionWeight / dist;
                    // Repulsion: invert direction
                    totalX -= (dx / dist) * weight;
                    totalY -= (dy / dist) * weight;
                    cellsProcessed++;
                }
            }
            
            // OPTIMIZATION: Early exit if we've processed enough
            if (foodProcessed >= MAX_FOOD_CHECKS && cellsProcessed >= MAX_CELL_CHECKS) {
                break;
            }
        }
        
        return new Vector2D(totalX, totalY);
    }
    
    /**
     * OPTIMIZATION: Only check immediate grid cell for food eating.
     */
    private void tryEatNearbyFoodOptimized() {
        if (currentMatrixCell == null) return;
        
        List<PhysicsObj> sameCellEntities = currentMatrixCell.getCells();
        
        for (PhysicsObj obj : sameCellEntities) {
            if (obj instanceof Food) {
                // OPTIMIZATION: Simple distance check without wrapping
                double dx = obj.getX() - getX();
                double dy = obj.getY() - getY();
                double distSq = dx * dx + dy * dy;
                double eatDistSq = (eatingDistance * 30) * (eatingDistance * 30);
                
                if (distSq < eatDistSq) {
                    Food food = (Food) obj;
                    energy += food.getNutritionalValue();
                    food.destroy();
                    break; // Only eat one per frame
                }
            }
        }
    }
    
    /**
     * OPTIMIZATION: Get entities from same grid cell + 8 neighbors.
     * Much faster than radius search.
     */
    private List<PhysicsObj> getCellsInSameGridAndNeighbors() {
        java.util.ArrayList<PhysicsObj> result = new java.util.ArrayList<>();
        
        if (currentMatrixCell == null) return result;
        
        SimulationWorld world = SimulationWorld.getInstance();
        Matrix matrix = world.getMatrix();
        
        int gridX = currentMatrixCell.getGridX();
        int gridY = currentMatrixCell.getGridY();
        
        // Check 3x3 grid around current position
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MatrixCell cell = matrix.getMatrixCell(gridX + dx, gridY + dy);
                if (cell != null) {
                    result.addAll(cell.getCells());
                }
            }
        }
        
        return result;
    }
    
    /**
     * OPTIMIZATION: Simplified reproduction (less mutation calculations).
     */
    private void reproduce() {
        energy -= eatingDistance * 50;
        
        Cell offspring = new Cell(getX() + 10, getY() + 10);
        offspring.setEnergy(eatingDistance * 50);
        
        // OPTIMIZATION: Fewer mutation calculations
        //double mutateFactor = (MathFunctions.evolve(evolveRate) + 1);
        
        offspring.setReproductionThreshold((MathFunctions.evolve(evolveRate) + 1)  * this.reproductionThreshold);
        offspring.setMovementForce(this.movementForce * (MathFunctions.evolve(evolveRate) + 1) );
        offspring.setEatingDistance(this.eatingDistance * (MathFunctions.evolve(evolveRate) + 1) );
        offspring.setFoodAttractionWeight(this.foodAttractionWeight * (MathFunctions.evolve(evolveRate) + 1) );
        offspring.setCellRepulsionWeight(this.cellRepulsionWeight * (MathFunctions.evolve(evolveRate) + 1) );
        
        // Simplified color mutation
        offspring.setColor(new Color(
            Math.min(255, Math.max(0, (int)(getColor().getRed() * (MathFunctions.evolve(evolveRate) + 1) ))),
            Math.min(255, Math.max(0, (int)(getColor().getGreen() * (MathFunctions.evolve(evolveRate) + 1) ))),
            Math.min(255, Math.max(0, (int)(getColor().getBlue() * (MathFunctions.evolve(evolveRate) + 1) )))
        ));
        
        SimulationWorld.getInstance().queueAddition(offspring);
    }
    
    // === Setters (unchanged) ===
    
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
    }
    
    @Override
    public void setSize(int size) {
        if (size > 200 || size < 2) {
            destroy();
            return;
        }
        super.setSize(size);
    }
    
    // === Getters ===
    
    public double getEnergy() { return energy; }
    public int getAge() { return age; }
    public double getMovementForce() { return movementForce; }
    public double getFoodDetectionRadius() { return foodDetectionRadius; }
    public double getCellDetectionRadius() { return cellDetectionRadius; }
    public double getFoodAttractionWeight() { return foodAttractionWeight; }
    public double getCellRepulsionWeight() { return cellRepulsionWeight; }
    public double getEatingDistance() { return eatingDistance; }
    public double getEvolveRate() { return evolveRate; }
    public double getReproductionThreshold() { return reproductionThreshold; }
    
    @Override
    public String toString() {
        return String.format("Cell[pos=(%.1f, %.1f), energy=%.1f, age=%d]",
            getX(), getY(), energy, age);
    }
}