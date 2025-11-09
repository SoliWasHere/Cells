//MAIN.JAVA (GRADIENT-BASED)

package Cells;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Main entry point for gradient-based simulation.
 */
public class Main {
    private static Displayer displayer;
    private static InputManager inputManager;
    private static MouseManager mouseManager;
    
    private static boolean autoCamera = false;
    
    public static void main(String[] args) {
        SimulationWorld.initialize(50, 200, 200);
        SimulationWorld world = SimulationWorld.getInstance();
        
        inputManager = new InputManager();
        mouseManager = new MouseManager();
        
        displayer = new Displayer( (int) world.getTotalWidth(), (int) world.getTotalHeight(), mouseManager);
        world.setDisplayer(displayer);
        
        setupKeyboard();
        setupMouse();
        
        createInitialScene();
        int cycles = 0;
        
        while (true) {
            cycles++;

            inputManager.update(displayer);
            mouseManager.update(displayer);
            
            world.update();

            // Spawn food periodically
            if (cycles % 50 == 0) {
                createOrbitingFood(10);
            }
            
            world.processPendingChanges();
            
            render();
            
            displayer.getPanel().sleep(16);
        }
    }
    
    private static void createInitialScene() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        double centerX = world.getTotalWidth() / 2.0;
        double centerY = world.getTotalHeight() / 2.0;
        
        // Create central sun
        Food sun = new Food(centerX, centerY, 0);
        sun.setMass(1000);
        sun.setColor(new Color(255, 200, 0));
        sun.setSize(10);
        sun.setStatic(true);
        world.addEntity(sun);
        
        // Create initial cell
        Cell cell = new Cell(100, 100);
        cell.setSize(20);
        cell.setColor(Color.MAGENTA);
        cell.setVelocity(10, 10);
        world.addEntity(cell);
        
        // Create initial food
        createOrbitingFood(8000);
        
        System.out.println("Scene created with " + world.getEntityCount() + " entities");
        System.out.println("Press G to toggle gradient visualization");
        System.out.println("Hover over entities to see their info!");
    }
    
    private static void createOrbitingFood(int count) {
        SimulationWorld world = SimulationWorld.getInstance();
        
        double centerX = world.getTotalWidth() / 2.0;
        double centerY = world.getTotalHeight() / 2.0;
        
        for (int i = 0; i < count; i++) {
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
            
            double dx = x - centerX;
            double dy = y - centerY;
            double angle = Math.atan2(dy, dx);
            double perpAngle = angle + Math.PI / 2.0;
            double speed = 5 + world.getRandom().nextDouble() * 5;
            
            food.setVelocity(
                Math.cos(perpAngle) * speed,
                Math.sin(perpAngle) * speed
            );
            
            food.dampingFactor = 1.0;
            
            world.addEntity(food);
        }
    }
    
    private static void render() {
        if (autoCamera) {
            displayer.updateCamera(SimulationWorld.getInstance().getEntities());
        }
        
        displayer.display();
    }
    
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
            public void keyTyped(KeyEvent e) {}
        });
    }
    
    private static void setupMouse() {
        displayer.getPanel().addMouseListener(mouseManager);
    }
    
    public static void toggleAutoCamera() {
        autoCamera = !autoCamera;
        System.out.println("Camera mode: " + (autoCamera ? "AUTO" : "MANUAL"));
    }
    
    public static boolean isAutoCameraEnabled() {
        return autoCamera;
    }
}