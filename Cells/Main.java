package Cells;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

/**
 * Interactive cell simulation with physics and spatial optimization.
 * Controls: SPACE=pause, WASD=camera, E/Q=zoom, 1-5=parameter select, [/]=adjust
 */
public class Main {
    private static Matrix matrix;
    private static Displayer displayer;
    private static ArrayList<PhysicsObj> cells;
    private static Random rand = new Random();

    // At the top of Main class:
    private static ArrayList<PhysicsObj> pendingAdditions = new ArrayList<>();
    private static ArrayList<PhysicsObj> pendingRemovals = new ArrayList<>();
    
    // Simulation parameters
    private static double dt = 0.1;
    private static double gravityConstant = 10.0;
    private static boolean isPaused = false;
    private static boolean autoCamera = true;
    
    // Camera control
    private static final double CAMERA_SPEED = 10.0;
    private static final double ZOOM_SPEED = 0.1;
    
    // Parameter selection
    private static int currentChoice = 1;
    private static String[] choiceNames = {"", "Gravity", "Dampening", "dt", "Camera", "Cells"};
    
    // Keyboard state
    private static boolean[] keys = new boolean[256];
    
    public static void main(String[] args) {
        // Create matrix: 50px cells, 20x20 grid = 1000x1000 world
        matrix = new Matrix(50, 20, 20);
        displayer = new Displayer(matrix);
        cells = new ArrayList<>();
        
        // Setup keyboard listener
        setupKeyboard();
        
        // Create initial cells
        createCells(1000);
        
        // Add a static "sun" in the center
        PhysicsObj sun = new food(matrix.getTotalWidth() / 2.0, matrix.getTotalHeight() / 2.0);
        Cell a = new Cell(100,100);
        sun.setMass(1000);
        sun.setColor(new Color(255, 200, 0));
        sun.setSize(10);
        sun.setStatic(true);
        matrix.insertCell(sun);
        cells.add(sun);

        matrix.insertCell(a);
        cells.add(a);
        a.setSize(20);
        a.setStatic(false);
        a.setColor(Color.MAGENTA);
        a.applyForce(10, 10);
        
        // Main simulation loop
        while (true) {
            a.update(dt, matrix, cells);
            handleInput();
            
            if (!isPaused) {
                updatePhysics();
            }
            
            render();
            
            processPendingChanges();  // ADD THIS
            
            displayer.getPanel().sleep(16);
        }
    }
    
    private static void createCells(int count) {
        cells.clear();
        
        int worldWidth = matrix.getTotalWidth();
        int worldHeight = matrix.getTotalHeight();
        
        for (int i = 0; i < count; i++) {
            double x = rand.nextDouble() * worldWidth;
            double y = rand.nextDouble() * worldHeight;
            
            PhysicsObj cell = new food(x, y);
            cell.setMass(1);
            cell.setColor(new Color(100 + rand.nextInt(155), 
                                   100 + rand.nextInt(155), 
                                   255));
            cell.setSize(3);
            
            // Give some initial velocity for orbit-like behavior
            double centerX = worldWidth / 2.0;
            double centerY = worldHeight / 2.0;
            double dx = x - centerX;
            double dy = y - centerY;
            double angle = Math.atan2(dy, dx);
            double perpAngle = angle + Math.PI / 2;
            double speed = 5 + rand.nextDouble() * 5;
            cell.DAMPING_FACTOR = 1.00;
            cell.setVelocity(Math.cos(perpAngle) * speed, Math.sin(perpAngle) * speed);
            
            cell.G = gravityConstant;
            
            matrix.insertCell(cell);
            cells.add(cell);
        }
        
        System.out.println("Created " + count + " cells");
    }
    
    private static void updatePhysics() {
        // Apply physics parameters
        for (PhysicsObj cell : cells) {
            cell.G = gravityConstant;
        }
        
        // Create snapshot to avoid ConcurrentModificationException
        ArrayList<PhysicsObj> cellsSnapshot = new ArrayList<>(cells);
        
        // Apply gravity between cells (use radius search for efficiency)
        for (PhysicsObj cell : cellsSnapshot) {
            if (cell.isStatic()) continue;
            
            // Get nearby cells for gravity calculations
            ArrayList<PhysicsObj> nearby = cell.getCellsInRadius(200, matrix);
            for (PhysicsObj other : nearby) {
                if (other != cell && other.getMass() > 10) { // Only large masses
                    cell.applyGravity(other, matrix);
                }
            }
        }
        
        // Update all cells
        for (PhysicsObj cell : cellsSnapshot) {
            cell.update(dt, matrix, cells);
            matrix.updateCellGrid(cell);
        }
    }
    
    private static void render() {
        // Update camera if auto mode
        if (autoCamera) {
            displayer.updateCamera(cells);
        }
        
        // Render scene
        displayer.display();
    }
    
    private static void handleInput() {
        // Manual camera control
        if (!autoCamera) {
            if (keys[KeyEvent.VK_W]) displayer.cameraY -= CAMERA_SPEED / displayer.zoom;
            if (keys[KeyEvent.VK_S]) displayer.cameraY += CAMERA_SPEED / displayer.zoom;
            if (keys[KeyEvent.VK_A]) displayer.cameraX -= CAMERA_SPEED / displayer.zoom;
            if (keys[KeyEvent.VK_D]) displayer.cameraX += CAMERA_SPEED / displayer.zoom;
            
            if (keys[KeyEvent.VK_E]) displayer.zoom *= (1 + ZOOM_SPEED);
            if (keys[KeyEvent.VK_Q]) displayer.zoom *= (1 - ZOOM_SPEED);
            displayer.zoom = Math.max(0.1, Math.min(5.0, displayer.zoom));
        }
    }
    
    private static void setupKeyboard() {
        displayer.getPanel().addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode < keys.length) {
                    keys[keyCode] = true;
                }
                
                // Number keys for parameter selection
                if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_5) {
                    currentChoice = keyCode - KeyEvent.VK_0;
                }
                
                // SPACE to pause
                if (keyCode == KeyEvent.VK_SPACE) {
                    isPaused = !isPaused;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode < keys.length) {
                    keys[keyCode] = false;
                }
                
                // [ and ] to adjust parameters
                if (keyCode == KeyEvent.VK_OPEN_BRACKET) {
                    adjustParameter(false);
                }
                if (keyCode == KeyEvent.VK_CLOSE_BRACKET) {
                    adjustParameter(true);
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                // Not needed
            }
        });
    }
    
    private static void adjustParameter(boolean increase) {
        double factor = increase ? 1.1 : 0.9;
        
        switch (currentChoice) {
            case 1: // Gravity
                gravityConstant *= factor;
                gravityConstant = Math.max(0.1, gravityConstant);
                System.out.println("Gravity: " + gravityConstant);
                break;
                
            case 2: // Dampening
            /* 
                dampening *= factor;
                dampening = Math.max(0.01, Math.min(1.0, dampening));
                System.out.println("Dampening: " + dampening);
                break;
                */
            case 3: // dt
                dt *= factor;
                dt = Math.max(0.01, Math.min(10.0, dt));
                System.out.println("dt: " + dt);
                break;
                
            case 4: // Toggle camera mode
                autoCamera = !autoCamera;
                System.out.println("Camera: " + (autoCamera ? "AUTO" : "MANUAL"));
                break;
                
            case 5: // Add/Remove cells
                if (increase) {
                    // Add 10 cells
                    for (int i = 0; i < 10; i++) {
                        double x = rand.nextDouble() * matrix.getTotalWidth();
                        double y = rand.nextDouble() * matrix.getTotalHeight();
                        food cell = new food(x, y);
                        cell.setMass(1);
                        cell.setColor(new Color(100 + rand.nextInt(155), 
                                            100 + rand.nextInt(155), 
                                            255));
                        cell.setSize(3);
                        cell.G = gravityConstant;
                        cell.DAMPING_FACTOR = 0.9;
                        matrix.insertCell(cell);
                        pendingAdditions.add(cell); 
                    }

                    System.out.println("Queued 10 foods for addition");
                } else {
                    // Remove 10 cells
                    int toRemove = Math.min(10, cells.size() - 1);
                    for (int i = 0; i < toRemove; i++) {
                        if (cells.size() > 1) {
                            PhysicsObj cell = cells.get(cells.size() - 1 - i);
                            pendingRemovals.add(cell);  // ADD TO PENDING
                        }
                    }
                    System.out.println("Queued " + toRemove + " cells for removal");
                }
            break;
        }
    }


    private static void processPendingChanges() {
        // Add new cells
        if (!pendingAdditions.isEmpty()) {
            cells.addAll(pendingAdditions);
            System.out.println("Added " + pendingAdditions.size() + " cells. Total: " + cells.size());
            pendingAdditions.clear();
        }
        
        // Remove cells
        if (!pendingRemovals.isEmpty()) {
            for (PhysicsObj cell : pendingRemovals) {
                cells.remove(cell);
                matrix.removeCell(cell);
            }
            System.out.println("Removed " + pendingRemovals.size() + " cells. Total: " + cells.size());
            pendingRemovals.clear();
        }
    }
}