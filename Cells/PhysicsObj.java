package Cells;

import java.awt.Color;
import java.util.ArrayList;

/**
 * Represents an entity with physics properties and spatial awareness.
 * Combines physics simulation with spatial grid optimization.
 */
public class PhysicsObj {
    // Physics constants
    public double G = 10.0;
    public double MAX_VELOCITY = 10000;
    public double DAMPING_FACTOR = 1.00;
    
    // Position (pixel coordinates)
    private double x;
    private double y;
    
    // Velocity
    private double dx;
    private double dy;
    
    // Acceleration
    private double ax;
    private double ay;
    
    // Physical properties
    private double mass = 1.0;
    private Color color = Color.WHITE;
    private int size = 5;
    private boolean isStatic = false;
    
    // Spatial tracking
    private MatrixCell currentMatrixCell;
    
    public PhysicsObj(double x, double y) {
        this.x = x;
        this.y = y;
        this.dx = 0;
        this.dy = 0;
        this.ax = 0;
        this.ay = 0;
    }
    
    // --- Position/Velocity Getters ---
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDX() { return dx; }
    public double getDY() { return dy; }
    public double getAX() { return ax; }
    public double getAY() { return ay; }
    
    // --- Property Getters ---
    public double getMass() { return mass; }
    public Color getColor() { return color; }
    public int getSize() { return size; }
    public boolean isStatic() { return isStatic; }
    public MatrixCell getCurrentMatrixCell() { return currentMatrixCell; }
    
    // --- Setters ---
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public void setVelocity(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }
    public void setAcceleration(double ax, double ay) {
        this.ax = ax;
        this.ay = ay;
    }
    public void setMass(double mass) {
        if (mass <= 0) throw new IllegalArgumentException("Mass must be positive");
        this.mass = mass;
    }
    public void setColor(Color color) { this.color = color; }
    public void setSize(int size) {
        if (size <= 0) throw new IllegalArgumentException("Size must be positive");
        this.size = size;
    }
    public void setStatic(boolean isStatic) { this.isStatic = isStatic; }
    public void setCurrentMatrixCell(MatrixCell matrixCell) {
        this.currentMatrixCell = matrixCell;
    }
    
    // --- Physics Methods ---
    
    /**
     * Apply external force to this cell.
     */
    public void applyForce(double fx, double fy) {
        this.ax += fx / this.mass;
        this.ay += fy / this.mass;
    }
    
    /**
     * Apply gravitational attraction from another cell.
     */
    public void applyGravity(PhysicsObj other, Matrix matrix) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        
        // Consider wrapping for shortest path
        int totalWidth = matrix.getTotalWidth();
        int totalHeight = matrix.getTotalHeight();
        
        if (Math.abs(dx) > totalWidth / 2.0) {
            dx = dx > 0 ? dx - totalWidth : dx + totalWidth;
        }
        if (Math.abs(dy) > totalHeight / 2.0) {
            dy = dy > 0 ? dy - totalHeight : dy + totalHeight;
        }
        
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Avoid division by very small distances
        if (distance < 10) distance = 10;
        
        double force = (G * this.mass * other.mass) / (distance * distance);
        double angle = Math.atan2(dy, dx);
        double fx = Math.cos(angle) * force;
        double fy = Math.sin(angle) * force;
        
        this.applyForce(fx, fy);
    }
    
    /**
     * Apply velocity limiting and damping.
     */
    public void applyVelocityLimiting() {
        double speed = Math.sqrt(dx * dx + dy * dy);
        
        // Apply damping
        dx *= DAMPING_FACTOR;
        dy *= DAMPING_FACTOR;
        
        // Cap maximum velocity
        if (speed > MAX_VELOCITY) {
            double scale = MAX_VELOCITY / speed;
            dx *= scale;
            dy *= scale;
        }
    }
    
    /**
     * Update physics using symplectic Euler integration.
     */
    public void removeSelf(ArrayList<PhysicsObj> objects) {
        if (currentMatrixCell != null) {
            currentMatrixCell.removeCell(this);
            currentMatrixCell = null;
        }

        objects.remove(this);
    }

    public void update(double dt, Matrix matrix, ArrayList<PhysicsObj> cells) {
    // In your update method:

    /* 
    if (this.getCellsInRadius(50, matrix).size() > 0) {
        this.color = Color.RED;
    } else {
        this.color = Color.WHITE;
    }
    */

        if (isStatic) {
            this.ax = 0;
            this.ay = 0;
            return;
        }
        
        // 1. Update velocity based on current acceleration
        this.dx += this.ax * dt;
        this.dy += this.ay * dt;
        
        applyVelocityLimiting();
        
        // 2. Update position based on new velocity
        this.x += this.dx * dt;
        this.y += this.dy * dt;
        
        // 3. Wrap position (toroidal space)
        int totalWidth = matrix.getTotalWidth();
        int totalHeight = matrix.getTotalHeight();
        
        while (this.x < 0) this.x += totalWidth;
        while (this.x >= totalWidth) this.x -= totalWidth;
        while (this.y < 0) this.y += totalHeight;
        while (this.y >= totalHeight) this.y -= totalHeight;
        
        // 4. Reset acceleration for next frame
        this.ax = 0;
        this.ay = 0;
    }
    
    /**
     * Returns all cells within a specified radius using efficient spatial lookup.
     * Uses the matrix grid system for optimized searching with toroidal wrapping.
     */
    public ArrayList<PhysicsObj> getCellsInRadius(double radius, Matrix matrix) {
        ArrayList<PhysicsObj> nearbyCreatures = new ArrayList<>();
        
        if (currentMatrixCell == null || matrix == null) {
            return nearbyCreatures;
        }
        
        // Calculate which matrix cells to check based on radius
        int cellSize = matrix.getCellSize();
        int cellRadius = (int) Math.ceil(radius / cellSize);
        
        // Get current grid position from our MatrixCell (it knows its position)
        int currentGridX = currentMatrixCell.getGridX();
        int currentGridY = currentMatrixCell.getGridY();
        
        // Check all matrix cells within the grid radius
        for (int dx = -cellRadius; dx <= cellRadius; dx++) {
            for (int dy = -cellRadius; dy <= cellRadius; dy++) {
                // Calculate target grid position with proper wrapping
                int checkX = currentGridX + dx;
                int checkY = currentGridY + dy;
                
                // Get the matrix cell (getMatrixCell handles wrapping)
                MatrixCell cell = matrix.getMatrixCell(checkX, checkY);
                
                if (cell != null) {
                    // Check each creature in this matrix cell
                    for (PhysicsObj other : cell.getCells()) {
                        if (other != this) {
                            double distance = getDistanceToWithWrapping(other, matrix);
                            if (distance <= radius) {
                                nearbyCreatures.add(other);
                            }
                        }
                    }
                }
            }
        }
        
        return nearbyCreatures;
    }

    /**
     * Calculates Euclidean distance to another cell with toroidal wrapping.
     */
    public double getDistanceToWithWrapping(PhysicsObj other, Matrix matrix) {
        double dx = Math.abs(this.x - other.x);
        double dy = Math.abs(this.y - other.y);
        
        int totalWidth = matrix.getTotalWidth();
        int totalHeight = matrix.getTotalHeight();
        
        // Consider wrapping for shorter distance
        if (dx > totalWidth / 2.0) {
            dx = totalWidth - dx;
        }
        if (dy > totalHeight / 2.0) {
            dy = totalHeight - dy;
        }
        
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Returns all OTHER cells in the same grid square (for debugging).
     */
    public ArrayList<PhysicsObj> getCellsInSameGridSquare() {
        ArrayList<PhysicsObj> sameCells = new ArrayList<>();
        
        if (currentMatrixCell != null) {
            for (PhysicsObj other : currentMatrixCell.getCells()) {
                if (other != this) {
                    sameCells.add(other);
                }
            }
        }
        
        return sameCells;
    }

    public void update() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    public void update(int dt, Matrix matrix) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }
}