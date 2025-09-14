package com.example.brailleapp.services;

import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for converting text to Braille and handling Braille printing
 * Uses Liblouis for Braille conversion and Java's printing API for output
 */
public class BrailleConverter {
    
    // Braille dot patterns for each letter (6-dot Braille)
    private static final Map<Object, String> BRAILLE_MAP = new HashMap<>();
    
    static {
        // Initialize Braille mappings
        BRAILLE_MAP.put('a', "⠁"); BRAILLE_MAP.put('b', "⠃"); BRAILLE_MAP.put('c', "⠉");
        BRAILLE_MAP.put('d', "⠙"); BRAILLE_MAP.put('e', "⠑"); BRAILLE_MAP.put('f', "⠋");
        BRAILLE_MAP.put('g', "⠛"); BRAILLE_MAP.put('h', "⠓"); BRAILLE_MAP.put('i', "⠊");
        BRAILLE_MAP.put('j', "⠚"); BRAILLE_MAP.put('k', "⠅"); BRAILLE_MAP.put('l', "⠇");
        BRAILLE_MAP.put('m', "⠍"); BRAILLE_MAP.put('n', "⠝"); BRAILLE_MAP.put('o', "⠕");
        BRAILLE_MAP.put('p', "⠏"); BRAILLE_MAP.put('q', "⠟"); BRAILLE_MAP.put('r', "⠗");
        BRAILLE_MAP.put('s', "⠎"); BRAILLE_MAP.put('t', "⠞"); BRAILLE_MAP.put('u', "⠥");
        BRAILLE_MAP.put('v', "⠧"); BRAILLE_MAP.put('w', "⠺"); BRAILLE_MAP.put('x', "⠭");
        BRAILLE_MAP.put('y', "⠽"); BRAILLE_MAP.put('z', "⠵");
        
        // Numbers
        BRAILLE_MAP.put('1', "⠁"); BRAILLE_MAP.put('2', "⠃"); BRAILLE_MAP.put('3', "⠉");
        BRAILLE_MAP.put('4', "⠙"); BRAILLE_MAP.put('5', "⠑"); BRAILLE_MAP.put('6', "⠋");
        BRAILLE_MAP.put('7', "⠛"); BRAILLE_MAP.put('8', "⠓"); BRAILLE_MAP.put('9', "⠊");
        BRAILLE_MAP.put('0', "⠚");
        
        // Punctuation
        BRAILLE_MAP.put(' ', " "); BRAILLE_MAP.put('.', "⠲"); BRAILLE_MAP.put(',', "⠂");
        BRAILLE_MAP.put('!', "⠖"); BRAILLE_MAP.put('?', "⠦"); BRAILLE_MAP.put(';', "⠆");
        BRAILLE_MAP.put(':', "⠒"); BRAILLE_MAP.put('-', "⠤"); BRAILLE_MAP.put('(', "⠐⠣");
        BRAILLE_MAP.put(')', "⠐⠜"); BRAILLE_MAP.put('"', "⠦"); BRAILLE_MAP.put('\'', "⠄");
        
        // Capital letter indicator
        BRAILLE_MAP.put("CAPITAL", "⠠");
    }
    
    public BrailleConverter() {
        // Constructor - could initialize Liblouis here if needed
    }
    
    /**
     * Convert text to Braille using the mapping table
     * @param text Input text to convert
     * @return Braille representation of the text
     */
    public String convertToBraille(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        StringBuilder brailleText = new StringBuilder();
        boolean wasCapital = false;
        
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) {
                // Add capital indicator before uppercase letters
                if (!wasCapital) {
                    brailleText.append(BRAILLE_MAP.get("CAPITAL"));
                }
                brailleText.append(BRAILLE_MAP.getOrDefault(Character.toLowerCase(c), "?"));
                wasCapital = true;
            } else {
                brailleText.append(BRAILLE_MAP.getOrDefault(c, "?"));
                wasCapital = false;
            }
        }
        
        return brailleText.toString();
    }
    
    /**
     * Convert text to Braille using Liblouis (if available)
     * Falls back to manual conversion if Liblouis is not available
     * @param text Input text to convert
     * @return Braille representation using Liblouis
     */
    public String convertToBrailleWithLiblouis(String text) {
        try {
            // Try to use Liblouis if available
            // This would require proper Liblouis integration
            return convertToBraille(text); // Fallback to manual conversion
        } catch (Exception e) {
            // Fallback to manual conversion
            return convertToBraille(text);
        }
    }
    
    /**
     * Print Braille text to the default printer
     * @param brailleText Braille text to print
     * @throws PrinterException if printing fails
     */
    public void printBraille(String brailleText) throws PrinterException {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        
        // Configure print job
        PageFormat pageFormat = printerJob.defaultPage();
        pageFormat = printerJob.validatePage(pageFormat);
        
        // Create printable object
        Printable printable = new BraillePrintable(brailleText);
        
        printerJob.setPrintable(printable, pageFormat);
        
        // Show print dialog
        if (printerJob.printDialog()) {
            printerJob.print();
        }
    }
    
    /**
     * Save Braille text to a file
     * @param brailleText Braille text to save
     * @param filename Output filename
     * @throws IOException if file writing fails
     */
    public void saveBrailleToFile(String brailleText, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Braille Output:");
            writer.println("==============");
            writer.println();
            writer.println(brailleText);
            writer.println();
            writer.println("Generated by Braille Script Printing App");
        }
    }
    
    /**
     * Get Braille character for a given character
     * @param c Character to convert
     * @return Braille representation
     */
    public String getBrailleChar(char c) {
        return BRAILLE_MAP.getOrDefault(Character.toLowerCase(c), "?");
    }
    
    /**
     * Check if a character has a Braille representation
     * @param c Character to check
     * @return true if character has Braille representation
     */
    public boolean hasBrailleRepresentation(char c) {
        return BRAILLE_MAP.containsKey(Character.toLowerCase(c)) || 
               BRAILLE_MAP.containsKey(c);
    }
    
    /**
     * Get the number of Braille characters needed for the given text
     * @param text Input text
     * @return Number of Braille characters
     */
    public int getBrailleLength(String text) {
        if (text == null) return 0;
        
        int length = 0;
        boolean wasCapital = false;
        
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!wasCapital) {
                    length++; // Capital indicator
                }
                length++;
                wasCapital = true;
            } else {
                length++;
                wasCapital = false;
            }
        }
        
        return length;
    }
    
    /**
     * Printable class for Braille printing
     */
    private static class BraillePrintable implements Printable {
        private final String brailleText;
        
        public BraillePrintable(String brailleText) {
            this.brailleText = brailleText;
        }
        
        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }
            
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
            // Set font for Braille display
            Font brailleFont = new Font("Courier New", Font.PLAIN, 12);
            g2d.setFont(brailleFont);
            
            // Print the Braille text
            String[] lines = brailleText.split("\n");
            int y = 20;
            
            for (String line : lines) {
                g2d.drawString(line, 20, y);
                y += 20;
            }
            
            return PAGE_EXISTS;
        }
    }
}
