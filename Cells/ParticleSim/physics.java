package ParticleSim;

import java.awt.Color;
public class physics {
    public double G = 10.0; // Gravitational constant (tweak for effect)
    // Position
    private double x;
    private double y;
    // Velocity
    private double dx;
    private double dy;
    // Acceleration
    private double ax;
    private double ay;
    // Other properties
    private double mass = 1.0; // Default mass
    private Color color = new Color(0,0,0); // Default color
    private int size = 50; // Default size
    private boolean STATIC = false;

    public double MAX_VELOCITY = 10000; // Maximum allowed velocity
    public double DAMPING_FACTOR = 0.98; // Damping factor for velocity
    
    physics(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    // --- Getters ---
    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public double getDX() { return this.dx; }
    public double getDY() { return this.dy; }
    public double getAX() { return this.ax; }
    public double getAY() { return this.ay; }
    public double getMass() { return this.mass; }
    public int getSize() { return this.size; }
    public boolean isStatic() { return this.STATIC; }
    
    // --- Setters ---
    public void setStatic(boolean isStatic) { this.STATIC = isStatic; }
    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public void setVelocity(double dx, double dy) { this.dx = dx; this.dy = dy; }
    public void setAcceleration(double ax, double ay) { this.ax = ax; this.ay = ay; }
    public void setColor(Color c) { this.color = c; }
    public void setSize(int s) {
        if (s <= 0) throw new IllegalArgumentException("Size must be positive");
        this.size = s;
    }
    public void setMass(double m) {
        if (m <= 0) throw new IllegalArgumentException("Mass must be positive");
        this.mass = m;
    }
    
    // --- Apply external force ---
    public void applyForce(double fx, double fy) {
        this.ax += fx / this.mass;
        this.ay += fy / this.mass;
    }

    public void applyVelocityLimiting() {
        double speed = Math.sqrt(dx * dx + dy * dy);
        
        // Apply general damping
        //dx *= DAMPING_FACTOR;
        //dy *= DAMPING_FACTOR;
        
        // Cap maximum velocity
        if (speed > MAX_VELOCITY) {
            double scale = MAX_VELOCITY / speed;
            dx *= scale;
            dy *= scale;
        }
    }
    
    // --- Apply gravitational attraction from another body ---
    public void applyGravity(physics other) {
        double dx = other.getX() - this.x;
        double dy = other.getY() - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Avoid division by very small distances
        if (distance < 50) distance = 50;
        
        double force = (G * this.mass * other.mass) / (distance * distance);
        double angle = Math.atan2(dy, dx);
        double fx = Math.cos(angle) * force;
        double fy = Math.sin(angle) * force;
        
        this.applyForce(fx, fy);
    }
    
    // --- Symplectic Euler update ---
    public void update(double dt) {
        // 1. Update velocity based on current acceleration
        this.dx += this.ax * dt;
        this.dy += this.ay * dt;

        applyVelocityLimiting();
        
        // 2. Update position based on new velocity
        if (!this.STATIC) {
            this.x += this.dx * dt;
            this.y += this.dy * dt;
        }
        
        // 3. Reset acceleration for next frame
        this.ax = 0;
        this.ay = 0;
    }
    
    // --- Drawing helpers ---
    public void draw() {
        mainFile.g2.setColor(this.color);
        mainFile.drawSquare(this.x, this.y, this.size);
    }
    
    public void erase() {
        mainFile.g2.setColor(Color.WHITE);
        mainFile.drawSquare(this.x, this.y, this.size);
    }
    
    // Erase with custom camera position and zoom
    public void erase(double camX, double camY, double zoom) {
        mainFile.g2.setColor(Color.WHITE);
        mainFile.drawSquare(this.x, this.y, this.size, camX, camY, zoom);
    }
}