//SIMULATIONWORLD.JAVA (WITH COLLISION DETECTION)

package Cells;

import java.util.*;

/**
 * Simulation world with gradient fields and collision detection.
 */
public class SimulationWorld {
    private static SimulationWorld instance;

    private Displayer displayer;
    
    private final GradientField foodGradientField;
    private final GradientField cellGradientField;
    
    private final Map<Integer, List<PhysicsObj>> entitySpatialHash;
    private final int cellSize;
    private final int gridWidth;
    private final int gridHeight;
    private final int totalWidth;
    private final int totalHeight;
    
    private final List<PhysicsObj> entities;
    private final List<PhysicsObj> pendingAdditions;
    private final List<PhysicsObj> pendingRemovals;
    private final Random random;
    
    private double timeStep;
    private double gravityConstant;
    private boolean paused;
    
    private int frameCount = 0;
    
    private List<PhysicsObj> gravitySources = new ArrayList<>();
    private int gravitySourcesUpdateFrame = -100;
    private static final int GRAVITY_UPDATE_INTERVAL = 100;
    private static final double GRAVITY_MASS_THRESHOLD = 100.0;
    
    // Collision detection
    private boolean collisionsEnabled = true;
    
    private SimulationWorld(int cellSize, int gridWidth, int gridHeight) {
        this.cellSize = cellSize;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.totalWidth = cellSize * gridWidth;
        this.totalHeight = cellSize * gridHeight;
        
        this.foodGradientField = new GradientField(cellSize, gridWidth, gridHeight, 300.0, 2.0);
        this.cellGradientField = new GradientField(cellSize, gridWidth, gridHeight, 150.0, 2.5);
        
        this.entitySpatialHash = new HashMap<>();
        this.entities = new ArrayList<>();
        this.pendingAdditions = new ArrayList<>();
        this.pendingRemovals = new ArrayList<>();
        this.random = new Random();
        
        this.timeStep = 0.1;
        this.gravityConstant = 10.0;
        this.paused = true;
    }
    
    public static void initialize(int cellSize, int gridWidth, int gridHeight) {
        if (instance != null) {
            throw new IllegalStateException("SimulationWorld already initialized");
        }
        instance = new SimulationWorld(cellSize, gridWidth, gridHeight);
    }
    
    public static SimulationWorld getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SimulationWorld not initialized");
        }
        return instance;
    }
    
    public void addEntity(PhysicsObj entity) {
        entities.add(entity);
        addToSpatialHash(entity);
        entity.onAddedToWorld();
    }
    
    public void queueAddition(PhysicsObj entity) {
        pendingAdditions.add(entity);
    }
    
    public void queueRemoval(PhysicsObj entity) {
        pendingRemovals.add(entity);
    }
    
    public void processPendingChanges() {
        for (PhysicsObj entity : pendingAdditions) {
            addEntity(entity);
        }
        pendingAdditions.clear();
        
        for (PhysicsObj entity : pendingRemovals) {
            removeFromSpatialHash(entity);
            entities.remove(entity);
            entity.onRemovedFromWorld();
        }
        pendingRemovals.clear();
    }
    
    private void addToSpatialHash(PhysicsObj entity) {
        int hash = getSpatialHash(entity.getX(), entity.getY());
        entitySpatialHash.computeIfAbsent(hash, k -> new ArrayList<>()).add(entity);
    }
    
    private void removeFromSpatialHash(PhysicsObj entity) {
        int hash = getSpatialHash(entity.getX(), entity.getY());
        List<PhysicsObj> cell = entitySpatialHash.get(hash);
        if (cell != null) {
            cell.remove(entity);
            if (cell.isEmpty()) {
                entitySpatialHash.remove(hash);
            }
        } else {
            throw new IllegalCallerException("NO SHOT");
        }
    }
    
    private void updateSpatialHash(PhysicsObj entity, double oldX, double oldY) {
        int oldHash = getSpatialHash(oldX, oldY);
        int newHash = getSpatialHash(entity.getX(), entity.getY());
        
        if (oldHash != newHash) {
            List<PhysicsObj> oldCell = entitySpatialHash.get(oldHash);
            if (oldCell != null) {
                oldCell.remove(entity);
                if (oldCell.isEmpty()) {
                    entitySpatialHash.remove(oldHash);
                }
            }
            
            entitySpatialHash.computeIfAbsent(newHash, k -> new ArrayList<>()).add(entity);
        }
    }
    
    private int getSpatialHash(double x, double y) {
        int gridX = ((int)(x / cellSize) % gridWidth + gridWidth) % gridWidth;
        int gridY = ((int)(y / cellSize) % gridHeight + gridHeight) % gridHeight;
        return gridX + gridY * gridWidth;
    }
    
    public List<PhysicsObj> getEntitiesInSpatialCell(int gridX, int gridY) {
        int hash = gridX + gridY * gridWidth;
        List<PhysicsObj> cell = entitySpatialHash.get(hash);
        return cell != null ? new ArrayList<>(cell) : new ArrayList<>();
    }
    
    public void update() {
        if (paused) return;
        
        frameCount++;
        
        // Update gravity sources periodically
        if (frameCount - gravitySourcesUpdateFrame > GRAVITY_UPDATE_INTERVAL) {
            updateGravitySources();
            gravitySourcesUpdateFrame = frameCount;
        }
        
        // Apply gravity
        if (!gravitySources.isEmpty()) {
            for (PhysicsObj entity : entities) {
                if (entity.isStatic()) continue;
                
                for (PhysicsObj source : gravitySources) {
                    if (source != entity) {
                        double dx = Math.abs(entity.getX() - source.getX());
                        double dy = Math.abs(entity.getY() - source.getY());
                        
                        if (dx < 500 && dy < 500) {
                            entity.applyGravityFrom(source);
                        }
                    }
                }
            }
        }
        
        // Update all entities
        for (PhysicsObj entity : entities) {
            double oldX = entity.getX();
            double oldY = entity.getY();
            
            entity.update();
            
            updateSpatialHash(entity, oldX, oldY);
        }
        
        // Handle collisions
        if (collisionsEnabled) {
            handleCollisions();
        }
    }
    
    /**
     * Handle collisions between all entities using spatial hashing.
     * Uses iterative resolution to ensure no overlapping.
     */
    private void handleCollisions() {
        // Perform multiple iterations to resolve all collisions
        final int MAX_ITERATIONS = 3;
        
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            Set<String> checkedPairs = new HashSet<>();
            boolean hadCollision = false;
            
            for (PhysicsObj entity : entities) {
                if (entity.isStatic()) continue;
                
                int gridX = (int)(entity.getX() / cellSize);
                int gridY = (int)(entity.getY() / cellSize);
                
                // Check this cell and adjacent cells (3x3 grid)
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int checkX = (gridX + dx + gridWidth) % gridWidth;
                        int checkY = (gridY + dy + gridHeight) % gridHeight;
                        
                        List<PhysicsObj> nearbyEntities = getEntitiesInSpatialCell(checkX, checkY);
                        
                        for (PhysicsObj other : nearbyEntities) {
                            if (entity == other) continue;
                            
                            // Create unique pair identifier (sorted to avoid duplicates)
                            int hash1 = System.identityHashCode(entity);
                            int hash2 = System.identityHashCode(other);
                            String pairKey = hash1 < hash2 
                                ? hash1 + "," + hash2
                                : hash2 + "," + hash1;
                            
                            if (checkedPairs.contains(pairKey)) continue;
                            checkedPairs.add(pairKey);
                            
                            // Check collision
                            Vector2D delta = getWrappedDelta(entity.getX(), entity.getY(), other.getX(), other.getY());
                            double distance = delta.magnitude();
                            double minDistance = (entity.getSize() + other.getSize()) / 2.0;
                            
                            if (distance < minDistance) {
                                entity.handleCollision(other);
                                hadCollision = true;
                            }
                        }
                    }
                }
            }
            
            // If no collisions this iteration, we're done
            if (!hadCollision) break;
        }
    }
    
    private void updateGravitySources() {
        gravitySources.clear();
        for (PhysicsObj entity : entities) {
            if (entity.getMass() >= GRAVITY_MASS_THRESHOLD) {
                gravitySources.add(entity);
            }
        }
    }
    
    public void clear() {
        entities.clear();
        pendingAdditions.clear();
        pendingRemovals.clear();
        gravitySources.clear();
        entitySpatialHash.clear();
        foodGradientField.clear();
        cellGradientField.clear();
    }
    
    // Getters
    public GradientField getFoodGradientField() { return foodGradientField; }
    public GradientField getCellGradientField() { return cellGradientField; }
    public List<PhysicsObj> getEntities() { return new ArrayList<>(entities); }
    public Random getRandom() { return random; }
    public double getTimeStep() { return timeStep; }
    public double getGravityConstant() { return gravityConstant; }
    public boolean isPaused() { return paused; }
    public int getEntityCount() { return entities.size(); }
    public int getFrameCount() { return frameCount; }
    public int getTotalWidth() { return totalWidth; }
    public int getTotalHeight() { return totalHeight; }
    public boolean areCollisionsEnabled() { return collisionsEnabled; }
    
    // Setters
    public void setTimeStep(double timeStep) {
        this.timeStep = Math.max(0.01, Math.min(10.0, timeStep));
    }
    
    public void setGravityConstant(double gravityConstant) {
        this.gravityConstant = Math.max(0.0, gravityConstant);
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    public void setDisplayer(Displayer displayer) {
        this.displayer = displayer;
    }

    public Displayer getDisplayer() {
        return displayer;
    }
    
    public void setCollisionsEnabled(boolean enabled) {
        this.collisionsEnabled = enabled;
    }
    
    // Utility methods
    public double getWrappedDistance(double x1, double y1, double x2, double y2) {
        double dx = Math.abs(x2 - x1);
        double dy = Math.abs(y2 - y1);
        
        if (dx > totalWidth / 2.0) dx = totalWidth - dx;
        if (dy > totalHeight / 2.0) dy = totalHeight - dy;
        
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public Vector2D getWrappedDelta(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        
        if (Math.abs(dx) > totalWidth / 2.0) {
            dx = dx > 0 ? dx - totalWidth : dx + totalWidth;
        }
        if (Math.abs(dy) > totalHeight / 2.0) {
            dy = dy > 0 ? dy - totalHeight : dy + totalHeight;
        }
        
        return new Vector2D(dx, dy);
    }
    
    public double wrapX(double x) {
        while (x < 0) x += totalWidth;
        while (x >= totalWidth) x -= totalWidth;
        return x;
    }
    
    public double wrapY(double y) {
        while (y < 0) y += totalHeight;
        while (y >= totalHeight) y -= totalHeight;
        return y;
    }
}