//PHYSICSOBJ.JAVA (OPTIMIZED - Add this method)

package Cells;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public abstract class PhysicsObj {
    protected double maxVelocity = 10000.0;
    protected double dampingFactor = 1.0;
    
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double accelerationX;
    private double accelerationY;
    
    private double mass;
    private Color color;
    private int size;
    private boolean isStatic;
    
    protected MatrixCell currentMatrixCell;  // CHANGED: protected for direct access
    
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
    
    protected void onAddedToWorld() {}
    protected void onRemovedFromWorld() {}
    
    public void update() {
        if (isStatic) {
            accelerationX = 0;
            accelerationY = 0;
            return;
        }
        
        SimulationWorld world = SimulationWorld.getInstance();
        double dt = world.getTimeStep();
        
        velocityX += accelerationX * dt;
        velocityY += accelerationY * dt;
        
        applyVelocityLimiting();
        
        x += velocityX * dt;
        y += velocityY * dt;
        
        x = world.wrapX(x);
        y = world.wrapY(y);
        
        accelerationX = 0;
        accelerationY = 0;
        
        onUpdate();
    }
    
    protected void onUpdate() {}
    
    public void applyForce(double fx, double fy) {
        accelerationX += fx / mass;
        accelerationY += fy / mass;
    }
    
    public void applyForce(Vector2D force) {
        applyForce(force.x, force.y);
    }
    
    public void applyGravityFrom(PhysicsObj other) {
        SimulationWorld world = SimulationWorld.getInstance();
        
        Vector2D delta = world.getWrappedDelta(x, y, other.x, other.y);
        double distance = delta.magnitude();
        
        if (distance < 10) distance = 10;
        
        double forceMagnitude = (world.getGravityConstant() * mass * other.mass) / (distance * distance);
        
        Vector2D direction = delta.normalize();
        applyForce(direction.scale(forceMagnitude));
    }
    
    private void applyVelocityLimiting() {
        velocityX *= dampingFactor;
        velocityY *= dampingFactor;
        
        double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (speed > maxVelocity) {
            double scale = maxVelocity / speed;
            velocityX *= scale;
            velocityY *= scale;
        }
    }
    
    public List<PhysicsObj> getCellsInRadius(double radius) {
        List<PhysicsObj> nearby = new ArrayList<>();
        
        if (currentMatrixCell == null) {
            return nearby;
        }
        
        SimulationWorld world = SimulationWorld.getInstance();
        Matrix matrix = world.getMatrix();
        
        int cellSize = matrix.getCellSize();
        int cellRadius = (int) Math.ceil(radius / cellSize);
        
        int currentGridX = currentMatrixCell.getGridX();
        int currentGridY = currentMatrixCell.getGridY();
        
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
    
    public double getDistanceTo(PhysicsObj other) {
        SimulationWorld world = SimulationWorld.getInstance();
        return world.getWrappedDistance(x, y, other.x, other.y);
    }
    
    public Vector2D getDirectionTo(PhysicsObj other) {
        SimulationWorld world = SimulationWorld.getInstance();
        Vector2D delta = world.getWrappedDelta(x, y, other.x, other.y);
        return delta.normalize();
    }
    
    public boolean isCollidingWith(PhysicsObj other) {
        double distance = getDistanceTo(other);
        double collisionDistance = (size + other.size) / 2.0;
        return distance < collisionDistance;
    }
    
    public void destroy() {
        SimulationWorld.getInstance().queueRemoval(this);
    }
    
    // Getters
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
    
    public Vector2D getVelocity() {
        return new Vector2D(velocityX, velocityY);
    }
    
    public double getSpeed() {
        return Math.sqrt(velocityX * velocityX + velocityY * velocityY);
    }
    
    // Setters
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