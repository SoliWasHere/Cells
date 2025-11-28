//MAIN.JAVA (DISCRETE FOOD TYPES WITH SPATIAL NICHES)

package Cells;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Main with discrete food types creating clear niches.
 */
public class Main {
    private static Displayer displayer;
    private static InputManager inputManager;
    private static MouseManager mouseManager;
    private static SimulationInterface simInterface;
    
    private static boolean autoCamera = false;
    
    // Food region parameters - each quadrant specialized
    private static int currentCycle = 0;
    private static final int CYCLE_LENGTH = 600; // 10 seconds per cycle
    
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

            // Spawn food continuously in different regions
            if (!world.isPaused() && cycles % 3 == 0) {
                spawnRegionalFood();
            }
            
            // Environmental shift every 10 seconds
            if (!world.isPaused() && cycles % CYCLE_LENGTH == 0) {
                environmentalShift();
            }
            
            world.processPendingChanges();
            
            render();
            
            displayer.getPanel().sleep(16);
        }
    }
    
    /**
     * Create initial scene with 4 clear regions.
     */
    public static void createInitialScene() {
        SimulationWorld world = SimulationWorld.getInstance();
        double width = world.getTotalWidth();
        double height = world.getTotalHeight();
        
        // Spawn initial cells in center
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double dist = Math.random() * 100;
            Cell cell = new Cell(
                width / 2 + Math.cos(angle) * dist,
                height / 2 + Math.sin(angle) * dist,
                ChemicalSignature.random()
            );
            world.addEntity(cell);
        }
        
        // Spawn initial food in all regions
        for (int i = 0; i < 20000; i++) {
            spawnRegionalFood();
        }
        
        System.out.println("=== DISCRETE FOOD TYPE SIMULATION ===");
        System.out.println("Food types:");
        System.out.println("  RED (top-left) - high energy, common");
        System.out.println("  GREEN (top-right) - medium energy, very common");
        System.out.println("  BLUE (bottom-left) - low energy, extremely common");
        System.out.println("  GRAY - dead matter, appears randomly");
        System.out.println("");
        System.out.println("Cells evolve red/green/blue eating efficiencies");
        System.out.println("Specialists eat faster but generalists survive shifts");
        System.out.println("Environmental shift every 10 seconds");
    }
    
    /**
     * Spawn food in specific regions based on type.
     */
    private static void spawnRegionalFood() {
        SimulationWorld world = SimulationWorld.getInstance();
        double width = world.getTotalWidth();
        double height = world.getTotalHeight();
        
        // Cycle determines which food type is abundant
        int dominantType = (currentCycle / CYCLE_LENGTH) % 3;
        
        // Choose random food type with bias
        int foodType;
        double r = Math.random();
        
        if (r < 0.5) {
            // 50% chance: spawn dominant type
            foodType = dominantType;
        } else if (r < 0.8) {
            // 30% chance: spawn other types
            foodType = (dominantType + 1 + (int)(Math.random() * 2)) % 3;
        } else {
            // 20% chance: spawn gray dead matter
            foodType = 3;
        }
        
        // Choose region based on food type
        double x, y;
        
        switch (foodType) {
            case 0: // RED - top-left quadrant
                x = Math.random() * width * 0.6;
                y = Math.random() * height * 0.6;
                break;
            case 1: // GREEN - top-right quadrant
                x = width * 0.4 + Math.random() * width * 0.6;
                y = Math.random() * height * 0.6;
                break;
            case 2: // BLUE - bottom half
                x = Math.random() * width;
                y = height * 0.4 + Math.random() * height * 0.6;
                break;
            case 3: // GRAY - anywhere
            default:
                x = Math.random() * width;
                y = Math.random() * height;
                break;
        }
        
        // Add some randomness
        x += (Math.random() - 0.5) * 50;
        y += (Math.random() - 0.5) * 50;
        x = ((x % width) + width) % width;
        y = ((y % height) + height) % height;
        
        // Nutrition varies by type
        double nutrition;
        Color color;
        
        switch (foodType) {
            case 0: // RED - high energy, rare
                nutrition = 80 + Math.random() * 40;
                color = new Color(200 + (int)(Math.random() * 55), 50, 50);
                break;
            case 1: // GREEN - medium energy, common
                nutrition = 50 + Math.random() * 30;
                color = new Color(50, 200 + (int)(Math.random() * 55), 50);
                break;
            case 2: // BLUE - low energy, very common
                nutrition = 30 + Math.random() * 20;
                color = new Color(50, 50, 200 + (int)(Math.random() * 55));
                break;
            case 3: // GRAY - dead matter
                nutrition = 40 + Math.random() * 30;
                color = new Color(100 + (int)(Math.random() * 80), 
                                 100 + (int)(Math.random() * 80), 
                                 100 + (int)(Math.random() * 80));
                break;
            default:
                nutrition = 50;
                color = Color.WHITE;
        }
        
        Food food = new Food(x, y, ChemicalSignature.random(), nutrition);
        food.setColor(color);
        food.setFoodType(foodType);
        food.setMass(0.5);
        world.queueAddition(food);
    }
    
    /**
     * Environmental shift - change which food type is abundant.
     */
    private static void environmentalShift() {
        currentCycle++;
        int dominantType = (currentCycle / CYCLE_LENGTH) % 3;
        String[] typeNames = {"RED", "GREEN", "BLUE"};
        System.out.println("");
        System.out.println("=== ENVIRONMENTAL SHIFT ===");
        System.out.println("Dominant food type: " + typeNames[dominantType]);
        System.out.println("Specialists in this type will thrive!");
        System.out.println("");
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
    
    public static int getCyclePhase() {
        return (currentCycle / CYCLE_LENGTH) % 3;
    }
    
    public static String getCyclePhaseName() {
        String[] names = {"RED DOMINANCE", "GREEN DOMINANCE", "BLUE DOMINANCE"};
        return names[getCyclePhase()];
    }
}