package com.example.brailleapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Tooltip;

import java.io.File;
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
    private TextField openaiKeyField;
    private BrailleConverter brailleConverter;
    private OCRService ocrService;
    private DocumentParser documentParser;
    private OpenAIService openaiService;
    private SecurityScanner securityScanner;
    private PenetrationTester penetrationTester;
    private SecurityAuditLogger securityLogger;
    
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
        
        root.setTop(topSection);
        root.setCenter(centerSection);
        root.setBottom(bottomSection);
        
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
        openaiKeyField = new TextField();
        openaiKeyField.setPromptText("Enter your OpenAI API key");
        openaiKeyField.setPrefWidth(300);
        
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
            uploadImageButton.setTooltip(new Tooltip("OCR not available - Tesseract not installed"));
        }
        
        uploadImageButton.setOnAction(e -> uploadImage());
        uploadPdfButton.setOnAction(e -> uploadDocument("pdf"));
        uploadDocxButton.setOnAction(e -> uploadDocument("docx"));
        
        fileButtons.getChildren().addAll(uploadImageButton, uploadPdfButton, uploadDocxButton);
        
        inputSection.getChildren().addAll(inputLabel, inputTextArea, fileButtons);
        
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
        
        centerSection.getChildren().addAll(inputSection, outputSection);
        return centerSection;
    }
    
    private HBox createBottomSection() {
        HBox bottomSection = new HBox(20);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setPadding(new Insets(20));
        
        Button convertButton = new Button("Convert to Braille");
        convertButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        convertButton.setOnAction(e -> convertToBraille());
        
        Button clearButton = new Button("Clear All");
        clearButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        clearButton.setOnAction(e -> clearAll());
        
        Button printButton = new Button("Print Braille");
        printButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        printButton.setOnAction(e -> printBraille());
        
        Button enhanceButton = new Button("Enhance with AI");
        enhanceButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        enhanceButton.setOnAction(e -> enhanceWithAI());
        
        Button securityScanButton = new Button("Security Scan");
        securityScanButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        securityScanButton.setOnAction(e -> performSecurityScan());
        
        Button penetrationTestButton = new Button("Penetration Test");
        penetrationTestButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        penetrationTestButton.setOnAction(e -> performPenetrationTest());
        
        Button securityReportButton = new Button("Security Report");
        securityReportButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        securityReportButton.setOnAction(e -> showSecurityReport());
        
        bottomSection.getChildren().addAll(convertButton, clearButton, printButton, enhanceButton, 
                                         securityScanButton, penetrationTestButton, securityReportButton);
        return bottomSection;
    }
    
    private void saveOpenAIKey() {
        String apiKey = openaiKeyField.getText().trim();
        if (!apiKey.isEmpty()) {
            openaiService.setApiKey(apiKey);
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
                if (extractedText.contains("OCR not available") || extractedText.contains("OCR failed")) {
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
            showInfo("Success", "Text converted to Braille successfully!");
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
        
        try {
            String enhancedText = openaiService.enhanceText(inputText);
            inputTextArea.setText(enhancedText);
            showInfo("Success", "Text enhanced with AI successfully!");
        } catch (Exception e) {
            showError("AI Enhancement Error", "Failed to enhance text: " + e.getMessage());
        }
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
    
    private void clearAll() {
        inputTextArea.clear();
        brailleOutputArea.clear();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
            // Log security scan event
            securityLogger.logSecurityEvent(new SecurityEvent("SECURITY_SCAN", 
                "Manual security scan initiated", "INFO"));
            
            // Perform penetration test on input text
            PenetrationTestResult result = penetrationTester.performPenetrationTest(inputText, PenetrationTestType.COMPREHENSIVE);
            securityLogger.logVulnerabilityScan(result);
            
            // Show results
            showPenetrationTestResults(result);
            
        } catch (Exception e) {
            securityLogger.logSecurityEvent(new SecurityEvent("SECURITY_SCAN_ERROR", 
                "Security scan failed: " + e.getMessage(), "ERROR"));
            showError("Security Scan Error", "Failed to perform security scan: " + e.getMessage());
        }
    }
    
    private void performPenetrationTest() {
        String inputText = inputTextArea.getText().trim();
        if (inputText.isEmpty()) {
            showError("Error", "Please enter some text to perform penetration testing.");
            return;
        }
        
        try {
            // Log penetration test event
            securityLogger.logSecurityEvent(new SecurityEvent("PENETRATION_TEST", 
                "Penetration test initiated", "INFO"));
            
            // Perform comprehensive penetration test
            PenetrationTestResult result = penetrationTester.performPenetrationTest(inputText, PenetrationTestType.COMPREHENSIVE);
            securityLogger.logVulnerabilityScan(result);
            
            // Show detailed results
            showPenetrationTestResults(result);
            
        } catch (Exception e) {
            securityLogger.logSecurityEvent(new SecurityEvent("PENETRATION_TEST_ERROR", 
                "Penetration test failed: " + e.getMessage(), "ERROR"));
            showError("Penetration Test Error", "Failed to perform penetration test: " + e.getMessage());
        }
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
