package com.example.brailleapp.services;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * OCR using EasyOCR (Python). Java invokes a bundled script via the system Python interpreter.
 */
public class OCRService {

    private static final Logger logger = Logger.getLogger(OCRService.class.getName());
    private static final String[][] PYTHON_TRIES = {
            {"py", "-3"},
            {"python"},
            {"python3"}
    };
    private static final int OCR_TIMEOUT_MINUTES = 15;

    private final boolean ready;
    private final List<String> pythonPrefix;
    private final Path scriptPath;

    public OCRService() {
        List<String> prefix = probePythonWithEasyOcr();
        Path script = null;
        if (prefix != null) {
            try {
                script = extractScriptToTemp();
            } catch (IOException e) {
                logger.warning("Failed to extract EasyOCR script: " + e.getMessage());
                prefix = null;
            }
        }
        this.pythonPrefix = prefix;
        this.scriptPath = script;
        this.ready = prefix != null && script != null;
        if (ready) {
            logger.info("EasyOCR backend ready (Python: " + String.join(" ", prefix) + ")");
        } else {
            logger.warning("EasyOCR not available. Install Python 3, then: pip install easyocr");
        }
    }

    private List<String> probePythonWithEasyOcr() {
        for (String[] tryPrefix : PYTHON_TRIES) {
            List<String> cmd = new ArrayList<>();
            for (String p : tryPrefix) {
                cmd.add(p);
            }
            cmd.add("-c");
            cmd.add("import easyocr");
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                boolean finished = p.waitFor(120, TimeUnit.SECONDS);
                if (finished && p.exitValue() == 0) {
                    return Arrays.asList(tryPrefix);
                }
            } catch (Exception e) {
                logger.fine("Python probe failed for " + Arrays.toString(tryPrefix) + ": " + e.getMessage());
            }
        }
        return null;
    }

    private Path extractScriptToTemp() throws IOException {
        try (InputStream in = OCRService.class.getResourceAsStream("/ocr/easyocr_ocr.py")) {
            if (in == null) {
                throw new IOException("Resource /ocr/easyocr_ocr.py not found");
            }
            Path tmp = Files.createTempFile("easyocr_ocr_", ".py");
            tmp.toFile().deleteOnExit();
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        }
    }

    /**
     * Extract text from an image file.
     */
    public String extractTextFromImage(File imageFile) throws IOException {
        if (!ready) {
            return unavailableMessage();
        }
        if (!imageFile.exists()) {
            throw new IOException("Image file does not exist: " + imageFile.getPath());
        }
        if (!isValidImageFormat(imageFile)) {
            throw new IOException("Unsupported image format. Supported formats: PNG, JPG, JPEG, GIF, BMP, TIFF");
        }
        return runEasyOcr(imageFile.getAbsolutePath());
    }

    /**
     * Extract text from a BufferedImage (written to a temporary PNG).
     */
    public String extractTextFromImage(BufferedImage image) throws IOException {
        if (!ready) {
            return unavailableMessage();
        }
        Path tmp = Files.createTempFile("ocr_img_", ".png");
        try {
            ImageIO.write(image, "png", tmp.toFile());
            return runEasyOcr(tmp.toAbsolutePath().toString());
        } finally {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignored) {
                // best effort
            }
        }
    }

    private String runEasyOcr(String absoluteImagePath) {
        List<String> cmd = new ArrayList<>(pythonPrefix);
        cmd.add(scriptPath.toAbsolutePath().toString());
        cmd.add(absoluteImagePath);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(false);
        try {
            Process p = pb.start();
            ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
            Thread errReader = new Thread(() -> {
                try {
                    p.getErrorStream().transferTo(errBuf);
                } catch (IOException ignored) {
                    // ignore
                }
            });
            errReader.start();
            byte[] outBytes = p.getInputStream().readAllBytes();
            errReader.join();
            boolean finished = p.waitFor(OCR_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!finished) {
                p.destroyForcibly();
                return "OCR timed out. First run may download models; try again or check network.\n\n"
                        + stderrHint(errBuf);
            }
            int code = p.exitValue();
            String text = new String(outBytes, StandardCharsets.UTF_8);
            text = cleanExtractedText(text);
            if (code != 0) {
                return "OCR failed (exit " + code + ").\n\n" + stderrHint(errBuf);
            }
            if (text.isEmpty()) {
                return "No text detected in the image. Please ensure the image contains clear, readable text.";
            }
            return text;
        } catch (Exception e) {
            logger.warning("EasyOCR process failed: " + e.getMessage());
            return "Error processing image: " + e.getMessage() + "\n\nPlease try again or manually type the text.";
        }
    }

    private String stderrHint(ByteArrayOutputStream errBuf) {
        String err = errBuf.toString(StandardCharsets.UTF_8);
        if (err == null || err.isBlank()) {
            return "";
        }
        String trimmed = err.trim();
        return trimmed.length() > 800 ? trimmed.substring(0, 800) + "..." : trimmed;
    }

    private String unavailableMessage() {
        return "OCR not available - EasyOCR (Python) is not set up.\n\n"
                + "1. Install Python 3 and add it to PATH (on Windows, `py` or `python`).\n"
                + "2. Run: pip install -r requirements-ocr.txt\n"
                + "   (or: pip install easyocr)\n"
                + "3. Restart the application.\n\n"
                + "First OCR run may download model files and take several minutes.\n\n"
                + "For now, you can manually type the text in the input field.";
    }

    private String cleanExtractedText(String rawText) {
        if (rawText == null) {
            return "";
        }
        String cleaned = rawText.replaceAll("\\s+", " ").trim();
        return cleaned.replaceAll("[\\x00-\\x1F\\x7F]", "");
    }

    private boolean isValidImageFormat(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".gif")
                || fileName.endsWith(".bmp")
                || fileName.endsWith(".tiff")
                || fileName.endsWith(".tif");
    }

    /**
     * EasyOCR language list is fixed to English in the bundled script; this is a no-op placeholder.
     */
    public void setLanguage(String language) {
        if (ready) {
            logger.info("OCR language request ignored (bundled script uses English): " + language);
        }
    }

    public String getLanguage() {
        return "en";
    }

    public boolean isInitialized() {
        return ready;
    }

    public String getOCRStatus() {
        if (!ready) {
            return "OCR not available - install Python 3 and run: pip install easyocr";
        }
        return "OCR is ready (EasyOCR, English).";
    }

    public String getOCRInfo() {
        if (ready) {
            return "EasyOCR (Python) - Language: en";
        }
        return "OCR not initialized";
    }
}
