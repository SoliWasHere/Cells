//MULTICHANNELGRADIENTFIELD.JAVA (UPDATED - proper weighted sampling)

package Cells;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified gradient field for 8D chemical space.
 * Each observer perceives the field differently based on their chemistry.
 */
public class MultiChannelGradientField {
    private final GradientField globalField;
    private final List<GradientSource> allSources;
    private final int cellSize;
    public static int NUM_CHANNELS = 8;
    
    public MultiChannelGradientField(int cellSize, int gridWidth, int gridHeight) {
        this.globalField = new GradientField(cellSize, gridWidth, gridHeight, 1000.0, 0.5);
        this.allSources = new ArrayList<>();
        this.cellSize = cellSize;
    }
    
    /**
     * Add a gradient source (cell or food).
     */
    public void addSource(GradientSource source) {
        globalField.addSource(source);
        allSources.add(source);
    }
    
    /**
     * Remove a gradient source.
     */
    public void removeSource(GradientSource source) {
        globalField.removeSource(source);
        allSources.remove(source);
    }
    
    /**
     * Update source position.
     */
    public void updateSource(GradientSource source, double oldX, double oldY) {
        globalField.updateSource(source, oldX, oldY);
    }
    
    /**
     * Sample gradient weighted by chemical preference.
     * This is the KEY method - cells only "see" compatible food sources.
     */
    public GradientSample sampleWeighted(double x, double y, ChemicalSignature observerPreference) {
        SimulationWorld world = SimulationWorld.getInstance();
        
        double totalDirX = 0;
        double totalDirY = 0;
        double totalStrength = 0;
        
        // Get nearby sources
        int gridX = (int)(x / cellSize);
        int gridY = (int)(y / cellSize);
        int checkRadius = (int)Math.ceil(300.0 / cellSize);
        
        for (int dx = -checkRadius; dx <= checkRadius; dx++) {
            for (int dy = -checkRadius; dy <= checkRadius; dy++) {
                int gx = ((gridX + dx) % globalField.getGridWidth() + globalField.getGridWidth()) % globalField.getGridWidth();
                int gy = ((gridY + dy) % globalField.getGridHeight() + globalField.getGridHeight()) % globalField.getGridHeight();
                
                List<PhysicsObj> entities = world.getEntitiesInSpatialCell(gx, gy);
                
                for (PhysicsObj entity : entities) {
                    GradientSource source = null;
                    boolean isCell = false;
                    
                    if (entity instanceof Cell) {
                        Cell cell = (Cell) entity;
                        source = cell.getCellGradientSource();
                        isCell = true;
                    } else if (entity instanceof Food) {
                        Food food = (Food) entity;
                        source = food.getGradientSource();
                        isCell = false;
                    }
                    
                    if (source == null || !allSources.contains(source)) continue;
                    
                    // Calculate direction and distance
                    Vector2D delta = world.getWrappedDelta(x, y, source.x, source.y);
                    double distance = delta.magnitude();
                    
                    if (distance < 5.0 || distance > 300.0) continue;
                    
                    // Calculate compatibility-based strength
                    double compatibility = observerPreference.compatibilityWith(source.chemistry);
                    
                    // Skip if incompatible (saves computation)
                    if (compatibility < 0.1) continue;
                    
                    // Calculate perceived strength using emission bias
                    double perceivedStrength = source.getPerceivedStrength(observerPreference);
                    
                    // Distance falloff
                    double falloff = Math.pow(1.0 - (distance / 300.0), 2.0);
                    
                    // Cells repel, food attracts
                    double attractionMultiplier = isCell ? -0.3 : 1.0;
                    
                    // Final weight
                    double weight = perceivedStrength * falloff * attractionMultiplier;
                    
                    if (Math.abs(weight) > 0.01) {
                        double normX = delta.x / distance;
                        double normY = delta.y / distance;
                        
                        totalDirX += normX * weight;
                        totalDirY += normY * weight;
                        totalStrength += Math.abs(weight);
                    }
                }
            }
        }
        
        // Normalize direction
        double dirMag = Math.sqrt(totalDirX * totalDirX + totalDirY * totalDirY);
        if (dirMag > 0.001) {
            totalDirX /= dirMag;
            totalDirY /= dirMag;
        } else {
            totalDirX = 0;
            totalDirY = 0;
        }
        
        return new GradientSample(totalStrength, totalDirX, totalDirY);
    }
    
    /**
     * Sample all sources at a point (for visualization).
     */
    public GradientSample sampleAll(double x, double y) {
        return globalField.sample(x, y);
    }
    
    public void clear() {
        globalField.clear();
        allSources.clear();
    }
    
    public GradientField getGlobalGradientField() {
        return globalField;
    }
}