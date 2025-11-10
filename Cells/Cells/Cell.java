//CELL.JAVA (WITH MULTI-CHANNEL RECEPTORS AND WASTE DAMAGE)

package Cells;

import java.awt.Color;
import java.util.List;

/**
 * Cell with multi-channel receptor system for specialized gradient sensing.
 * Waste particles drain energy from nearby cells.
 */
public class Cell extends PhysicsObj {
    private double movementForce;
    private double eatingDistance;
    private double evolveRate;
    private double reproductionThreshold;

    public int lastAte = 0;
    
    private double energy;
    private int age;
    
    // Multi-channel receptor system
    private ReceptorProfile receptorProfile;
    
    // Stomach system for waste
    private double stomachWasteX;
    private double stomachWasteY;
    private double wasteThreshold;
    
    private GradientSource cellGradientSource;
    
    private int updateSkipCounter = 0;
    private static final int UPDATE_SKIP_FREQUENCY = 2;
    
    private Vector2D cachedGradientDirection = new Vector2D(0, 0);
    
    // Waste damage tracking
    private double wasteDamageAccumulated = 0;
    
    public Cell(double x, double y) {
        super(x, y);
        this.dampingFactor = 0.95;
        
        this.movementForce = 10.0;
        this.eatingDistance = 1.0;
        this.evolveRate = Math.max(Math.random() * 10, 0.1);
        this.reproductionThreshold = eatingDistance * 100;
        
        this.energy = 100.0;
        this.age = 0;
        
        // Initialize receptor profile
        this.receptorProfile = new ReceptorProfile();
        
        // Initialize stomach
        this.stomachWasteX = 0;
        this.stomachWasteY = 0;
        this.wasteThreshold = 200.0;

        setSize((int) (eatingDistance * 20));
        setMass(eatingDistance);
        
        this.updateSkipCounter = (int)(Math.random() * UPDATE_SKIP_FREQUENCY);
        
        this.cellGradientSource = new GradientSource(x, y, 50.0, this);
    }

    @Override
    protected void onAddedToWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        // Register with cell repulsion channel (channel 0)
        world.getMultiChannelField().getChannel(0).addSource(cellGradientSource);
    }
    
    @Override
    protected void onRemovedFromWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getMultiChannelField().getChannel(0).removeSource(cellGradientSource);
    }

    @Override
    public void destroy() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        // Drop food with remaining energy
        if (energy > 10) {
            double foodEnergy = Math.min(energy * 0.5, 200.0);
            Food food = new Food(this.getX(), this.getY());
            food.setNutritionalValue(foodEnergy);
            
            // Death food has inverse receptor profile
            double[] deathFoodId = calculateInverseFoodId();
            food.setFoodId(deathFoodId[0], deathFoodId[1]);
            
            world.queueAddition(food);
        }
        
        super.destroy();
    }
    
    /**
     * Calculate food ID that is opposite to this cell's receptor preferences.
     */
    private double[] calculateInverseFoodId() {
        // Find dominant receptor channels
        double maxSensitivity = 0;
        int dominantChannel = 0;
        
        for (int i = 0; i < receptorProfile.getNumChannels(); i++) {
            if (receptorProfile.getSensitivity(i) > maxSensitivity) {
                maxSensitivity = receptorProfile.getSensitivity(i);
                dominantChannel = i;
            }
        }
        
        // Create food ID opposite to dominant channel
        double angle = dominantChannel * Math.PI / 4.0 + Math.PI; // Opposite angle
        double foodIdX = (Math.cos(angle) + 1.0) / 2.0;
        double foodIdY = (Math.sin(angle) + 1.0) / 2.0;
        
        return new double[]{foodIdX, foodIdY};
    }
    
    @Override
    public void onUpdate() {
        age++;
        
        // Base metabolism cost
        energy -= Math.pow(eatingDistance, 1/2.0) / 16.0;
        
        // Receptor maintenance cost
        energy -= receptorProfile.getEnergyCost();
        
        // Waste damage from nearby waste particles
        energy -= wasteDamageAccumulated;
        wasteDamageAccumulated = 0; // Reset for next frame
        
        // Update gradient source position
        double oldX = cellGradientSource.x;
        double oldY = cellGradientSource.y;
        cellGradientSource.updatePosition(getX(), getY());
        
        SimulationWorld world = SimulationWorld.getInstance();
        world.getMultiChannelField().getChannel(0).updateSource(cellGradientSource, oldX, oldY);
        
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
            energy -= movementForce * 0.001;
        }
        
        // Try to eat nearby food
        tryEatNearbyFood();
        
        // Reproduction
        if (energy > Math.max(eatingDistance * 50, reproductionThreshold)) {
            reproduce();
        }
    }

    /**
     * Reproduce and mutate receptor profile.
     */
    private void reproduce() {
        this.evolveRate = Math.max(Math.random(), this.getAge()/1000);
        SimulationWorld world = SimulationWorld.getInstance();
        
        energy -= eatingDistance * 50;
        
        double offsetAngle = Math.random() * 2 * Math.PI;
        double offsetDist = (getSize() + 20) / 2.0;
        
        Cell offspring = new Cell(
            getX() + Math.cos(offsetAngle) * offsetDist, 
            getY() + Math.sin(offsetAngle) * offsetDist
        );
        offspring.setEnergy(eatingDistance * 50);
        
        // Mutate parameters
        offspring.setReproductionThreshold(this.reproductionThreshold * (a()+1));
        offspring.setMovementForce(this.movementForce * (a() + 1));
        offspring.setEatingDistance(this.eatingDistance * (a() + 1));
        offspring.setWasteThreshold(this.wasteThreshold * (a() + 1));
        
        // Copy and mutate receptor profile
        offspring.receptorProfile = new ReceptorProfile(this.receptorProfile);
        offspring.receptorProfile.mutate(evolveRate);
        
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

    /**
     * Expel accumulated waste as harmful waste particles.
     */
    private void expelWaste() {
        double wasteMagnitude = Math.sqrt(stomachWasteX * stomachWasteX + stomachWasteY * stomachWasteY);
        
        if (wasteMagnitude < 0.001) return;
        
        // Normalize waste vector
        double wasteIdX = (stomachWasteX / wasteMagnitude + 1.0) / 2.0;
        double wasteIdY = (stomachWasteY / wasteMagnitude + 1.0) / 2.0;
        
        // Create waste food particle AWAY from cell
        double angle = Math.random() * 2 * Math.PI;
        double spawnDistance = (getSize() / 2.0) + 20;
        double spawnX = getX() + Math.cos(angle) * spawnDistance;
        double spawnY = getY() + Math.sin(angle) * spawnDistance;
        
        Food waste = new Food(spawnX, spawnY);
        waste.setNutritionalValue(wasteMagnitude * 0.5);
        waste.setFoodId(wasteIdX, wasteIdY);
        waste.setIsWaste(true); // Mark as waste
        waste.setColor(Color.red);
        
        waste.setVelocity(Math.cos(angle) * 20, Math.sin(angle) * 20);
        waste.dampingFactor = 0.9;
        
        SimulationWorld.getInstance().queueAddition(waste);
        
        stomachWasteX = 0;
        stomachWasteY = 0;
        
        energy -= 5;
    }
    
/**
 * Calculate movement direction using multi-channel receptor system.
 */
private Vector2D calculateGradientDirection() {
    SimulationWorld world = SimulationWorld.getInstance();
    MultiChannelGradientField multiField = world.getMultiChannelField();
    
    // Sample all channels
    GradientSample[] samples = multiField.sampleAll(getX(), getY());
    
    // Cell repulsion (channel 0) - always avoid other cells
    Vector2D avoidCells = new Vector2D(
        -samples[0].directionX * samples[0].strength * 0.5,
        -samples[0].directionY * samples[0].strength * 0.5
    );
    
    // Food attraction (channels 1-7) - extract food samples only
    GradientSample[] foodSamples = new GradientSample[7];
    System.arraycopy(samples, 1, foodSamples, 0, 7);
    
    Vector2D attractFood = receptorProfile.calculateMovementDirection(foodSamples);
    
    return avoidCells.add(attractFood);
}
    
    /**
     * Calculate food compatibility based on receptor profile.
     */
    private double calculateFoodEfficiency(Food food) {
        MultiChannelGradientField multiField = SimulationWorld.getInstance().getMultiChannelField();
        
        // Get food's emission pattern (7 values for food channels 1-7)
        double[] foodChannels = multiField.foodIdToChannelStrengths(food.getFoodIdX(), food.getFoodIdY());
        
        // Calculate dot product between receptor profile and food emission
        double compatibility = 0;
        double maxPossible = 0;
        
        for (int i = 0; i < receptorProfile.getNumChannels(); i++) {
            compatibility += receptorProfile.getSensitivity(i) * foodChannels[i];
            maxPossible += receptorProfile.getSensitivity(i);
        }
        
        if (maxPossible > 0.001) {
            return compatibility / maxPossible;
        }
        
        return 0.1; // Minimum efficiency
    }
    
    /**
     * Try to eat nearby food (only Food objects, never Cells).
     */
    private void tryEatNearbyFood() {
        SimulationWorld world = SimulationWorld.getInstance();
        GradientField cellGrid = world.getMultiChannelField().getChannel(0).getGradientField();
        
        int gridX = (int)(getX() / cellGrid.getCellSize());
        int gridY = (int)(getY() / cellGrid.getCellSize());
        
        double eatRadiusSq = Math.pow(eatingDistance * 30, 2);
        final double MIN_EFFICIENCY_TO_EAT = 0.1;
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int checkX = (gridX + dx + cellGrid.getGridWidth()) % cellGrid.getGridWidth();
                int checkY = (gridY + dy + cellGrid.getGridHeight()) % cellGrid.getGridHeight();
                
                List<PhysicsObj> entities = world.getEntitiesInSpatialCell(checkX, checkY);
                
                for (PhysicsObj obj : entities) {
                    if (!(obj instanceof Food)) continue;
                    
                    Vector2D delta = world.getWrappedDelta(getX(), getY(), obj.getX(), obj.getY());
                    double distSq = delta.x * delta.x + delta.y * delta.y;
                    
                    if (distSq < eatRadiusSq) {
                        Food food = (Food) obj;
                        
                        double efficiency = calculateFoodEfficiency(food);
                        
                        if (efficiency < MIN_EFFICIENCY_TO_EAT) {
                            continue;
                        }
                        
                        double energyGained = food.getNutritionalValue() * efficiency;
                        energy += energyGained;
                        
                        // Accumulate waste
                        double wasteAmount = food.getNutritionalValue() * (1.0 - efficiency);
                        if (wasteAmount > 0.001) {
                            double foodDirX = (food.getFoodIdX() * 2.0) - 1.0;
                            double foodDirY = (food.getFoodIdY() * 2.0) - 1.0;
                            
                            stomachWasteX += -foodDirX * wasteAmount;
                            stomachWasteY += -foodDirY * wasteAmount;
                        }
                        
                        food.destroy();
                        lastAte = world.getFrameCount();
                        return;
                    }
                }
            }
        }
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
    
    // Setters
    public void setEnergy(double energy) {
        this.energy = Math.max(0, energy);
    }
    
    public void setAge(int age) {
        this.age = Math.max(0, age);
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
    public double getStomachWasteX() { return stomachWasteX; }
    public double getStomachWasteY() { return stomachWasteY; }
    public double getWasteThreshold() { return wasteThreshold; }
    public ReceptorProfile getReceptorProfile() { return receptorProfile; }
    
    @Override
    public String toString() {
        return String.format("Cell[pos=(%.1f, %.1f), energy=%.1f, age=%d, spec=%.2f]",
            getX(), getY(), energy, age, receptorProfile.getSpecializationIndex());
    }
}