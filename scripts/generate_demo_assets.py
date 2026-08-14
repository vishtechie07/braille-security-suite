#!/usr/bin/env python3
"""Generate recruiter-facing demo screenshots + GIF. Never render secrets."""

from __future__ import annotations

import ctypes
import sys
import time
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageOps

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "demo"
SHOTS = OUT / "screenshots"
EXAMPLES = OUT / "examples"

# Same mapping as BrailleConverter.java
BRAILLE = {
    "a": "⠁", "b": "⠃", "c": "⠉", "d": "⠙", "e": "⠑", "f": "⠋", "g": "⠛",
    "h": "⠓", "i": "⠊", "j": "⠚", "k": "⠅", "l": "⠇", "m": "⠍", "n": "⠝",
    "o": "⠕", "p": "⠏", "q": "⠟", "r": "⠗", "s": "⠎", "t": "⠞", "u": "⠥",
    "v": "⠧", "w": "⠺", "x": "⠭", "y": "⠽", "z": "⠵",
    "1": "⠁", "2": "⠃", "3": "⠉", "4": "⠙", "5": "⠑", "6": "⠋", "7": "⠛",
    "8": "⠓", "9": "⠊", "0": "⠚",
    " ": " ", ".": "⠲", ",": "⠂", "!": "⠖", "?": "⠦", ";": "⠆", ":": "⠒",
    "-": "⠤", "(": "⠐⠣", ")": "⠐⠜", '"': "⠦", "'": "⠄",
}
CAPITAL = "⠠"

SAMPLES = [
    (
        "text-to-braille",
        "Text → Braille",
        "Welcome to Braille Security Suite.",
        "Paste plain text, convert, and copy or save Grade-1 style Braille output.",
    ),
    (
        "accessibility-phrase",
        "Accessibility phrase",
        "Access to information matters for everyone.",
        "Example conversion used in the portfolio demo walkthrough.",
    ),
]


def to_braille(text: str) -> str:
    out: list[str] = []
    was_capital = False
    for ch in text:
        if ch.isupper():
            if not was_capital:
                out.append(CAPITAL)
            out.append(BRAILLE.get(ch.lower(), "?"))
            was_capital = True
        else:
            out.append(BRAILLE.get(ch, "?"))
            was_capital = False
    return "".join(out)


def font(size: int, bold: bool = False):
    candidates = [
        "C:/Windows/Fonts/segoeuib.ttf" if bold else "C:/Windows/Fonts/segoeui.ttf",
        "C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf",
    ]
    for path in candidates:
        if Path(path).exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def braille_font(size: int):
    for path in ("C:/Windows/Fonts/seguisym.ttf", "C:/Windows/Fonts/arial.ttf"):
        if Path(path).exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def rounded_rect(draw, xy, radius, fill, outline=None, width=1):
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)


def redact_secrets_band(img: Image.Image) -> Image.Image:
    """Obliterate any API-key UI band so secrets cannot appear in portfolio media."""
    draw = ImageDraw.Draw(img)
    w, h = img.size
    # Cover title-adjacent key row on live JavaFX captures (field sits under the title)
    y0, y1 = int(h * 0.05), int(h * 0.22)
    draw.rectangle((0, y0, w, y1), fill="#eef2f5")
    draw.text((int(w * 0.03), y0 + 12), "Braille Script Printing App", fill="#2c3e50", font=font(22, True))
    draw.text(
        (int(w * 0.03), y0 + 48),
        "OpenAI API key: PasswordField only — never displayed in demo screenshots or GIF frames",
        fill="#566573",
        font=font(14),
    )
    return img


def assert_no_key_bytes(path: Path) -> None:
    data = path.read_bytes()
    # Real OpenAI keys look like sk-... with long alphanumerics; reject those patterns.
    import re

    if re.search(rb"sk-[A-Za-z0-9]{20,}", data):
        raise SystemExit(f"Refusing to keep possible API key bytes in {path}")


def make_overview() -> Path:
    w, h = 1280, 720
    img = Image.new("RGB", (w, h), "#f7f9fb")
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, w, 110), fill="#2c3e50")
    draw.text((48, 28), "Braille Security Suite", fill="white", font=font(32, True))
    draw.text(
        (48, 72),
        "Accessibility desktop demo · Java 17 · JavaFX · EasyOCR · OpenAI (key never shown)",
        fill="#bdc3c7",
        font=font(15),
    )
    cards = [
        ("Text → Braille", "Convert typed or pasted text to Braille and copy, save, or print."),
        ("Images & Docs", "OCR images with EasyOCR; parse PDF/DOCX into text first."),
        ("AI assist", "Optional OpenAI enhancement — API key stays masked in the UI."),
        ("Security checks", "Input scanning, audit logging, and reports for safer uploads."),
    ]
    for i, (title, body) in enumerate(cards):
        x = 48 + (i % 2) * 600
        y = 150 + (i // 2) * 220
        rounded_rect(draw, (x, y, x + 560, y + 180), 12, "#ffffff", "#d0d7de", 1)
        draw.text((x + 28, y + 28), title, fill="#2c3e50", font=font(22, True))
        draw.multiline_text((x + 28, y + 78), body, fill="#566573", font=font(16), spacing=4)
    path = SHOTS / "overview.png"
    img.save(path, optimize=True)
    assert_no_key_bytes(path)
    return path


def make_conversion_shot(slug: str, title: str, input_text: str, caption: str) -> Path:
    w, h = 1280, 720
    img = Image.new("RGB", (w, h), "#f4f6f7")
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, w, 36), fill="#2c3e50")
    draw.text((14, 8), "Braille Script Printing App", fill="white", font=font(14, True))
    draw.text((40, 52), "Braille Script Printing App", fill="#2c3e50", font=font(26, True))

    # No real key UI — static safe banner only
    draw.rectangle((40, 94, 1240, 130), fill="#e8eef2")
    draw.text(
        (52, 102),
        "OpenAI API key: PasswordField (masked) — omitted from demo screenshots",
        fill="#566573",
        font=font(14),
    )

    braille = to_braille(input_text)
    draw.text((40, 150), "Input Text:", fill="#2c3e50", font=font(15, True))
    rounded_rect(draw, (40, 178, 620, 430), 6, "#ffffff", "#bdc3c7", 1)
    draw.multiline_text((58, 198), input_text, fill="#2c3e50", font=font(18), spacing=6)

    draw.text((660, 150), "Braille Output:", fill="#2c3e50", font=font(15, True))
    rounded_rect(draw, (660, 178, 1240, 430), 6, "#ffffff", "#bdc3c7", 1)
    draw.multiline_text((678, 198), braille, fill="#1a1a1a", font=braille_font(26), spacing=8)

    for i, label in enumerate(("Upload Image", "Upload PDF", "Upload DOCX")):
        x0 = 40 + i * 140
        rounded_rect(draw, (x0, 450, x0 + 125, 484), 4, "#ecf0f1", "#bdc3c7", 1)
        draw.text((x0 + 12, 458), label, fill="#2c3e50", font=font(11))
    draw.text((40, 498), "OCR is ready.", fill="#27ae60", font=font(13))

    rounded_rect(draw, (660, 450, 740, 484), 4, "#ecf0f1", "#bdc3c7", 1)
    draw.text((680, 458), "Copy", fill="#2c3e50", font=font(12))
    rounded_rect(draw, (755, 450, 860, 484), 4, "#ecf0f1", "#bdc3c7", 1)
    draw.text((768, 458), "Save .txt", fill="#2c3e50", font=font(12))
    rounded_rect(draw, (875, 450, 1010, 484), 4, "#27ae60")
    draw.text((890, 458), "Print Braille", fill="white", font=font(12, True))

    for i, (label, color) in enumerate(
        (("Convert to Braille", "#3498db"), ("Enhance with AI", "#9b59b6"), ("Security Scan", "#f39c12"))
    ):
        x0 = 40 + i * 280
        rounded_rect(draw, (x0, 540, x0 + 250, 588), 8, color)
        draw.text((x0 + 40, 552), label, fill="white", font=font(15, True))

    rounded_rect(draw, (40, 620, 1240, 690), 8, "#ffffff", "#d0d7de", 1)
    draw.text((58, 640), f"Demo · {title}", fill="#2c3e50", font=font(16, True))
    draw.text((58, 666), caption, fill="#7f8c8d", font=font(13))

    path = SHOTS / f"{slug}.png"
    img.save(path, optimize=True)
    assert_no_key_bytes(path)
    (EXAMPLES / f"{slug}.txt").write_text(
        f"INPUT:\n{input_text}\n\nBRAILLE:\n{braille}\n", encoding="utf-8"
    )
    return path


def make_ocr_shot() -> Path:
    w, h = 1280, 720
    img = Image.new("RGB", (w, h), "#ecf0f1")
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, w, 36), fill="#2c3e50")
    draw.text((14, 8), "Braille Script Printing App", fill="white", font=font(14, True))
    draw.text((40, 52), "Braille Script Printing App", fill="#2c3e50", font=font(26, True))
    draw.text(
        (40, 96),
        "EasyOCR extracts text from images; Java converts the result to Braille.",
        fill="#7f8c8d",
        font=font(14),
    )

    sample = ROOT / "test_easyocr.png"
    if sample.exists():
        ocr_img = ImageOps.contain(Image.open(sample).convert("RGB"), (520, 220))
        panel = Image.new("RGB", (540, 240), "white")
        panel.paste(ocr_img, ((540 - ocr_img.width) // 2, (240 - ocr_img.height) // 2))
        img.paste(panel, (50, 150))
        draw.rectangle((50, 150, 590, 390), outline="#bdc3c7", width=2)
    else:
        rounded_rect(draw, (50, 150, 590, 390), 8, "#ffffff", "#bdc3c7", 2)
        draw.text((80, 250), "[OCR sample image]", fill="#7f8c8d", font=font(18))

    draw.text((50, 410), "Source image (OCR example)", fill="#2c3e50", font=font(14, True))
    extracted = "Hello EasyOCR"
    braille = to_braille(extracted)
    rounded_rect(draw, (650, 150, 1230, 300), 10, "#ffffff", "#bdc3c7", 2)
    draw.text((670, 166), "OCR extracted text", fill="#2c3e50", font=font(16, True))
    draw.text((670, 210), extracted, fill="#2c3e50", font=font(22))
    rounded_rect(draw, (650, 330, 1230, 520), 10, "#ffffff", "#bdc3c7", 2)
    draw.text((670, 346), "Braille output", fill="#2c3e50", font=font(16, True))
    draw.text((670, 400), braille, fill="#1a1a1a", font=braille_font(30))
    rounded_rect(draw, (50, 560, 1230, 660), 8, "#ffffff", "#d0d7de", 1)
    draw.text(
        (70, 585),
        "Pipeline: Image → EasyOCR (Python) → BrailleConverter (Java)",
        fill="#2c3e50",
        font=font(16, True),
    )
    draw.text(
        (70, 618),
        "No API keys appear in demo media. OpenAI key uses PasswordField in the live app.",
        fill="#7f8c8d",
        font=font(13),
    )
    path = SHOTS / "ocr-pipeline.png"
    img.save(path, optimize=True)
    assert_no_key_bytes(path)
    return path


class RECT(ctypes.Structure):
    _fields_ = [
        ("left", ctypes.c_long),
        ("top", ctypes.c_long),
        ("right", ctypes.c_long),
        ("bottom", ctypes.c_long),
    ]


def capture_live_window(exact_title: str = "Braille Script Printing App") -> Path | None:
    user32 = ctypes.windll.user32
    gdi32 = ctypes.windll.gdi32
    found = []

    @ctypes.WINFUNCTYPE(ctypes.c_bool, ctypes.c_void_p, ctypes.c_void_p)
    def enum_proc(hwnd, _lparam):
        if user32.IsWindowVisible(hwnd):
            length = user32.GetWindowTextLengthW(hwnd)
            buff = ctypes.create_unicode_buffer(length + 1)
            user32.GetWindowTextW(hwnd, buff, length + 1)
            if buff.value == exact_title:
                found.append(hwnd)
        return True

    user32.EnumWindows(enum_proc, 0)
    if not found:
        print("Live window not found; skipping capture.", file=sys.stderr)
        return None

    hwnd = found[-1]
    user32.ShowWindow(hwnd, 9)
    user32.SetForegroundWindow(hwnd)
    time.sleep(0.8)

    rect = RECT()
    user32.GetWindowRect(hwnd, ctypes.byref(rect))
    width = int(rect.right - rect.left)
    height = int(rect.bottom - rect.top)
    if width < 200 or height < 200:
        return None

    hwnd_dc = user32.GetWindowDC(hwnd)
    mem_dc = gdi32.CreateCompatibleDC(hwnd_dc)
    bmp = gdi32.CreateCompatibleBitmap(hwnd_dc, width, height)
    gdi32.SelectObject(mem_dc, bmp)
    ok = user32.PrintWindow(hwnd, mem_dc, 2) or user32.PrintWindow(hwnd, mem_dc, 0)
    if not ok:
        gdi32.DeleteObject(bmp)
        gdi32.DeleteDC(mem_dc)
        user32.ReleaseDC(hwnd, hwnd_dc)
        return None

    class BITMAPINFOHEADER(ctypes.Structure):
        _fields_ = [
            ("biSize", ctypes.c_uint32),
            ("biWidth", ctypes.c_int32),
            ("biHeight", ctypes.c_int32),
            ("biPlanes", ctypes.c_uint16),
            ("biBitCount", ctypes.c_uint16),
            ("biCompression", ctypes.c_uint32),
            ("biSizeImage", ctypes.c_uint32),
            ("biXPelsPerMeter", ctypes.c_int32),
            ("biYPelsPerMeter", ctypes.c_int32),
            ("biClrUsed", ctypes.c_uint32),
            ("biClrImportant", ctypes.c_uint32),
        ]

    bmi = BITMAPINFOHEADER()
    bmi.biSize = ctypes.sizeof(BITMAPINFOHEADER)
    bmi.biWidth = width
    bmi.biHeight = -height
    bmi.biPlanes = 1
    bmi.biBitCount = 32
    buf = (ctypes.c_ubyte * (width * height * 4))()
    gdi32.GetDIBits(mem_dc, bmp, 0, height, buf, ctypes.byref(bmi), 0)
    gdi32.DeleteObject(bmp)
    gdi32.DeleteDC(mem_dc)
    user32.ReleaseDC(hwnd, hwnd_dc)

    shot = Image.frombuffer("RGBA", (width, height), bytes(buf), "raw", "BGRA", 0, 1).convert("RGB")
    if shot.convert("L").getextrema()[1] < 20:
        return None

    # Ruthless: wipe any key UI pixels before the file exists on disk
    shot = redact_secrets_band(shot)
    max_w = 1280
    if shot.width > max_w:
        ratio = max_w / shot.width
        shot = shot.resize((max_w, int(shot.height * ratio)), Image.Resampling.LANCZOS)
        shot = redact_secrets_band(shot)

    path = SHOTS / "live-ui.png"
    shot.save(path, optimize=True)
    assert_no_key_bytes(path)
    print(f"Captured live UI -> {path} ({shot.width}x{shot.height})")
    return path


def make_gif(paths: list[Path]) -> Path:
    frames = []
    for p in paths:
        im = ImageOps.contain(Image.open(p).convert("RGB"), (960, 540), Image.Resampling.LANCZOS)
        canvas = Image.new("RGB", (960, 540), "#ecf0f1")
        canvas.paste(im, ((960 - im.width) // 2, (540 - im.height) // 2))
        # Final safety pass on every GIF frame
        frames.append(redact_secrets_band(canvas))
    gif_path = OUT / "demo.gif"
    frames[0].save(
        gif_path,
        save_all=True,
        append_images=frames[1:],
        duration=1800,
        loop=0,
        optimize=False,
    )
    assert_no_key_bytes(gif_path)
    print(f"Wrote {gif_path}")
    return gif_path


def main() -> int:
    SHOTS.mkdir(parents=True, exist_ok=True)
    EXAMPLES.mkdir(parents=True, exist_ok=True)

    # Remove old filenames that may still be CDN-cached with leaked pixels
    for stale in SHOTS.glob("*"):
        if stale.suffix.lower() == ".png":
            stale.unlink()

    paths: list[Path] = [make_overview()]
    for slug, title, text, caption in SAMPLES:
        paths.append(make_conversion_shot(slug, title, text, caption))
    paths.append(make_ocr_shot())
    live = capture_live_window()
    if live:
        paths.append(live)

    make_gif(paths)
    (EXAMPLES / "README.md").write_text(
        "# Demo examples\n\nInputs match `docs/demo` screenshots. API keys are never included in demo media.\n",
        encoding="utf-8",
    )
    print("Demo assets ready (API key row redacted).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
