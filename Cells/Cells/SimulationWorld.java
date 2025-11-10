//SIMULATIONWORLD.JAVA (WITH MULTI-CHANNEL SYSTEM AND AUTO-RESET)

package Cells;

import java.util.*;

/**
 * Simulation world with multi-channel gradient system and auto-reset.
 */
public class SimulationWorld {
    private static SimulationWorld instance;

    private Displayer displayer;
    
    private final MultiChannelGradientField multiChannelField;
    
    private final Map<Integer, List<PhysicsObj>> entitySpatialHash;
    private final int cellSize;
    private final int gridWidth;
    private final int gridHeight;
    private final int totalWidth;
    private final int totalHeight;
    
    private final List<PhysicsObj> entities;
    private final Set<PhysicsObj> pendingAdditions;
    private final Set<PhysicsObj> pendingRemovals;
    private final Random random;
    
    private double timeStep;
    private boolean paused;
    
    private int frameCount = 0;
    
    private boolean collisionsEnabled = true;
    
    // Auto-reset tracking
    private int framesWithoutCells = 0;
    private static final int RESET_AFTER_FRAMES = 10; // 5 seconds at 60 FPS
    
    private SimulationWorld(int cellSize, int gridWidth, int gridHeight) {
        this.cellSize = cellSize;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.totalWidth = cellSize * gridWidth;
        this.totalHeight = cellSize * gridHeight;
        
        this.multiChannelField = new MultiChannelGradientField(cellSize, gridWidth, gridHeight);
        
        this.entitySpatialHash = new HashMap<>();
        this.entities = new ArrayList<>();
        this.pendingAdditions = new HashSet<>();
        this.pendingRemovals = new HashSet<>();
        this.random = new Random();
        
        this.timeStep = 0.1;
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
            if (entities.contains(entity)) {
                removeFromSpatialHash(entity);
                entities.remove(entity);
                entity.onRemovedFromWorld();
            }
        }
        pendingRemovals.clear();
    }
    
    private void addToSpatialHash(PhysicsObj entity) {
        int hash = getSpatialHash(entity.getX(), entity.getY());
        entity.setSpatialHashKey(hash);
        entitySpatialHash.computeIfAbsent(hash, k -> new ArrayList<>()).add(entity);
    }
    
    private void removeFromSpatialHash(PhysicsObj entity) {
        Integer hash = entity.getSpatialHashKey();
        if (hash == null) return;
        
        List<PhysicsObj> cell = entitySpatialHash.get(hash);
        if (cell != null) {
            cell.remove(entity);
            if (cell.isEmpty()) {
                entitySpatialHash.remove(hash);
            }
        }
        entity.setSpatialHashKey(null);
    }
    
    private void updateSpatialHash(PhysicsObj entity, double oldX, double oldY) {
        int oldHash = entity.getSpatialHashKey() != null 
            ? entity.getSpatialHashKey() 
            : getSpatialHash(oldX, oldY);
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
            entity.setSpatialHashKey(newHash);
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
        
        // Check for cell extinction
        if (countCells() == 0) {
            framesWithoutCells++;
            if (framesWithoutCells >= RESET_AFTER_FRAMES) {
                System.out.println("No cells remaining. Resetting world...");
                resetWorld();
                return;
            }
        } else {
            framesWithoutCells = 0;
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
     * Count number of Cell entities in the world.
     */
    private int countCells() {
        int count = 0;
        for (PhysicsObj entity : entities) {
            if (entity instanceof Cell) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Reset the entire world to initial conditions.
     */
    private void resetWorld() {
        // Clear everything
        clear();
        
        // Reset frame counter
        frameCount = 0;
        framesWithoutCells = 0;
        
        // Recreate initial scene (delegate to Main)
        Main.createInitialScene();
        
        System.out.println("World reset complete!");
    }
    
    private void handleCollisions() {
        final int MAX_ITERATIONS = 3;
        
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            Set<String> checkedPairs = new HashSet<>();
            boolean hadCollision = false;
            
            for (PhysicsObj entity : entities) {
                if (entity.isStatic()) continue;
                
                int gridX = (int)(entity.getX() / cellSize);
                int gridY = (int)(entity.getY() / cellSize);
                
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int checkX = (gridX + dx + gridWidth) % gridWidth;
                        int checkY = (gridY + dy + gridHeight) % gridHeight;
                        
                        List<PhysicsObj> nearbyEntities = getEntitiesInSpatialCell(checkX, checkY);
                        
                        for (PhysicsObj other : nearbyEntities) {
                            if (entity == other) continue;
                            
                            int hash1 = System.identityHashCode(entity);
                            int hash2 = System.identityHashCode(other);
                            String pairKey = hash1 < hash2 
                                ? hash1 + "," + hash2
                                : hash2 + "," + hash1;
                            
                            if (checkedPairs.contains(pairKey)) continue;
                            checkedPairs.add(pairKey);
                            
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
            
            if (!hadCollision) break;
        }
    }
    
    public void clear() {
        entities.clear();
        pendingAdditions.clear();
        pendingRemovals.clear();
        entitySpatialHash.clear();
        multiChannelField.clear();
    }
    
    // Getters
    public MultiChannelGradientField getMultiChannelField() { return multiChannelField; }
    public List<PhysicsObj> getEntities() { return new ArrayList<>(entities); }
    public Random getRandom() { return random; }
    public double getTimeStep() { return timeStep; }
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