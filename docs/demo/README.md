# Portfolio demo assets

Generated screenshots, example inputs/outputs, and an animated walkthrough GIF for recruiters.

## Contents

| File | Description |
|------|-------------|
| `demo.gif` | Short looping walkthrough (overview → text→Braille → OCR → live UI) |
| `screenshots/00-overview.png` | Feature overview card |
| `screenshots/01-text-to-braille.png` | Example: *Welcome to Braille Security Suite.* |
| `screenshots/02-accessibility-phrase.png` | Example: accessibility phrase → Braille |
| `screenshots/03-ocr-pipeline.png` | EasyOCR image → text → Braille |
| `screenshots/04-live-ui.png` | Live JavaFX window capture (API key redacted) |
| `examples/*.txt` | Exact input/output pairs used in the shots |

## Regenerate

With the app running (`mvn javafx:run`) optionally for the live capture:

```bash
python scripts/generate_demo_assets.py
```

Braille in the example panels uses the same Grade-1 mapping as `BrailleConverter.java`.
