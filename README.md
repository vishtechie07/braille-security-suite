# Braille Security Suite

A comprehensive Java application that converts text, images, and documents to Braille with enterprise-grade security features including file scanning, vulnerability detection, and penetration testing capabilities.

## 🚀 Features

### Core Functionality
- **Text-to-Braille Conversion**: Convert plain text to Braille script
- **OCR Support**: Extract text from images using Tesseract OCR
- **Document Parsing**: Parse PDF and DOCX files to extract text
- **AI Enhancement**: Integrate with OpenAI API for text optimization
- **Braille Printing**: Print Braille output to physical printers

### Security Features
- **File Security Scanning**: Comprehensive malware and threat detection
- **Penetration Testing**: Automated vulnerability assessment
- **Input Validation**: SQL injection, XSS, and command injection protection
- **Audit Logging**: Complete security event tracking
- **Threat Detection**: Real-time security monitoring
- **Security Reporting**: Detailed vulnerability reports

## 🛠️ Technology Stack

- **Java 17**: Core application framework
- **JavaFX**: Modern desktop UI
- **Maven**: Dependency management and build automation
- **Tesseract OCR**: Optical character recognition
- **Apache POI**: Microsoft Office document processing
- **PDFBox**: PDF document parsing
- **OpenAI API**: AI-powered text enhancement
- **Jackson**: JSON processing
- **Apache HttpClient**: HTTP communication

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Tesseract OCR (for image text extraction)
- OpenAI API key (for AI enhancement)

## 🔧 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/vishtechie07/braille-security-suite.git
   cd braille-security-suite
   ```

2. **Install Tesseract OCR**
   - **Windows**: Download from [UB-Mannheim Tesseract](https://github.com/UB-Mannheim/tesseract/wiki)
   - **macOS**: `brew install tesseract`
   - **Linux**: `sudo apt-get install tesseract-ocr`

3. **Download Tesseract Language Data**
   ```bash
   # Create tessdata directory
   mkdir tessdata
   
   # Download English language data
   curl -L https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata -o tessdata/eng.traineddata
   ```

4. **Build the project**
   ```bash
   mvn clean compile
   ```

## 🚀 Usage

### Running the Application

**Windows:**
```bash
mvn javafx:run
```

**Linux/macOS:**
```bash
mvn javafx:run
```

**Or use the provided scripts:**
- Windows: `run.bat`
- Linux/macOS: `./run.sh`

### Basic Workflow

1. **Enter Text**: Type or paste text in the input area
2. **Upload Files**: Use the upload buttons for images, PDFs, or DOCX files
3. **Convert to Braille**: Click "Convert to Braille" to generate Braille output
4. **Enhance with AI**: Use "Enhance with AI" for text optimization (requires OpenAI API key)
5. **Print**: Click "Print Braille" to print the output

### Security Features

1. **Security Scan**: Click "Security Scan" to analyze input text for vulnerabilities
2. **Penetration Test**: Click "Penetration Test" for comprehensive security testing
3. **Security Report**: Click "Security Report" to view detailed security statistics

## 🔐 Security Features

### File Security Scanning
- Malware detection and file signature validation
- Content analysis for malicious patterns
- Embedded executable detection
- PDF security analysis

### Penetration Testing
- SQL injection testing
- XSS vulnerability detection
- Command injection testing
- File upload security validation
- Authentication security testing

### Security Monitoring
- Comprehensive audit logging
- Real-time threat detection
- Security event tracking
- Vulnerability reporting

## 📁 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/brailleapp/
│   │       ├── BrailleApp.java              # Main application class
│   │       ├── BrailleDisplayComponent.java # Braille display component
│   │       ├── services/                    # Service layer
│   │       │   ├── BrailleConverter.java    # Braille conversion logic
│   │       │   ├── DocumentParser.java      # Document parsing service
│   │       │   ├── OCRService.java          # OCR service
│   │       │   └── OpenAIService.java       # OpenAI integration
│   │       └── security/                    # Security module
│   │           ├── SecurityScanner.java     # File security scanning
│   │           ├── PenetrationTester.java   # Penetration testing
│   │           ├── SecurityAuditLogger.java # Audit logging
│   │           └── ...                      # Other security classes
│   └── resources/
│       └── application.properties           # Application configuration
├── tessdata/                                # Tesseract language data
├── .gitignore                              # Git ignore rules
└── README.md
```

## 🔧 Configuration

### OpenAI API Key
1. Get your API key from [OpenAI Platform](https://platform.openai.com/api-keys)
2. Enter the key in the application's API key field
3. Click "Save Key" to store it

### Tesseract Configuration
- Ensure `tessdata/eng.traineddata` is present
- The application will automatically detect the tessdata directory

## 🐛 Troubleshooting

### OCR Issues
- **Problem**: "OCR not available" error
- **Solution**: Install Tesseract OCR and download `eng.traineddata`

### Security Scanning
- **Problem**: Security scan fails
- **Solution**: Check file permissions and ensure input is valid

### Build Issues
- **Problem**: Maven build fails
- **Solution**: Ensure Java 17+ and Maven 3.6+ are installed

## 📊 Security Logs

Security events are logged in the following files:
- `security_logs/security_audit.log` - General security events
- `security_logs/threat_detection.log` - Threat detections
- `security_logs/vulnerability_scan.log` - Penetration test results

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Tesseract OCR](https://github.com/tesseract-ocr/tesseract) for OCR capabilities
- [Apache POI](https://poi.apache.org/) for Office document processing
- [PDFBox](https://pdfbox.apache.org/) for PDF processing
- [OpenAI](https://openai.com/) for AI text enhancement
- [JavaFX](https://openjfx.io/) for the modern UI framework

**Note**: This application is designed for educational and professional use. Always ensure you have proper authorization before performing security testing on any systems.
