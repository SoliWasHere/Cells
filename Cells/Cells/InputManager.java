//INPUTMANAGER.JAVA (UPDATED)

package Cells;

import java.awt.Color;
import java.awt.event.KeyEvent;

/**
 * Manages keyboard input and parameter adjustment.
 */
public class InputManager {
    private static final String[] PARAMETER_NAMES = {
        "", "Gone sorryy", "TimeStep", "Camera", "Entities", "Clear"
    };
    
    private boolean[] keys = new boolean[256];
    private int selectedParameter = 1;
    
    /**
     * Handle key press event.
     */
    public void keyPressed(int keyCode) {
        if (keyCode < keys.length) {
            keys[keyCode] = true;
        }
        
        // Parameter selection (1-5)
        if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_5) {
            selectedParameter = keyCode - KeyEvent.VK_0;
            System.out.println("Selected: " + PARAMETER_NAMES[selectedParameter]);
        }
        
        // Pause toggle
        if (keyCode == KeyEvent.VK_SPACE) {
            togglePause();
        }
        
        // Toggle gradient visualization
        if (keyCode == KeyEvent.VK_G) {
            toggleGradientVisualization();
        }
    }
    
    /**
     * Handle key release event.
     */
    public void keyReleased(int keyCode) {
        if (keyCode < keys.length) {
            keys[keyCode] = false;
        }
        
        // Parameter adjustment
        if (keyCode == KeyEvent.VK_OPEN_BRACKET) {
            adjustParameter(false);
        } else if (keyCode == KeyEvent.VK_CLOSE_BRACKET) {
            adjustParameter(true);
        }
    }
    
    /**
     * Update input state (called every frame).
     */
    public void update(Displayer displayer) {
        // Manual camera control
        if (!Main.isAutoCameraEnabled()) {
            double speed = CAMERA_SPEED / displayer.zoom;
            
            if (keys[KeyEvent.VK_W]) displayer.cameraY -= speed;
            if (keys[KeyEvent.VK_S]) displayer.cameraY += speed;
            if (keys[KeyEvent.VK_A]) displayer.cameraX -= speed;
            if (keys[KeyEvent.VK_D]) displayer.cameraX += speed;
            
            if (keys[KeyEvent.VK_E]) {
                displayer.zoom *= (1 + ZOOM_SPEED);
            }
            if (keys[KeyEvent.VK_Q]) {
                displayer.zoom *= (1 - ZOOM_SPEED);
            }
            
            // Clamp zoom
            displayer.zoom = Math.max(0.01, Math.min(100.0, displayer.zoom));
        }
    }
    
    /**
     * Toggle simulation pause.
     */
    private void togglePause() {
        SimulationWorld world = SimulationWorld.getInstance();
        world.setPaused(!world.isPaused());
        System.out.println(world.isPaused() ? "PAUSED" : "RUNNING");
    }
    
    /**
     * Toggle gradient field visualization.
     */
    private void toggleGradientVisualization() {
        SimulationWorld world = SimulationWorld.getInstance();
        Displayer displayer = world.getDisplayer();
        displayer.toggleGradientField();
        System.out.println("Gradient visualization: " + (displayer.isShowingGradientField() ? "ON" : "OFF"));
    }
    
    /**
     * Adjust the selected parameter.
     */
    private void adjustParameter(boolean increase) {
        SimulationWorld world = SimulationWorld.getInstance();
        double factor = increase ? 1.1 : 0.9;
        
        switch (selectedParameter) {
            case 1: 
                System.out.println("GONE");
                
            case 2: // Time step
                adjustTimeStep(factor);
                break;
                
            case 3: // Camera mode
                Main.toggleAutoCamera();
                break;
                
            case 4: // Add/remove entities
                adjustEntityCount(increase);
                break;
                
            case 5: // Clear all
                if (increase) {
                    clearAllEntities();
                }
                break;
        }
    }
    
    /**
     * Adjust time step.
     */
    private void adjustTimeStep(double factor) {
        SimulationWorld world = SimulationWorld.getInstance();
        double newDt = world.getTimeStep() * factor;
        world.setTimeStep(newDt);
        System.out.printf("Time step: %.3f\n", newDt);
    }
    
    /**
     * Add or remove entities.
     */
    private void adjustEntityCount(boolean increase) {
        SimulationWorld world = SimulationWorld.getInstance();
        
        if (increase) {
            // Add 10 food particles
            for (int i = 0; i < 10; i++) {
                double x = world.getRandom().nextDouble() * world.getTotalWidth();
                double y = world.getRandom().nextDouble() * world.getTotalHeight();
                
                Food food = new Food(x, y);
                food.setMass(1);
                food.setSize(3);
                food.setColor(new Color(
                    100 + world.getRandom().nextInt(155),
                    100 + world.getRandom().nextInt(155),
                    255
                ));
                food.dampingFactor = 0.99;
                
                world.queueAddition(food);
            }
            System.out.println("Added 10 food particles");
        } else {
            // Remove 10 entities (but keep at least 1)
            int toRemove = Math.min(10, world.getEntityCount() - 1);
            var entities = world.getEntities();
            
            for (int i = 0; i < toRemove && i < entities.size(); i++) {
                entities.get(entities.size() - 1 - i).destroy();
            }
            
            System.out.println("Removed " + toRemove + " entities");
        }
    }
    
    /**
     * Clear all entities except static ones.
     */
    private void clearAllEntities() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        for (PhysicsObj entity : world.getEntities()) {
            if (!entity.isStatic()) {
                entity.destroy();
            }
        }
        
        System.out.println("Cleared all non-static entities");
    }
    
    private static final double CAMERA_SPEED = 10.0;
    private static final double ZOOM_SPEED = 0.1;
}