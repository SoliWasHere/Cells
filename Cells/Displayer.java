package Cells;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Handles all rendering with camera system and double buffering.
 * Provides smooth camera tracking and zoom capabilities.
 */
public class Displayer {
    private final DrawingPanel panel;
    private final BufferedImage buffer;
    private final Graphics2D g2;
    private final Graphics panelGraphics;

    private MouseManager mouseManager;
    
    // Camera system
    public double cameraX;
    public double cameraY;
    public double zoom;
    
    // Camera smoothing
    private static final double CAMERA_SMOOTH = 0.05;
    private static final double ZOOM_SMOOTH = 0.1;
    private static final double ZOOM_MARGIN = 100.0;
    
    // Visual constants
    private static final Color GRID_COLOR = new Color(40, 40, 40);
    private static final Color BACKGROUND_COLOR = new Color(10, 10, 15);
    private static final Color UI_TEXT_COLOR = new Color(200, 200, 200);
    private static final Font UI_FONT = new Font("Monospaced", Font.PLAIN, 12);
    
    /**
     * Create a new displayer for the simulation.
     */
    public Displayer(Matrix matrix, MouseManager mouseManager) {
        int width = matrix.getTotalWidth();
        int height = matrix.getTotalHeight();
        
        this.panel = new DrawingPanel(width, height);
        this.buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.g2 = buffer.createGraphics();
        this.panelGraphics = panel.getGraphics();
        this.mouseManager = mouseManager;
        
        // Initialize camera at center
        this.cameraX = width / 2.0;
        this.cameraY = height / 2.0;
        this.zoom = 1.0;
        
        setupRenderingHints();
    }
    
    /**
     * Configure rendering quality settings.
     */
    private void setupRenderingHints() {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
                           RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    
    /**
     * Main render method - draws the entire scene.
     */
    public void display() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        clearBuffer();
        drawGrid(world.getMatrix());
        drawEntities(world.getEntities());
        drawUI(world);

        drawTooltips();
        
        // Copy buffer to screen
        panelGraphics.drawImage(buffer, 0, 0, null);
    }
    
    /**
     * Update camera to follow entities smoothly.
     */
    public void updateCamera(List<PhysicsObj> entities) {
        if (entities.isEmpty()) return;
        
        // Calculate center of mass
        Vector2D centerOfMass = calculateCenterOfMass(entities);
        
        // Calculate ideal zoom based on entity spread
        double idealZoom = calculateIdealZoom(entities);
        
        // Smoothly interpolate camera position
        cameraX += (centerOfMass.x - cameraX) * CAMERA_SMOOTH;
        cameraY += (centerOfMass.y - cameraY) * CAMERA_SMOOTH;
        
        // Smoothly interpolate zoom
        zoom += (idealZoom - zoom) * ZOOM_SMOOTH;
    }

    /*
     * Draw tooltip for hovered entity.
     */
    private void drawTooltips() {
        if (mouseManager.isHoveringEntity()) {
            PhysicsObj hoveredEntity = mouseManager.getHoveredEntity();
            
            // Highlight hovered entity
            g2.setColor(new Color(255, 255, 255, 100));
            g2.setStroke(new BasicStroke(2));
            double screenX = worldToScreenX(hoveredEntity.getX());
            double screenY = worldToScreenY(hoveredEntity.getY());
            int screenSize = Math.max(1, (int) (hoveredEntity.getSize() * zoom));
            
            g2.drawOval(
                (int) (screenX - screenSize / 2.0 - 3),
                (int) (screenY - screenSize / 2.0 - 3),
                screenSize + 6,
                screenSize + 6
            );
            
            // Draw tooltip
            EntityTooltip.draw(
                g2, 
                hoveredEntity, 
                mouseManager.getMouseX(), 
                mouseManager.getMouseY(),
                buffer.getWidth(),
                buffer.getHeight()
            );
        }
    }
    
    /**
     * Clear the render buffer.
     */
    private void clearBuffer() {
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
    }
    
    /**
     * Draw the spatial grid.
     */
    private void drawGrid(Matrix matrix) {
        g2.setColor(GRID_COLOR);
        int cellSize = matrix.getCellSize();
        
        // Only draw grid if zoomed in enough
        if (zoom < 0.5) return;
        
        // Calculate visible grid range
        int startX = Math.max(0, (int) screenToWorldX(0) / cellSize);
        int endX = Math.min(matrix.getGridWidth(), (int) screenToWorldX(buffer.getWidth()) / cellSize + 1);
        int startY = Math.max(0, (int) screenToWorldY(0) / cellSize);
        int endY = Math.min(matrix.getGridHeight(), (int) screenToWorldY(buffer.getHeight()) / cellSize + 1);
        
        // Draw vertical lines
        for (int x = startX; x <= endX; x++) {
            int worldX = x * cellSize;
            drawLine(worldX, 0, worldX, matrix.getTotalHeight());
        }
        
        // Draw horizontal lines
        for (int y = startY; y <= endY; y++) {
            int worldY = y * cellSize;
            drawLine(0, worldY, matrix.getTotalWidth(), worldY);
        }
    }
    
    /**
     * Draw all entities in the world.
     */
    private void drawEntities(List<PhysicsObj> entities) {
        // Draw in layers: food first, then cells
        for (PhysicsObj entity : entities) {
            if (entity instanceof Food) {
                drawEntity(entity);
            }
        }
        
        for (PhysicsObj entity : entities) {
            if (entity instanceof Cell) {
                drawEntity(entity);
            }
        }
        
        // Draw other entities
        for (PhysicsObj entity : entities) {
            if (!(entity instanceof Food) && !(entity instanceof Cell)) {
                drawEntity(entity);
            }
        }
    }
    
    /**
     * Draw a single entity.
     */
    private void drawEntity(PhysicsObj entity) {
        g2.setColor(entity.getColor());
        
        drawCircle(entity.getX(), entity.getY(), entity.getSize());
        
        // Draw velocity vector for debugging (if zoomed in)
        // if (zoom > 1.5 && !entity.isStatic()) {
        //    drawVelocityVector(entity);
        //}
    }
    
    /**
     * Draw a glowing halo around large masses.
     */
    private void drawGlow(PhysicsObj entity) {
        Color glowColor = new Color(
            entity.getColor().getRed(),
            entity.getColor().getGreen(),
            entity.getColor().getBlue(),
            50
        );
        
        g2.setColor(glowColor);
        int glowSize = entity.getSize() * 2;
        drawCircle(entity.getX(), entity.getY(), glowSize);
    }
    
    /**
     * Draw velocity vector for debugging.
     */
    private void drawVelocityVector(PhysicsObj entity) {
        double speed = entity.getSpeed();
        if (speed < 0.1) return;
        
        g2.setColor(new Color(255, 255, 0, 150));
        
        double endX = entity.getX() + entity.getVelocityX() * 2;
        double endY = entity.getY() + entity.getVelocityY() * 2;
        
        drawLine(entity.getX(), entity.getY(), endX, endY);
    }
    
    /**
     * Draw UI overlay with simulation stats.
     */
    private void drawUI(SimulationWorld world) {
        g2.setColor(UI_TEXT_COLOR);
        g2.setFont(UI_FONT);
        
        int x = 10;
        int y = 20;
        int lineHeight = 15;
        
        // Simulation stats
        drawText(String.format("Entities: %d", world.getEntityCount()), x, y);
        y += lineHeight;
        
        drawText(String.format("Gravity: %.2f", world.getGravityConstant()), x, y);
        y += lineHeight;
        
        drawText(String.format("Time Step: %.3f", world.getTimeStep()), x, y);
        y += lineHeight;
        
        drawText(String.format("Status: %s", world.isPaused() ? "PAUSED" : "RUNNING"), x, y);
        y += lineHeight;
        
        drawText(String.format("Zoom: %.2fx", zoom), x, y);
        y += lineHeight;
        
        // Controls help (bottom of screen)
        drawControlsHelp();
    }
    
    /**
     * Draw controls help at bottom of screen.
     */
    private void drawControlsHelp() {
        g2.setColor(new Color(150, 150, 150, 200));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        
        String[] controls = {
            "SPACE:Pause  WASD:Camera  E/Q:Zoom  1-5:Parameter  [/]:Adjust"
        };
        
        int y = buffer.getHeight() - 10;
        for (int i = controls.length - 1; i >= 0; i--) {
            drawText(controls[i], 10, y);
            y -= 12;
        }
    }
    
    /**
     * Draw text directly on buffer (no camera transform).
     */
    private void drawText(String text, int x, int y) {
        g2.drawString(text, x, y);
    }
    
    // === Drawing Primitives with Camera Transform ===
    
    /**
     * Draw a filled circle in world coordinates.
     */
    private void drawCircle(double worldX, double worldY, int size) {
        double screenX = worldToScreenX(worldX);
        double screenY = worldToScreenY(worldY);
        int scaledSize = Math.max(1, (int) (size * zoom));
        
        g2.fillOval(
            (int) (screenX - scaledSize / 2.0),
            (int) (screenY - scaledSize / 2.0),
            scaledSize,
            scaledSize
        );
    }
    
    /**
     * Draw a line in world coordinates.
     */
    private void drawLine(double worldX1, double worldY1, double worldX2, double worldY2) {
        int screenX1 = (int) worldToScreenX(worldX1);
        int screenY1 = (int) worldToScreenY(worldY1);
        int screenX2 = (int) worldToScreenX(worldX2);
        int screenY2 = (int) worldToScreenY(worldY2);
        
        g2.drawLine(screenX1, screenY1, screenX2, screenY2);
    }
    
    // === Coordinate Conversion ===
    
    /**
     * Convert screen X coordinate to world X coordinate.
     */
    public double screenToWorldX(double screenX) {
        return (screenX - buffer.getWidth() / 2.0) / zoom + cameraX;
    }
    
    /**
     * Convert screen Y coordinate to world Y coordinate.
     */
    public double screenToWorldY(double screenY) {
        return (screenY - buffer.getHeight() / 2.0) / zoom + cameraY;
    }
    
    /**
     * Convert world X coordinate to screen X coordinate.
     */
    public double worldToScreenX(double worldX) {
        return (worldX - cameraX) * zoom + buffer.getWidth() / 2.0;
    }
    
    /**
     * Convert world Y coordinate to screen Y coordinate.
     */
    public double worldToScreenY(double worldY) {
        return (worldY - cameraY) * zoom + buffer.getHeight() / 2.0;
    }
    
    // === Camera Calculation Helpers ===
    
    /**
     * Calculate the center of mass of all entities.
     */
    private Vector2D calculateCenterOfMass(List<PhysicsObj> entities) {
        double totalMass = 0;
        double centerX = 0;
        double centerY = 0;
        
        for (PhysicsObj entity : entities) {
            double mass = entity.getMass();
            centerX += entity.getX() * mass;
            centerY += entity.getY() * mass;
            totalMass += mass;
        }
        
        if (totalMass > 0) {
            centerX /= totalMass;
            centerY /= totalMass;
        }
        
        return new Vector2D(centerX, centerY);
    }
    
    /**
     * Calculate ideal zoom level to fit all entities.
     */
    private double calculateIdealZoom(List<PhysicsObj> entities) {
        if (entities.isEmpty()) return 1.0;
        
        BoundingBox bounds = calculateBoundingBox(entities);
        
        double objectWidth = bounds.width + ZOOM_MARGIN * 2;
        double objectHeight = bounds.height + ZOOM_MARGIN * 2;
        
        double zoomForWidth = buffer.getWidth() / objectWidth;
        double zoomForHeight = buffer.getHeight() / objectHeight;
        
        double idealZoom = Math.min(zoomForWidth, zoomForHeight);
        return Math.max(0.1, Math.min(3.0, idealZoom));
    }
    
    /**
     * Calculate bounding box containing all entities.
     */
    private BoundingBox calculateBoundingBox(List<PhysicsObj> entities) {
        if (entities.isEmpty()) {
            return new BoundingBox(0, 0, 0, 0);
        }
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        
        for (PhysicsObj entity : entities) {
            double x = entity.getX();
            double y = entity.getY();
            double halfSize = entity.getSize() / 2.0;
            
            minX = Math.min(minX, x - halfSize);
            minY = Math.min(minY, y - halfSize);
            maxX = Math.max(maxX, x + halfSize);
            maxY = Math.max(maxY, y + halfSize);
        }
        
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }
    
    // === Getters ===
    
    public DrawingPanel getPanel() {
        return panel;
    }
    
    public Graphics2D getGraphics() {
        return g2;
    }
    
    /**
     * Simple bounding box container.
     */
    private static class BoundingBox {
        final double x, y, width, height;
        
        BoundingBox(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}