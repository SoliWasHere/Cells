//ENTITYTOOLTIP.JAVA (UPDATED FOR FOOD IDS AND STOMACH)

package Cells;

import java.awt.*;

/**
 * Renders detailed information tooltips with food ID and stomach data.
 */
public class EntityTooltip {
    private static final Color TOOLTIP_BG = new Color(20, 20, 30, 230);
    private static final Color TOOLTIP_BORDER = new Color(100, 100, 150, 255);
    private static final Color TOOLTIP_TEXT = new Color(220, 220, 220);
    private static final Color TOOLTIP_HEADER = new Color(255, 200, 100);
    private static final Font TOOLTIP_FONT = new Font("Monospaced", Font.PLAIN, 11);
    private static final Font TOOLTIP_HEADER_FONT = new Font("Monospaced", Font.BOLD, 12);
    
    private static final int PADDING = 8;
    private static final int LINE_HEIGHT = 14;
    private static final int OFFSET_X = 15;
    private static final int OFFSET_Y = 15;
    
    public static void draw(Graphics2D g2, PhysicsObj entity, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        String[] lines = buildTooltipContent(entity);
        
        FontMetrics fm = g2.getFontMetrics(TOOLTIP_FONT);
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, fm.stringWidth(line));
        }
        
        int tooltipWidth = maxWidth + PADDING * 2;
        int tooltipHeight = PADDING * 2 + LINE_HEIGHT * lines.length;
        
        int tooltipX = mouseX + OFFSET_X;
        int tooltipY = mouseY + OFFSET_Y;
        
        if (tooltipX + tooltipWidth > screenWidth - 10) {
            tooltipX = mouseX - tooltipWidth - OFFSET_X;
        }
        if (tooltipY + tooltipHeight > screenHeight - 10) {
            tooltipY = mouseY - tooltipHeight - OFFSET_Y;
        }
        
        // Background
        g2.setColor(TOOLTIP_BG);
        g2.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 8, 8);
        
        // Border
        g2.setColor(TOOLTIP_BORDER);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 8, 8);
        
        // Content
        int textX = tooltipX + PADDING;
        int textY = tooltipY + PADDING + LINE_HEIGHT - 3;
        
        for (int i = 0; i < lines.length; i++) {
            if (i == 0) {
                g2.setColor(TOOLTIP_HEADER);
                g2.setFont(TOOLTIP_HEADER_FONT);
            } else {
                g2.setColor(TOOLTIP_TEXT);
                g2.setFont(TOOLTIP_FONT);
            }
            
            g2.drawString(lines[i], textX, textY + i * LINE_HEIGHT);
        }
        
        drawIndicatorLine(g2, entity, mouseX, mouseY, tooltipX, tooltipY, tooltipWidth, tooltipHeight);
    }
    
    private static String[] buildTooltipContent(PhysicsObj entity) {
        String entityType = getEntityTypeName(entity);
        java.util.List<String> lines = new java.util.ArrayList<>();
        
        lines.add("=== " + entityType + " ===");
        lines.add(String.format("Position: (%.1f, %.1f)", entity.getX(), entity.getY()));
        lines.add(String.format("Velocity: (%.2f, %.2f)", entity.getVelocityX(), entity.getVelocityY()));
        lines.add(String.format("Speed: %.2f", entity.getSpeed()));
        lines.add(String.format("Mass: %.2f", entity.getMass()));
        lines.add(String.format("Size: %d", entity.getSize()));
        
        if (entity.isStatic()) {
            lines.add("Status: STATIC");
        } else {
            lines.add("Status: Dynamic");
        }
        
        // Entity-specific info
        if (entity instanceof Cell) {
            addCellInfo(lines, (Cell) entity);
        } else if (entity instanceof Food) {
            addFoodInfo(lines, (Food) entity);
        }
        
        // Gradient info
        addGradientInfo(lines, entity);
        
        return lines.toArray(new String[0]);
    }
    
    private static void addCellInfo(java.util.List<String> lines, Cell cell) {
        lines.add("--- Cell Data ---");
        lines.add(String.format("Energy: %.1f", cell.getEnergy()));
        lines.add(String.format("Age: %d ticks", cell.getAge()));
        lines.add(String.format("Movement Force: %.1f", cell.getMovementForce()));
        lines.add(String.format("Eating Distance: %.2f", cell.getEatingDistance()));
        
        lines.add("--- Food Preference ---");
        lines.add(String.format("Preferred ID: (%.2f, %.2f)", 
            cell.getPreferredFoodIdX(), cell.getPreferredFoodIdY()));
        
        lines.add("--- Stomach ---");
        double wasteAmount = Math.sqrt(
            cell.getStomachWasteX() * cell.getStomachWasteX() + 
            cell.getStomachWasteY() * cell.getStomachWasteY()
        );
        
        if (true) {
            double wasteNorm = Math.sqrt(
                cell.getStomachWasteX() * cell.getStomachWasteX() + 
                cell.getStomachWasteY() * cell.getStomachWasteY()
            );
            lines.add(String.format("Waste ID: (%.2f, %.2f)",
                Math.abs(cell.getStomachWasteX() / wasteNorm),
                Math.abs(cell.getStomachWasteY() / wasteNorm)));
        }
        lines.add(String.format("Stomach fullness: (%.2f, %.2f)", wasteAmount, cell.getWasteThreshold()));
    }
    
    private static void addFoodInfo(java.util.List<String> lines, Food food) {
        lines.add("--- Food Data ---");
        lines.add(String.format("Nutrition: %.1f", food.getNutritionalValue()));
        lines.add(String.format("Food ID: (%.2f, %.2f)", 
            food.getFoodIdX(), food.getFoodIdY()));
    }
    
    private static void addGradientInfo(java.util.List<String> lines, PhysicsObj entity) {
        SimulationWorld world = SimulationWorld.getInstance();
        
        GradientSample foodGrad = world.getFoodGradientField().sample(entity.getX(), entity.getY());
        GradientSample cellGrad = world.getCellGradientField().sample(entity.getX(), entity.getY());
        
        lines.add("--- Gradient Info ---");
        lines.add(String.format("Food Gradient: %.1f", foodGrad.strength));
        lines.add(String.format("Cell Gradient: %.1f", cellGrad.strength));
    }
    
    private static String getEntityTypeName(PhysicsObj entity) {
        if (entity instanceof Cell) {
            return "Cell";
        } else if (entity instanceof Food) {
            return "Food";
        } else if (entity.isStatic() && entity.getMass() > 100) {
            return "Sun";
        } else {
            return "Entity";
        }
    }
    
    private static void drawIndicatorLine(Graphics2D g2, PhysicsObj entity, 
                                          int mouseX, int mouseY,
                                          int tooltipX, int tooltipY, 
                                          int tooltipWidth, int tooltipHeight) {
        SimulationWorld world = SimulationWorld.getInstance();
        Displayer displayer = world.getDisplayer();
        
        double screenX = displayer.worldToScreenX(entity.getX());
        double screenY = displayer.worldToScreenY(entity.getY());
        
        double distToMouse = Math.hypot(screenX - mouseX, screenY - mouseY);
        if (distToMouse < 30) return;
        
        int lineStartX = tooltipX + tooltipWidth / 2;
        int lineStartY = tooltipY;
        
        g2.setColor(new Color(150, 150, 200, 100));
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                     0, new float[]{5, 5}, 0));
        g2.drawLine(lineStartX, lineStartY, (int) screenX, (int) screenY);
    }
}