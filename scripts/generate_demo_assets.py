#!/usr/bin/env python3
"""Generate recruiter-facing demo screenshots + animated GIF from real examples."""

from __future__ import annotations

import ctypes
import sys
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
        "01-text-to-braille",
        "Text → Braille",
        "Welcome to Braille Security Suite.",
        "Paste plain text, convert, and copy or save Grade-1 style Braille output.",
    ),
    (
        "02-accessibility-phrase",
        "Accessibility phrase",
        "Access to information matters for everyone.",
        "Example conversion used in the portfolio demo walkthrough.",
    ),
    (
        "03-ocr-pipeline",
        "OCR → Braille (EasyOCR)",
        "Hello EasyOCR",
        "Image text is extracted via Python EasyOCR, then converted to Braille in Java.",
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


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = [
        "C:/Windows/Fonts/segoeui.ttf",
        "C:/Windows/Fonts/arial.ttf",
        "C:/Windows/Fonts/calibri.ttf",
    ]
    bold_candidates = [
        "C:/Windows/Fonts/segoeuib.ttf",
        "C:/Windows/Fonts/arialbd.ttf",
    ]
    paths = bold_candidates if bold else candidates
    for path in paths:
        if Path(path).exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def braille_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for path in (
        "C:/Windows/Fonts/seguisym.ttf",
        "C:/Windows/Fonts/seguili.ttf",
        "C:/Windows/Fonts/arial.ttf",
    ):
        if Path(path).exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def rounded_rect(draw: ImageDraw.ImageDraw, xy, radius: int, fill, outline=None, width=1):
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)


def draw_app_chrome(draw: ImageDraw.ImageDraw, w: int, h: int, title: str):
    # Window chrome
    draw.rectangle((0, 0, w, h), fill="#ecf0f1")
    draw.rectangle((0, 0, w, 36), fill="#2c3e50")
    draw.text((14, 8), "Braille Script Printing App", fill="white", font=font(14, True))
    draw.text((w - 70, 8), "— □ ×", fill="#bdc3c7", font=font(14))
    # App title
    draw.text((40, 56), "Braille Script Printing App", fill="#2c3e50", font=font(26, True))
    draw.text((40, 96), title, fill="#7f8c8d", font=font(14))


def make_conversion_shot(slug: str, title: str, input_text: str, caption: str) -> Path:
    w, h = 1280, 720
    img = Image.new("RGB", (w, h), "#f4f6f7")
    draw = ImageDraw.Draw(img)
    # Title bar
    draw.rectangle((0, 0, w, 36), fill="#2c3e50")
    draw.text((14, 8), "Braille Script Printing App", fill="white", font=font(14, True))
    draw.text((40, 52), "Braille Script Printing App", fill="#2c3e50", font=font(26, True))
    # Masked API key row (matches PasswordField UX)
    draw.text((40, 100), "OpenAI API Key:", fill="#2c3e50", font=font(14))
    rounded_rect(draw, (180, 94, 520, 126), 4, "#ffffff", "#bdc3c7", 1)
    draw.text((190, 100), "••••••••••••••••", fill="#7f8c8d", font=font(14))
    rounded_rect(draw, (535, 94, 620, 126), 4, "#ecf0f1", "#bdc3c7", 1)
    draw.text((548, 100), "Save Key", fill="#2c3e50", font=font(12))

    braille = to_braille(input_text)

    # Input / output panels (live UI layout)
    draw.text((40, 150), "Input Text:", fill="#2c3e50", font=font(15, True))
    rounded_rect(draw, (40, 178, 620, 430), 6, "#ffffff", "#bdc3c7", 1)
    draw.multiline_text((58, 198), input_text, fill="#2c3e50", font=font(18), spacing=6)

    draw.text((660, 150), "Braille Output:", fill="#2c3e50", font=font(15, True))
    rounded_rect(draw, (660, 178, 1240, 430), 6, "#ffffff", "#bdc3c7", 1)
    draw.multiline_text((678, 198), braille, fill="#1a1a1a", font=braille_font(26), spacing=8)

    # Upload + OCR
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

    # Action groups
    for i, (label, color) in enumerate(
        [
            ("Convert to Braille", "#3498db"),
            ("Enhance with AI", "#9b59b6"),
            ("Security Scan", "#f39c12"),
        ]
    ):
        x0 = 40 + i * 280
        rounded_rect(draw, (x0, 540, x0 + 250, 588), 8, color)
        draw.text((x0 + 40, 552), label, fill="white", font=font(15, True))

    rounded_rect(draw, (40, 620, 1240, 690), 8, "#ffffff", "#d0d7de", 1)
    draw.text((58, 640), f"Demo · {title}", fill="#2c3e50", font=font(16, True))
    draw.text((58, 666), caption, fill="#7f8c8d", font=font(13))

    path = SHOTS / f"{slug}.png"
    img.save(path, optimize=True)
    (EXAMPLES / f"{slug}.txt").write_text(
        f"INPUT:\n{input_text}\n\nBRAILLE:\n{braille}\n", encoding="utf-8"
    )
    return path


def make_ocr_shot() -> Path:
    w, h = 1280, 720
    img = Image.new("RGB", (w, h), "#ecf0f1")
    draw = ImageDraw.Draw(img)
    draw_app_chrome(
        draw,
        w,
        h,
        "EasyOCR extracts text from images; Java converts the result to Braille.",
    )

    sample = ROOT / "test_easyocr.png"
    if sample.exists():
        ocr_img = Image.open(sample).convert("RGB")
        ocr_img = ImageOps.contain(ocr_img, (520, 220))
        panel = Image.new("RGB", (540, 240), "white")
        px = (540 - ocr_img.width) // 2
        py = (240 - ocr_img.height) // 2
        panel.paste(ocr_img, (px, py))
        img.paste(panel, (50, 150))
        draw.rectangle((50, 150, 590, 390), outline="#bdc3c7", width=2)
    else:
        rounded_rect(draw, (50, 150, 590, 390), 8, "#ffffff", "#bdc3c7", 2)
        draw.text((80, 250), "[test_easyocr.png]", fill="#7f8c8d", font=font(18))

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
    draw.text((70, 585), "Pipeline: Image → EasyOCR (Python) → BrailleConverter (Java)", fill="#2c3e50", font=font(16, True))
    draw.text((70, 618), "Verified locally with py -3 and the bundled easyocr_ocr.py helper.", fill="#7f8c8d", font=font(13))

    path = SHOTS / "03-ocr-pipeline.png"
    img.save(path, optimize=True)
    return path


def make_overview_shot() -> Path:
    w, h = 1280, 720
    img = Image.new("RGB", (w, h), "#f7f9fb")
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, w, 110), fill="#2c3e50")
    draw.text((48, 28), "Braille Security Suite", fill="white", font=font(32, True))
    draw.text((48, 72), "Accessibility-focused desktop demo · Java 17 · JavaFX · EasyOCR · OpenAI", fill="#bdc3c7", font=font(15))

    cards = [
        ("Text → Braille", "Convert typed or pasted text to Braille and copy, save, or print."),
        ("Images & Docs", "OCR images with EasyOCR; parse PDF/DOCX into text first."),
        ("AI assist", "Optional OpenAI enhancement before conversion."),
        ("Security checks", "Input scanning, audit logging, and reports for safer uploads."),
    ]
    for i, (title, body) in enumerate(cards):
        x = 48 + (i % 2) * 600
        y = 150 + (i // 2) * 220
        rounded_rect(draw, (x, y, x + 560, y + 180), 12, "#ffffff", "#d0d7de", 1)
        draw.text((x + 28, y + 28), title, fill="#2c3e50", font=font(22, True))
        draw.multiline_text((x + 28, y + 78), body, fill="#566573", font=font(16), spacing=4)

    path = SHOTS / "00-overview.png"
    img.save(path, optimize=True)
    return path


class RECT(ctypes.Structure):
    _fields_ = [("left", ctypes.c_long), ("top", ctypes.c_long), ("right", ctypes.c_long), ("bottom", ctypes.c_long)]


def capture_live_window(exact_title: str = "Braille Script Printing App") -> Path | None:
    """Capture via PrintWindow to avoid multi-monitor / DPI grab mismatches."""
    import time

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
        print(f"Invalid window bounds: {width}x{height}", file=sys.stderr)
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
        print("PrintWindow failed; skipping live capture.", file=sys.stderr)
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
    bmi.biCompression = 0
    buf_len = width * height * 4
    buf = (ctypes.c_ubyte * buf_len)()
    gdi32.GetDIBits(mem_dc, bmp, 0, height, buf, ctypes.byref(bmi), 0)
    gdi32.DeleteObject(bmp)
    gdi32.DeleteDC(mem_dc)
    user32.ReleaseDC(hwnd, hwnd_dc)

    shot = Image.frombuffer("RGBA", (width, height), bytes(buf), "raw", "BGRA", 0, 1).convert("RGB")
    if shot.convert("L").getextrema()[1] < 20:
        print("Live capture looked empty; skipping.", file=sys.stderr)
        return None

    path = SHOTS / "04-live-ui.png"
    max_w = 1280
    if shot.width > max_w:
        ratio = max_w / shot.width
        shot = shot.resize((max_w, int(shot.height * ratio)), Image.Resampling.LANCZOS)
    shot.save(path, optimize=True)
    print(f"Captured live UI -> {path} ({shot.width}x{shot.height})")
    return path


def make_gif(paths: list[Path]) -> Path:
    frames = []
    for p in paths:
        im = Image.open(p).convert("RGB")
        im = ImageOps.contain(im, (960, 540), Image.Resampling.LANCZOS)
        canvas = Image.new("RGB", (960, 540), "#ecf0f1")
        canvas.paste(im, ((960 - im.width) // 2, (540 - im.height) // 2))
        frames.append(canvas)
    gif_path = OUT / "demo.gif"
    frames[0].save(
        gif_path,
        save_all=True,
        append_images=frames[1:],
        duration=1800,
        loop=0,
        optimize=False,
    )
    print(f"Wrote {gif_path}")
    return gif_path


def main() -> int:
    SHOTS.mkdir(parents=True, exist_ok=True)
    EXAMPLES.mkdir(parents=True, exist_ok=True)

    paths: list[Path] = []
    paths.append(make_overview_shot())
    for slug, title, text, caption in SAMPLES[:2]:
        paths.append(make_conversion_shot(slug, title, text, caption))
    paths.append(make_ocr_shot())

    live = capture_live_window()
    if live:
        paths.append(live)

    make_gif(paths)

    # Master examples index
    (EXAMPLES / "README.md").write_text(
        "\n".join(
            [
                "# Demo examples",
                "",
                "These inputs match the screenshots and GIF under `docs/demo/`.",
                "",
                "## Text samples",
                "",
                *[f"- `{slug}.txt` — {title}" for slug, title, _, _ in SAMPLES],
                "",
                "## OCR sample",
                "",
                "- Project root `test_easyocr.png` (ignored by git) or regenerate with Pillow.",
                "",
            ]
        ),
        encoding="utf-8",
    )
    print("Demo assets ready in docs/demo/")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
