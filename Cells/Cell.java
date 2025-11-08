package Cells;

import java.awt.Color;
import java.util.ArrayList;

public class Cell extends PhysicsObj {
    // === GENOME (evolvable traits) ===
    private double adhesion_strength;
    private double adhesion_distance;
    private double motility_speed;
    private double motility_bias;
    private double uptake_rate;
    private double uptake_efficiency;
    private double leakiness;
    private double energy_storage_capacity;
    private double density_motility_modifier;
    private double density_uptake_modifier;
    private double adhesion_response_sensitivity;
    private double reproduction_threshold;
    private double[] signal_point;
    private double signal_strength;
    private double senescence_rate;
    private double longevity_gene;
    private double division_cost_modifier;
    private double mutation_rate;
    private double[] resource_affinity;  // for multiple resource types
    
    // === STATE (changes every tick) ===
    private double energy;
    private int age;

    public Cell(double x, double y) {
        super(x, y);
        this.DAMPING_FACTOR = 0.95; // Cells have higher damping by default
    }

    public void getCollision(PhysicsObj other, ArrayList<PhysicsObj> cells) {
        if (other != this) {
            double deltaX = other.getX() - this.getX();
            double deltaY = other.getY() - this.getY();
            double distance = Math.hypot(deltaX, deltaY);
            if (distance < (this.getSize()/2 + other.getSize()/2)) {
                other.removeSelf(cells);
            }
        }
    }

    public void update(double dt, Matrix matrix, ArrayList<PhysicsObj> cells) {
        super.update(dt, matrix, cells);
        // Update cell-specific logic here
        PhysicsObj a = this.moveTowardsFood(matrix);
        if (a != null) {
            this.getCollision(a, cells);
        }
    }

    public void moveTowards(double targetX, double targetY, double speed) {
        double deltaX = targetX - this.getX();
        double deltaY = targetY - this.getY();
        double[] normalized = MathFunctions.normalizeVector(deltaX, deltaY);
        this.applyForce(normalized[0] * speed, normalized[1] * speed);
    }

    public PhysicsObj moveTowardsFood(Matrix matrix) {
        double shortestDistance = Double.MAX_VALUE;
        PhysicsObj closestCell = null;
        for (PhysicsObj f : this.getCellsInRadius(500, matrix)) {
            if (f instanceof food) {
                double dist = Math.hypot(f.getX() - this.getX(), f.getY() - this.getY());
                if (dist < shortestDistance) {
                    shortestDistance = dist;
                    closestCell = f;
                }
            }
        }
        if (closestCell != null) {
            moveTowards(closestCell.getX(), closestCell.getY(), 10);
            //closestCell.setColor(Color.GREEN); // Highlight the food being targeted
            //closestCell.setSize(50);
            return closestCell;
        }
        return null;
    }
    
}
