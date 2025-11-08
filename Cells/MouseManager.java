package Cells;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

/**
 * Manages mouse input for entity selection and camera control.
 */
public class MouseManager implements MouseListener, MouseMotionListener, MouseWheelListener {
    private int mouseX;
    private int mouseY;
    private PhysicsObj hoveredEntity;
    private boolean mouseInWindow;
    
    private static final double HOVER_DISTANCE_THRESHOLD = 20.0; // pixels
    private static final double ZOOM_WHEEL_FACTOR = 0.1;
    
    public MouseManager() {
        this.mouseX = 0;
        this.mouseY = 0;
        this.hoveredEntity = null;
        this.mouseInWindow = false;
    }
    
    /**
     * Update hover detection (call every frame).
     */
    public void update(Displayer displayer) {
        if (!mouseInWindow) {
            hoveredEntity = null;
            return;
        }
        
        // Convert mouse position to world coordinates
        double worldX = displayer.screenToWorldX(mouseX);
        double worldY = displayer.screenToWorldY(mouseY);
        
        // Find closest entity to mouse
        hoveredEntity = findClosestEntity(worldX, worldY, displayer);
    }
    
    /**
     * Find the entity closest to the given world position.
     */
    private PhysicsObj findClosestEntity(double worldX, double worldY, Displayer displayer) {
        SimulationWorld world = SimulationWorld.getInstance();
        List<PhysicsObj> entities = world.getEntities();
        
        PhysicsObj closest = null;
        double closestScreenDist = HOVER_DISTANCE_THRESHOLD;
        
        for (PhysicsObj entity : entities) {
            // Calculate screen-space distance (accounts for zoom and entity size)
            double screenX = displayer.worldToScreenX(entity.getX());
            double screenY = displayer.worldToScreenY(entity.getY());
            double screenDist = Math.hypot(screenX - mouseX, screenY - mouseY);
            
            // Consider entity size in screen space
            double entityScreenRadius = entity.getSize() * displayer.zoom / 2.0;
            double effectiveDist = screenDist - entityScreenRadius;
            
            if (effectiveDist < closestScreenDist) {
                closestScreenDist = effectiveDist;
                closest = entity;
            }
        }
        
        return closest;
    }
    
    // === MouseListener ===
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (hoveredEntity != null) {
            System.out.println("Clicked: " + hoveredEntity);
            // Future: Could select entity, show more info, etc.
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        // Future: Could implement dragging entities
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        // Future: Could implement dragging entities
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        mouseInWindow = true;
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        mouseInWindow = false;
        hoveredEntity = null;
    }
    
    // === MouseMotionListener ===
    
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        // Future: Could implement camera panning with drag
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
    
    // === MouseWheelListener ===
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Zoom with mouse wheel (if not in auto camera mode)
        if (!Main.isAutoCameraEnabled()) {
            Displayer displayer = SimulationWorld.getInstance().getDisplayer();
            
            double zoomFactor = 1.0 - (e.getWheelRotation() * ZOOM_WHEEL_FACTOR);
            displayer.zoom *= zoomFactor;
            displayer.zoom = Math.max(0.1, Math.min(5.0, displayer.zoom));
        }
    }
    
    // === Getters ===
    
    public int getMouseX() {
        return mouseX;
    }
    
    public int getMouseY() {
        return mouseY;
    }
    
    public PhysicsObj getHoveredEntity() {
        return hoveredEntity;
    }
    
    public boolean isMouseInWindow() {
        return mouseInWindow;
    }
    
    public boolean isHoveringEntity() {
        return hoveredEntity != null;
    }
}