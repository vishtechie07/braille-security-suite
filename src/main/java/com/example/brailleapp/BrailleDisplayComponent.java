package com.example.brailleapp;

import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * Custom JavaFX component for displaying Braille text with visual dot patterns
 * Shows both the Braille characters and their visual representation
 */
public class BrailleDisplayComponent extends Control {
    
    private String brailleText;
    private VBox contentBox;
    
    public BrailleDisplayComponent() {
        this.brailleText = "";
        this.contentBox = new VBox();
        this.contentBox.setSpacing(10);
        this.contentBox.setPadding(new Insets(10));
    }
    
    /**
     * Set the Braille text to display
     * @param brailleText Braille text to display
     */
    public void setBrailleText(String brailleText) {
        this.brailleText = brailleText != null ? brailleText : "";
        updateDisplay();
    }
    
    /**
     * Get the current Braille text
     * @return Current Braille text
     */
    public String getBrailleText() {
        return brailleText;
    }
    
    /**
     * Update the display with current Braille text
     */
    private void updateDisplay() {
        contentBox.getChildren().clear();
        
        if (brailleText.isEmpty()) {
            Text emptyText = new Text("No Braille text to display");
            emptyText.setFill(Color.GRAY);
            emptyText.setFont(Font.font("Arial", 14));
            contentBox.getChildren().add(emptyText);
            return;
        }
        
        // Split text into lines for better display
        String[] lines = brailleText.split("\n");
        
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                contentBox.getChildren().add(new Text(" ")); // Empty line
                continue;
            }
            
            VBox lineBox = createBrailleLine(line);
            contentBox.getChildren().add(lineBox);
        }
    }
    
    /**
     * Create a visual representation of a Braille line
     * @param line Braille line text
     * @return VBox containing the visual representation
     */
    private VBox createBrailleLine(String line) {
        VBox lineBox = new VBox(5);
        lineBox.setAlignment(Pos.CENTER_LEFT);
        
        // Create the visual dot pattern
        GridPane dotPattern = createDotPattern(line);
        
        // Create the text representation
        Text textRepresentation = new Text(line);
        textRepresentation.setFont(Font.font("Courier New", 16));
        textRepresentation.setFill(Color.BLACK);
        
        lineBox.getChildren().addAll(dotPattern, textRepresentation);
        return lineBox;
    }
    
    /**
     * Create a visual dot pattern for Braille characters
     * @param brailleLine Line of Braille characters
     * @return GridPane with dot pattern
     */
    private GridPane createDotPattern(String brailleLine) {
        GridPane patternGrid = new GridPane();
        patternGrid.setHgap(5);
        patternGrid.setVgap(2);
        patternGrid.setAlignment(Pos.CENTER_LEFT);
        
        int charIndex = 0;
        for (char brailleChar : brailleLine.toCharArray()) {
            if (brailleChar == ' ') {
                // Add space
                charIndex++;
                continue;
            }
            
            // Create a 2x3 grid for each Braille character
            GridPane charGrid = createBrailleCharGrid(brailleChar);
            patternGrid.add(charGrid, charIndex, 0);
            charIndex++;
        }
        
        return patternGrid;
    }
    
    /**
     * Create a 2x3 grid representing a single Braille character
     * @param brailleChar Braille character
     * @return GridPane with dot pattern for the character
     */
    private GridPane createBrailleCharGrid(char brailleChar) {
        GridPane charGrid = new GridPane();
        charGrid.setHgap(2);
        charGrid.setVgap(2);
        charGrid.setAlignment(Pos.CENTER);
        
        // Create 6 dots (2 columns, 3 rows) for each Braille character
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                Circle dot = new Circle(3);
                dot.setFill(Color.TRANSPARENT);
                dot.setStroke(Color.LIGHTGRAY);
                dot.setStrokeWidth(0.5);
                
                // Check if this dot should be filled based on the Braille character
                if (shouldShowDot(brailleChar, row, col)) {
                    dot.setFill(Color.BLACK);
                }
                
                charGrid.add(dot, col, row);
            }
        }
        
        return charGrid;
    }
    
    /**
     * Determine if a dot should be shown for a given Braille character
     * This is a simplified mapping - in reality, Braille uses a 6-dot system
     * @param brailleChar Braille character
     * @param row Row position (0-2)
     * @param col Column position (0-1)
     * @return true if dot should be shown
     */
    private boolean shouldShowDot(char brailleChar, int row, int col) {
        // This is a simplified representation
        // In a real implementation, you would map each Braille character
        // to its specific dot pattern
        
        // For demonstration, show some dots for common characters
        switch (brailleChar) {
            case '⠁': // 'a'
                return (row == 2 && col == 0);
            case '⠃': // 'b'
                return (row == 2 && col == 0) || (row == 1 && col == 0);
            case '⠉': // 'c'
                return (row == 2 && col == 0) || (row == 2 && col == 1);
            case '⠙': // 'd'
                return (row == 2 && col == 0) || (row == 2 && col == 1) || (row == 1 && col == 1);
            case '⠑': // 'e'
                return (row == 2 && col == 0) || (row == 1 && col == 1);
            case '⠋': // 'f'
                return (row == 2 && col == 0) || (row == 1 && col == 0) || (row == 2 && col == 1);
            case '⠛': // 'g'
                return (row == 2 && col == 0) || (row == 1 && col == 0) || (row == 2 && col == 1) || (row == 1 && col == 1);
            case '⠓': // 'h'
                return (row == 2 && col == 0) || (row == 0 && col == 0);
            case '⠊': // 'i'
                return (row == 0 && col == 0) || (row == 2 && col == 1);
            case '⠚': // 'j'
                return (row == 0 && col == 0) || (row == 2 && col == 1) || (row == 1 && col == 1);
            case ' ': // Space
                return false;
            default:
                // For unknown characters, show a pattern
                return (row + col) % 2 == 0;
        }
    }
    
    /**
     * Get the estimated width needed for the display
     * @return Estimated width in pixels
     */
    public double getEstimatedWidth() {
        if (brailleText.isEmpty()) {
            return 200;
        }
        
        String[] lines = brailleText.split("\n");
        int maxLineLength = 0;
        for (String line : lines) {
            maxLineLength = Math.max(maxLineLength, line.length());
        }
        
        return Math.max(200, maxLineLength * 20 + 40);
    }
    
    /**
     * Get the estimated height needed for the display
     * @return Estimated height in pixels
     */
    public double getEstimatedHeight() {
        if (brailleText.isEmpty()) {
            return 100;
        }
        
        String[] lines = brailleText.split("\n");
        return Math.max(100, lines.length * 60 + 40);
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new BrailleDisplaySkin(this);
    }
    
    /**
     * Custom skin for the Braille display component
     */
    private static class BrailleDisplaySkin implements Skin<BrailleDisplayComponent> {
        private final BrailleDisplayComponent component;
        private final VBox container;
        
        public BrailleDisplaySkin(BrailleDisplayComponent component) {
            this.component = component;
            this.container = new VBox();
            this.container.getChildren().add(component.contentBox);
        }
        
        @Override
        public BrailleDisplayComponent getSkinnable() {
            return component;
        }
        
        @Override
        public javafx.scene.Node getNode() {
            return container;
        }
        
        @Override
        public void dispose() {
            // Cleanup if needed
        }
    }
}
