//FOOD.JAVA (UPDATED FOR 8D CHEMISTRY)

package Cells;

import java.awt.Color;
import java.util.List;

/**
 * Food particle with 8D chemical signature.
 */
public class Food extends PhysicsObj {
    private double nutritionalValue;
    private ChemicalSignature chemistry;
    private GradientSource gradientSource;
    
    private double lastX;
    private double lastY;
    
    private boolean isWaste = false;
    private static final double WASTE_DAMAGE_RADIUS = 100.0;
    private static final double WASTE_DAMAGE_PER_FRAME = 0.5;
    
    public Food(double x, double y, ChemicalSignature chemistry, double nutritionalValue) {
        super(x, y);
        this.chemistry = chemistry;
        this.nutritionalValue = nutritionalValue;
        
        setSize(3);
        this.lastX = x;
        this.lastY = y;
        
        this.gradientSource = new GradientSource(x, y, nutritionalValue, this, chemistry);
    }
    
    @Override
    protected void onAddedToWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getMultiChannelField().addSource(gradientSource);
    }
    
    @Override
    protected void onRemovedFromWorld() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.getMultiChannelField().removeSource(gradientSource);
    }
    
    @Override
    protected void onUpdate() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        // If this is waste, damage nearby cells
        if (isWaste) {
            damageNearbyCells();
        }
        
        // Update gradient source position if moved
        if (Math.abs(getX() - lastX) > 0.1 || Math.abs(getY() - lastY) > 0.1) {
            gradientSource.updatePosition(getX(), getY());
            world.getMultiChannelField().updateSource(gradientSource, lastX, lastY);
            
            lastX = getX();
            lastY = getY();
        }
    }
    
    private void damageNearbyCells() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        int gridX = (int)(getX() / 50);
        int gridY = (int)(getY() / 50);
        
        double damageRadiusSq = WASTE_DAMAGE_RADIUS * WASTE_DAMAGE_RADIUS;
for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int checkX = (gridX + dx + 200) % 200;
                int checkY = (gridY + dy + 200) % 200;
                
                List<PhysicsObj> entities = world.getEntitiesInSpatialCell(checkX, checkY);
                
                for (PhysicsObj obj : entities) {
                    if (!(obj instanceof Cell)) continue;
                    
                    Cell cell = (Cell) obj;
                    
                    Vector2D delta = world.getWrappedDelta(getX(), getY(), cell.getX(), cell.getY());
                    double distSq = delta.magnitudeSquared();
                    
                    if (distSq < damageRadiusSq) {
                        double distance = Math.sqrt(distSq);
                        double damageMultiplier = 1.0 - (distance / WASTE_DAMAGE_RADIUS);
                        double damage = WASTE_DAMAGE_PER_FRAME * damageMultiplier;
                        
                        cell.damageFromWaste(damage);
                    }
                }
            }
        }
    }
    
    // Getters
    public double getNutritionalValue() { return nutritionalValue; }
    public ChemicalSignature getChemistry() { return chemistry; }
    public boolean isWaste() { return isWaste; }
    
    // Setters
    public void setNutritionalValue(double value) {
        this.nutritionalValue = Math.max(0, value);
        gradientSource.strength = nutritionalValue;
    }
    
    public void setIsWaste(boolean isWaste) {
        this.isWaste = isWaste;
        if (isWaste) {
            setSize(5);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Food[pos=(%.1f, %.1f), nutrition=%.1f, chem=%s, waste=%s]",
            getX(), getY(), nutritionalValue, chemistry, isWaste);
    }

    public GradientSource getGradientSource() {
        return this.gradientSource;
    }
}