package ParticleSim;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.event.MouseInputAdapter;

class mainFile {
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 1000;
    public static DrawingPanel panel = new DrawingPanel(WIDTH, HEIGHT);
    
    // Double buffering setup
    public static BufferedImage buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    public static Graphics2D g2 = buffer.createGraphics();
    //g2.fillRect(0, 0, WIDTH, HEIGHT);
    public static Graphics panelGraphics = panel.getGraphics();
    
    public static Random rand = new Random();
    private static final long startTime = System.currentTimeMillis();
    private static long pauseStartTime = 0;
    private static long totalPauseTime = 0;
    private static double dt = 0.1;

    public static final double BOUNDARY_RADIUS = 2000;
    public static final double BOUNDARY_STRENGTH = 50;
    
    // Camera variables
    public static double cameraX = 0;
    public static double cameraY = 0;
    public static int zoomLevel = 0; // Powers of 2: 2^zoomLevel
    public static double zoom = 1.0; // Actual zoom value: 2^zoomLevel
    public static final double CAMERA_SMOOTH = 0.02;
    public static final double ZOOM_SMOOTH = 0.05;
    public static final double ZOOM_MARGIN = 100;
    
    // Mouse variables
    public static int mouseScreenX = 0;
    public static int mouseScreenY = 0;
    public static boolean mouseLocked = false;
    public static double lockedMouseX = WIDTH / 2.0;
    public static double lockedMouseY = HEIGHT / 2.0;
    
    // Control variables
    public static boolean autoControl = true;
    public static boolean boundaryControl = false;
    public static boolean isPaused = false;
    public static boolean resetRequested = false;
    public static double dampening = 0.98;
    public static double gravityConstant = 10.0;
    public static double mouseMass = 1000;
    public static int currentChoice = 1;
    public static int bodyCount = 100000;
    
    // Manual camera controls
    public static final double CAMERA_SPEED = 50.0;
    public static final double ZOOM_SPEED = 0.1;
    
    // Key states
    public static boolean[] keys = new boolean[256];
    
    // Store initial state for reset
    public static ArrayList<physics> initialBodies;

    public static void main(String args[]) {
        // Enable antialiasing for smoother graphics
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        panel.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!mouseLocked) {
                    mouseScreenX = e.getX();
                    mouseScreenY = e.getY();
                }
            }
        });

        panel.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode < keys.length) {
                    keys[keyCode] = true;
                }
                
                // Number keys for parameter selection
                if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_7) {
                    currentChoice = keyCode - KeyEvent.VK_0;
                }
                
                // Toggle mouse lock with SPACE
                if (keyCode == KeyEvent.VK_SPACE) {
                    mouseLocked = !mouseLocked;
                    if (mouseLocked) {
                        // Lock mouse at current world position
                        lockedMouseX = screenToWorldX(mouseScreenX);
                        lockedMouseY = screenToWorldY(mouseScreenY);
                    }
                }
                
                // Toggle auto control with TAB
                if (keyCode == KeyEvent.VK_0) {
                    System.out.println("AAAAA");
                    autoControl = !autoControl;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode < keys.length) {
                    keys[keyCode] = false;
                }
                
                // Parameter adjustment with [ and ]
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
        
        physics mouse = new physics(0, 0);
        mouse.setColor(Color.RED);
        mouse.setMass(mouseMass);
        mouse.setSize(5);
        
        ArrayList<physics> bodies = new ArrayList<>();
        
        // Initialize bodies
        initializeBodies(bodies);
        
        // Store initial state for reset
        saveInitialState(bodies);
        
        Color clearColor = new Color(0,0,0,150);

        while (true) {
            // Clear the buffer with black background
            g2.setColor(clearColor);
            g2.fillRect(0, 0, WIDTH, HEIGHT);
            
            // Handle camera controls regardless of pause state
            if (!autoControl) {
                handleManualControls();
            } else if (!isPaused) {
                // Only do auto control when not paused
                double[] centerOfMass = getCenterOfMass(bodies);
                double targetX = centerOfMass[0];
                double targetY = centerOfMass[1];
                
                // Calculate bounding box and target zoom
                double[] bounds = getBoundingBox(bodies);
                double minX = bounds[0];
                double minY = bounds[1];
                double maxX = bounds[2];
                double maxY = bounds[3];
                
                double objectWidth = maxX - minX + ZOOM_MARGIN * 2;
                double objectHeight = maxY - minY + ZOOM_MARGIN * 2;
                
                double zoomForWidth = WIDTH / objectWidth;
                double zoomForHeight = HEIGHT / objectHeight;
                
                double idealZoom = Math.min(zoomForWidth, zoomForHeight);
                idealZoom = Math.max(0.2, Math.min(1.0, idealZoom)); // Use original limits
                
                // Smoothly move camera toward center of mass
                cameraX += (targetX - cameraX) * CAMERA_SMOOTH;
                cameraY += (targetY - cameraY) * CAMERA_SMOOTH;
                
                // Smoothly adjust zoom and keep zoomLevel in sync
                zoom += (idealZoom - zoom) * ZOOM_SMOOTH;
                zoomLevel = (int) Math.round(Math.log(zoom) / Math.log(2)); // Keep zoomLevel synced
            }
            
            // Set mouse position (after camera updates)
            if (mouseLocked) {
                mouse.setPosition(lockedMouseX, lockedMouseY);
            } else {
                double worldMouseX = screenToWorldX(mouseScreenX);
                double worldMouseY = screenToWorldY(mouseScreenY);
                mouse.setPosition(worldMouseX, worldMouseY);
            }
            mouse.setMass(mouseMass);
            
            // Update physics parameters for all bodies
            updatePhysicsParameters(bodies, mouse);
            
            if (!isPaused) {
                // Physics updates only when not paused
                for (physics body : bodies) {
                    if (boundaryControl) {
                        double[] centerOfMass = getCenterOfMass(bodies);
                        applyBoundaryForce(body, centerOfMass[0], centerOfMass[1]);
                    }
                    body.applyGravity(mouse);
                    body.update(dt);
                    body.draw();
                }
            } else {
                // If paused, just draw bodies without updating
                for (physics body : bodies) {
                    body.draw();
                }
            }
            
            mouse.draw();
            
            // Draw UI information
            drawUI(bodies.size());

            if (resetRequested) {
                resetSimulation(bodies);
                resetRequested = false;
            }
            
            // Copy buffer to panel
            panelGraphics.drawImage(buffer, 0, 0, null);
            
            panel.sleep(16);
        }
    }
    
    public static void initializeBodies(ArrayList<physics> bodies) {
        bodies.clear();
        for (int i = 0; i < bodyCount; i++) {
            physics body = new physics(
                rand.nextDouble(-1,1) + WIDTH/2,
                rand.nextDouble(-1,1) + HEIGHT/2
            );
            body.setMass(1);
            body.setColor(new Color(255,255,255));
            body.setSize(1);
            body.DAMPING_FACTOR = dampening;
            bodies.add(body);
        }
    }
    
    public static void saveInitialState(ArrayList<physics> bodies) {
        initialBodies = new ArrayList<>();
        for (physics body : bodies) {
            physics copy = new physics(body.getX(), body.getY());
            copy.setMass(body.getMass());
            copy.setColor(new Color(255,255,255));
            copy.setSize(body.getSize());
            copy.setVelocity(0, 0);
            copy.setAcceleration(0, 0);
            copy.DAMPING_FACTOR = dampening;
            initialBodies.add(copy);
        }
    }
    
    public static void resetSimulation(ArrayList<physics> bodies) {
        bodies.clear();
        for (physics initial : initialBodies) {
            physics copy = new physics(initial.getX(), initial.getY());
            copy.setMass(initial.getMass());
            copy.setColor(new Color(255,255,255));
            copy.setSize(initial.getSize());
            copy.setVelocity(0, 0);
            copy.setAcceleration(0, 0);
            copy.DAMPING_FACTOR = dampening;
            bodies.add(copy);
        }
        
        // Reset camera
        cameraX = 0;
        cameraY = 0;
        zoomLevel = 0;
        zoom = 1.0;
        
        // Reset time tracking
        totalPauseTime = 0;
        pauseStartTime = 0;
        
        // Reset mouse lock
        mouseLocked = false;
    }
    
    public static void handleManualControls() {
        // WASD for camera movement
        if (keys[KeyEvent.VK_W]) {
            cameraY -= CAMERA_SPEED / zoom;
        }
        if (keys[KeyEvent.VK_S]) {
            cameraY += CAMERA_SPEED / zoom;
        }
        if (keys[KeyEvent.VK_A]) {
            cameraX -= CAMERA_SPEED / zoom;
        }
        if (keys[KeyEvent.VK_D]) {
            cameraX += CAMERA_SPEED / zoom;
        }
        
        // E and Q for zoom - zoom based on mouse object's world position
        if (keys[KeyEvent.VK_E] || keys[KeyEvent.VK_Q]) {
            // Get the mouse object's current world position
            double mouseWorldX, mouseWorldY;
            if (mouseLocked) {
                mouseWorldX = lockedMouseX;
                mouseWorldY = lockedMouseY;
            } else {
                mouseWorldX = screenToWorldX(mouseScreenX);
                mouseWorldY = screenToWorldY(mouseScreenY);
            }
            
            // Calculate where the mouse object appears on screen currently
            double mouseScreenPosX = worldToScreenX(mouseWorldX);
            double mouseScreenPosY = worldToScreenY(mouseWorldY);
            
            // Change zoom
            if (keys[KeyEvent.VK_E]) {
                zoomLevel++;
            }
            if (keys[KeyEvent.VK_Q]) {
                zoomLevel--;
            }
            zoom = Math.pow(2, zoomLevel);
            
            // Adjust camera so the mouse object stays in the same screen position
            double newMouseScreenX = worldToScreenX(mouseWorldX);
            double newMouseScreenY = worldToScreenY(mouseWorldY);
            
            double deltaX = (mouseScreenPosX - newMouseScreenX) / zoom;
            double deltaY = (mouseScreenPosY - newMouseScreenY) / zoom;
            
            cameraX += deltaX;
            cameraY += deltaY;
        }
    }
    
    public static void adjustParameter(boolean increase) {
        double factor = increase ? 1.1 : 0.9;
        
        switch (currentChoice) {
            case 1: // Dampening
                dampening *= factor;
                dampening = Math.max(0.01, Math.min(1.0, dampening));
                break;
            case 2: // G Constant
                gravityConstant *= factor;
                gravityConstant = Math.max(0.1, gravityConstant);
                break;
            case 3: // Mouse Mass
                mouseMass *= factor;
                mouseMass = Math.max(1, mouseMass);
                break;
            case 4: // Boundary Control
                boundaryControl = !boundaryControl;
                break;
            case 5: // dt
                dt *= factor;
                dt = Math.max(0.01, Math.min(10.0, dt));
                break;
            case 6: // Reset
                resetRequested = true;
                break;
            case 7: // Pause
                isPaused = !isPaused;
                if (isPaused) {
                    pauseStartTime = System.currentTimeMillis();
                } else {
                    totalPauseTime += System.currentTimeMillis() - pauseStartTime;
                }
                break;
        }
    }
    
    public static void updatePhysicsParameters(ArrayList<physics> bodies, physics mouse) {
        for (physics body : bodies) {
            body.DAMPING_FACTOR = dampening;
            body.G = gravityConstant;
        }
        mouse.G = gravityConstant;
    }
    
    public static void drawUI(int objectCount) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        
        int lineHeight = 16;
        int startY = 20;
        
        // Time elapsed (accounting for pause time)
        double timeElapsed = isPaused ? 
            (pauseStartTime - startTime - totalPauseTime) / 1000.0 : 
            (System.currentTimeMillis() - startTime - totalPauseTime) / 1000.0;
        g2.drawString(String.format("Time: %.1fs", timeElapsed), 10, startY);
        
        g2.drawString(String.format("Objects: %d", objectCount), 10, startY + lineHeight);
        g2.drawString(String.format("Zoom: %.2f", zoom), 10, startY + 2 * lineHeight);
        g2.drawString(String.format("Gravity: %.2f", gravityConstant), 10, startY + 3 * lineHeight);
        g2.drawString(String.format("Dampening: %.3f", dampening), 10, startY + 4 * lineHeight);
        
        String controlStatus = autoControl ? "AUTO" : "MANUAL";
        g2.drawString("Control: " + controlStatus, 10, startY + 5 * lineHeight);
        
        String mouseStatus = mouseLocked ? "LOCKED" : "FREE";
        g2.drawString("Mouse: " + mouseStatus, 10, startY + 6 * lineHeight);
        
        String pauseStatus = isPaused ? "PAUSED" : "RUNNING";
        g2.drawString("Status: " + pauseStatus, 10, startY + 7 * lineHeight);
        
        String[] choiceNames = {"", "Dampening", "G Constant", "Mouse Mass", "Boundary", "dt", "Reset", "Pause"};
        g2.drawString("Edit: " + choiceNames[currentChoice], 10, startY + 10 * lineHeight);
        
        g2.drawString(String.format("Mouse Mass: %.0f", mouseMass), 10, startY + 9 * lineHeight);
        g2.drawString(String.format("Boundary: %s", boundaryControl ? "ON" : "OFF"), 10, startY + 8 * lineHeight);
        g2.drawString(String.format("dt: %.2f", dt), 10, startY + 11 * lineHeight);

        // Instructions
        g2.setColor(Color.GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString("SPACE: Lock/Unlock mouse | 0: Toggle control | 1-7: Select | [/]: Adjust", 10, HEIGHT - 40);
        g2.drawString("Manual: WASD: Move | E/Q: Zoom", 10, HEIGHT - 25);
        g2.drawString("Current: " + choiceNames[currentChoice], 10, HEIGHT - 10);
    }
    
    // Coordinate conversion methods
    public static double screenToWorldX(int screenX) {
        return (screenX - WIDTH / 2.0) / zoom + cameraX;
    }
    
    public static double screenToWorldY(int screenY) {
        return (screenY - HEIGHT / 2.0) / zoom + cameraY;
    }
    
    public static double worldToScreenX(double worldX) {
        return (worldX - cameraX) * zoom + WIDTH / 2.0;
    }
    
    public static double worldToScreenY(double worldY) {
        return (worldY - cameraY) * zoom + HEIGHT / 2.0;
    }
    
    public static double[] getCenterOfMass(ArrayList<physics> bodies) {
        double totalMass = 0;
        double centerX = 0;
        double centerY = 0;
        
        for (physics body : bodies) {
            double mass = body.getMass();
            centerX += body.getX() * mass;
            centerY += body.getY() * mass;
            totalMass += mass;
        }
        
        if (totalMass > 0) {
            centerX /= totalMass;
            centerY /= totalMass;
        }
        
        return new double[]{centerX, centerY};
    }
    
    public static double[] getBoundingBox(ArrayList<physics> bodies) {
        if (bodies.isEmpty()) {
            return new double[]{0, 0, 0, 0};
        }
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        
        for (physics body : bodies) {
            double x = body.getX();
            double y = body.getY();
            double halfSize = body.getSize() / 2.0;
            
            minX = Math.min(minX, x - halfSize);
            minY = Math.min(minY, y - halfSize);
            maxX = Math.max(maxX, x + halfSize);
            maxY = Math.max(maxY, y + halfSize);
        }
        
        return new double[]{minX, minY, maxX, maxY};
    }
    
    public static double calculateTargetZoom(double[] bounds) {
        double minX = bounds[0];
        double minY = bounds[1];
        double maxX = bounds[2];
        double maxY = bounds[3];
        
        double objectWidth = maxX - minX + ZOOM_MARGIN * 2;
        double objectHeight = maxY - minY + ZOOM_MARGIN * 2;
        
        double zoomForWidth = WIDTH / objectWidth;
        double zoomForHeight = HEIGHT / objectHeight;
        
        double idealZoom = Math.min(zoomForWidth, zoomForHeight);
        
        // Find the closest power of 2 zoom level
        int targetZoomLevel = (int) Math.round(Math.log(idealZoom) / Math.log(2));
        
        return Math.pow(2, targetZoomLevel);
    }
    
    public static void drawSquare(double x, double y, int size) {
        drawSquare(x, y, size, cameraX, cameraY, zoom);
    }
    
    public static void drawSquare(double x, double y, int size, double camX, double camY, double zoomLevel) {
        double screenX = (x - camX) * zoomLevel + WIDTH / 2;
        double screenY = (y - camY) * zoomLevel + HEIGHT / 2;
        int scaledSize = Math.max(1, (int) (size * zoomLevel));
        
        g2.fillRect((int) (screenX - scaledSize / 2),
                    (int) (screenY - scaledSize / 2),
                    scaledSize, scaledSize);
    }
    
    public static void applyBoundaryForce(physics body, double centerX, double centerY) {
        double dx = body.getX() - centerX;
        double dy = body.getY() - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > BOUNDARY_RADIUS) {
            double forceStrength = BOUNDARY_STRENGTH * (distance - BOUNDARY_RADIUS) / BOUNDARY_RADIUS;
            double angle = Math.atan2(-dy, -dx);
            double fx = Math.cos(angle) * forceStrength;
            double fy = Math.sin(angle) * forceStrength;
            body.applyForce(fx, fy);
        }
    }
    
    public static double getTime() {
        if (isPaused) {
            return (pauseStartTime - startTime - totalPauseTime) / 1000.0;
        }
        return (System.currentTimeMillis() - startTime - totalPauseTime) / 1000.0;
    }
}