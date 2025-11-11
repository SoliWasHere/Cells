//MAIN.JAVA (UPDATED WITH INTERFACE)

package Cells;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Main entry point with clean spawning interface.
 */
public class Main {
    private static Displayer displayer;
    private static InputManager inputManager;
    private static MouseManager mouseManager;
    private static SimulationInterface simInterface;
    
    private static boolean autoCamera = false;
    
    public static void main(String[] args) {
        SimulationWorld.initialize(50, 600, 600);
        SimulationWorld world = SimulationWorld.getInstance();
        
        inputManager = new InputManager();
        mouseManager = new MouseManager();
        
        displayer = new Displayer((int) world.getTotalWidth(), (int) world.getTotalHeight(), mouseManager);
        world.setDisplayer(displayer);
        
        simInterface = new SimulationInterface(world);
        
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
            if ((cycles % 1 == 0) && !world.isPaused() && (world.getEntityCount() < 30000)) {
                spawnEnvironmentalFood(100);
            }
            
            world.processPendingChanges();
            
            render();
            
            displayer.getPanel().sleep(16);
        }
    }
    
    /**
     * Create initial scene with interface.
     */
    public static void createInitialScene() {
        SimulationWorld world = SimulationWorld.getInstance();
        
        double centerX = world.getTotalWidth() / 2.0;
        double centerY = world.getTotalHeight() / 2.0;
        
        // Create central sun
        Food sun = new Food(centerX, centerY, ChemicalSignature.random(), 0);
        sun.setMass(1000);
        sun.setColor(new Color(255, 200, 0));
        sun.setSize(10);
        sun.setStatic(true);
        world.addEntity(sun);
        
        // Create initial cell with random chemistry
        simInterface.spawnCell(100, 100);
        
        // Spawn food with environmental gradient
        spawnEnvironmentalFood(30000);
        
        System.out.println("=== 8D CHEMICAL EVOLUTION SYSTEM ===");
        System.out.println("Scene created with " + world.getEntityCount() + " entities");
        System.out.println("All entities exist in unified 8D chemical space");
        System.out.println("Food compatibility thresholds:");
        System.out.println("  - Distance < " + String.format("%.2f", ChemicalSignature.MAX_DISTANCE / 4.0) + ": Edible (varying efficiency)");
        System.out.println("  - Distance > " + String.format("%.2f", ChemicalSignature.MAX_DISTANCE / 4.0) + ": Inedible (0 energy)");
        System.out.println("  - Distance > " + String.format("%.2f", ChemicalSignature.MAX_DISTANCE * 3.0 / 4.0) + ": Toxic (lose energy)");
        System.out.println("Cells can eat other cells if chemistry matches and size is smaller!");
    }
    
    /**
     * Spawn food with environmental chemistry gradients.
     */
    private static void spawnEnvironmentalFood(int count) {
        double worldWidth = SimulationWorld.getInstance().getTotalWidth();
        double worldHeight = SimulationWorld.getInstance().getTotalHeight();
        
        simInterface.spawnFoods(count,
            // Spatial bias: creates density pockets
            (x, y) -> {
                double xNorm = x / worldWidth;
                double yNorm = y / worldHeight;
                double bias = (Math.sin(xNorm * Math.PI * 2) + Math.cos(yNorm * Math.PI * 2 + xNorm * Math.PI) + 2.0) / 4.0;
                return Math.pow(bias, 3.0);
            },
            // Chemistry function: creates spatial niches
            (x, y) -> {
                double xNorm = x / worldWidth;
                double yNorm = y / worldHeight;
                double[] comps = new double[ChemicalSignature.DIMENSIONS];
                
                for (int i = 0; i < ChemicalSignature.DIMENSIONS; i++) {
                    double angle = i * Math.PI / 4.0;
                    double freq = 2.0 + i * 0.5; // Different frequencies per dimension
                    
                    double pattern = Math.sin(xNorm * Math.PI * freq + angle) * 
                                   Math.cos(yNorm * Math.PI * freq - angle * 0.7);
                    
                    // Add some cross-dimension coupling
                    if (i > 0) {
                        double coupling = Math.sin((xNorm + yNorm) * Math.PI * freq + comps[i-1] * Math.PI);
                        pattern = pattern * 0.7 + coupling * 0.3;
                    }
                    
                    comps[i] = (pattern + 1.0) / 2.0;
                    
                    // Add noise
                    comps[i] += (Math.random() - 0.5) * 0.15;
                    comps[i] = Math.max(0, Math.min(1, comps[i]));
                }
                
                return new ChemicalSignature(comps);
            },
            // Nutrition varies with chemistry complexity
            (x, y) -> {
                double xNorm = x / worldWidth;
                double yNorm = y / worldHeight;
                double variation = 0.5 + 0.5 * Math.sin(xNorm * Math.PI * 3) * Math.cos(yNorm * Math.PI * 2);
                return 25.0 + variation * 50.0; // 25-75 range
            }
        );
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
    
    public static SimulationInterface getInterface() {
        return simInterface;
    }
}