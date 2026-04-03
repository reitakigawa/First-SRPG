from __future__ import annotations

import json
from datetime import date
from pathlib import Path
from typing import Any

from flask import Flask, abort, render_template, url_for

app = Flask(__name__)

BASE_DIR = Path(__file__).resolve().parent
CONTENT_DIR = BASE_DIR / "content"

NOVEL_DIR = CONTENT_DIR / "novels"
BLOG_DIR = CONTENT_DIR / "blog"
GALLERY_DIR = CONTENT_DIR / "gallery"
GAME_DIR = CONTENT_DIR / "games"


def read_json_file(path: Path) -> dict[str, Any]:
    if not path.exists():
        abort(404, description=f"JSON not found: {path.name}")
    return json.loads(path.read_text(encoding="utf-8"))


def normalize_date(value: str | None) -> str | None:
    if not value:
        return None
    try:
        return date.fromisoformat(value).isoformat()
    except ValueError:
        return value


def parse_markdown_content(path: Path) -> dict[str, Any]:
    if not path.exists():
        abort(404, description=f"Markdown not found: {path.name}")

    raw_text = path.read_text(encoding="utf-8")
    metadata: dict[str, str] = {}
    body = raw_text

    if raw_text.startswith("---\n"):
        parts = raw_text.split("\n---\n", 1)
        if len(parts) == 2:
            header_text, body_text = parts
            for line in header_text.splitlines()[1:]:
                if ":" not in line:
                    continue
                key, value = line.split(":", 1)
                metadata[key.strip()] = value.strip().strip('"').strip("'")
            body = body_text

    slug = path.stem
    return {
        "slug": slug,
        "title": metadata.get("title", slug),
        "date": normalize_date(metadata.get("date")),
        "content": body.strip(),
    }


def list_markdown_items(directory: Path) -> list[dict[str, Any]]:
    items = [parse_markdown_content(path) for path in sorted(directory.glob("*.md"))]
    items.sort(key=lambda item: item["date"] or "", reverse=True)
    return items


def read_json_item(directory: Path, item_id: str) -> dict[str, Any]:
    data = read_json_file(directory / f"{item_id}.json")
    data.setdefault("id", item_id)
    data.setdefault("slug", item_id)
    data.setdefault("title", item_id)
    if "date" in data:
        data["date"] = normalize_date(str(data["date"]))
    return data


def list_json_items(directory: Path) -> list[dict[str, Any]]:
    json_paths = [path for path in sorted(directory.glob("*.json")) if path.stem != "index"]
    items = [read_json_item(directory, path.stem) for path in json_paths]
    items.sort(key=lambda item: item.get("date") or "", reverse=True)
    return items


@app.get("/")
def index():
    return render_template(
        "home.html",
        latest_novels=list_markdown_items(NOVEL_DIR)[:3],
        latest_posts=list_markdown_items(BLOG_DIR)[:3],
        latest_gallery_items=list_json_items(GALLERY_DIR)[:3],
        latest_games=list_json_items(GAME_DIR)[:3],
    )


@app.get("/novels")
def novels_index():
    return render_template("novels/list.html", novels=list_markdown_items(NOVEL_DIR))


@app.get("/novels/<slug>")
def novels_detail(slug: str):
    return render_template("novels/detail.html", novel=parse_markdown_content(NOVEL_DIR / f"{slug}.md"))


@app.get("/blog")
def blog_index():
    return render_template("blog/list.html", posts=list_markdown_items(BLOG_DIR))


@app.get("/blog/<slug>")
def blog_detail(slug: str):
    return render_template("blog/detail.html", post=parse_markdown_content(BLOG_DIR / f"{slug}.md"))


@app.get("/gallery")
def gallery_index():
    return render_template("gallery/list.html", items=list_json_items(GALLERY_DIR))


@app.get("/gallery/<item_id>")
def gallery_detail(item_id: str):
    return render_template("gallery/detail.html", item=read_json_item(GALLERY_DIR, item_id))


@app.get("/games")
def games_index():
    return render_template("games/list.html", games=list_json_items(GAME_DIR))


@app.get("/games/<slug>")
def games_detail(slug: str):
    game = read_json_item(GAME_DIR, slug)
    static_path = game.get("static_path")
    game_url = None
    if static_path:
        game_url = url_for("static", filename=str(static_path).lstrip("/"))
    return render_template("games/detail.html", game=game, game_url=game_url)


if __name__ == "__main__":
    app.run(debug=True)
