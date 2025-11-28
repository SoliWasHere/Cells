//CELL.JAVA (DISCRETE FOOD TYPES - RADICALLY SIMPLIFIED)

package Cells;

import java.awt.Color;
import java.util.List;

/**
 * Cell with discrete food type preferences.
 * Instead of 8D chemistry, cells specialize in 1-3 specific food colors.
 */
public class Cell extends PhysicsObj {
    // Food type specializations (each value 0-1 represents efficiency eating that color)
    private double redEfficiency = 0.5;
    private double greenEfficiency = 0.5;
    private double blueEfficiency = 0.5;
    
    private double movementForce;
    private double senseRange; // How far can they detect food
    private double eatingDistance;
    private double evolveRate;
    private double reproductionThreshold;
    private double maxSpeed;

    public int lastAte = 0;
    
    private double energy;
    private int age;
    
    private GradientSource cellGradientSource;
    
    private int updateSkipCounter = 0;
    private static final int UPDATE_SKIP_FREQUENCY = 3;
    
    private Vector2D cachedMoveDirection = new Vector2D(0, 0);
    
    // Predator traits
    private boolean isPredator = false;
    private double predatorEfficiency = 0.3;
    
    public Cell(double x, double y, ChemicalSignature preference) {
        super(x, y);
        this.dampingFactor = 0.96;
        
        this.movementForce = 5.0 + Math.random() * 5.0;
        this.senseRange = 100.0 + Math.random() * 100.0;
        this.eatingDistance = 0.8 + Math.random() * 0.4; // 0.8-1.2
        this.maxSpeed = 100.0 + Math.random() * 100.0;
        this.evolveRate = Math.max(Math.random() * 8, 0.5);
        this.reproductionThreshold = 250;
        
        this.energy = 150.0;
        this.age = 0;
        
        // Random starting specialization
        randomizeEfficiencies();

        setSize((int) (eatingDistance * 25));
        setMass(eatingDistance * 2);
        
        this.updateSkipCounter = (int)(Math.random() * UPDATE_SKIP_FREQUENCY);
        
        // Dummy gradient source
        this.cellGradientSource = new GradientSource(x, y, 10.0, this, preference);
    }

    private void randomizeEfficiencies() {
        // Start with one strong preference
        double r = Math.random();
        if (r < 0.33) {
            redEfficiency = 0.7 + Math.random() * 0.3;
            greenEfficiency = Math.random() * 0.3;
            blueEfficiency = Math.random() * 0.3;
        } else if (r < 0.66) {
            redEfficiency = Math.random() * 0.3;
            greenEfficiency = 0.7 + Math.random() * 0.3;
            blueEfficiency = Math.random() * 0.3;
        } else {
            redEfficiency = Math.random() * 0.3;
            greenEfficiency = Math.random() * 0.3;
            blueEfficiency = 0.7 + Math.random() * 0.3;
        }
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
        
        // Drop gray food on death
        if (energy > 20) {
            Food food = new Food(this.getX(), this.getY(), ChemicalSignature.zeros(), energy * 0.3);
            food.setColor(new Color(120, 120, 120));
            food.setFoodType(3); // Gray = dead matter
            world.queueAddition(food);
        }
        
        super.destroy();
    }
    
    @Override
    public void onUpdate() {
        age++;
        
        // Simpler metabolism: just based on size
        double metabolism = Math.pow(eatingDistance, 1.2) * 0.3;
        energy -= metabolism;
        
        // Small cost for sensing far
        energy -= senseRange * 0.0005;
        
        // Update gradient source position
        double oldX = cellGradientSource.x;
        double oldY = cellGradientSource.y;
        cellGradientSource.updatePosition(getX(), getY());
        
        SimulationWorld world = SimulationWorld.getInstance();
        world.getMultiChannelField().updateSource(cellGradientSource, oldX, oldY);
        
        if (energy <= 0) {
            destroy();
            return;
        }
        
        // Find and move toward nearest compatible food
        updateSkipCounter++;
        if (updateSkipCounter >= UPDATE_SKIP_FREQUENCY) {
            updateSkipCounter = 0;
            cachedMoveDirection = findBestFoodDirection();
        }
        
        // Apply movement
        if (cachedMoveDirection.magnitudeSquared() > 0.01) {
            Vector2D dir = cachedMoveDirection.normalize();
            applyForce(dir.scale(movementForce));
            energy -= movementForce * 0.01;
        } else {
            // Random walk if no food found
            double angle = Math.random() * Math.PI * 2;
            applyForce(new Vector2D(Math.cos(angle), Math.sin(angle)).scale(movementForce * 0.3));
        }
        
        // Apply max speed limit
        double currentSpeed = getSpeed();
        if (currentSpeed > maxSpeed) {
            double scale = maxSpeed / currentSpeed;
            setVelocity(getVelocityX() * scale, getVelocityY() * scale);
        }
        
        // Try to eat
        tryEatNearbyEntities();
        
        // Reproduction
        if (energy > reproductionThreshold) {
            reproduce();
        }
    }

    /**
     * Find direction to nearest compatible food.
     */
    private Vector2D findBestFoodDirection() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        Food bestFood = null;
        double bestScore = -999;
        
        // Check spatial cells in sense range
        int gridSize = 50;
        int gridX = (int)(getX() / gridSize);
        int gridY = (int)(getY() / gridSize);
        int checkRadius = (int)(senseRange / gridSize) + 1;
        
        for (int dx = -checkRadius; dx <= checkRadius; dx++) {
            for (int dy = -checkRadius; dy <= checkRadius; dy++) {
                int cx = (gridX + dx + 200) % 200;
                int cy = (gridY + dy + 200) % 200;
                
                List<PhysicsObj> entities = world.getEntitiesInSpatialCell(cx, cy);
                
                for (PhysicsObj obj : entities) {
                    if (!(obj instanceof Food)) continue;
                    
                    Food food = (Food) obj;
                    if (food.isStatic()) continue; // Don't target barriers
                    
                    Vector2D delta = world.getWrappedDelta(getX(), getY(), food.getX(), food.getY());
                    double dist = delta.magnitude();
                    
                    if (dist > senseRange || dist < 1) continue;
                    
                    // Calculate compatibility with this food
                    double efficiency = calculateFoodEfficiency(food);
                    
                    if (efficiency < 0.2) continue; // Ignore incompatible food
                    
                    // Score: efficiency / distance (prefer close efficient food)
                    double score = efficiency / dist;
                    
                    if (score > bestScore) {
                        bestScore = score;
                        bestFood = food;
                    }
                }
            }
        }
        
        if (bestFood != null) {
            return world.getWrappedDelta(getX(), getY(), bestFood.getX(), bestFood.getY());
        }
        
        return new Vector2D(0, 0);
    }

    /**
     * Calculate how efficiently this cell can eat this food.
     */
    private double calculateFoodEfficiency(Food food) {
        Color c = food.getColor();
        
        // Normalize RGB to 0-1
        double r = c.getRed() / 255.0;
        double g = c.getGreen() / 255.0;
        double b = c.getBlue() / 255.0;
        
        // Weighted efficiency
        double efficiency = redEfficiency * r + greenEfficiency * g + blueEfficiency * b;
        
        // Normalize by color intensity (grayscale food is harder to digest)
        double intensity = r + g + b;
        if (intensity > 0.1) {
            efficiency = efficiency / intensity * 3.0;
        }
        System.out.println(efficiency);
        return Math.min(1.0, Math.max(0, efficiency));
    }

    private void reproduce() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        energy -= 100;
        
        double offsetAngle = Math.random() * 2 * Math.PI;
        double offsetDist = (getSize() + 15) / 2.0;
        
        Cell offspring = new Cell(
            getX() + Math.cos(offsetAngle) * offsetDist, 
            getY() + Math.sin(offsetAngle) * offsetDist,
            ChemicalSignature.random()
        );
        offspring.setEnergy(100);
        
        // Inherit and mutate traits
        offspring.redEfficiency = mutateValue(this.redEfficiency, 0.15);
        offspring.greenEfficiency = mutateValue(this.greenEfficiency, 0.15);
        offspring.blueEfficiency = mutateValue(this.blueEfficiency, 0.15);
        
        offspring.movementForce = mutateValue(this.movementForce, 0.1);
        offspring.senseRange = mutateValue(this.senseRange, 0.1);
        offspring.eatingDistance = mutateValue(this.eatingDistance, 0.08);
        offspring.maxSpeed = mutateValue(this.maxSpeed, 0.1);
        offspring.reproductionThreshold = mutateValue(this.reproductionThreshold, 0.1);
        
        // Rare predator mutation
        offspring.isPredator = this.isPredator;
        if (Math.random() < 0.02) {
            offspring.isPredator = !offspring.isPredator;
            offspring.predatorEfficiency = 0.3 + Math.random() * 0.3;
        } else if (isPredator) {
            offspring.predatorEfficiency = mutateValue(this.predatorEfficiency, 0.1);
        }
        
        // Clamp values
        offspring.redEfficiency = Math.max(0, Math.min(1, offspring.redEfficiency));
        offspring.greenEfficiency = Math.max(0, Math.min(1, offspring.greenEfficiency));
        offspring.blueEfficiency = Math.max(0, Math.min(1, offspring.blueEfficiency));
        offspring.movementForce = Math.max(2, Math.min(20, offspring.movementForce));
        offspring.senseRange = Math.max(50, Math.min(300, offspring.senseRange));
        offspring.maxSpeed = Math.max(50, Math.min(300, offspring.maxSpeed));
        offspring.reproductionThreshold = Math.max(150, Math.min(500, offspring.reproductionThreshold));
        
        offspring.setEatingDistance(offspring.eatingDistance);
        
        offspring.setVelocity(
            getVelocityX() + Math.cos(offsetAngle) * 3,
            getVelocityY() + Math.sin(offsetAngle) * 3
        );

        // Color based on specialization
        offspring.setColor(getSpecializationColor(offspring));
        
        world.queueAddition(offspring);
    }

    private double mutateValue(double value, double rate) {
        return value * (1.0 + MathFunctions.evolve(evolveRate) * rate);
    }

    private Color getSpecializationColor(Cell cell) {
        int r = (int)(cell.redEfficiency * 200 + 55);
        int g = (int)(cell.greenEfficiency * 200 + 55);
        int b = (int)(cell.blueEfficiency * 200 + 55);
        
        if (cell.isPredator) {
            r = Math.min(255, r + 80);
            g = Math.max(0, g - 50);
            b = Math.max(0, b - 50);
        }
        
        return new Color(r, g, b);
    }
    
    private void tryEatNearbyEntities() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        double eatRadiusSq = Math.pow(eatingDistance * 30, 2);
        
        int gridX = (int)(getX() / 50);
        int gridY = (int)(getY() / 50);
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int cx = (gridX + dx + 200) % 200;
                int cy = (gridY + dy + 200) % 200;
                
                List<PhysicsObj> entities = world.getEntitiesInSpatialCell(cx, cy);
                
                for (PhysicsObj obj : entities) {
                    if (obj == this) continue;


                    Vector2D delta = world.getWrappedDelta(getX(), getY(), obj.getX(), obj.getY());
                    System.out.println(delta);
                    double distSq = delta.magnitudeSquared();
                    
                    if (distSq < eatRadiusSq) {
                        System.out.println("CEHCK");
                        if (obj instanceof Food) {
                            Food food = (Food) obj;
                            if (food.isStatic()) continue; // Don't eat barriers
                            
                            double efficiency = calculateFoodEfficiency(food);
                            
                            if (efficiency > 0.1) { // Only eat if somewhat compatible
                                energy += food.getNutritionalValue() * efficiency;
                                food.destroy();
                                lastAte = world.getFrameCount();
                                return;
                            }
                        } else if (obj instanceof Cell && isPredator) {
                            Cell prey = (Cell) obj;
                            if (prey.getSize() < this.getSize() * 0.7) {
                                energy += prey.energy * predatorEfficiency * 0.4;
                                prey.destroy();
                                lastAte = world.getFrameCount();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Getters
    public double getEnergy() { return energy; }
    public int getAge() { return age; }
    public ChemicalSignature getChemicalPreference() { return ChemicalSignature.random(); }
    public double getSpecializationIndex() { 
        double total = redEfficiency + greenEfficiency + blueEfficiency;
        double max = Math.max(redEfficiency, Math.max(greenEfficiency, blueEfficiency));
        return total > 0 ? max / total : 0;
    }
    public ChemicalSignature getStomachWaste() { return ChemicalSignature.zeros(); }
    public double getWasteThreshold() { return 0; }
    public GradientSource getCellGradientSource() { return cellGradientSource; }
    public ReceptorSensitivity getReceptorSensitivity() { return new ReceptorSensitivity(); }
    public boolean isPredator() { return isPredator; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getRedEfficiency() { return redEfficiency; }
    public double getGreenEfficiency() { return greenEfficiency; }
    public double getBlueEfficiency() { return blueEfficiency; }
    public double getSenseRange() { return senseRange; }
    
    // Setters
    public void setEnergy(double energy) {
        this.energy = Math.max(0, energy);
    }
    
    public void setReproductionThreshold(double amount) {
        this.reproductionThreshold = Math.max(amount, 100.0);
    }
    
    public void setMovementForce(double force) {
        this.movementForce = Math.max(1.0, Math.min(20.0, force));
    }
    
    public void setWasteThreshold(double threshold) {
        // No-op
    }
    
    public void setMaxSpeed(double speed) {
        this.maxSpeed = Math.max(50.0, Math.min(300.0, speed));
    }
    
    public void setEatingDistance(double distance) {
        if (distance > 3.0 || distance < 0.3) {
            destroy();
            return;
        }
        
        this.eatingDistance = distance;
        
        int newSize = (int) (eatingDistance * 25);
        if (newSize > 75 || newSize < 8) {
            destroy();
            return;
        }
        
        super.setSize(newSize);
        setMass(eatingDistance * 2);
    }

    public void damageFromWaste(double damage) {
        // No-op
    }
}