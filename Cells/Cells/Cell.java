//CELL.JAVA (FIXED COMPATIBILITY THRESHOLDS)

package Cells;

import java.awt.Color;
import java.util.List;

/**
 * Cell with 8D chemical preference system.
 * Can eat food AND other cells if chemistry matches.
 */
public class Cell extends PhysicsObj {
    // RELAXED Compatibility thresholds - cells can eat more food types
    private static final double EDIBLE_THRESHOLD = ChemicalSignature.MAX_DISTANCE * 0.6; // ~1.7 (was 0.707)
    private static final double TOXIC_THRESHOLD = ChemicalSignature.MAX_DISTANCE * 0.9; // ~2.5 (was 2.12)
    
    private double movementForce;
    private double eatingDistance;
    private double evolveRate;
    private double reproductionThreshold;

    public int lastAte = 0;
    
    private double energy;
    private int age;
    
    // 8D chemical preference
    private ChemicalSignature chemicalPreference;
    
    // Receptor sensitivity (can be tuned)
    private ReceptorSensitivity receptorSensitivity;
    
    // Stomach for waste accumulation
    private ChemicalSignature stomachWaste;
    private double wasteThreshold;
    
    private GradientSource cellGradientSource;
    
    private int updateSkipCounter = 0;
    private static final int UPDATE_SKIP_FREQUENCY = 2;
    
    private Vector2D cachedGradientDirection = new Vector2D(0, 0);
    
    // Waste damage tracking
    private double wasteDamageAccumulated = 0;
    
    // Specialization affects mutation rate
    private double specializationIndex = 0.5;
    
    public Cell(double x, double y, ChemicalSignature preference) {
        super(x, y);
        this.dampingFactor = 0.95;
        
        this.movementForce = 10.0;
        this.eatingDistance = 1.0;
        this.evolveRate = Math.max(Math.random() * 10, 0.1);
        this.reproductionThreshold = eatingDistance * 100;
        
        this.energy = 100.0;
        this.age = 0;
        
        this.chemicalPreference = preference;
        this.receptorSensitivity = new ReceptorSensitivity();
        this.stomachWaste = ChemicalSignature.zeros();
        this.wasteThreshold = 200.0;

        setSize((int) (eatingDistance * 20));
        setMass(eatingDistance);
        
        this.updateSkipCounter = (int)(Math.random() * UPDATE_SKIP_FREQUENCY);
        
        this.cellGradientSource = new GradientSource(x, y, 50.0, this, chemicalPreference);
    }

    @Override
    protected void onAddedToWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getMultiChannelField().addSource(cellGradientSource);
    }
    
    @Override
    protected void onRemovedFromWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getMultiChannelField().removeSource(cellGradientSource);
    }

    @Override
    public void destroy() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        // Drop food with remaining energy (inverse chemistry)
        if (energy > 10) {
            double foodEnergy = Math.min(energy * 0.5, 200.0);
            ChemicalSignature deathChem = chemicalPreference.opposite();
            
            Food food = new Food(this.getX(), this.getY(), deathChem, foodEnergy);
            food.setColor(new Color(80, 80, 80)); // Gray death food
            world.queueAddition(food);
        }
        
        super.destroy();
    }
    
    @Override
    public void onUpdate() {
        age++;
        
        // REDUCED metabolism costs so cells can survive longer
        energy -= Math.pow(eatingDistance, 1/2.0) / 32.0; // Was /16.0
        
        // Specialization cost (reduced)
        updateSpecialization();
        energy -= specializationIndex * 0.05; // Was 0.1
        
        // Receptor cost (reduced)
        energy -= receptorSensitivity.getEnergyCost() * 0.5; // Halved
        
        // Waste damage from nearby waste particles
        energy -= wasteDamageAccumulated;
        wasteDamageAccumulated = 0;
        
        // Update gradient source position
        double oldX = cellGradientSource.x;
        double oldY = cellGradientSource.y;
        cellGradientSource.updatePosition(getX(), getY());
        
        SimulationWorld world = SimulationWorld.getInstance();
        world.getMultiChannelField().updateSource(cellGradientSource, oldX, oldY);
        
        if (energy <= 10) {
            destroy();
            return;
        }
        
        // Check if waste threshold reached
        if (stomachWaste.magnitude() >= wasteThreshold) {
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
            energy -= movementForce * 0.0005; // Was 0.001
        }
        
        // Try to eat nearby entities (food or cells)
        tryEatNearbyEntities();
        
        // Reproduction
        if (energy > Math.max(eatingDistance * 50, reproductionThreshold)) {
            reproduce();
        }
    }

    private void updateSpecialization() {
        double variance = 0;
        double mean = 0;
        
        for (int i = 0; i < ChemicalSignature.DIMENSIONS; i++) {
            mean += chemicalPreference.get(i);
        }
        mean /= ChemicalSignature.DIMENSIONS;
        
        for (int i = 0; i < ChemicalSignature.DIMENSIONS; i++) {
            double diff = chemicalPreference.get(i) - mean;
            variance += diff * diff;
        }
        variance /= ChemicalSignature.DIMENSIONS;
        
        specializationIndex = Math.min(1.0, Math.sqrt(variance) * 4.0);
    }

    private void reproduce() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        energy -= eatingDistance * 50;
        
        double offsetAngle = Math.random() * 2 * Math.PI;
        double offsetDist = (getSize() + 20) / 2.0;
        
        // Mutation rate varies with specialization (specialists mutate less)
        double adaptiveEvolveRate = evolveRate * (1.0 - specializationIndex * 0.5);
        
        Cell offspring = new Cell(
            getX() + Math.cos(offsetAngle) * offsetDist, 
            getY() + Math.sin(offsetAngle) * offsetDist,
            chemicalPreference.mutate(adaptiveEvolveRate)
        );
        offspring.setEnergy(eatingDistance * 50);
        
        // Mutate parameters
        offspring.setReproductionThreshold(this.reproductionThreshold * (a()+1));
        offspring.setMovementForce(this.movementForce * (a() + 1));
        offspring.setEatingDistance(this.eatingDistance * (a() + 1));
        offspring.setWasteThreshold(this.wasteThreshold * (a() + 1));
        
        // Mutate receptor sensitivity
        offspring.receptorSensitivity = this.receptorSensitivity.mutate(adaptiveEvolveRate);
        
        offspring.setVelocity(
            getVelocityX() + Math.cos(offsetAngle) * 5,
            getVelocityY() + Math.sin(offsetAngle) * 5
        );

        // Mutate color
        offspring.setColor(new Color(
            Math.clamp((int)(this.getColor().getRed() + (this.getColor().getRed() * (a()))), 0, 255),
            Math.clamp((int)(this.getColor().getGreen() + (this.getColor().getGreen() * (a()))), 0, 255),
            Math.clamp((int)(this.getColor().getBlue() + (this.getColor().getBlue() * (a()))), 0, 255)
        ));
        
        world.queueAddition(offspring);
    }

    private void expelWaste() {
        double wasteMag = stomachWaste.magnitude();
        
        if (wasteMag < 0.001) return;
        
        // Waste has same chemistry signature
        ChemicalSignature wasteChemistry = stomachWaste.normalize();
        
        // Create waste food particle
        double angle = Math.random() * 2 * Math.PI;
        double spawnDistance = (getSize() / 2.0) + 20;
        double spawnX = getX() + Math.cos(angle) * spawnDistance;
        double spawnY = getY() + Math.sin(angle) * spawnDistance;
        
        Food waste = new Food(spawnX, spawnY, wasteChemistry, wasteMag * 0.5);
        waste.setIsWaste(true);
        waste.setColor(new Color(100, 60, 40)); // Brown waste
        waste.setVelocity(Math.cos(angle) * 20, Math.sin(angle) * 20);
        waste.dampingFactor = 0.9;
        
        SimulationWorld.getInstance().queueAddition(waste);
        
        stomachWaste = ChemicalSignature.zeros();
        energy -= 5;
    }
    
    private Vector2D calculateGradientDirection() {
        SimulationWorld world = SimulationWorld.getInstance();
        MultiChannelGradientField multiField = world.getMultiChannelField();
        
        // Apply receptor gains to preference before sampling
        ChemicalSignature perceivedPreference = receptorSensitivity.applyGains(chemicalPreference);
        
        GradientSample sample = multiField.sampleWeighted(getX(), getY(), perceivedPreference);
        
        return new Vector2D(sample.directionX, sample.directionY);
    }
    
    /**
     * Calculate eating efficiency based on chemical compatibility.
     * RELAXED VERSION - more lenient thresholds.
     */
    private double calculateEatingEfficiency(ChemicalSignature foodChem) {
        double distance = chemicalPreference.distanceTo(foodChem);
        
        if (distance > TOXIC_THRESHOLD) {
            // Toxic - lose energy
            return -0.3; // Less toxic than before
        } else if (distance > EDIBLE_THRESHOLD) {
            // Low efficiency but still edible
            double normalized = (distance - EDIBLE_THRESHOLD) / (TOXIC_THRESHOLD - EDIBLE_THRESHOLD);
            return 0.1 * (1.0 - normalized); // 0% to 10% efficiency
        } else {
            // Good efficiency - linear with distance
            return 0.2 + 0.8 * (1.0 - (distance / EDIBLE_THRESHOLD)); // 20% to 100% efficiency
        }
    }
    
    /**
     * Try to eat nearby entities (food or other cells).
     */
    private void tryEatNearbyEntities() {
        SimulationWorld world = SimulationWorld.getInstance();
        GradientField cellGrid = world.getMultiChannelField().getGlobalGradientField();
        
        int gridX = (int)(getX() / cellGrid.getCellSize());
        int gridY = (int)(getY() / cellGrid.getCellSize());
        
        double eatRadiusSq = Math.pow(eatingDistance * 30, 2);
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int checkX = (gridX + dx + cellGrid.getGridWidth()) % cellGrid.getGridWidth();
                int checkY = (gridY + dy + cellGrid.getGridHeight()) % cellGrid.getGridHeight();
                
                List<PhysicsObj> entities = world.getEntitiesInSpatialCell(checkX, checkY);
                
                for (PhysicsObj obj : entities) {
                    if (obj == this) continue;
                    
                    Vector2D delta = world.getWrappedDelta(getX(), getY(), obj.getX(), obj.getY());
                    double distSq = delta.x * delta.x + delta.y * delta.y;
                    
                    if (distSq < eatRadiusSq) {
                        if (obj instanceof Food) {
                            tryEatFood((Food) obj);
                            return;
                        } else if (obj instanceof Cell) {
                            tryEatCell((Cell) obj);
                            return;
                        }
                    }
                }
            }
        }
    }
    
    private void tryEatFood(Food food) {
        double efficiency = calculateEatingEfficiency(food.getChemistry());
        
        // Always try to eat - even low efficiency food
        double energyGained = food.getNutritionalValue() * Math.max(efficiency, -0.3);
        energy += energyGained;
        
        // Accumulate waste (undigested portion) - only if positive efficiency
        if (efficiency > 0) {
            double wasteAmount = food.getNutritionalValue() * (1.0 - efficiency);
            if (wasteAmount > 0.001) {
                stomachWaste = stomachWaste.add(food.getChemistry().scale(wasteAmount));
            }
        }
        
        food.destroy();
        lastAte = SimulationWorld.getInstance().getFrameCount();
    }
    
    private void tryEatCell(Cell other) {
        // Can only eat cells that are significantly smaller
        if (other.getSize() >= this.getSize() * 0.7) return;
        
        double efficiency = calculateEatingEfficiency(other.chemicalPreference);
        
        if (efficiency < 0.1) {
            // Predation is too inefficient
            return;
        }
        
        // Predation successful
        double energyGained = other.energy * efficiency * 0.5; // Only get half
        energy += energyGained;
        
        // Accumulate waste
        double wasteAmount = other.energy * (1.0 - efficiency * 0.5);
        stomachWaste = stomachWaste.add(other.chemicalPreference.scale(wasteAmount));
        
        other.destroy();
        lastAte = SimulationWorld.getInstance().getFrameCount();
    }
    
    /**
     * Accumulate energy damage from nearby waste.
     */
    public void damageFromWaste(double damage) {
        wasteDamageAccumulated += damage;
    }

    private double a() {
        return MathFunctions.evolve(evolveRate);
    }
    
    // Getters
    public double getEnergy() { return energy; }
    public int getAge() { return age; }
    public ChemicalSignature getChemicalPreference() { return chemicalPreference; }
    public double getSpecializationIndex() { return specializationIndex; }
    public ChemicalSignature getStomachWaste() { return stomachWaste; }
    public double getWasteThreshold() { return wasteThreshold; }
    public GradientSource getCellGradientSource() { return cellGradientSource; }
    public ReceptorSensitivity getReceptorSensitivity() { return receptorSensitivity; }
    
    // Setters
    public void setEnergy(double energy) {
        this.energy = Math.max(0, energy);
    }
    
    public void setReproductionThreshold(double amount) {
        this.reproductionThreshold = Math.max(amount, 150.0);
        this.reproductionThreshold = Math.min(this.reproductionThreshold, 5000.0);
    }
    
    public void setMovementForce(double force) {
        this.movementForce = Math.max(1.0, force);
    }
    
    public void setWasteThreshold(double threshold) {
        this.wasteThreshold = Math.max(50.0, threshold);
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
}