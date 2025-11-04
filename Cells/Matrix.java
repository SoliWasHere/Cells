package Cells;

import java.util.ArrayList;

/**
 * Spatial grid system for efficient cell management and lookup.
 * Pure spatial indexing - cells handle their own physics.
 */
public class Matrix {
    private MatrixCell[][] grid;
    private int cellSize;
    private int gridWidth;
    private int gridHeight;
    private int totalWidth;
    private int totalHeight;
    
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
     * Inserts a cell into the matrix at its current position.
     */
    public boolean insertCell(PhysicsObj cell) {
        // DON'T wrap coordinates here - cell position should be preserved
        // Just get the grid position based on current coordinates
        double x = cell.getX();
        double y = cell.getY();
        
        // Wrap only for grid calculation
        x = wrapCoordinate(x, totalWidth);
        y = wrapCoordinate(y, totalHeight);
        
        int gridX = getGridX(x);
        int gridY = getGridY(y);
        
        MatrixCell matrixCell = grid[gridX][gridY];
        matrixCell.addCell(cell);
        cell.setCurrentMatrixCell(matrixCell);
        return true;
    }

        public boolean insertCell(Cell cell) {
        // DON'T wrap coordinates here - cell position should be preserved
        // Just get the grid position based on current coordinates
        double x = cell.getX();
        double y = cell.getY();
        
        // Wrap only for grid calculation
        x = wrapCoordinate(x, totalWidth);
        y = wrapCoordinate(y, totalHeight);
        
        int gridX = getGridX(x);
        int gridY = getGridY(y);
        
        MatrixCell matrixCell = grid[gridX][gridY];
        matrixCell.addCell(cell);
        cell.setCurrentMatrixCell(matrixCell);
        return true;
    }
    
    /**
     * Updates all cells' grid positions based on their current coordinates.
     * Call this after physics updates to keep spatial index accurate.
     */
    public void updateCellGrid(PhysicsObj cell) {
        MatrixCell oldMatrixCell = cell.getCurrentMatrixCell();
        if (oldMatrixCell == null) return;
        
        // Wrap coordinates for grid calculation
        double wrappedX = wrapCoordinate(cell.getX(), totalWidth);
        double wrappedY = wrapCoordinate(cell.getY(), totalHeight);
        
        int newGridX = getGridX(wrappedX);
        int newGridY = getGridY(wrappedY);
        
        // If cell moved to a different grid square, update tracking
        if (oldMatrixCell.getGridX() != newGridX || oldMatrixCell.getGridY() != newGridY) {
            oldMatrixCell.removeCell(cell);
            
            MatrixCell newMatrixCell = grid[newGridX][newGridY];
            newMatrixCell.addCell(cell);
            cell.setCurrentMatrixCell(newMatrixCell);
        }
    }
    
    /**
     * Removes a cell from the matrix.
     */
    public boolean removeCell(PhysicsObj cell) {
        MatrixCell matrixCell = cell.getCurrentMatrixCell();
        if (matrixCell != null) {
            matrixCell.removeCell(cell);
            cell.setCurrentMatrixCell(null);
            return true;
        }
        return false;
    }
    
    /**
     * Gets all cells in the entire matrix.
     */
    public ArrayList<PhysicsObj> getAllCells() {
        ArrayList<PhysicsObj> allCells = new ArrayList<>();
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                allCells.addAll(grid[x][y].getCells());
            }
        }
        return allCells;
    }
    
    /**
     * Wrap a coordinate to stay within bounds [0, max).
     */
    private double wrapCoordinate(double coord, int max) {
        while (coord < 0) coord += max;
        while (coord >= max) coord -= max;
        return coord;
    }
    
    /**
     * Get grid X index from world X coordinate.
     * Handles wrapping correctly.
     */
    public int getGridX(double x) {
        // Make sure x is positive and wrapped
        x = wrapCoordinate(x, totalWidth);
        int gridX = (int) (x / cellSize);
        // Clamp to valid range (should already be valid after wrapping)
        if (gridX < 0) gridX = 0;
        if (gridX >= gridWidth) gridX = gridWidth - 1;
        return gridX;
    }
    
    /**
     * Get grid Y index from world Y coordinate.
     * Handles wrapping correctly.
     */
    public int getGridY(double y) {
        // Make sure y is positive and wrapped
        y = wrapCoordinate(y, totalHeight);
        int gridY = (int) (y / cellSize);
        // Clamp to valid range (should already be valid after wrapping)
        if (gridY < 0) gridY = 0;
        if (gridY >= gridHeight) gridY = gridHeight - 1;
        return gridY;
    }
    
    public MatrixCell getMatrixCell(int gridX, int gridY) {
        // Wrap grid coordinates
        gridX = ((gridX % gridWidth) + gridWidth) % gridWidth;
        gridY = ((gridY % gridHeight) + gridHeight) % gridHeight;
        return grid[gridX][gridY];
    }
    
    // Getters
    public int getCellSize() { return cellSize; }
    public int getGridWidth() { return gridWidth; }
    public int getGridHeight() { return gridHeight; }
    public int getTotalWidth() { return totalWidth; }
    public int getTotalHeight() { return totalHeight; }
    public MatrixCell[][] getGrid() { return grid; }
}

/**
 * Container for cells occupying the same grid square.
 */
class MatrixCell {
    private ArrayList<PhysicsObj> cells;
    private int gridX;
    private int gridY;
    
    public MatrixCell(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.cells = new ArrayList<>();
    }
    
    public void addCell(PhysicsObj cell) {
        // Remove first to avoid duplicates (in case of fast movement)
        cells.remove(cell);
        cells.add(cell);
    }

    public void add(Cell a) {
        cells.remove(a);
        cells.add(a);
    }
    
    public void removeCell(PhysicsObj cell) {
        cells.remove(cell);
    }
    
    public ArrayList<PhysicsObj> getCells() {
        return cells;
    }
    
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
}