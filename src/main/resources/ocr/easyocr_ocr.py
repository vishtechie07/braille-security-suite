# -*- coding: utf-8 -*-
"""CLI for Java app: read image path, print extracted text to stdout (UTF-8)."""
from __future__ import annotations

import os
import sys


def main() -> None:
    if len(sys.argv) < 2:
        print("Usage: easyocr_ocr.py <image_path>", file=sys.stderr)
        sys.exit(2)
    path = sys.argv[1]
    if not os.path.isfile(path):
        print("File not found: " + path, file=sys.stderr)
        sys.exit(3)
    try:
        import easyocr  # noqa: PLC0415
    except ImportError:
        print(
            "easyocr is not installed. Run: pip install easyocr",
            file=sys.stderr,
        )
        sys.exit(4)
    reader = easyocr.Reader(["en"], gpu=False, verbose=False)
    result = reader.readtext(path)
    parts = [item[1] for item in result]
    text = " ".join(parts).strip()
    sys.stdout.buffer.write(text.encode("utf-8"))
    sys.stdout.flush()


if __name__ == "__main__":
    main()
