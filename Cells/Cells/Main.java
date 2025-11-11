//MAIN.JAVA (WITH ENVIRONMENTAL BIAS)

package Cells;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Main entry point with environmental food bias.
 */
public class Main {
    private static Displayer displayer;
    private static InputManager inputManager;
    private static MouseManager mouseManager;
    
    private static boolean autoCamera = false;
    
    public static void main(String[] args) {
        SimulationWorld.initialize(50, 600, 600);
        SimulationWorld world = SimulationWorld.getInstance();
        
        inputManager = new InputManager();
        mouseManager = new MouseManager();
        
        displayer = new Displayer((int) world.getTotalWidth(), (int) world.getTotalHeight(), mouseManager);
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

            // Spawn food periodically with environmental bias
            if ((cycles % 1 == 0) && !world.isPaused() && (world.getEntityCount() < 10000)) {
                createEnvironmentalFood(100);
            }
            
            world.processPendingChanges();
            
            render();
            
            displayer.getPanel().sleep(16);
        }
    }
    
    /**
     * Create initial scene (public so SimulationWorld can call it for reset).
     */
    public static void createInitialScene() {
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
        cell.setColor(new Color(125, 125, 125));
        cell.setVelocity(10, 10);
        world.addEntity(cell);
        
        // Create initial food with environmental bias
        createEnvironmentalFood(30000);
        
        System.out.println("Scene created with " + world.getEntityCount() + " entities");
        System.out.println("Multi-channel gradient system active with " + MultiChannelGradientField.NUM_CHANNELS + " channels");
        System.out.println("Food spawns biased by sin(x) + cos(y + x)");
        System.out.println("That's all lol");
    }
    //Let's see

    
    /**
     * Create food with environmental bias: more likely where sin(x) + cos(y + x) is higher.
     */
    private static void createEnvironmentalFood(int count) {
        SimulationWorld world = SimulationWorld.getInstance();
        
        double centerX = world.getTotalWidth() / 2.0;
        double centerY = world.getTotalHeight() / 2.0;
        
        for (int i = 0; i < count; i++) {
            double x, y;
            double bias;
            
            // Rejection sampling based on environmental function
            do {
                x = world.getRandom().nextDouble() * world.getTotalWidth();
                y = world.getRandom().nextDouble() * world.getTotalHeight();
                
                // Environmental bias function (normalized to [0, 1])
                double xScaled = x / 1000.0; // Scale for reasonable frequency
                double yScaled = y / 1000.0;
                bias = (Math.sin(xScaled) + Math.cos(yScaled + xScaled) + 2.0) / 4.0;
                bias = Math.pow(bias, (double) 3);
                
            } while (world.getRandom().nextDouble() > bias);
            
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
            
            food.dampingFactor = 0.95;
            food.setFoodId(Math.random(), Math.random());
            
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