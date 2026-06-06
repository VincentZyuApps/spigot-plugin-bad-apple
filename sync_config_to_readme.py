import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent
CONFIG_YML = PROJECT_ROOT / "src" / "main" / "resources" / "config.yml"

README_FILES = {
    PROJECT_ROOT / "README.md": "Default config:",
    PROJECT_ROOT / "README-zh.cn.md": "默认配置如下：",
}


def update_readme(filepath: Path, marker: str):
    config_text = CONFIG_YML.read_text(encoding="utf-8").strip()
    readme_text = filepath.read_text(encoding="utf-8")

    marker_idx = readme_text.index(marker)
    after_marker = readme_text[marker_idx:]

    fence_start = after_marker.index("```yml")
    fence_end = after_marker.index("```", fence_start + 6)

    new_block = f"```yml\n{config_text}\n```"

    new_readme = (
        readme_text[: marker_idx + fence_start]
        + new_block
        + readme_text[marker_idx + fence_end + 3 :]
    )

    filepath.write_text(new_readme, encoding="utf-8")
    print(f"  ✅ {filepath.name} synchronised")


def main():
    if not CONFIG_YML.exists():
        print(f"❌ Config not found: {CONFIG_YML}", file=sys.stderr)
        sys.exit(1)

    targets = sys.argv[1:] if len(sys.argv) > 1 else list(README_FILES.keys())

    for target in targets:
        path = Path(target)
        if not path.is_absolute():
            path = PROJECT_ROOT / path
        if path not in README_FILES:
            print(f"⚠️  Skipping unknown file: {path.name}", file=sys.stderr)
            continue
        if not path.exists():
            print(f"❌ File not found: {path}", file=sys.stderr)
            continue
        update_readme(path, README_FILES[path])


if __name__ == "__main__":
    main()
