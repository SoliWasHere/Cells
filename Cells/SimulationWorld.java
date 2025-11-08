//SIMULATIONWORLD.JAVA (ULTRA-OPTIMIZED)

package Cells;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ULTRA-OPTIMIZED: Minimal gravity calculations.
 */
public class SimulationWorld {
    private static SimulationWorld instance;

    private Displayer displayer;
    
    private final Matrix matrix;
    private final List<PhysicsObj> entities;
    private final List<PhysicsObj> pendingAdditions;
    private final List<PhysicsObj> pendingRemovals;
    private final Random random;
    
    private double timeStep;
    private double gravityConstant;
    private boolean paused;
    
    private int frameCount = 0;
    
    // OPTIMIZATION: Cache static gravity sources
    private List<PhysicsObj> gravitySources = new ArrayList<>();
    private int gravitySourcesUpdateFrame = -100;
    private static final int GRAVITY_UPDATE_INTERVAL = 100; // Update gravity list every 100 frames
    
    private static final double GRAVITY_MASS_THRESHOLD = 100.0;
    
    private SimulationWorld(int cellSize, int gridWidth, int gridHeight) {
        this.matrix = new Matrix(cellSize, gridWidth, gridHeight);
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
            throw new IllegalStateException("SimulationWorld not initialized. Call initialize() first.");
        }
        return instance;
    }
    
    public void addEntity(PhysicsObj entity) {
        matrix.insertCell(entity);
        entities.add(entity);
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
            matrix.removeCell(entity);
            entities.remove(entity);
            entity.onRemovedFromWorld();
        }
        pendingRemovals.clear();
    }
    
    /**
     * ULTRA-OPTIMIZED: Update gravity sources rarely, process entities efficiently.
     */
    public void update() {
        if (paused) return;
        
        frameCount++;
        
        // OPTIMIZATION: Update gravity sources list infrequently
        if (frameCount - gravitySourcesUpdateFrame > GRAVITY_UPDATE_INTERVAL) {
            updateGravitySources();
            gravitySourcesUpdateFrame = frameCount;
        }
        
        // OPTIMIZATION: Apply gravity only if there are gravity sources
        if (!gravitySources.isEmpty()) {
            for (PhysicsObj entity : entities) {
                if (entity.isStatic()) continue;
                
                for (PhysicsObj source : gravitySources) {
                    if (source != entity) {
                        // OPTIMIZATION: Quick distance check before expensive calculation
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
            entity.update();
            matrix.updateCellGrid(entity);
        }
    }
    
    /**
     * OPTIMIZATION: Cache list of massive objects that create gravity.
     */
    private void updateGravitySources() {
        gravitySources.clear();
        for (PhysicsObj entity : entities) {
            if (entity.getMass() >= GRAVITY_MASS_THRESHOLD) {
                gravitySources.add(entity);
            }
        }
    }
    
    public void clear() {
        for (PhysicsObj entity : new ArrayList<>(entities)) {
            matrix.removeCell(entity);
        }
        entities.clear();
        pendingAdditions.clear();
        pendingRemovals.clear();
        gravitySources.clear();
    }
    
    // Getters
    public Matrix getMatrix() { return matrix; }
    public List<PhysicsObj> getEntities() { return new ArrayList<>(entities); }
    public Random getRandom() { return random; }
    public double getTimeStep() { return timeStep; }
    public double getGravityConstant() { return gravityConstant; }
    public boolean isPaused() { return paused; }
    public int getEntityCount() { return entities.size(); }
    public int getFrameCount() { return frameCount; }
    
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
    
    // Utility methods
    public double getWrappedDistance(double x1, double y1, double x2, double y2) {
        double dx = Math.abs(x2 - x1);
        double dy = Math.abs(y2 - y1);
        
        int width = matrix.getTotalWidth();
        int height = matrix.getTotalHeight();
        
        if (dx > width / 2.0) dx = width - dx;
        if (dy > height / 2.0) dy = height - dy;
        
        return Math.sqrt(dx * dx + dy * dy);
    }
    
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