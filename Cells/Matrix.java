//MATRIX.JAVA

package Cells;

import java.util.ArrayList;
import java.util.List;

/**
 * Spatial grid system for efficient entity lookup.
 * Divides the world into grid cells for O(1) spatial queries.
 */
public class Matrix {
    private final MatrixCell[][] grid;
    private final int cellSize;
    private final int gridWidth;
    private final int gridHeight;
    private final int totalWidth;
    private final int totalHeight;
    
    /**
     * Create a new spatial grid.
     * 
     * @param cellSize Size of each grid cell in pixels
     * @param gridWidth Number of grid cells horizontally
     * @param gridHeight Number of grid cells vertically
     */
    public Matrix(int cellSize, int gridWidth, int gridHeight) {
        this.cellSize = cellSize;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.totalWidth = cellSize * gridWidth;
        this.totalHeight = cellSize * gridHeight;
        
        // Initialize grid
        grid = new MatrixCell[gridWidth][gridHeight];
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                grid[x][y] = new MatrixCell(x, y);
            }
        }
    }
    
    /**
     * Insert an entity into the spatial grid.
     */
    public void insertCell(PhysicsObj entity) {
        double x = wrapCoordinate(entity.getX(), totalWidth);
        double y = wrapCoordinate(entity.getY(), totalHeight);
        
        int gridX = getGridX(x);
        int gridY = getGridY(y);
        
        MatrixCell matrixCell = grid[gridX][gridY];
        matrixCell.addCell(entity);
        entity.setCurrentMatrixCell(matrixCell);
    }
    
    /**
     * Update an entity's position in the grid after it moves.
     */
    public void updateCellGrid(PhysicsObj entity) {
        MatrixCell oldCell = entity.getCurrentMatrixCell();
        if (oldCell == null) return;
        
        // Calculate new grid position
        double wrappedX = wrapCoordinate(entity.getX(), totalWidth);
        double wrappedY = wrapCoordinate(entity.getY(), totalHeight);
        
        int newGridX = getGridX(wrappedX);
        int newGridY = getGridY(wrappedY);
        
        // Move to new cell if changed
        if (oldCell.getGridX() != newGridX || oldCell.getGridY() != newGridY) {
            oldCell.removeCell(entity);
            
            MatrixCell newCell = grid[newGridX][newGridY];
            newCell.addCell(entity);
            entity.setCurrentMatrixCell(newCell);
        }
    }
    
    /**
     * Remove an entity from the spatial grid.
     */
    public void removeCell(PhysicsObj entity) {
        MatrixCell matrixCell = entity.getCurrentMatrixCell();
        if (matrixCell != null) {
            matrixCell.removeCell(entity);
            entity.setCurrentMatrixCell(null);
        }
    }
    
    /**
     * Get all entities in the world.
     */
    public List<PhysicsObj> getAllCells() {
        List<PhysicsObj> allCells = new ArrayList<>();
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                allCells.addAll(grid[x][y].getCells());
            }
        }
        return allCells;
    }
    
    /**
     * Get a grid cell at the specified grid coordinates (with wrapping).
     */
    public MatrixCell getMatrixCell(int gridX, int gridY) {
        gridX = wrapGridCoordinate(gridX, gridWidth);
        gridY = wrapGridCoordinate(gridY, gridHeight);
        return grid[gridX][gridY];
    }
    
    /**
     * Convert world X coordinate to grid X index.
     */
    public int getGridX(double x) {
        x = wrapCoordinate(x, totalWidth);
        int gridX = (int) (x / cellSize);
        return Math.max(0, Math.min(gridWidth - 1, gridX));
    }
    
    /**
     * Convert world Y coordinate to grid Y index.
     */
    public int getGridY(double y) {
        y = wrapCoordinate(y, totalHeight);
        int gridY = (int) (y / cellSize);
        return Math.max(0, Math.min(gridHeight - 1, gridY));
    }
    
    /**
     * Wrap a coordinate to stay within [0, max).
     */
    private double wrapCoordinate(double coord, int max) {
        while (coord < 0) coord += max;
        while (coord >= max) coord -= max;
        return coord;
    }
    
    /**
     * Wrap a grid coordinate with modulo arithmetic.
     */
    private int wrapGridCoordinate(int coord, int max) {
        return ((coord % max) + max) % max;
    }
    
    // === Getters ===
    
    public int getCellSize() { return cellSize; }
    public int getGridWidth() { return gridWidth; }
    public int getGridHeight() { return gridHeight; }
    public int getTotalWidth() { return totalWidth; }
    public int getTotalHeight() { return totalHeight; }
    public MatrixCell[][] getGrid() { return grid; }
}