import os
import re
import json
import pathlib

ROOT = pathlib.Path(__file__).parent / 'markdown'
OUT = pathlib.Path(__file__).parent / 'static' / 'search-index.json'

docs = []

# Scan markdown files
for md_path in ROOT.rglob('*.md'):
    try:
        content = md_path.read_text(encoding='utf-8')
    except Exception:
        continue

    rel_path = md_path.relative_to(ROOT).as_posix()
    category = "Documentation"
    if "reference/blocks/" in rel_path:
        category = "Blocks"
    elif "reference/components/" in rel_path:
        category = "Components"
    elif "tutorial" in rel_path.lower():
        category = "Tutorials"
    elif "faq" in rel_path.lower():
        category = "FAQs"

    if "reference/components/" in rel_path:
        base_name = md_path.stem
        html_base = f"/reference/components/{base_name}.html"
        current_section = ""
        current_anchor = ""
        current_body = []

        for line in content.splitlines():
            m = re.match(r'^##\s+([A-Za-z0-9_]+)\s*(?:\{#([A-Za-z0-9_\-\.]+)\})?', line)
            if m:
                if current_section and current_section != "Table of Contents":
                    url = f"{html_base}#{current_anchor or current_section}"
                    docs.append({
                        "id": f"{rel_path}#{current_section}",
                        "title": current_section,
                        "category": "Documentation",
                        "url": url,
                        "content": "\n".join(current_body)
                    })
                current_section = m.group(1)
                current_anchor = m.group(2) if m.group(2) else current_section
                current_body = []
            else:
                current_body.append(line)

        if current_section:
            url = f"{html_base}#{current_anchor or current_section}"
            docs.append({
                "id": f"{rel_path}#{current_section}",
                "title": current_section,
                "category": "Documentation",
                "url": url,
                "content": "\n".join(current_body)
            })
    else:
        title = md_path.stem.replace('_', ' ')
        title_m = re.search(r'(?m)^title:\s*(.+)$', content)
        h1_m = re.search(r'(?m)^#\s+(.+)$', content)
        if title_m:
            title = title_m.group(1).strip()
        elif h1_m:
            title = h1_m.group(1).strip()

        url = f"/{rel_path[:-3]}.html"
        docs.append({
            "id": rel_path,
            "title": title,
            "category": category,
            "url": url,
            "content": content
        })

# Curated tutorials and sample projects
tutorials_and_examples = [
    {
        "id": "tut-camera",
        "title": "Using the Camera in Your App",
        "category": "Tutorials",
        "url": "http://appinventor.mit.edu/explore/ai2/tutorials",
        "content": "Step-by-step instructions for adding camera functionality, taking pictures, and saving photos."
    },
    {
        "id": "faq-camera-perms",
        "title": "Camera Permission and Storage Access Issues",
        "category": "FAQs",
        "url": "https://community.appinventor.mit.edu",
        "content": "Common camera issues, runtime permissions on Android, error messages, and troubleshooting camera access."
    },
    {
        "id": "example-photo-capture",
        "title": "Photo Capture & Image Filter Sample Project",
        "category": "Examples",
        "url": "http://appinventor.mit.edu/explore/resources",
        "content": "Sample application project demonstrating Camera, Canvas, ImagePicker, and saving photos to Gallery."
    },
    {
        "id": "tut-bluetooth",
        "title": "Bluetooth Client and Server Communication",
        "category": "Tutorials",
        "url": "http://appinventor.mit.edu/explore/ai2/tutorials",
        "content": "Tutorial on connecting via BluetoothClient, scanning devices, and sending data to Arduino."
    },
    {
        "id": "example-drawing-app",
        "title": "PaintPot: Drawing and Color Palette App",
        "category": "Examples",
        "url": "http://appinventor.mit.edu/explore/ai2/paintpot-all",
        "content": "Step-by-step example project creating a painting and drawing app using Canvas and Camera."
    }
]

docs.extend(tutorials_and_examples)

OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps({"docs": docs}, indent=2, ensure_ascii=False), encoding='utf-8')
print(f"Wrote {len(docs)} docs to {OUT}")
