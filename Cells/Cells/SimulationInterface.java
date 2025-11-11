//SIMULATIONINTERFACE.JAVA - Clean spawning interface

package Cells;

import java.awt.Color;
import java.util.function.BiFunction;

/**
 * Clean interface for spawning entities in the simulation.
 */
public class SimulationInterface {
    private final SimulationWorld world;
    
    public SimulationInterface(SimulationWorld world) {
        this.world = world;
    }
    
    /**
     * Spawn a single cell at position with random chemistry.
     */
    public Cell spawnCell(double x, double y) {
        return spawnCell(x, y, ChemicalSignature.random());
    }
    
    /**
     * Spawn a cell with specific chemical preference.
     */
    public Cell spawnCell(double x, double y, ChemicalSignature preference) {
        Cell cell = new Cell(x, y, preference);
        cell.setColor(chemistryToColor(preference));
        world.queueAddition(cell);
        return cell;
    }
    
    /**
     * Spawn a single food particle at position with random chemistry.
     */
    public Food spawnFood(double x, double y) {
        return spawnFood(x, y, ChemicalSignature.random(), 50.0);
    }
    
    /**
     * Spawn food with specific chemistry and nutrition.
     */
    public Food spawnFood(double x, double y, ChemicalSignature chemistry, double nutrition) {
        Food food = new Food(x, y, chemistry, nutrition);
        food.setColor(chemistryToColor(chemistry));
        world.queueAddition(food);
        return food;
    }
    
    /**
     * Spawn many food particles using spatial and chemical distribution functions.
     * 
     * @param count Number of food particles to spawn
     * @param spatialProbability Function(x, y) -> [0, 1] probability of spawning at location
     * @param chemistryFunction Function(x, y) -> ChemicalSignature determining food type by location
     * @param nutritionFunction Function(x, y) -> double determining nutrition value by location
     */
    public void spawnFoods(int count,
                          BiFunction<Double, Double, Double> spatialProbability,
                          BiFunction<Double, Double, ChemicalSignature> chemistryFunction,
                          BiFunction<Double, Double, Double> nutritionFunction) {
        
        int spawned = 0;
        int attempts = 0;
        int maxAttempts = count * 10; // Prevent infinite loop
        
        while (spawned < count && attempts < maxAttempts) {
            attempts++;
            
            // Random position
            double x = world.getRandom().nextDouble() * world.getTotalWidth();
            double y = world.getRandom().nextDouble() * world.getTotalHeight();
            
            // Check spatial probability
            double prob = spatialProbability.apply(x, y);
            if (world.getRandom().nextDouble() > prob) {
                continue; // Reject this location
            }
            
            // Determine chemistry and nutrition
            ChemicalSignature chemistry = chemistryFunction.apply(x, y);
            double nutrition = nutritionFunction.apply(x, y);
            
            spawnFood(x, y, chemistry, nutrition);
            spawned++;
        }
        
        if (spawned < count) {
            System.out.println("Warning: Only spawned " + spawned + "/" + count + " foods");
        }
    }
    
    /**
     * Spawn foods with simple uniform distribution.
     */
    public void spawnFoodsUniform(int count) {
        spawnFoods(count,
            (x, y) -> 1.0, // Uniform spatial
            (x, y) -> ChemicalSignature.random(), // Random chemistry
            (x, y) -> 50.0 // Fixed nutrition
        );
    }
    
    /**
     * Spawn foods with gradient-based chemistry (creates niches).
     */
    public void spawnFoodsWithGradient(int count) {
        double worldWidth = world.getTotalWidth();
        double worldHeight = world.getTotalHeight();
        
        spawnFoods(count,
            // Spatial: bias using environmental function
            (x, y) -> {
                double xNorm = x / worldWidth;
                double yNorm = y / worldHeight;
                double bias = (Math.sin(xNorm * Math.PI * 2) + Math.cos(yNorm * Math.PI * 2 + xNorm * Math.PI) + 2.0) / 4.0;
                return Math.pow(bias, 2.0);
            },
            // Chemistry: varies smoothly with position
            (x, y) -> {
                double[] comps = new double[ChemicalSignature.DIMENSIONS];
                double xNorm = x / worldWidth;
                double yNorm = y / worldHeight;
                
                for (int i = 0; i < ChemicalSignature.DIMENSIONS; i++) {
                    double angle = i * Math.PI / 4.0;
                    double phase = Math.sin(xNorm * Math.PI * 3 + angle) * Math.cos(yNorm * Math.PI * 3 - angle);
                    comps[i] = (phase + 1.0) / 2.0;
                    
                    // Add some noise
                    comps[i] += (Math.random() - 0.5) * 0.2;
                    comps[i] = Math.max(0, Math.min(1, comps[i]));
                }
                
                return new ChemicalSignature(comps);
            },
            // Nutrition: varies with position
            (x, y) -> {
                double xNorm = x / worldWidth;
                double yNorm = y / worldHeight;
                double variation = 0.5 + 0.5 * Math.sin(xNorm * Math.PI * 2) * Math.cos(yNorm * Math.PI * 2);
                return 30.0 + variation * 40.0; // 30-70 range
            }
        );
    }
    
    /**
     * Convert chemistry to color for visualization.
     */
    private Color chemistryToColor(ChemicalSignature chemistry) {
        // Use first 3 dimensions for RGB
        int r = (int)(chemistry.get(0) * 200 + 55);
        int g = (int)(chemistry.get(1) * 200 + 55);
        int b = (int)(chemistry.get(2) * 200 + 55);
        return new Color(r, g, b);
    }
}