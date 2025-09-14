package com.example.brailleapp.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Document parser for extracting text from PDF and DOCX files
 * Uses Apache PDFBox for PDF processing and Apache POI for DOCX processing
 */
public class DocumentParser {
    
    private static final Logger logger = Logger.getLogger(DocumentParser.class.getName());
    
    public DocumentParser() {
        // Constructor
    }
    
    /**
     * Parse a document and extract text content
     * @param file The document file to parse
     * @param fileType Type of file ("pdf" or "docx")
     * @return Extracted text content
     * @throws IOException if file reading fails
     * @throws IllegalArgumentException if unsupported file type
     */
    public String parseDocument(File file, String fileType) throws IOException, IllegalArgumentException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist: " + (file != null ? file.getPath() : "null"));
        }
        
        String lowerFileType = fileType.toLowerCase();
        
        switch (lowerFileType) {
            case "pdf":
                return parsePDF(file);
            case "docx":
                return parseDOCX(file);
            default:
                throw new IllegalArgumentException("Unsupported file type: " + fileType + 
                    ". Supported types: pdf, docx");
        }
    }
    
    /**
     * Parse PDF document and extract text
     * @param pdfFile PDF file to parse
     * @return Extracted text content
     * @throws IOException if PDF reading fails
     */
    public String parsePDF(File pdfFile) throws IOException {
        if (!pdfFile.getName().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("File is not a PDF: " + pdfFile.getName());
        }
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            
            // Configure text extraction settings
            pdfStripper.setSortByPosition(true);
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(document.getNumberOfPages());
            
            // Extract text
            String text = pdfStripper.getText(document);
            
            // Clean up the extracted text
            text = cleanExtractedText(text);
            
            logger.info("Successfully parsed PDF: " + pdfFile.getName() + 
                       " (Pages: " + document.getNumberOfPages() + ")");
            
            return text;
            
        } catch (Exception e) {
            logger.severe("Error parsing PDF: " + e.getMessage());
            throw new IOException("Failed to parse PDF file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse DOCX document and extract text
     * @param docxFile DOCX file to parse
     * @return Extracted text content
     * @throws IOException if DOCX reading fails
     */
    public String parseDOCX(File docxFile) throws IOException {
        if (!docxFile.getName().toLowerCase().endsWith(".docx")) {
            throw new IllegalArgumentException("File is not a DOCX: " + docxFile.getName());
        }
        
        try (FileInputStream fis = new FileInputStream(docxFile);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder text = new StringBuilder();
            
            // Extract text from paragraphs
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String paragraphText = paragraph.getText();
                if (paragraphText != null && !paragraphText.trim().isEmpty()) {
                    text.append(paragraphText).append("\n");
                }
            }
            
            // Extract text from tables
            List<XWPFTable> tables = document.getTables();
            for (XWPFTable table : tables) {
                text.append(extractTextFromTable(table));
            }
            
            // Clean up the extracted text
            String extractedText = cleanExtractedText(text.toString());
            
            logger.info("Successfully parsed DOCX: " + docxFile.getName());
            
            return extractedText;
            
        } catch (Exception e) {
            logger.severe("Error parsing DOCX: " + e.getMessage());
            throw new IOException("Failed to parse DOCX file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract text from a table in DOCX document
     * @param table The table to extract text from
     * @return Extracted text from the table
     */
    private String extractTextFromTable(XWPFTable table) {
        StringBuilder tableText = new StringBuilder();
        
        for (XWPFTableRow row : table.getRows()) {
            StringBuilder rowText = new StringBuilder();
            for (XWPFTableCell cell : row.getTableCells()) {
                String cellText = cell.getText();
                if (cellText != null && !cellText.trim().isEmpty()) {
                    rowText.append(cellText.trim()).append(" | ");
                }
            }
            if (rowText.length() > 0) {
                // Remove the last " | " separator
                rowText.setLength(rowText.length() - 3);
                tableText.append(rowText.toString()).append("\n");
            }
        }
        
        return tableText.toString();
    }
    
    /**
     * Clean up extracted text by removing unwanted characters and formatting
     * @param rawText Raw text from document parsing
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
        
        // Remove page numbers and headers/footers (basic cleanup)
        cleaned = cleaned.replaceAll("\\b\\d+\\b(?=\\s*$)", ""); // Remove standalone numbers at end of lines
        
        return cleaned;
    }
    
    /**
     * Get document information (page count, etc.)
     * @param file Document file
     * @param fileType Type of file ("pdf" or "docx")
     * @return Document information string
     * @throws IOException if file reading fails
     */
    public String getDocumentInfo(File file, String fileType) throws IOException {
        String lowerFileType = fileType.toLowerCase();
        
        switch (lowerFileType) {
            case "pdf":
                return getPDFInfo(file);
            case "docx":
                return getDOCXInfo(file);
            default:
                return "Unsupported file type: " + fileType;
        }
    }
    
    /**
     * Get PDF document information
     * @param pdfFile PDF file
     * @return PDF information string
     * @throws IOException if PDF reading fails
     */
    private String getPDFInfo(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            return "PDF Document - Pages: " + document.getNumberOfPages() + 
                   ", Title: " + document.getDocumentInformation().getTitle();
        } catch (Exception e) {
            throw new IOException("Failed to get PDF info: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get DOCX document information
     * @param docxFile DOCX file
     * @return DOCX information string
     * @throws IOException if DOCX reading fails
     */
    private String getDOCXInfo(File docxFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(docxFile);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            int paragraphCount = document.getParagraphs().size();
            int tableCount = document.getTables().size();
            
            return "DOCX Document - Paragraphs: " + paragraphCount + 
                   ", Tables: " + tableCount;
                   
        } catch (Exception e) {
            throw new IOException("Failed to get DOCX info: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if a file is a supported document type
     * @param file File to check
     * @return true if file type is supported
     */
    public boolean isSupportedDocument(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".pdf") || fileName.endsWith(".docx");
    }
    
    /**
     * Get the file type based on file extension
     * @param file File to check
     * @return File type ("pdf", "docx", or "unknown")
     */
    public String getFileType(File file) {
        if (file == null) {
            return "unknown";
        }
        
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) {
            return "pdf";
        } else if (fileName.endsWith(".docx")) {
            return "docx";
        } else {
            return "unknown";
        }
    }
}
