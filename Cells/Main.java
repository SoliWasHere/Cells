//MAIN.JAVA (OPTIMIZED)

package Cells;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * OPTIMIZED: Reduced food spawning rate for better performance.
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
        
        displayer = new Displayer(world.getMatrix(), mouseManager);
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

            // OPTIMIZATION: Spawn food much less frequently (every 500 cycles instead of 100)
            // and spawn fewer at a time (10 instead of 100)
            if (cycles % 1 == 0) {
                createOrbitingFood(10);
            }
            
            world.processPendingChanges();
            
            render();
            
            displayer.getPanel().sleep(16);
        }
    }
    
    private static void createInitialScene() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        int worldWidth = world.getMatrix().getTotalWidth();
        int worldHeight = world.getMatrix().getTotalHeight();
        double centerX = worldWidth / 2.0;
        double centerY = worldHeight / 2.0;
        
        Food sun = new Food(centerX, centerY, 0);
        sun.setMass(1000);
        sun.setColor(new Color(255, 200, 0));
        sun.setSize(10);
        sun.setStatic(true);
        world.addEntity(sun);
        
        Cell cell = new Cell(100, 100);
        cell.setSize(20);
        cell.setColor(Color.MAGENTA);
        cell.setVelocity(10, 10);
        world.addEntity(cell);
        
        // OPTIMIZATION: Start with fewer particles
        createOrbitingFood(8000);
        
        System.out.println("Scene created with " + world.getEntityCount() + " entities");
        System.out.println("Hover over entities to see their info!");
    }
    
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