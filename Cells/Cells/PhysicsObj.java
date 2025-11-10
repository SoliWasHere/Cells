//PHYSICSOBJ.JAVA (WITH COLLISION)

package Cells;

import java.awt.Color;

public abstract class PhysicsObj {
    protected double maxVelocity = 10000.0;
    protected double dampingFactor = 1.0;
    
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double accelerationX;
    private double accelerationY;

    private Integer spatialHashKey = null;

    private double mass;
    private Color color;
    private int size;
    private boolean isStatic;
    
    // Collision properties
    private double restitution = 1; // Bounciness (0 = no bounce, 1 = perfect bounce)
    
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
    
    /**
     * Handle collision with another physics object.
     * Ensures proper separation and applies elastic collision physics.
     */
    public void handleCollision(PhysicsObj other) {
        if (this.isStatic && other.isStatic) return;
        
        SimulationWorld world = SimulationWorld.getInstance();
        
        // Get wrapped delta (from this to other)
        Vector2D delta = world.getWrappedDelta(this.x, this.y, other.x, other.y);
        double distance = delta.magnitude();
        
        // Check if actually colliding
        double minDistance = (this.size + other.size) / 2.0;
        if (distance >= minDistance) return;
        
        // Prevent division by zero
        if (distance < 0.1) {
            // Objects are on top of each other - separate randomly
            double angle = Math.random() * 2 * Math.PI;
            delta = new Vector2D(Math.cos(angle), Math.sin(angle));
            distance = 0.1;
        }
        
        // Calculate overlap amount
        double overlap = minDistance - distance;
        
        // Normalize the delta to get collision normal (points from this to other)
        Vector2D normal = delta.normalize();
        
        // Separate objects based on mass ratio
        if (!this.isStatic && !other.isStatic) {
            // Both moveable - split separation inversely proportional to mass
            double totalMass = this.mass + other.mass;
            double pushThis = overlap * (other.mass / totalMass) * 1.01; // 1.01 adds tiny extra separation
            double pushOther = overlap * (this.mass / totalMass) * 1.01;
            
            // Push this away from other
            this.x -= normal.x * pushThis;
            this.y -= normal.y * pushThis;
            
            // Push other away from this
            other.x += normal.x * pushOther;
            other.y += normal.y * pushOther;
            
            // Wrap coordinates
            this.x = world.wrapX(this.x);
            this.y = world.wrapY(this.y);
            other.x = world.wrapX(other.x);
            other.y = world.wrapY(other.y);
        } else if (!this.isStatic) {
            // Only this is moveable - push it away completely
            this.x -= normal.x * overlap * 1.01;
            this.y -= normal.y * overlap * 1.01;
            this.x = world.wrapX(this.x);
            this.y = world.wrapY(this.y);
        } else {
            // Only other is moveable - push it away completely
            other.x += normal.x * overlap * 1.01;
            other.y += normal.y * overlap * 1.01;
            other.x = world.wrapX(other.x);
            other.y = world.wrapY(other.y);
        }
        
        // Apply velocity changes (impulse resolution)
        if (!this.isStatic && !other.isStatic) {
            // Relative velocity (velocity of this relative to other)
            double relVelX = this.velocityX - other.velocityX;
            double relVelY = this.velocityY - other.velocityY;
            
            // Relative velocity along collision normal
            double relVelNormal = relVelX * normal.x + relVelY * normal.y;
            
            // Only resolve if objects are approaching
            if (relVelNormal < 0) {
                // Calculate impulse scalar
                double restitutionAvg = (this.restitution + other.restitution) / 2.0;
                double impulseMagnitude = -(1.0 + restitutionAvg) * relVelNormal;
                impulseMagnitude /= (1.0 / this.mass + 1.0 / other.mass);
                
                // Apply impulse in direction of normal
                double impulseX = impulseMagnitude * normal.x;
                double impulseY = impulseMagnitude * normal.y;
                
                this.velocityX += impulseX / this.mass;
                this.velocityY += impulseY / this.mass;
                other.velocityX -= impulseX / other.mass;
                other.velocityY -= impulseY / other.mass;
            }
        } else if (!this.isStatic) {
            // Bounce off static object
            double velDotNormal = this.velocityX * normal.x + this.velocityY * normal.y;
            
            if (velDotNormal < 0) {
                // Reflect velocity across normal with restitution
                this.velocityX -= (1.0 + this.restitution) * velDotNormal * normal.x;
                this.velocityY -= (1.0 + this.restitution) * velDotNormal * normal.y;
            }
        } else {
            // Other bounces off this (static)
            double velDotNormal = other.velocityX * normal.x + other.velocityY * normal.y;
            
            if (velDotNormal > 0) {
                // Reflect velocity across normal with restitution
                other.velocityX -= (1.0 + other.restitution) * velDotNormal * normal.x;
                other.velocityY -= (1.0 + other.restitution) * velDotNormal * normal.y;
            }
        }
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
    public double getRestitution() { return restitution; }
    
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
    
    public void setRestitution(double restitution) {
        this.restitution = Math.max(0, Math.min(1, restitution));
    }



public Integer getSpatialHashKey() { return spatialHashKey; }
public void setSpatialHashKey(Integer key) { this.spatialHashKey = key; }

    
    @Override
    public String toString() {
        return String.format("%s[pos=(%.1f, %.1f), vel=(%.1f, %.1f), mass=%.1f]",
            getClass().getSimpleName(), x, y, velocityX, velocityY, mass);
    }
}