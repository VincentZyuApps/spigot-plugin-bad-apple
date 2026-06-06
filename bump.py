#!/usr/bin/env python3
"""Bump version across tracked project files. Usage: python bump.py <new_version>"""

from __future__ import annotations

import os
import re
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))

FILES = {
    "build.gradle": "regex",
    "src/main/resources/plugin.yml": "regex",
    "bin/main/plugin.yml": "regex",
    "README.md": "regex",
    "README-zh.cn.md": "regex",
}

VERSION_SOURCE = os.path.join(ROOT, "build.gradle")


class S:
    BOLD = "\033[1m"
    GREEN = "\033[92m"
    YELLOW = "\033[93m"
    RED = "\033[91m"
    CYAN = "\033[96m"
    MAGENTA = "\033[95m"
    DIM = "\033[2m"
    RESET = "\033[0m"


def ok(msg: str) -> None:
    print(f"  {S.GREEN}✅{S.RESET} {msg}")


def warn(msg: str) -> None:
    print(f"  {S.YELLOW}⚠️ {S.RESET} {msg}")


def err(msg: str) -> None:
    print(f"  {S.RED}❌{S.RESET} {msg}")


def info(msg: str) -> None:
    print(f"  {S.CYAN}ℹ️ {S.RESET} {S.DIM}{msg}{S.RESET}")


def banner() -> None:
    print()
    print(f"  {S.BOLD}╔══════════════════════════════════════╗{S.RESET}")
    print(
        f"  {S.BOLD}║       {S.MAGENTA}📦  Version Bumper{S.RESET}{S.BOLD}             ║{S.RESET}"
    )
    print(f"  {S.BOLD}╚══════════════════════════════════════╝{S.RESET}")
    print()


def footer(old_ver: str, new_ver: str) -> None:
    print("  ---------------------------------------------")
    ok(f"{S.BOLD}Done: {old_ver} → {new_ver}{S.RESET}")
    print()


def extract_base(ver: str) -> str | None:
    match = re.match(r"(\d+\.\d+\.\d+)", ver)
    return match.group(1) if match else None


def build_regex(base: str) -> re.Pattern[str]:
    escaped = base.replace(".", r"\.")
    return re.compile(rf"{escaped}[-+\w.]*")


def replace_regex(content: str, old_ver: str, new_ver: str) -> str:
    base = extract_base(old_ver)
    if not base:
        return content
    return build_regex(base).sub(new_ver, content)


def find_old_values(content: str, old_ver: str) -> list[str]:
    base = extract_base(old_ver)
    if not base:
        return []
    return list(dict.fromkeys(build_regex(base).findall(content)))


def read_current_version() -> str:
    with open(VERSION_SOURCE, "r", encoding="utf-8") as f:
        content = f.read()

    match = re.search(r"^version\s*=\s*'([^']+)'", content, re.MULTILINE)
    if not match:
        raise RuntimeError("Could not locate version = '...' in build.gradle")
    return match.group(1)


def main() -> None:
    if len(sys.argv) != 2 or sys.argv[1] in ("-h", "--help"):
        banner()
        print(f"  {S.BOLD}Usage:{S.RESET} python {sys.argv[0]} {S.CYAN}<new_version>{S.RESET}")
        print(f"  {S.BOLD}e.g.:{S.RESET}  python {sys.argv[0]} 0.2.5-beta.2")
        print()
        sys.exit(1)

    banner()

    new_ver = sys.argv[1]
    old_ver = read_current_version()

    if old_ver == new_ver:
        warn(f"Already at {old_ver}, nothing to do.")
        print()
        return

    info(f"Old version: {old_ver}")
    info(f"New version: {new_ver}")
    print("  ---------------------------------------------")
    print()

    for rel_path, mode in FILES.items():
        path = os.path.join(ROOT, rel_path)
        print(f"  {S.BOLD}📄 {rel_path}{S.RESET}")

        if not os.path.exists(path):
            warn("file not found, skipped")
            print()
            continue

        with open(path, "r", encoding="utf-8") as f:
            content = f.read()

        old_vals: list[str] = []
        count = 0

        if mode == "regex":
            old_vals = find_old_values(content, old_ver)
            count = len(old_vals)
            if count:
                content = replace_regex(content, old_ver, new_ver)
        else:
            count = content.count(old_ver)
            if count:
                content = content.replace(old_ver, new_ver)

        if count == 0:
            warn("no version strings found, skipped")
            print()
            continue

        with open(path, "w", encoding="utf-8") as f:
            f.write(content)

        ok(f"replaced {count} occurrence(s): {', '.join(old_vals) if old_vals else old_ver}")
        print()

    footer(old_ver, new_ver)


if __name__ == "__main__":
    try:
        main()
    except Exception as ex:  # pragma: no cover
        err(str(ex))
        print()
        sys.exit(1)
