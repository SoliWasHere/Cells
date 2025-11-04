package Cells;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Handles visualization with camera system and double buffering.
 * Based on particle simulation rendering approach.
 */
public class Displayer {
    private DrawingPanel panel;
    private Matrix matrix;
    private BufferedImage buffer;
    private Graphics2D g2;
    private Graphics panelGraphics;
    
    // Camera system
    public double cameraX;
    public double cameraY;
    public double zoom;
    private static final double CAMERA_SMOOTH = 0.05;
    private static final double ZOOM_SMOOTH = 0.1;
    private static final double ZOOM_MARGIN = 100;
    
    // Visual constants
    private static final Color GRID_COLOR = new Color(50, 50, 50);
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0);
    
    public Displayer(Matrix matrix) {
        this.matrix = matrix;
        this.panel = new DrawingPanel(matrix.getTotalWidth(), matrix.getTotalHeight());
        this.buffer = new BufferedImage(matrix.getTotalWidth(), matrix.getTotalHeight(), 
                                        BufferedImage.TYPE_INT_ARGB);
        this.g2 = buffer.createGraphics();
        this.panelGraphics = panel.getGraphics();
        
        // Initialize camera
        this.cameraX = matrix.getTotalWidth() / 2.0;
        this.cameraY = matrix.getTotalHeight() / 2.0;
        this.zoom = 1.0;
        
        // Enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
    
    /**
     * Main display method - renders everything.
     */
    public void display() {
        clearBuffer();
        drawGrid();
        drawCells();
        
        // Copy buffer to panel
        panelGraphics.drawImage(buffer, 0, 0, null);
    }
    
    /**
     * Updates camera to follow center of mass smoothly.
     */
    public void updateCamera(java.util.ArrayList<PhysicsObj> cells) {
        if (cells.isEmpty()) return;
        
        double[] centerOfMass = getCenterOfMass(cells);
        double targetX = centerOfMass[0];
        double targetY = centerOfMass[1];
        
        // Calculate bounding box and target zoom
        double[] bounds = getBoundingBox(cells);
        double minX = bounds[0];
        double minY = bounds[1];
        double maxX = bounds[2];
        double maxY = bounds[3];
        
        double objectWidth = maxX - minX + ZOOM_MARGIN * 2;
        double objectHeight = maxY - minY + ZOOM_MARGIN * 2;
        
        double zoomForWidth = matrix.getTotalWidth() / objectWidth;
        double zoomForHeight = matrix.getTotalHeight() / objectHeight;
        
        double idealZoom = Math.min(zoomForWidth, zoomForHeight);
        idealZoom = Math.max(0.1, Math.min(2.0, idealZoom));
        
        // Smoothly move camera toward center of mass
        cameraX += (targetX - cameraX) * CAMERA_SMOOTH;
        cameraY += (targetY - cameraY) * CAMERA_SMOOTH;
        
        // Smoothly adjust zoom
        zoom += (idealZoom - zoom) * ZOOM_SMOOTH;
    }
    
    private void clearBuffer() {
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, matrix.getTotalWidth(), matrix.getTotalHeight());
    }
    
    private void drawGrid() {
        g2.setColor(GRID_COLOR);
        int cellSize = matrix.getCellSize();
        
        // Draw vertical lines
        for (int x = 0; x <= matrix.getGridWidth(); x++) {
            int xPos = x * cellSize;
            drawLine(xPos, 0, xPos, matrix.getTotalHeight());
        }
        
        // Draw horizontal lines
        for (int y = 0; y <= matrix.getGridHeight(); y++) {
            int yPos = y * cellSize;
            drawLine(0, yPos, matrix.getTotalWidth(), yPos);
        }
    }
    
    private void drawCells() {
        for (PhysicsObj cell : matrix.getAllCells()) {
            g2.setColor(cell.getColor());
            drawCircle(cell.getX(), cell.getY(), cell.getSize());
        }
    }
    
    // --- Drawing helper methods with camera transform ---
    
    private void drawCircle(double worldX, double worldY, int size) {
        double screenX = worldToScreenX(worldX);
        double screenY = worldToScreenY(worldY);
        int scaledSize = Math.max(1, (int) (size * zoom));
        
        g2.fillOval((int) (screenX - scaledSize / 2),
                    (int) (screenY - scaledSize / 2),
                    scaledSize, scaledSize);
    }
    
    private void drawLine(double worldX1, double worldY1, double worldX2, double worldY2) {
        int screenX1 = (int) worldToScreenX(worldX1);
        int screenY1 = (int) worldToScreenY(worldY1);
        int screenX2 = (int) worldToScreenX(worldX2);
        int screenY2 = (int) worldToScreenY(worldY2);
        
        g2.drawLine(screenX1, screenY1, screenX2, screenY2);
    }
    
    // --- Coordinate conversion ---
    
    public double screenToWorldX(int screenX) {
        return (screenX - matrix.getTotalWidth() / 2.0) / zoom + cameraX;
    }
    
    public double screenToWorldY(int screenY) {
        return (screenY - matrix.getTotalHeight() / 2.0) / zoom + cameraY;
    }
    
    public double worldToScreenX(double worldX) {
        return (worldX - cameraX) * zoom + matrix.getTotalWidth() / 2.0;
    }
    
    public double worldToScreenY(double worldY) {
        return (worldY - cameraY) * zoom + matrix.getTotalHeight() / 2.0;
    }
    
    // --- Helper methods ---
    
    private double[] getCenterOfMass(java.util.ArrayList<PhysicsObj> cells) {
        double totalMass = 0;
        double centerX = 0;
        double centerY = 0;
        
        for (PhysicsObj cell : cells) {
            double mass = cell.getMass();
            centerX += cell.getX() * mass;
            centerY += cell.getY() * mass;
            totalMass += mass;
        }
        
        if (totalMass > 0) {
            centerX /= totalMass;
            centerY /= totalMass;
        }
        
        return new double[]{centerX, centerY};
    }
    
    private double[] getBoundingBox(java.util.ArrayList<PhysicsObj> cells) {
        if (cells.isEmpty()) {
            return new double[]{0, 0, 0, 0};
        }
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        
        for (PhysicsObj cell : cells) {
            double x = cell.getX();
            double y = cell.getY();
            double halfSize = cell.getSize() / 2.0;
            
            minX = Math.min(minX, x - halfSize);
            minY = Math.min(minY, y - halfSize);
            maxX = Math.max(maxX, x + halfSize);
            maxY = Math.max(maxY, y + halfSize);
        }
        
        return new double[]{minX, minY, maxX, maxY};
    }
    
    /**
     * Draw UI overlay text.
     */
    public void drawUI(String text, int x, int y) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString(text, x, y);
    }
    
    public DrawingPanel getPanel() {
        return panel;
    }
    
    public Graphics2D getGraphics() {
        return g2;
    }
}