//GRADIENTFIELD.JAVA

package Cells;

import java.util.*;

/**
 * Represents a gradient field for a specific entity type.
 * Uses spatial hashing for efficient gradient calculation.
 */
public class GradientField {
    private final int cellSize;
    private final int gridWidth;
    private final int gridHeight;
    private final int totalWidth;
    private final int totalHeight;
    
    // Spatial hash: maps grid cell to list of sources
    private final Map<Integer, List<GradientSource>> spatialHash;
    
    // Gradient parameters
    private final double maxInfluenceRadius;
    private final double falloffExponent;
    
    /**
     * Create a new gradient field.
     * 
     * @param cellSize Size of spatial hash cells
     * @param gridWidth Number of cells horizontally
     * @param gridHeight Number of cells vertically
     * @param maxInfluenceRadius Maximum distance a source can influence
     * @param falloffExponent How quickly influence falls off (higher = steeper)
     */
    public GradientField(int cellSize, int gridWidth, int gridHeight, 
                         double maxInfluenceRadius, double falloffExponent) {
        this.cellSize = cellSize;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.totalWidth = cellSize * gridWidth;
        this.totalHeight = cellSize * gridHeight;
        this.spatialHash = new HashMap<>();
        this.maxInfluenceRadius = maxInfluenceRadius;
        this.falloffExponent = falloffExponent;
    }
    
    /**
     * Add a gradient source to the field.
     */
    public void addSource(GradientSource source) {
        int hash = getHash(source.x, source.y);
        spatialHash.computeIfAbsent(hash, k -> new ArrayList<>()).add(source);
    }
    
    /**
     * Remove a gradient source from the field.
     */
    public void removeSource(GradientSource source) {
        int hash = getHash(source.x, source.y);
        List<GradientSource> cell = spatialHash.get(hash);
        if (cell != null) {
            cell.remove(source);
            if (cell.isEmpty()) {
                spatialHash.remove(hash);
            }
        }
    }
    
    /**
     * Update a source's position in the spatial hash.
     */
    public void updateSource(GradientSource source, double oldX, double oldY) {
        int oldHash = getHash(oldX, oldY);
        int newHash = getHash(source.x, source.y);
        
        if (oldHash != newHash) {
            // Remove from old cell
            List<GradientSource> oldCell = spatialHash.get(oldHash);
            if (oldCell != null) {
                oldCell.remove(source);
                if (oldCell.isEmpty()) {
                    spatialHash.remove(oldHash);
                }
            }
            
            // Add to new cell
            spatialHash.computeIfAbsent(newHash, k -> new ArrayList<>()).add(source);
        }
    }
    
    /**
     * Clear all sources from the field.
     */
    public void clear() {
        spatialHash.clear();
    }
    
    /**
     * Sample the gradient at a specific point.
     * Returns the combined strength and direction from all nearby sources.
     */
    public GradientSample sample(double x, double y) {
        double totalStrength = 0;
        double directionX = 0;
        double directionY = 0;
        
        // Get nearby cells to check
        int centerGridX = (int)(x / cellSize);
        int centerGridY = (int)(y / cellSize);
        int checkRadius = (int)Math.ceil(maxInfluenceRadius / cellSize);
        
        // Check all cells within influence radius
        for (int dx = -checkRadius; dx <= checkRadius; dx++) {
            for (int dy = -checkRadius; dy <= checkRadius; dy++) {
                int gridX = wrapGridCoord(centerGridX + dx, gridWidth);
                int gridY = wrapGridCoord(centerGridY + dy, gridHeight);
                int hash = gridX + gridY * gridWidth;
                
                List<GradientSource> sources = spatialHash.get(hash);
                if (sources == null) continue;
                
                // Calculate contribution from each source
                for (GradientSource source : sources) {
                    double dx_world = getWrappedDelta(x, source.x, totalWidth);
                    double dy_world = getWrappedDelta(y, source.y, totalHeight);
                    double distSq = dx_world * dx_world + dy_world * dy_world;
                    double dist = Math.sqrt(distSq);
                    
                    if (dist < maxInfluenceRadius && dist > 0.1) {
                        // Calculate strength with falloff
                        double normalizedDist = dist / maxInfluenceRadius;
                        double strength = source.strength * Math.pow(1 - normalizedDist, falloffExponent);
                        
                        totalStrength += strength;
                        
                        // Accumulate direction (normalized)
                        directionX += (dx_world / dist) * strength;
                        directionY += (dy_world / dist) * strength;
                    }
                }
            }
        }
        
        // Normalize direction
        double dirMag = Math.sqrt(directionX * directionX + directionY * directionY);
        if (dirMag > 0.001) {
            directionX /= dirMag;
            directionY /= dirMag;
        }
        
        return new GradientSample(totalStrength, directionX, directionY);
    }
    
    /**
     * Get spatial hash key for a position.
     */
    private int getHash(double x, double y) {
        int gridX = (int)(x / cellSize);
        int gridY = (int)(y / cellSize);
        gridX = wrapGridCoord(gridX, gridWidth);
        gridY = wrapGridCoord(gridY, gridHeight);
        return gridX + gridY * gridWidth;
    }
    
    /**
     * Wrap grid coordinate with modulo arithmetic.
     */
    private int wrapGridCoord(int coord, int max) {
        return ((coord % max) + max) % max;
    }
    
    /**
     * Get wrapped delta for toroidal space.
     */
    private double getWrappedDelta(double coord1, double coord2, int max) {
        double delta = coord2 - coord1;
        if (Math.abs(delta) > max / 2.0) {
            delta = delta > 0 ? delta - max : delta + max;
        }
        return delta;
    }
    
    // Getters
    public int getTotalWidth() { return totalWidth; }
    public int getTotalHeight() { return totalHeight; }
    public int getGridWidth() { return gridWidth; }
    public int getGridHeight() { return gridHeight; }
    public int getCellSize() { return cellSize; }
}