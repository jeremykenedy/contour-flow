#!/usr/bin/env python3
"""Check repository Markdown links and the untested-device request."""

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MARKDOWN_LINK = re.compile(r"\[[^\]]*\]\(([^)]+)\)")


def check_links():
    broken = []
    for markdown in ROOT.rglob("*.md"):
        if ".git" in markdown.parts or "build" in markdown.parts:
            continue
        for target in MARKDOWN_LINK.findall(markdown.read_text()):
            target = target.strip().split(" ", 1)[0].strip("<>")
            if not target or target.startswith(("https://", "http://", "mailto:", "#")):
                continue
            path = target.split("#", 1)[0]
            if not path:
                continue
            if not (markdown.parent / path).resolve().exists():
                broken.append(f"{markdown.relative_to(ROOT)}: {target}")
    if broken:
        raise SystemExit("Broken documentation links:\n" + "\n".join(broken))


def check_device_request():
    verification = (ROOT / "docs/VERIFICATION.md").read_text()
    required = (
        "Fire TV owner",
        "Android TV owner",
        "Google TV owner",
        "model",
        "resolution",
        "results",
    )
    missing = [text for text in required if text.lower() not in verification.lower()]
    if missing:
        raise SystemExit(
            "Device verification request is missing: " + ", ".join(missing)
        )


if __name__ == "__main__":
    check_links()
    check_device_request()
    print("Documentation links and device requests are valid.")
