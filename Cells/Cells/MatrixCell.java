//MATRIXCELL.JAVA

package Cells;

import java.util.ArrayList;
import java.util.List;

/**
 * A single cell in the spatial grid.
 * Contains all entities currently in this region of space.
 */
public class MatrixCell {
    private final List<PhysicsObj> cells;
    private final int gridX;
    private final int gridY;
    
    /**
     * Create a new grid cell at the specified grid coordinates.
     */
    public MatrixCell(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.cells = new ArrayList<>();
    }

    /**
     * Add an entity to this grid cell.
     * Prevents duplicates by removing first if already present.
     */
    public void addCell(PhysicsObj entity) {
        cells.remove(entity); // Remove if already present
        cells.add(entity);
    }
    
    /**
     * Remove an entity from this grid cell.
     */
    public void removeCell(PhysicsObj entity) {
        cells.remove(entity);
    }
    
    /**
     * Get all entities in this grid cell.
     */
    public List<PhysicsObj> getCells() {
        return new ArrayList<>(cells); // Return copy for safety
    }
    
    /**
     * Get the number of entities in this cell.
     */
    public int getEntityCount() {
        return cells.size();
    }
    
    /**
     * Check if this cell is empty.
     */
    public boolean isEmpty() {
        return cells.isEmpty();
    }
    
    // === Getters ===
    
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    
    @Override
    public String toString() {
        return String.format("MatrixCell[%d,%d] (%d entities)", gridX, gridY, cells.size());
    }
}