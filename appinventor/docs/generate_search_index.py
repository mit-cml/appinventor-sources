import os, json, pathlib

ROOT = pathlib.Path(os.path.abspath(__file__)).parents[1] / 'markdown'
OUT = pathlib.Path(__file__).parents[1] / 'static' / 'search-index.json'

docs = []
for md_path in ROOT.rglob('*.md'):
    try:
        text = md_path.read_text(encoding='utf-8')
    except Exception:
        continue
    title = md_path.stem.replace('_', ' ')
    # simple route: derive URL from path relative to markdown folder
    rel = md_path.relative_to(ROOT).with_suffix('')
    url = f"/markdown/{rel.as_posix()}"
    docs.append({
        'id': str(md_path.relative_to(ROOT)),
        'title': title,
        'content': text,
        'url': url
    })

OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps(docs, ensure_ascii=False), encoding='utf-8')
print(f"Wrote {len(docs)} docs to {OUT}")
