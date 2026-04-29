#!/usr/bin/env python3
"""Insert a KDoc stub above any Kotlin function, class, interface, or object
declaration that is not already preceded by a documentation comment.

Usage: python3 insert_kdoc.py [file.kt ...]
"""

import re
import sys

# Matches the start of a Kotlin declaration we want to document.
# Captures leading indentation in group 'indent'.
# Requires a word character after the keyword to avoid matching anonymous
# objects used as expressions (e.g. `object : SomeInterface { ... }`).
DECL_RE = re.compile(
    r'^(?P<indent>\s*)'
    r'(?:(?:public|private|protected|internal|open|abstract|override|'
    r'sealed|data|inline|tailrec|suspend|companion|enum|actual|expect|'
    r'external|operator|infix|value|inner)\s+)*'
    r'(?:fun|class|interface|object)\s+\w'
)


def _find_annotation_start(out: list, before_idx: int) -> int:
    """Return the index in *out* at which to insert a KDoc stub so it appears
    before any annotation lines that immediately precede the declaration."""
    i = before_idx - 1
    while i >= 0 and out[i].strip().startswith('@'):
        i -= 1
    return i + 1


def _preceding_is_comment(lines: list, before_idx: int) -> bool:
    """Return True if the line immediately before *before_idx* (skipping blank
    lines and annotation lines) looks like a documentation or line comment."""
    i = before_idx - 1
    while i >= 0:
        s = lines[i].strip()
        if s == '' or s.startswith('@'):
            i -= 1
            continue
        # Ends with */ (close of block comment) or starts with // or *
        return s.endswith('*/') or s.startswith('//') or s.startswith('*')
    return False


def process_file(path: str) -> bool:
    """Scan *path* and insert KDoc stubs where needed.
    Returns True if the file was modified, False otherwise."""
    with open(path, encoding='utf-8') as fh:
        lines = fh.readlines()

    out = []
    changed = False
    in_block_comment = False

    for line in lines:
        stripped = line.strip()

        # ── block-comment tracking ──────────────────────────────────────
        if not in_block_comment and '/*' in stripped:
            after_open = stripped[stripped.index('/*') + 2:]
            if '*/' not in after_open:
                # Multi-line block comment opened here; skip until it closes.
                in_block_comment = True
            out.append(line)
            continue

        if in_block_comment:
            if '*/' in stripped:
                in_block_comment = False
            out.append(line)
            continue
        # ───────────────────────────────────────────────────────────────

        m = DECL_RE.match(line)
        if m and not _preceding_is_comment(out, len(out)):
            indent = m.group('indent')
            insert_at = _find_annotation_start(out, len(out))
            out.insert(insert_at, f'{indent}/** TODO: add documentation. */\n')
            changed = True

        out.append(line)

    if changed:
        with open(path, 'w', encoding='utf-8') as fh:
            fh.writelines(out)
        print(f'Updated: {path}')
    return changed


def main() -> None:
    for path in sys.argv[1:]:
        try:
            process_file(path)
        except Exception as exc:
            print(f'ERROR processing {path}: {exc}', file=sys.stderr)


if __name__ == '__main__':
    main()
