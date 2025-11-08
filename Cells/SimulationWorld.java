package Cells;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Central simulation state container.
 * Provides global access to world parameters and state without constant parameter passing.
 */
public class SimulationWorld {
    private static SimulationWorld instance;

    private Displayer displayer;
    
    private final Matrix matrix;
    private final List<PhysicsObj> entities;
    private final List<PhysicsObj> pendingAdditions;
    private final List<PhysicsObj> pendingRemovals;
    private final Random random;
    
    // Simulation parameters
    private double timeStep;
    private double gravityConstant;
    private boolean paused;
    
    private SimulationWorld(int cellSize, int gridWidth, int gridHeight) {
        this.matrix = new Matrix(cellSize, gridWidth, gridHeight);
        this.entities = new ArrayList<>();
        this.pendingAdditions = new ArrayList<>();
        this.pendingRemovals = new ArrayList<>();
        this.random = new Random();
        
        // Default parameters
        this.timeStep = 0.1;
        this.gravityConstant = 10.0;
        this.paused = false;
    }
    
    /**
     * Initialize the simulation world singleton.
     */
    public static void initialize(int cellSize, int gridWidth, int gridHeight) {
        if (instance != null) {
            throw new IllegalStateException("SimulationWorld already initialized");
        }
        instance = new SimulationWorld(cellSize, gridWidth, gridHeight);
    }
    
    /**
     * Get the singleton instance.
     */
    public static SimulationWorld getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SimulationWorld not initialized. Call initialize() first.");
        }
        return instance;
    }
    
    // === Entity Management ===
    
    /**
     * Add an entity to the world immediately.
     */
    public void addEntity(PhysicsObj entity) {
        matrix.insertCell(entity);
        entities.add(entity);
        entity.onAddedToWorld();
    }
    
    /**
     * Queue an entity for addition (safe during iteration).
     */
    public void queueAddition(PhysicsObj entity) {
        pendingAdditions.add(entity);
    }
    
    /**
     * Queue an entity for removal (safe during iteration).
     */
    public void queueRemoval(PhysicsObj entity) {
        pendingRemovals.add(entity);
    }
    
    /**
     * Process all pending additions and removals.
     */
    public void processPendingChanges() {
        // Add new entities
        for (PhysicsObj entity : pendingAdditions) {
            addEntity(entity);
        }
        pendingAdditions.clear();
        
        // Remove entities
        for (PhysicsObj entity : pendingRemovals) {
            matrix.removeCell(entity);
            entities.remove(entity);
            entity.onRemovedFromWorld();
        }
        pendingRemovals.clear();
    }
    
    /**
     * Update all entities in the world.
     */
    public void update() {
        if (paused) return;
        
        // Create snapshot to avoid concurrent modification
        List<PhysicsObj> snapshot = new ArrayList<>(entities);
        
        // Apply inter-entity forces (gravity)
        for (PhysicsObj entity : snapshot) {
            if (entity.isStatic()) continue;
            
            // Only calculate gravity from large masses
            List<PhysicsObj> nearby = entity.getCellsInRadius(200);
            for (PhysicsObj other : nearby) {
                if (other != entity && other.getMass() > 10) {
                    entity.applyGravityFrom(other);
                }
            }
        }
        
        // Update all entities
        for (PhysicsObj entity : snapshot) {
            entity.update();
            matrix.updateCellGrid(entity);
        }
    }
    
    /**
     * Clear all entities from the world.
     */
    public void clear() {
        for (PhysicsObj entity : new ArrayList<>(entities)) {
            matrix.removeCell(entity);
        }
        entities.clear();
        pendingAdditions.clear();
        pendingRemovals.clear();
    }
    
    // === Getters ===
    
    public Matrix getMatrix() { return matrix; }
    public List<PhysicsObj> getEntities() { return new ArrayList<>(entities); }
    public Random getRandom() { return random; }
    public double getTimeStep() { return timeStep; }
    public double getGravityConstant() { return gravityConstant; }
    public boolean isPaused() { return paused; }
    public int getEntityCount() { return entities.size(); }
    
    // === Setters ===
    
    public void setTimeStep(double timeStep) {
        this.timeStep = Math.max(0.01, Math.min(10.0, timeStep));
    }
    
    public void setGravityConstant(double gravityConstant) {
        this.gravityConstant = Math.max(0.0, gravityConstant);
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    // === Utility Methods ===
    
    /**
     * Calculate wrapped distance accounting for toroidal topology.
     */
    public double getWrappedDistance(double x1, double y1, double x2, double y2) {
        double dx = Math.abs(x2 - x1);
        double dy = Math.abs(y2 - y1);
        
        int width = matrix.getTotalWidth();
        int height = matrix.getTotalHeight();
        
        if (dx > width / 2.0) dx = width - dx;
        if (dy > height / 2.0) dy = height - dy;
        
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculate wrapped delta (shortest path considering wrapping).
     */
    public Vector2D getWrappedDelta(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        
        int width = matrix.getTotalWidth();
        int height = matrix.getTotalHeight();
        
        if (Math.abs(dx) > width / 2.0) {
            dx = dx > 0 ? dx - width : dx + width;
        }
        if (Math.abs(dy) > height / 2.0) {
            dy = dy > 0 ? dy - height : dy + height;
        }
        
        return new Vector2D(dx, dy);
    }
    
    /**
     * Wrap coordinate to stay within world bounds.
     */
    public double wrapX(double x) {
        int width = matrix.getTotalWidth();
        while (x < 0) x += width;
        while (x >= width) x -= width;
        return x;
    }
    
    public double wrapY(double y) {
        int height = matrix.getTotalHeight();
        while (y < 0) y += height;
        while (y >= height) y -= height;
        return y;
    }

    public void setDisplayer(Displayer displayer) {
        this.displayer = displayer;
    }

    public Displayer getDisplayer() {
        return displayer;
    }
}