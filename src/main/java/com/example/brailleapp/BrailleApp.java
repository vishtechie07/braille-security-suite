package com.example.brailleapp;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Tooltip;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import com.example.brailleapp.services.BrailleConverter;
import com.example.brailleapp.services.OCRService;
import com.example.brailleapp.services.DocumentParser;
import com.example.brailleapp.services.OpenAIService;
import com.example.brailleapp.security.SecurityScanner;
import com.example.brailleapp.security.SecurityScanResult;
import com.example.brailleapp.security.PenetrationTester;
import com.example.brailleapp.security.PenetrationTestResult;
import com.example.brailleapp.security.PenetrationTestType;
import com.example.brailleapp.security.SecurityAuditLogger;
import com.example.brailleapp.security.SecurityEvent;

/**
 * Main JavaFX application for Braille Script Printing
 * Supports text input, image OCR, and document parsing to convert to Braille
 */
public class BrailleApp extends Application {
    
    private TextArea inputTextArea;
    private TextArea brailleOutputArea;
    private PasswordField openaiKeyField;
    private Label statusLabel;
    private ProgressBar progressBar;
    private BrailleConverter brailleConverter;
    private OCRService ocrService;
    private DocumentParser documentParser;
    private OpenAIService openaiService;
    private SecurityScanner securityScanner;
    private PenetrationTester penetrationTester;
    private SecurityAuditLogger securityLogger;

    // Action buttons (used for busy-state disabling)
    private Button convertButton;
    private Button clearButton;
    private Button printButton;
    private Button enhanceButton;
    private Button securityScanButton;
    private Button penetrationTestButton;
    private Button securityReportButton;
    
    @Override
    public void start(Stage primaryStage) {
        initializeServices();
        setupUI(primaryStage);
    }
    
    private void initializeServices() {
        try {
            brailleConverter = new BrailleConverter();
            ocrService = new OCRService();
            documentParser = new DocumentParser();
            openaiService = new OpenAIService();
            securityScanner = new SecurityScanner();
            penetrationTester = new PenetrationTester();
            securityLogger = new SecurityAuditLogger();
            
            // Log application startup
            securityLogger.logSecurityEvent(new SecurityEvent("APP_STARTUP", "Application started successfully", "INFO"));
        } catch (Exception e) {
            showError("Initialization Error", "Failed to initialize services: " + e.getMessage());
        }
    }
    
    private void setupUI(Stage primaryStage) {
        primaryStage.setTitle("Braille Script Printing App");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Top section - API Key and controls
        VBox topSection = createTopSection();
        
        // Center section - Input and Output
        HBox centerSection = createCenterSection();
        
        // Bottom section - Action buttons
        HBox bottomSection = createBottomSection();
        HBox statusBar = createStatusBar();
        
        root.setTop(topSection);
        root.setCenter(centerSection);
        root.setBottom(new VBox(10, bottomSection, statusBar));
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createTopSection() {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(10));
        
        // Title
        Label titleLabel = new Label("Braille Script Printing App");
        titleLabel.setFont(Font.font("Arial", 24));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // OpenAI API Key input
        HBox apiKeyBox = new HBox(10);
        apiKeyBox.setAlignment(Pos.CENTER_LEFT);
        
        Label apiKeyLabel = new Label("OpenAI API Key:");
        openaiKeyField = new PasswordField();
        openaiKeyField.setPromptText("Enter your OpenAI API key");
        openaiKeyField.setPrefWidth(300);
        openaiKeyField.setTooltip(new Tooltip("Key is masked while typing and after save"));
        
        Button saveKeyButton = new Button("Save Key");
        saveKeyButton.setOnAction(e -> saveOpenAIKey());
        
        apiKeyBox.getChildren().addAll(apiKeyLabel, openaiKeyField, saveKeyButton);
        
        topSection.getChildren().addAll(titleLabel, apiKeyBox);
        return topSection;
    }
    
    private HBox createCenterSection() {
        HBox centerSection = new HBox(20);
        centerSection.setPadding(new Insets(10));
        
        // Input section
        VBox inputSection = new VBox(10);
        inputSection.setPrefWidth(400);
        
        Label inputLabel = new Label("Input Text:");
        inputLabel.setFont(Font.font("Arial", 14));
        inputLabel.setStyle("-fx-font-weight: bold;");
        
        inputTextArea = new TextArea();
        inputTextArea.setPromptText("Enter text here or use file upload buttons below...");
        inputTextArea.setPrefRowCount(15);
        inputTextArea.setWrapText(true);
        
        // File upload buttons
        HBox fileButtons = new HBox(10);
        Button uploadImageButton = new Button("Upload Image");
        Button uploadPdfButton = new Button("Upload PDF");
        Button uploadDocxButton = new Button("Upload DOCX");
        
        // Disable image upload if OCR is not available
        if (!ocrService.isInitialized()) {
            uploadImageButton.setDisable(true);
            uploadImageButton.setTooltip(new Tooltip("OCR not available - install Python 3 and pip install easyocr"));
        }
        
        uploadImageButton.setOnAction(e -> uploadImage());
        uploadPdfButton.setOnAction(e -> uploadDocument("pdf"));
        uploadDocxButton.setOnAction(e -> uploadDocument("docx"));
        
        fileButtons.getChildren().addAll(uploadImageButton, uploadPdfButton, uploadDocxButton);
        
        Label ocrHint = new Label(
            ocrService.isInitialized()
                ? "OCR is ready."
                : "OCR is disabled: install Python 3 and run `pip install easyocr` (see requirements-ocr.txt)."
        );
        ocrHint.setWrapText(true);
        ocrHint.setStyle(
            "-fx-text-fill: " + (ocrService.isInitialized() ? "#2e7d32" : "#b71c1c") + "; -fx-font-size: 11;"
        );
        
        inputSection.getChildren().addAll(inputLabel, inputTextArea, fileButtons, ocrHint);
        
        // Output section
        VBox outputSection = new VBox(10);
        outputSection.setPrefWidth(400);
        
        Label outputLabel = new Label("Braille Output:");
        outputLabel.setFont(Font.font("Arial", 14));
        outputLabel.setStyle("-fx-font-weight: bold;");
        
        brailleOutputArea = new TextArea();
        brailleOutputArea.setPrefRowCount(15);
        brailleOutputArea.setWrapText(true);
        brailleOutputArea.setEditable(false);
        brailleOutputArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14;");
        
        outputSection.getChildren().addAll(outputLabel, brailleOutputArea);
        
        HBox outputActions = new HBox(10);
        outputActions.setAlignment(Pos.CENTER_LEFT);
        
        Button copyButton = new Button("Copy");
        copyButton.setOnAction(e -> copyBrailleOutput());
        copyButton.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 12; -fx-padding: 6 12;");
        
        Button saveButton = new Button("Save .txt");
        saveButton.setOnAction(e -> saveBrailleOutput());
        saveButton.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 12; -fx-padding: 6 12;");
        
        printButton = new Button("Print Braille");
        printButton.setOnAction(e -> printBraille());
        printButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 6 12;");
        
        outputActions.getChildren().addAll(copyButton, saveButton, printButton);
        outputSection.getChildren().add(outputActions);
        
        centerSection.getChildren().addAll(inputSection, outputSection);
        return centerSection;
    }
    
    private HBox createBottomSection() {
        HBox bottomSection = new HBox(20);
        // Align groups by their top edge (Security is taller: 2 rows).
        bottomSection.setAlignment(Pos.TOP_CENTER);
        bottomSection.setPadding(new Insets(20));

        VBox conversionGroup = new VBox(8);
        conversionGroup.setAlignment(Pos.TOP_CENTER);
        Label conversionLabel = new Label("Conversion");
        conversionLabel.setStyle("-fx-font-weight: bold;");

        convertButton = new Button("Convert to Braille");
        convertButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 14;");
        convertButton.setOnAction(e -> convertToBraille());

        clearButton = new Button("Clear All");
        clearButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 14;");
        clearButton.setOnAction(e -> clearAll());

        HBox conversionButtons = new HBox(10, convertButton, clearButton);
        conversionButtons.setAlignment(Pos.CENTER);
        conversionGroup.getChildren().addAll(conversionLabel, conversionButtons);

        VBox aiGroup = new VBox(8);
        aiGroup.setAlignment(Pos.TOP_CENTER);
        Label aiLabel = new Label("AI");
        aiLabel.setStyle("-fx-font-weight: bold;");

        enhanceButton = new Button("Enhance with AI");
        enhanceButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 14;");
        enhanceButton.setOnAction(e -> enhanceWithAI());

        aiGroup.getChildren().addAll(aiLabel, enhanceButton);

        VBox securityGroup = new VBox(8);
        securityGroup.setAlignment(Pos.TOP_CENTER);
        Label securityLabel = new Label("Security");
        securityLabel.setStyle("-fx-font-weight: bold;");

        securityScanButton = new Button("Security Scan");
        securityScanButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 14;");
        securityScanButton.setOnAction(e -> performSecurityScan());

        penetrationTestButton = new Button("Penetration Test");
        penetrationTestButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 14;");
        penetrationTestButton.setOnAction(e -> performPenetrationTest());

        securityReportButton = new Button("Security Report");
        // Slightly smaller so all 3 buttons fit in a single row.
        securityReportButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 7 12;");
        securityReportButton.setOnAction(e -> showSecurityReport());

        HBox securityButtonsRow = new HBox(10, securityScanButton, penetrationTestButton, securityReportButton);
        securityButtonsRow.setAlignment(Pos.CENTER);

        securityGroup.getChildren().addAll(securityLabel, securityButtonsRow);

        bottomSection.getChildren().addAll(conversionGroup, aiGroup, securityGroup);
        return bottomSection;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(0, 10, 10, 10));

        statusLabel = new Label("Ready.");
        statusLabel.setStyle("-fx-font-size: 12;");

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(180);
        progressBar.setVisible(false);
        progressBar.setProgress(-1);

        statusBar.getChildren().addAll(statusLabel, progressBar);
        return statusBar;
    }

    private void setStatus(String message) {
        if (statusLabel != null) statusLabel.setText(message);
    }

    private void disableActionButtons(boolean disabled) {
        if (convertButton != null) convertButton.setDisable(disabled);
        if (clearButton != null) clearButton.setDisable(disabled);
        if (printButton != null) printButton.setDisable(disabled);
        if (enhanceButton != null) enhanceButton.setDisable(disabled);
        if (securityScanButton != null) securityScanButton.setDisable(disabled);
        if (penetrationTestButton != null) penetrationTestButton.setDisable(disabled);
        if (securityReportButton != null) securityReportButton.setDisable(disabled);
    }

    private <T> void runAsync(String busyMessage, Callable<T> work, Consumer<T> onSuccess, String errorTitle) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };

        task.setOnRunning(e -> {
            setStatus(busyMessage != null && !busyMessage.isBlank() ? busyMessage : "Working...");
            if (progressBar != null) {
                progressBar.setVisible(true);
                progressBar.setProgress(-1);
            }
            disableActionButtons(true);
        });
        task.setOnSucceeded(e -> {
            try {
                onSuccess.accept(task.getValue());
            } finally {
                if (progressBar != null) progressBar.setVisible(false);
                disableActionButtons(false);
                setStatus("Ready.");
            }
        });
        task.setOnFailed(e -> {
            try {
                Throwable ex = task.getException();
                showError(errorTitle, ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error");
            } finally {
                if (progressBar != null) progressBar.setVisible(false);
                disableActionButtons(false);
                setStatus("Ready.");
            }
        });

        Thread t = new Thread(task, "braille-worker");
        t.setDaemon(true);
        t.start();
    }
    
    private void saveOpenAIKey() {
        String apiKey = openaiKeyField.getText().trim();
        if (!apiKey.isEmpty()) {
            openaiService.setApiKey(apiKey);
            openaiKeyField.clear();
            openaiKeyField.setPromptText("API key saved (hidden)");
            setStatus("OpenAI API key saved.");
            showInfo("Success", "OpenAI API key saved successfully!");
        } else {
            showError("Error", "Please enter a valid API key.");
        }
    }
    
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.tiff")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Security scan the uploaded file
                SecurityScanResult scanResult = securityScanner.scanFile(selectedFile);
                securityLogger.logFileUploadScan(scanResult);
                
                // Check if file is safe to process
                if (!scanResult.isSafe()) {
                    showSecurityAlert("File Security Warning", scanResult);
                    return;
                }
                
                // Log file upload event
                securityLogger.logSecurityEvent(new SecurityEvent("FILE_UPLOAD", 
                    "Image file uploaded: " + selectedFile.getName(), "INFO"));
                
                String extractedText = ocrService.extractTextFromImage(selectedFile);
                inputTextArea.setText(extractedText);

                // Check if OCR returned an error message
                if (extractedText.contains("OCR not available")
                        || extractedText.contains("OCR failed")
                        || extractedText.contains("OCR timed out")
                        || extractedText.contains("Error processing image")) {
                    showError("OCR Not Available", extractedText);
                } else {
                    showInfo("Success", "Text extracted from image successfully!");
                }
            } catch (Exception e) {
                securityLogger.logSecurityEvent(new SecurityEvent("FILE_UPLOAD_ERROR", 
                    "Failed to process image: " + e.getMessage(), "ERROR"));
                showError("OCR Error", "Failed to extract text from image: " + e.getMessage());
            }
        }
    }
    
    private void uploadDocument(String type) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select " + type.toUpperCase() + " File");
        
        if ("pdf".equals(type)) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        } else if ("docx".equals(type)) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Documents", "*.docx"));
        }
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Security scan the uploaded file
                SecurityScanResult scanResult = securityScanner.scanFile(selectedFile);
                securityLogger.logFileUploadScan(scanResult);
                
                // Check if file is safe to process
                if (!scanResult.isSafe()) {
                    showSecurityAlert("File Security Warning", scanResult);
                    return;
                }
                
                // Log file upload event
                securityLogger.logSecurityEvent(new SecurityEvent("FILE_UPLOAD", 
                    type.toUpperCase() + " file uploaded: " + selectedFile.getName(), "INFO"));
                
                String extractedText = documentParser.parseDocument(selectedFile, type);
                inputTextArea.setText(extractedText);
                showInfo("Success", "Text extracted from " + type.toUpperCase() + " successfully!");
            } catch (Exception e) {
                securityLogger.logSecurityEvent(new SecurityEvent("FILE_UPLOAD_ERROR", 
                    "Failed to process " + type.toUpperCase() + " file: " + e.getMessage(), "ERROR"));
                showError("Document Parsing Error", "Failed to parse document: " + e.getMessage());
            }
        }
    }
    
    private void convertToBraille() {
        String inputText = inputTextArea.getText().trim();
        if (inputText.isEmpty()) {
            showError("Error", "Please enter some text to convert.");
            return;
        }
        
        try {
            String brailleText = brailleConverter.convertToBraille(inputText);
            brailleOutputArea.setText(brailleText);
            setStatus("Converted to Braille.");
        } catch (Exception e) {
            showError("Conversion Error", "Failed to convert text to Braille: " + e.getMessage());
        }
    }
    
    private void enhanceWithAI() {
        String inputText = inputTextArea.getText().trim();
        if (inputText.isEmpty()) {
            showError("Error", "Please enter some text to enhance.");
            return;
        }
        
        if (!openaiService.isApiKeySet()) {
            showError("Error", "Please set your OpenAI API key first.");
            return;
        }

        runAsync(
            "Enhancing with AI...",
            () -> openaiService.enhanceText(inputText),
            enhancedText -> {
                inputTextArea.setText(enhancedText);
                setStatus("AI enhancement complete.");
            },
            "AI Enhancement Error"
        );
    }
    
    private void printBraille() {
        String brailleText = brailleOutputArea.getText().trim();
        if (brailleText.isEmpty()) {
            showError("Error", "No Braille text to print. Please convert text first.");
            return;
        }
        
        try {
            brailleConverter.printBraille(brailleText);
            showInfo("Success", "Braille text sent to printer successfully!");
        } catch (Exception e) {
            showError("Printing Error", "Failed to print Braille: " + e.getMessage());
        }
    }

    private void copyBrailleOutput() {
        if (brailleOutputArea == null) return;
        String text = brailleOutputArea.getText();
        if (text == null || text.trim().isEmpty()) {
            setStatus("Nothing to copy yet. Convert first.");
            return;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
        setStatus("Copied braille output to clipboard.");
    }

    private void saveBrailleOutput() {
        if (brailleOutputArea == null) return;
        String text = brailleOutputArea.getText();
        if (text == null || text.trim().isEmpty()) {
            setStatus("Nothing to save yet. Convert first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Braille Output");
        fileChooser.setInitialFileName("braille-output.txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile == null) return;

        try {
            Path out = selectedFile.toPath();
            Files.write(out, text.getBytes(StandardCharsets.UTF_8));
            setStatus("Saved braille output: " + selectedFile.getName());
        } catch (Exception e) {
            showError("Save Error", "Failed to save braille output: " + e.getMessage());
        }
    }
    
    private void clearAll() {
        inputTextArea.clear();
        brailleOutputArea.clear();
        setStatus("Cleared.");
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        String t = (title != null) ? title.trim() : "";
        String m = (message != null) ? message.trim() : "";
        if (!t.isEmpty() && !m.isEmpty()) setStatus(t + ": " + m);
        else if (!m.isEmpty()) setStatus(m);
        else setStatus("Done.");
    }
    
    private void showSecurityAlert(String title, SecurityScanResult scanResult) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Security threats detected in uploaded file");
        
        StringBuilder content = new StringBuilder();
        content.append("File: ").append(scanResult.getFileName()).append("\n");
        content.append("Security Status: ").append(scanResult.getSecurityStatus()).append("\n");
        content.append("File Size: ").append(scanResult.getFileSize()).append(" bytes\n");
        content.append("File Hash: ").append(scanResult.getFileHash()).append("\n\n");
        
        if (!scanResult.getThreats().isEmpty()) {
            content.append("Threats Detected:\n");
            for (com.example.brailleapp.security.SecurityThreat threat : scanResult.getThreats()) {
                content.append("• ").append(threat.getType()).append(": ").append(threat.getDescription()).append("\n");
            }
        }
        
        content.append("\nThis file has been blocked for security reasons.");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private void performSecurityScan() {
        String inputText = inputTextArea.getText().trim();
        if (inputText.isEmpty()) {
            showError("Error", "Please enter some text to scan for security threats.");
            return;
        }

        try {
            // Logging only (should be quick)
            securityLogger.logSecurityEvent(new SecurityEvent(
                "SECURITY_SCAN", "Manual security scan initiated", "INFO"
            ));
        } catch (Exception ignored) {
            // Non-fatal
        }

        runAsync(
            "Running security scan...",
            () -> penetrationTester.performPenetrationTest(inputText, PenetrationTestType.COMPREHENSIVE),
            result -> {
                try {
                    securityLogger.logVulnerabilityScan(result);
                } catch (Exception ignored) {
                    // Non-fatal
                }
                showPenetrationTestResults(result);
            },
            "Security Scan Error"
        );
    }
    
    private void performPenetrationTest() {
        String inputText = inputTextArea.getText().trim();
        if (inputText.isEmpty()) {
            showError("Error", "Please enter some text to perform penetration testing.");
            return;
        }

        try {
            // Logging only (should be quick)
            securityLogger.logSecurityEvent(new SecurityEvent(
                "PENETRATION_TEST", "Penetration test initiated", "INFO"
            ));
        } catch (Exception ignored) {
            // Non-fatal
        }

        runAsync(
            "Running penetration test...",
            () -> penetrationTester.performPenetrationTest(inputText, PenetrationTestType.COMPREHENSIVE),
            result -> {
                try {
                    securityLogger.logVulnerabilityScan(result);
                } catch (Exception ignored) {
                    // Non-fatal
                }
                showPenetrationTestResults(result);
            },
            "Penetration Test Error"
        );
    }
    
    private void showPenetrationTestResults(PenetrationTestResult result) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Security Test Results");
        alert.setHeaderText("Penetration Test Results");
        
        StringBuilder content = new StringBuilder();
        content.append("Test Type: ").append(result.getTestType()).append("\n");
        content.append("Test Status: ").append(result.getTestStatus()).append("\n");
        content.append("Total Vulnerabilities: ").append(result.getVulnerabilities().size()).append("\n\n");
        
        if (!result.getVulnerabilities().isEmpty()) {
            content.append("Vulnerabilities Found:\n");
            for (com.example.brailleapp.security.SecurityVulnerability vuln : result.getVulnerabilities()) {
                content.append("• ").append(vuln.getType()).append(" (").append(vuln.getLevel()).append("): ")
                       .append(vuln.getDescription()).append("\n");
                content.append("  Remediation: ").append(vuln.getRemediation()).append("\n\n");
            }
        } else {
            content.append("No vulnerabilities found. The input appears to be secure.");
        }
        
        alert.setContentText(content.toString());
        alert.getDialogPane().setPrefSize(600, 400);
        alert.showAndWait();
    }
    
    private void showSecurityReport() {
        try {
            // Generate security report
            String report = securityLogger.generateSecurityReport();
            
            // Show report in a new window
            Stage reportStage = new Stage();
            reportStage.setTitle("Security Audit Report");
            reportStage.setWidth(800);
            reportStage.setHeight(600);
            
            TextArea reportArea = new TextArea(report);
            reportArea.setEditable(false);
            reportArea.setWrapText(true);
            reportArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");
            
            ScrollPane scrollPane = new ScrollPane(reportArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            
            Scene reportScene = new Scene(scrollPane);
            reportStage.setScene(reportScene);
            reportStage.show();
            
            // Log report generation
            securityLogger.logSecurityEvent(new SecurityEvent("SECURITY_REPORT", 
                "Security report generated and displayed", "INFO"));
            
        } catch (Exception e) {
            securityLogger.logSecurityEvent(new SecurityEvent("SECURITY_REPORT_ERROR", 
                "Failed to generate security report: " + e.getMessage(), "ERROR"));
            showError("Security Report Error", "Failed to generate security report: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
