package Cells;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all entities in the physics simulation.
 * Handles position, velocity, forces, and spatial queries.
 */
public abstract class PhysicsObj {
    // Physics constants (can be overridden by subclasses)
    protected double maxVelocity = 10000.0;
    protected double dampingFactor = 1.0;
    
    // Position and motion
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double accelerationX;
    private double accelerationY;
    
    // Physical properties
    private double mass;
    private Color color;
    private int size;
    private boolean isStatic;
    
    // Spatial tracking
    private MatrixCell currentMatrixCell;
    
    /**
     * Create a new physics object at the specified position.
     */
    public PhysicsObj(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocityX = 0;
        this.velocityY = 0;
        this.accelerationX = 0;
        this.accelerationY = 0;
        this.mass = 1.0;
        this.color = Color.WHITE;
        this.size = 5;
        this.isStatic = false;
    }
    
    // === Lifecycle Methods (subclasses can override) ===
    
    /**
     * Called when this entity is added to the world.
     */
    protected void onAddedToWorld() {
        // Override in subclasses if needed
    }
    
    /**
     * Called when this entity is removed from the world.
     */
    protected void onRemovedFromWorld() {
        // Override in subclasses if needed
    }
    
    /**
     * Update this entity's physics and behavior.
     * Called every simulation tick.
     */
    public void update() {
        if (isStatic) {
            accelerationX = 0;
            accelerationY = 0;
            return;
        }
        
        SimulationWorld world = SimulationWorld.getInstance();
        double dt = world.getTimeStep();
        
        // Update velocity from acceleration
        velocityX += accelerationX * dt;
        velocityY += accelerationY * dt;
        
        // Apply damping and velocity limiting
        applyVelocityLimiting();
        
        // Update position from velocity
        x += velocityX * dt;
        y += velocityY * dt;
        
        // Wrap position (toroidal world)
        x = world.wrapX(x);
        y = world.wrapY(y);
        
        // Reset acceleration for next frame
        accelerationX = 0;
        accelerationY = 0;
        
        // Allow subclasses to add custom behavior
        onUpdate();
    }
    
    /**
     * Override this method for custom update logic.
     */
    protected void onUpdate() {
        // Override in subclasses
    }
    
    // === Force Application ===
    
    /**
     * Apply a force to this object (F = ma, so a = F/m).
     */
    public void applyForce(double fx, double fy) {
        accelerationX += fx / mass;
        accelerationY += fy / mass;
    }
    
    /**
     * Apply a force in the direction of a vector.
     */
    public void applyForce(Vector2D force) {
        applyForce(force.x, force.y);
    }
    
    /**
     * Apply gravitational attraction from another object.
     */
    public void applyGravityFrom(PhysicsObj other) {
        SimulationWorld world = SimulationWorld.getInstance();
        
        // Get shortest path considering wrapping
        Vector2D delta = world.getWrappedDelta(x, y, other.x, other.y);
        double distance = delta.magnitude();
        
        // Avoid singularities
        if (distance < 10) distance = 10;
        
        // Calculate gravitational force: F = G * m1 * m2 / r^2
        double forceMagnitude = (world.getGravityConstant() * mass * other.mass) / (distance * distance);
        
        // Apply force in direction of other object
        Vector2D direction = delta.normalize();
        applyForce(direction.scale(forceMagnitude));
    }
    
    /**
     * Apply velocity damping and enforce maximum velocity.
     */
    private void applyVelocityLimiting() {
        // Apply damping
        velocityX *= dampingFactor;
        velocityY *= dampingFactor;
        
        // Cap maximum velocity
        double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (speed > maxVelocity) {
            double scale = maxVelocity / speed;
            velocityX *= scale;
            velocityY *= scale;
        }
    }
    
    // === Spatial Queries ===
    
    /**
     * Get all entities within a specified radius.
     * Uses spatial partitioning for efficiency.
     */
    public List<PhysicsObj> getCellsInRadius(double radius) {
        List<PhysicsObj> nearby = new ArrayList<>();
        
        if (currentMatrixCell == null) {
            return nearby;
        }
        
        SimulationWorld world = SimulationWorld.getInstance();
        Matrix matrix = world.getMatrix();
        
        // Calculate grid cells to check
        int cellSize = matrix.getCellSize();
        int cellRadius = (int) Math.ceil(radius / cellSize);
        
        int currentGridX = currentMatrixCell.getGridX();
        int currentGridY = currentMatrixCell.getGridY();
        
        // Check all grid cells within radius
        for (int dx = -cellRadius; dx <= cellRadius; dx++) {
            for (int dy = -cellRadius; dy <= cellRadius; dy++) {
                MatrixCell cell = matrix.getMatrixCell(currentGridX + dx, currentGridY + dy);
                
                if (cell != null) {
                    for (PhysicsObj other : cell.getCells()) {
                        if (other != this) {
                            double distance = getDistanceTo(other);
                            if (distance <= radius) {
                                nearby.add(other);
                            }
                        }
                    }
                }
            }
        }
        
        return nearby;
    }
    
    /**
     * Get all entities in the same grid cell.
     */
    public List<PhysicsObj> getCellsInSameGrid() {
        List<PhysicsObj> sameCells = new ArrayList<>();
        
        if (currentMatrixCell != null) {
            for (PhysicsObj other : currentMatrixCell.getCells()) {
                if (other != this) {
                    sameCells.add(other);
                }
            }
        }
        
        return sameCells;
    }
    
    /**
     * Calculate wrapped distance to another entity.
     */
    public double getDistanceTo(PhysicsObj other) {
        SimulationWorld world = SimulationWorld.getInstance();
        return world.getWrappedDistance(x, y, other.x, other.y);
    }
    
    /**
     * Get wrapped direction vector to another entity.
     */
    public Vector2D getDirectionTo(PhysicsObj other) {
        SimulationWorld world = SimulationWorld.getInstance();
        Vector2D delta = world.getWrappedDelta(x, y, other.x, other.y);
        return delta.normalize();
    }
    
    /**
     * Check if this entity is colliding with another.
     */
    public boolean isCollidingWith(PhysicsObj other) {
        double distance = getDistanceTo(other);
        double collisionDistance = (size + other.size) / 2.0;
        return distance < collisionDistance;
    }
    
    // === Removal ===
    
    /**
     * Remove this entity from the world.
     */
    public void destroy() {
        SimulationWorld.getInstance().queueRemoval(this);
    }
    
    // === Getters ===
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getAccelerationX() { return accelerationX; }
    public double getAccelerationY() { return accelerationY; }
    public double getMass() { return mass; }
    public Color getColor() { return color; }
    public int getSize() { return size; }
    public boolean isStatic() { return isStatic; }
    public MatrixCell getCurrentMatrixCell() { return currentMatrixCell; }
    
    /**
     * Get velocity as a vector.
     */
    public Vector2D getVelocity() {
        return new Vector2D(velocityX, velocityY);
    }
    
    /**
     * Get current speed (magnitude of velocity).
     */
    public double getSpeed() {
        return Math.sqrt(velocityX * velocityX + velocityY * velocityY);
    }
    
    // === Setters ===
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void setVelocity(double vx, double vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }
    
    public void setVelocity(Vector2D velocity) {
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
    }
    
    public void setMass(double mass) {
        if (mass <= 0) {
            throw new IllegalArgumentException("Mass must be positive : " + mass);
        }
        this.mass = mass;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be positive: " + size);
        }
        this.size = size;
    }
    
    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
    
    public void setCurrentMatrixCell(MatrixCell matrixCell) {
        this.currentMatrixCell = matrixCell;
    }
    
    @Override
    public String toString() {
        return String.format("%s[pos=(%.1f, %.1f), vel=(%.1f, %.1f), mass=%.1f]",
            getClass().getSimpleName(), x, y, velocityX, velocityY, mass);
    }
}