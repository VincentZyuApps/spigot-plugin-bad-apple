from __future__ import annotations

import base64
from pathlib import Path


ROOT = Path(__file__).resolve().parent
LOGO_DIR = ROOT.parent / "logo"


def svg_data_url(svg_path: Path) -> str:
    raw = svg_path.read_bytes()
    encoded = base64.b64encode(raw).decode("ascii")
    return f"data:image/svg+xml;base64,{encoded}"


def escape_text(text: str) -> str:
    return (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
    )


def badge_svg(
    *,
    width: int,
    height: int,
    label: str,
    message: str,
    bg: str,
    label_bg: str,
    text_color: str,
    logo_path: Path,
    logo_box: tuple[int, int, int, int],
    logo_bg: str | None = None,
    font_size: int = 12,
    radius: int = 9,
    font_family: str = "Arial, Helvetica, sans-serif",
) -> str:
    lx, ly, lw, lh = logo_box
    logo_href = svg_data_url(logo_path)
    logo_end_x = lx + lw
    label_estimated_w = len(label) * 7 + 16
    label_w = max(68, logo_end_x + label_estimated_w)
    message_w = width - label_w

    left_bg = label_bg
    right_bg = bg
    logo_bg_rect = ""
    if logo_bg:
        logo_bg_rect = (
            f'<rect x="{lx}" y="{ly}" width="{lw}" height="{lh}" rx="{lh // 2}" ry="{lh // 2}" fill="{logo_bg}"/>'
        )

    label_text_x = logo_end_x + (label_w - logo_end_x) // 2
    message_text_x = label_w + message_w // 2
    text_y = height // 2 + font_size // 3

    return f"""<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}" role="img" aria-label="{escape_text(label)}: {escape_text(message)}">
  <title>{escape_text(label)}: {escape_text(message)}</title>
  <defs>
    <clipPath id="clip">
      <rect width="{width}" height="{height}" rx="{radius}" ry="{radius}"/>
    </clipPath>
  </defs>
  <g clip-path="url(#clip)">
    <rect width="{label_w}" height="{height}" fill="{left_bg}"/>
    <rect x="{label_w}" width="{message_w}" height="{height}" fill="{right_bg}"/>
    <rect width="{width}" height="{height}" fill="none" stroke="rgba(0,0,0,0.16)" stroke-width="1"/>
    {logo_bg_rect}
    <image href="{logo_href}" x="{lx}" y="{ly}" width="{lw}" height="{lh}" preserveAspectRatio="xMidYMid meet"/>
    <text x="{label_text_x}" y="{text_y}" fill="{text_color}" text-anchor="middle"
          font-family="{font_family}" font-size="{font_size}" font-weight="700">{escape_text(label)}</text>
    <text x="{message_text_x}" y="{text_y}" fill="{text_color}" text-anchor="middle"
          font-family="{font_family}" font-size="{font_size}" font-weight="700">{escape_text(message)}</text>
  </g>
</svg>
"""


def minecraft_badge_svg(*, logo_path: Path, message: str, output_width: int = 196) -> str:
    label = "Minecraft"
    height = 30
    left_w = 96
    min_right_w = 216
    message_padding = 40
    message_estimated_w = len(message) * 7 + message_padding
    right_w = max(min_right_w, message_estimated_w)
    output_width = max(output_width, left_w + right_w)
    logo_href = svg_data_url(logo_path)
    logo_end_x = 8 + 20
    label_text_x = logo_end_x + (left_w - logo_end_x) // 2
    message_text_x = left_w + right_w // 2
    text_y = 19
    return f"""<svg xmlns="http://www.w3.org/2000/svg" width="{output_width}" height="{height}" viewBox="0 0 {output_width} {height}" role="img" aria-label="{escape_text(label)}: {escape_text(message)}">
  <title>{escape_text(label)}: {escape_text(message)}</title>
  <defs>
    <linearGradient id="grass" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#4CAF50"/>
      <stop offset="100%" stop-color="#2E7D32"/>
    </linearGradient>
    <linearGradient id="stone" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#9E9E9E"/>
      <stop offset="100%" stop-color="#616161"/>
    </linearGradient>
    <pattern id="pixel-noise" width="8" height="8" patternUnits="userSpaceOnUse">
      <rect width="8" height="8" fill="rgba(255,255,255,0.03)"/>
      <rect x="0" y="0" width="2" height="2" fill="rgba(0,0,0,0.08)"/>
      <rect x="4" y="2" width="2" height="2" fill="rgba(255,255,255,0.08)"/>
      <rect x="2" y="5" width="2" height="2" fill="rgba(0,0,0,0.07)"/>
    </pattern>
    <clipPath id="clip">
      <rect width="{output_width}" height="{height}" rx="4" ry="4"/>
    </clipPath>
  </defs>
  <g clip-path="url(#clip)">
    <rect width="{left_w}" height="{height}" fill="url(#grass)"/>
    <rect x="{left_w}" width="{right_w}" height="{height}" fill="url(#stone)"/>
    <rect width="{output_width}" height="{height}" fill="url(#pixel-noise)"/>
    <rect x="0" y="0" width="{output_width}" height="{height}" fill="none" stroke="rgba(0,0,0,0.18)" stroke-width="1"/>
    <image href="{logo_href}" x="8" y="5" width="20" height="20" preserveAspectRatio="xMidYMid meet"/>
    <text x="{label_text_x}" y="{text_y}" fill="#ffffff" text-anchor="middle"
          font-family="Arial, Helvetica, sans-serif" font-size="12" font-weight="700">{escape_text(label)}</text>
    <text x="{message_text_x}" y="{text_y}" fill="#ffffff" text-anchor="middle"
          font-family="Arial, Helvetica, sans-serif" font-size="12" font-weight="700">{escape_text(message)}</text>
  </g>
</svg>
"""


def main() -> None:
    print(f"[badge] output dir: {ROOT}")
    print(f"[badge] logo dir:   {LOGO_DIR}")

    outputs = {
        "paper.svg": badge_svg(
            width=188,
            height=28,
            label="Paper",
            message="1.21.8",
            bg="#1F93FF",
            label_bg="#0F172A",
            text_color="#FFFFFF",
            logo_path=LOGO_DIR / "papermc.svg",
            logo_box=(7, 4, 20, 20),
            logo_bg="#FFFFFF",
            font_size=12,
            radius=10,
        ),
        "spigot.svg": badge_svg(
            width=188,
            height=28,
            label="Spigot",
            message="1.21.8",
            bg="#ED8106",
            label_bg="#1F2937",
            text_color="#FFFFFF",
            logo_path=LOGO_DIR / "spigotmc.svg",
            logo_box=(7, 4, 20, 20),
            logo_bg="#FFFFFF",
            font_size=12,
            radius=10,
        ),
        "minecraft.svg": minecraft_badge_svg(
            logo_path=LOGO_DIR / "minecraft.svg",
            message="CLICK HERE TO DOWNLOAD RESOURCE PACK",
        ),
    }

    for filename, content in outputs.items():
        output_path = ROOT / filename
        output_path.write_text(content, encoding="utf-8")
        print(f"[badge] wrote {output_path.name}")

    print(f"[badge] done, generated {len(outputs)} badge svg files.")


if __name__ == "__main__":
    main()
