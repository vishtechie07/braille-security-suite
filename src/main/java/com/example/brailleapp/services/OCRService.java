package com.example.brailleapp.services;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * OCR Service using Tess4J for extracting text from images
 * Supports various image formats including PNG, JPG, TIFF, etc.
 */
public class OCRService {
    
    private static final Logger logger = Logger.getLogger(OCRService.class.getName());
    private ITesseract tesseract;
    
    public OCRService() {
        initializeTesseract();
    }
    
    /**
     * Initialize Tesseract OCR engine
     */
    private void initializeTesseract() {
        try {
            // Check if tessdata directory exists and has required files
            String tessDataPath = getTessDataPath();
            if (tessDataPath == null) {
                logger.warning("Tesseract data path not found. OCR will be disabled.");
                tesseract = null;
                return;
            }
            
            // Check if eng.traineddata exists
            File engDataFile = new File(tessDataPath, "eng.traineddata");
            if (!engDataFile.exists()) {
                logger.warning("English language data file (eng.traineddata) not found. OCR will be disabled.");
                logger.warning("Please download eng.traineddata from: https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata");
                tesseract = null;
                return;
            }
            
            tesseract = new Tesseract();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");
            
            // Configure OCR settings for better accuracy
            try {
                tesseract.setTessVariable("tessedit_char_whitelist", 
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,!?;:()\"'- ");
            } catch (Exception e) {
                logger.warning("Could not set character whitelist: " + e.getMessage());
            }
            
            // Set page segmentation mode
            tesseract.setPageSegMode(1);
            
            logger.info("Tesseract OCR initialized successfully");
            
        } catch (Exception e) {
            logger.warning("Failed to initialize Tesseract: " + e.getMessage());
            logger.warning("OCR functionality will be disabled");
            tesseract = null;
        }
    }
    
    /**
     * Get the path to tessdata directory
     * This method tries to find tessdata in common locations
     * @return Path to tessdata directory or null if not found
     */
    private String getTessDataPath() {
        // Common tessdata locations
        String[] possiblePaths = {
            "tessdata",
            "src/main/resources/tessdata",
            "resources/tessdata",
            System.getProperty("user.home") + "/tessdata",
            "C:/Program Files/Tesseract-OCR/tessdata",
            "C:/Program Files (x86)/Tesseract-OCR/tessdata"
        };
        
        for (String path : possiblePaths) {
            File tessDataDir = new File(path);
            if (tessDataDir.exists() && tessDataDir.isDirectory()) {
                return path;
            }
        }
        
        logger.warning("Tessdata directory not found. Please ensure Tesseract is installed and tessdata is available.");
        return null;
    }
    
    /**
     * Extract text from an image file
     * @param imageFile The image file to process
     * @return Extracted text from the image
     * @throws TesseractException if OCR processing fails
     * @throws IOException if image file cannot be read
     */
    public String extractTextFromImage(File imageFile) throws TesseractException, IOException {
        // Check if OCR is available
        if (tesseract == null) {
            return "OCR not available - Tesseract not properly installed.\n\n" +
                   "To fix this issue:\n" +
                   "1. Install Tesseract OCR from: https://github.com/UB-Mannheim/tesseract/wiki\n" +
                   "2. Download English language data (eng.traineddata)\n" +
                   "3. Place tessdata files in the tessdata/ directory\n" +
                   "4. Restart the application\n\n" +
                   "For now, you can manually type the text in the input field.";
        }
        
        if (!imageFile.exists()) {
            throw new IOException("Image file does not exist: " + imageFile.getPath());
        }
        
        // Validate image format
        if (!isValidImageFormat(imageFile)) {
            throw new IOException("Unsupported image format. Supported formats: PNG, JPG, JPEG, GIF, BMP, TIFF");
        }
        
        try {
            // Load and preprocess image
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                throw new IOException("Could not read image file: " + imageFile.getPath());
            }
            
            // Preprocess image for better OCR results
            BufferedImage processedImage = preprocessImage(image);
            
            // Perform OCR with comprehensive error handling
            String extractedText;
            try {
                extractedText = tesseract.doOCR(processedImage);
            } catch (Throwable ocrException) {
                // Catch any type of error including memory access violations
                logger.warning("OCR failed with error: " + ocrException.getClass().getSimpleName() + " - " + ocrException.getMessage());
                return "OCR failed due to system error.\n\n" +
                       "This usually means:\n" +
                       "1. Tesseract is not properly installed\n" +
                       "2. Missing language data files (eng.traineddata)\n" +
                       "3. System compatibility issues\n\n" +
                       "Please install Tesseract OCR and try again, or manually type the text.";
            }
            
            // Clean up the extracted text
            extractedText = cleanExtractedText(extractedText);
            
            // Check if OCR returned meaningful text
            if (extractedText == null || extractedText.trim().isEmpty() || extractedText.contains("Empty page")) {
                return "No text detected in the image. Please ensure the image contains clear, readable text.";
            }
            
            logger.info("Successfully extracted text from image: " + imageFile.getName());
            return extractedText;
            
        } catch (Exception e) {
            logger.severe("Error during OCR processing: " + e.getMessage());
            return "Error processing image: " + e.getMessage() + "\n\nPlease try again or manually type the text.";
        }
    }
    
    /**
     * Extract text from a BufferedImage
     * @param image The BufferedImage to process
     * @return Extracted text from the image
     * @throws TesseractException if OCR processing fails
     */
    public String extractTextFromImage(BufferedImage image) throws TesseractException {
        if (tesseract == null) {
            return "OCR not available - Tesseract not properly installed.\n\n" +
                   "Please install Tesseract OCR and try again, or manually type the text.";
        }
        
        try {
            // Preprocess image for better OCR results
            BufferedImage processedImage = preprocessImage(image);
            
            // Perform OCR with comprehensive error handling
            String extractedText;
            try {
                extractedText = tesseract.doOCR(processedImage);
            } catch (Throwable ocrException) {
                logger.warning("OCR failed with error: " + ocrException.getClass().getSimpleName() + " - " + ocrException.getMessage());
                return "OCR failed due to system error.\n\n" +
                       "Please install Tesseract OCR and try again, or manually type the text.";
            }
            
            // Clean up the extracted text
            extractedText = cleanExtractedText(extractedText);
            
            logger.info("Successfully extracted text from BufferedImage");
            return extractedText;
            
        } catch (Exception e) {
            logger.severe("Error during OCR processing: " + e.getMessage());
            return "Error processing image: " + e.getMessage() + "\n\nPlease try again or manually type the text.";
        }
    }
    
    /**
     * Preprocess image to improve OCR accuracy
     * @param originalImage Original image
     * @return Preprocessed image
     */
    private BufferedImage preprocessImage(BufferedImage originalImage) {
        // Convert to grayscale for better OCR results
        BufferedImage grayscaleImage = convertToGrayscale(originalImage);
        
        // Apply additional preprocessing if needed
        // This could include noise reduction, contrast enhancement, etc.
        
        return grayscaleImage;
    }
    
    /**
     * Convert image to grayscale
     * @param originalImage Original color image
     * @return Grayscale image
     */
    private BufferedImage convertToGrayscale(BufferedImage originalImage) {
        BufferedImage grayscaleImage = new BufferedImage(
            originalImage.getWidth(),
            originalImage.getHeight(),
            BufferedImage.TYPE_BYTE_GRAY
        );
        
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                int rgb = originalImage.getRGB(x, y);
                grayscaleImage.setRGB(x, y, rgb);
            }
        }
        
        return grayscaleImage;
    }
    
    /**
     * Clean up extracted text by removing unwanted characters and formatting
     * @param rawText Raw text from OCR
     * @return Cleaned text
     */
    private String cleanExtractedText(String rawText) {
        if (rawText == null) {
            return "";
        }
        
        // Remove excessive whitespace
        String cleaned = rawText.replaceAll("\\s+", " ");
        
        // Remove leading/trailing whitespace
        cleaned = cleaned.trim();
        
        // Remove any non-printable characters except newlines
        cleaned = cleaned.replaceAll("[\\x00-\\x1F\\x7F]", "");
        
        return cleaned;
    }
    
    /**
     * Check if the file is a valid image format
     * @param file File to check
     * @return true if valid image format
     */
    private boolean isValidImageFormat(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || 
               fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || 
               fileName.endsWith(".gif") || 
               fileName.endsWith(".bmp") || 
               fileName.endsWith(".tiff") || 
               fileName.endsWith(".tif");
    }
    
    /**
     * Set the language for OCR processing
     * @param language Language code (e.g., "eng", "spa", "fra")
     */
    public void setLanguage(String language) {
        if (tesseract != null) {
            tesseract.setLanguage(language);
            logger.info("OCR language set to: " + language);
        }
    }
    
    /**
     * Get the current OCR language
     * @return Current language setting
     */
    public String getLanguage() {
        if (tesseract != null) {
            return "eng"; // Default language since getLanguage() is not available
        }
        return "eng";
    }
    
    /**
     * Check if OCR service is properly initialized
     * @return true if OCR is ready to use
     */
    public boolean isInitialized() {
        return tesseract != null;
    }
    
    /**
     * Get OCR status message
     * @return Status message about OCR availability
     */
    public String getOCRStatus() {
        if (tesseract == null) {
            return "OCR not available - Tesseract not properly installed or missing language data files.";
        }
        return "OCR is ready to use.";
    }
    
    /**
     * Get OCR engine information
     * @return OCR engine version and configuration info
     */
    public String getOCRInfo() {
        if (tesseract != null) {
            return "Tesseract OCR - Language: " + getLanguage() + 
                   ", Page Seg Mode: 1";
        }
        return "OCR not initialized";
    }
}
