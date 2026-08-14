# Portfolio demo assets

Screenshots and GIF for recruiters. **OpenAI API keys are never shown** in these assets.

## Contents

| File | Description |
|------|-------------|
| `demo.gif` | Walkthrough GIF |
| `screenshots/overview.png` | Feature overview |
| `screenshots/text-to-braille.png` | Text → Braille example |
| `screenshots/accessibility-phrase.png` | Accessibility phrase example |
| `screenshots/ocr-pipeline.png` | EasyOCR → Braille |
| `screenshots/live-ui.png` | Live JavaFX capture (API key row redacted) |
| `examples/*.txt` | Exact input/output pairs |

## Regenerate

```bash
# optional: mvn javafx:run  (for live-ui.png)
python scripts/generate_demo_assets.py
```

The generator wipes the API-key UI band on every live frame and rejects files that contain `sk-...` key-like byte patterns.
