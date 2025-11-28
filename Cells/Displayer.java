//DISPLAYER.JAVA (WITH MULTI-CHANNEL VISUALIZATION)

package Cells;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Handles rendering with multi-channel gradient visualization.
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
    
    private static final double CAMERA_SMOOTH = 0.05;
    private static final double ZOOM_SMOOTH = 0.1;
    private static final double ZOOM_MARGIN = 100.0;
    
    private static final Color BACKGROUND_COLOR = new Color(10, 10, 15);
    private static final Color UI_TEXT_COLOR = new Color(200, 200, 200);
    private static final Color LOOP_LINE_COLOR = new Color(255, 255, 255, 50);
    private static final Font UI_FONT = new Font("Monospaced", Font.PLAIN, 12);
    
    // Gradient visualization
    private boolean showGradientField = true;
    private int gradientResolution = 20;
    
    public Displayer(int width, int height, MouseManager mouseManager) {
        this.panel = new DrawingPanel(Math.min(1000, width), Math.min(1000, height));
        this.buffer = new BufferedImage(
            Math.min(1000, width), 
            Math.min(1000, height), 
            BufferedImage.TYPE_INT_ARGB
        );
        this.g2 = buffer.createGraphics();
        this.panelGraphics = panel.getGraphics();
        this.mouseManager = mouseManager;
        
        this.cameraX = width / 2.0;
        this.cameraY = height / 2.0;
        this.zoom = 1.0;
        
        setupRenderingHints();
    }
    
    private void setupRenderingHints() {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
                           RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    
    public void display() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        clearBuffer();
        
        if (showGradientField) {
            drawMultiChannelGradientField();
        }
        
        drawEntitiesWithLooping(world.getEntities());
        drawLoopLines();
        drawUI(world);
        drawTooltips();
        
        panelGraphics.drawImage(buffer, 0, 0, null);
    }
    
    private void drawMultiChannelGradientField() {
        SimulationWorld world = SimulationWorld.getInstance();
        MultiChannelGradientField multiField = world.getMultiChannelField();
        
        int screenWidth = buffer.getWidth();
        int screenHeight = buffer.getHeight();
        
        for (int screenX = 0; screenX < screenWidth; screenX += gradientResolution) {
            for (int screenY = 0; screenY < screenHeight; screenY += gradientResolution) {
                double worldX = screenToWorldX(screenX);
                double worldY = screenToWorldY(screenY);
                
                // Sample gradient field
                GradientSample sample = multiField.sampleAll(worldX, worldY);
                
                float intensity = (float)Math.min(1.0, sample.strength / 100.0);
                
                if (intensity > 0.05f) {
                    // Visualize as white gradient
                    Color gradientColor = new Color(intensity, intensity, intensity, intensity * 0.3f);
                    g2.setColor(gradientColor);
                    
                    int size = gradientResolution;
                    g2.fillRect(screenX, screenY, size, size);
                    
                    // Draw direction arrow for strong gradients
                    if (zoom > 0.5 && intensity > 0.3f) {
                        drawGradientArrow(screenX + size/2, screenY + size/2, 
                                        sample.directionX, sample.directionY, 
                                        intensity, Color.WHITE);
                    }
                }
            }
        }
    }
    
    /**
     * Draw gradient arrow with specified color.
     */
    private void drawGradientArrow(int x, int y, double dirX, double dirY, float intensity, Color color) {
        g2.setColor(new Color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, intensity * 0.5f));
        
        int arrowLength = (int)(gradientResolution * 0.4);
        int endX = x + (int)(dirX * arrowLength);
        int endY = y + (int)(dirY * arrowLength);
        
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(x, y, endX, endY);
        
        // Arrowhead
        double angle = Math.atan2(dirY, dirX);
        int headSize = 3;
        int[] xPoints = {
            endX,
            endX - (int)(headSize * Math.cos(angle - Math.PI/6)),
            endX - (int)(headSize * Math.cos(angle + Math.PI/6))
        };
        int[] yPoints = {
            endY,
            endY - (int)(headSize * Math.sin(angle - Math.PI/6)),
            endY - (int)(headSize * Math.sin(angle + Math.PI/6))
        };
        g2.fillPolygon(xPoints, yPoints, 3);
    }
    
    /**
     * Draw entities with looping (show copies across boundaries).
     */
    private void drawEntitiesWithLooping(List<PhysicsObj> entities) {
        SimulationWorld world = SimulationWorld.getInstance();
        double worldWidth = world.getTotalWidth();
        double worldHeight = world.getTotalHeight();
        
        // Draw in layers (Food, then Cells, then others)
        for (PhysicsObj entity : entities) {
            if (entity instanceof Food) {
                drawEntityWithLooping(entity, worldWidth, worldHeight);
            }
        }
        
        for (PhysicsObj entity : entities) {
            if (entity instanceof Cell) {
                drawEntityWithLooping(entity, worldWidth, worldHeight);
            }
        }
        
        for (PhysicsObj entity : entities) {
            if (!(entity instanceof Food) && !(entity instanceof Cell)) {
                drawEntityWithLooping(entity, worldWidth, worldHeight);
            }
        }
    }
    
    /**
     * Draw an entity and its looping copies if they're visible.
     */
    private void drawEntityWithLooping(PhysicsObj entity, double worldWidth, double worldHeight) {
        double x = entity.getX();
        double y = entity.getY();
        
        // Draw main entity
        drawEntity(entity, x, y);
        
        // Check if entity is near boundaries and draw copies
        double screenX = worldToScreenX(x);
        double screenY = worldToScreenY(y);
        double entityScreenSize = entity.getSize() * zoom;
        
        // Left/Right wrapping
        if (screenX < entityScreenSize) {
            drawEntity(entity, x + worldWidth, y);
        } else if (screenX > buffer.getWidth() - entityScreenSize) {
            drawEntity(entity, x - worldWidth, y);
        }
        
        // Top/Bottom wrapping
        if (screenY < entityScreenSize) {
            drawEntity(entity, x, y + worldHeight);
        } else if (screenY > buffer.getHeight() - entityScreenSize) {
            drawEntity(entity, x, y - worldHeight);
        }
        
        // Corner wrapping
        if (screenX < entityScreenSize && screenY < entityScreenSize) {
            drawEntity(entity, x + worldWidth, y + worldHeight);
        } else if (screenX > buffer.getWidth() - entityScreenSize && screenY < entityScreenSize) {
            drawEntity(entity, x - worldWidth, y + worldHeight);
        } else if (screenX < entityScreenSize && screenY > buffer.getHeight() - entityScreenSize) {
            drawEntity(entity, x + worldWidth, y - worldHeight);
        } else if (screenX > buffer.getWidth() - entityScreenSize && screenY > buffer.getHeight() - entityScreenSize) {
            drawEntity(entity, x - worldWidth, y - worldHeight);
        }
    }
    
    /**
     * Draw a single entity at specific world coordinates.
     */
    private void drawEntity(PhysicsObj entity, double worldX, double worldY) {
        g2.setColor(entity.getColor());
        drawCircle(worldX, worldY, entity.getSize());
    }
    
    /**
     * Draw thin white lines showing where the world loops.
     */
    private void drawLoopLines() {
        SimulationWorld world = SimulationWorld.getInstance();
        double worldWidth = world.getTotalWidth();
        double worldHeight = world.getTotalHeight();
        
        g2.setColor(LOOP_LINE_COLOR);
        g2.setStroke(new BasicStroke(1.0f));
        
        // Draw vertical lines
        for (double worldX = 0; worldX <= worldWidth; worldX += worldWidth) {
            double screenX = worldToScreenX(worldX);
            if (screenX >= 0 && screenX <= buffer.getWidth()) {
                g2.drawLine((int)screenX, 0, (int)screenX, buffer.getHeight());
            }
        }
        
        // Draw horizontal lines
        for (double worldY = 0; worldY <= worldHeight; worldY += worldHeight) {
            double screenY = worldToScreenY(worldY);
            if (screenY >= 0 && screenY <= buffer.getHeight()) {
                g2.drawLine(0, (int)screenY, buffer.getWidth(), (int)screenY);
            }
        }
    }
    
    public void updateCamera(List<PhysicsObj> entities) {
        if (entities.isEmpty()) return;
        
        Vector2D centerOfMass = calculateCenterOfMass(entities);
        double idealZoom = calculateIdealZoom(entities);
        
        cameraX += (centerOfMass.x - cameraX) * CAMERA_SMOOTH;
        cameraY += (centerOfMass.y - cameraY) * CAMERA_SMOOTH;
        zoom += (idealZoom - zoom) * ZOOM_SMOOTH;
    }

    private void drawTooltips() {
        if (mouseManager.isHoveringEntity()) {
            PhysicsObj hoveredEntity = mouseManager.getHoveredEntity();
            
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
    
    private void clearBuffer() {
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
    }
    
    private void drawUI(SimulationWorld world) {
        g2.setColor(UI_TEXT_COLOR);
        g2.setFont(UI_FONT);
        
        int x = 10;
        int y = 20;
        int lineHeight = 15;
        
        drawText(String.format("Entities: %d", world.getEntityCount()), x, y);
        y += lineHeight;
        
        drawText(String.format("Time Step: %.3f", world.getTimeStep()), x, y);
        y += lineHeight;
        
        drawText(String.format("Status: %s", world.isPaused() ? "PAUSED" : "RUNNING"), x, y);
        y += lineHeight;
        
        drawText(String.format("Zoom: %.2fx", zoom), x, y);
        y += lineHeight;
        
        drawText(String.format("Gradient: %s (%d channels)", showGradientField ? "ON" : "OFF", MultiChannelGradientField.NUM_CHANNELS), x, y);
        y += lineHeight;
        
        drawControlsHelp();
    }
    
    private void drawControlsHelp() {
        g2.setColor(new Color(150, 150, 150, 200));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        
        String[] controls = {
            "SPACE:Pause  WASD:Camera  E/Q:Zoom  G:Toggle Gradient  1-5:Parameter  [/]:Adjust"
        };
        
        int y = buffer.getHeight() - 10;
        for (int i = controls.length - 1; i >= 0; i--) {
            drawText(controls[i], 10, y);
            y -= 12;
        }
    }
    
    private void drawText(String text, int x, int y) {
        g2.drawString(text, x, y);
    }
    
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
    
    // Coordinate conversion
    public double screenToWorldX(double screenX) {
        return (screenX - buffer.getWidth() / 2.0) / zoom + cameraX;
    }
    
    public double screenToWorldY(double screenY) {
        return (screenY - buffer.getHeight() / 2.0) / zoom + cameraY;
    }
    
    public double worldToScreenX(double worldX) {
        return (worldX - cameraX) * zoom + buffer.getWidth() / 2.0;
    }
    
    public double worldToScreenY(double worldY) {
        return (worldY - cameraY) * zoom + buffer.getHeight() / 2.0;
    }
    
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
    
    public DrawingPanel getPanel() {
        return panel;
    }
    
    public Graphics2D getGraphics() {
        return g2;
    }
    
    public void toggleGradientField() {
        showGradientField = !showGradientField;
    }
    
    public boolean isShowingGradientField() {
        return showGradientField;
    }
    
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