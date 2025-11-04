package Cells;

import java.awt.Color;

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

    public void moveTowards(double targetX, double targetY, double speed) {
        double deltaX = targetX - this.getX();
        double deltaY = targetY - this.getY();
        double[] normalized = MathFunctions.normalizeVector(deltaX, deltaY);
        this.applyForce(normalized[0] * speed, normalized[1] * speed);
    }

    public void moveTowardsFood(Matrix matrix) {
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
            closestCell.setColor(Color.GREEN); // Highlight the food being targeted
            closestCell.setSize(50);
        }
    }
    
}
