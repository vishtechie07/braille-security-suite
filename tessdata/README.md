# Tesseract OCR Data Files

This directory should contain Tesseract OCR language data files.

## Required Files

To enable OCR functionality, you need to download the following file:

- `eng.traineddata` - English language data file

## How to Get the Files

1. **Download from Tesseract GitHub:**
   - Go to: https://github.com/tesseract-ocr/tessdata
   - Download `eng.traineddata` file
   - Place it in this `tessdata/` directory

2. **Alternative Download:**
   - Visit: https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata
   - Save the file as `eng.traineddata` in this directory

## Installation Instructions

1. Download `eng.traineddata` from the links above
2. Place the file in this `tessdata/` directory
3. Restart the Braille Script Printing App
4. OCR functionality should now work

## File Structure

```
tessdata/
├── README.md (this file)
└── eng.traineddata (download this file)
```

## Troubleshooting

If OCR still doesn't work after adding the file:

1. Ensure the file is named exactly `eng.traineddata`
2. Check that the file is not corrupted (should be several MB in size)
3. Restart the application
4. Check the application logs for any error messages

## Supported Languages

Currently, the application is configured for English (`eng`). To add other languages:

1. Download the corresponding `.traineddata` file (e.g., `spa.traineddata` for Spanish)
2. Place it in this directory
3. The application will automatically detect it
