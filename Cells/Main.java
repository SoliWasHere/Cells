package Cells;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Main simulation runner with interactive controls.
 * 
 * CONTROLS:
 * - SPACE: Pause/unpause simulation
 * - WASD: Move camera (manual mode)
 * - E/Q: Zoom in/out (manual mode)
 * - 1-5: Select parameter (Gravity, Dampening, TimeStep, Camera, Entities)
 * - [ / ]: Decrease/increase selected parameter
 */
public class Main {
    private static Displayer displayer;
    private static InputManager inputManager;
    
    // Camera control
    private static boolean autoCamera = true;
    private static final double CAMERA_SPEED = 10.0;
    private static final double ZOOM_SPEED = 0.1;
    
    public static void main(String[] args) {
        // Initialize simulation world
        SimulationWorld.initialize(50, 20, 20);
        SimulationWorld world = SimulationWorld.getInstance();
        
        // Setup display
        displayer = new Displayer(world.getMatrix());
        inputManager = new InputManager();
        setupKeyboard();
        
        // Create initial scene
        createInitialScene();
        
        // Main simulation loop
        while (true) {
            // Handle user input
            inputManager.update(displayer);
            
            // Update simulation
            world.update();
            
            // Process entity additions/removals
            world.processPendingChanges();
            
            // Render
            render();
            
            // Frame limiting (60 FPS)
            displayer.getPanel().sleep(16);
        }
    }
    
    /**
     * Create the initial simulation scene.
     */
    private static void createInitialScene() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        int worldWidth = world.getMatrix().getTotalWidth();
        int worldHeight = world.getMatrix().getTotalHeight();
        double centerX = worldWidth / 2.0;
        double centerY = worldHeight / 2.0;
        
        // Create a massive central "sun"
        Food sun = new Food(centerX, centerY, 0);
        sun.setMass(1000);
        sun.setColor(new Color(255, 200, 0));
        sun.setSize(10);
        sun.setStatic(true);
        world.addEntity(sun);
        
        // Create a cell
        Cell cell = new Cell(100, 100);
        cell.setSize(20);
        cell.setColor(Color.MAGENTA);
        cell.setVelocity(10, 10);
        world.addEntity(cell);
        
        // Create orbiting food particles
        createOrbitingFood(1000);
        
        System.out.println("Scene created with " + world.getEntityCount() + " entities");
    }
    
    /**
     * Create food particles with orbital velocities around the center.
     */
    private static void createOrbitingFood(int count) {
        SimulationWorld world = SimulationWorld.getInstance();
        
        int worldWidth = world.getMatrix().getTotalWidth();
        int worldHeight = world.getMatrix().getTotalHeight();
        double centerX = worldWidth / 2.0;
        double centerY = worldHeight / 2.0;
        
        for (int i = 0; i < count; i++) {
            double x = world.getRandom().nextDouble() * worldWidth;
            double y = world.getRandom().nextDouble() * worldHeight;
            
            Food food = new Food(x, y);
            food.setMass(1);
            food.setSize(3);
            
            // Random color
            food.setColor(new Color(
                100 + world.getRandom().nextInt(155),
                100 + world.getRandom().nextInt(155),
                255
            ));
            
            // Calculate orbital velocity
            double dx = x - centerX;
            double dy = y - centerY;
            double angle = Math.atan2(dy, dx);
            double perpAngle = angle + Math.PI / 2.0;
            double speed = 5 + world.getRandom().nextDouble() * 5;
            
            food.setVelocity(
                Math.cos(perpAngle) * speed,
                Math.sin(perpAngle) * speed
            );
            
            food.dampingFactor = 1.0; // No damping for space particles
            
            world.addEntity(food);
        }
    }
    
    /**
     * Render the current frame.
     */
    private static void render() {
        // Update camera if in auto mode
        if (autoCamera) {
            displayer.updateCamera(SimulationWorld.getInstance().getEntities());
        }
        
        displayer.display();
    }
    
    /**
     * Setup keyboard input handling.
     */
    private static void setupKeyboard() {
        displayer.getPanel().addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                inputManager.keyPressed(e.getKeyCode());
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                inputManager.keyReleased(e.getKeyCode());
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                // Not used
            }
        });
    }
    
    /**
     * Toggle auto camera mode.
     */
    public static void toggleAutoCamera() {
        autoCamera = !autoCamera;
        System.out.println("Camera mode: " + (autoCamera ? "AUTO" : "MANUAL"));
    }
    
    /**
     * Check if auto camera is enabled.
     */
    public static boolean isAutoCameraEnabled() {
        return autoCamera;
    }
}