#!/usr/bin/env bash
#
# Renames the template package and app name in all tracked files,
# moves Kotlin source directories and reformats with ktlint.
#
# Usage:
#   scripts/rename-template.sh <new.package.name> <NewAppName> [--dry-run]
#
# Example:
#   scripts/rename-template.sh com.acme.myapp AcmeApp
#
# Requires a clean git working tree; only tracked files are processed.
# Not renamed: the Room database file name "android_arch_sample.db".

set -euo pipefail

OLD_PACKAGE="ru.marwinka.androidarchsample"
OLD_APP_NAME="AndroidArchSample"

if [[ $# -lt 2 || $# -gt 3 ]]; then
    echo "Usage: $0 <new.package.name> <NewAppName> [--dry-run]" >&2
    exit 1
fi

NEW_PACKAGE="$1"
NEW_APP_NAME="$2"
DRY_RUN=false
if [[ "${3:-}" == "--dry-run" ]]; then
    DRY_RUN=true
fi

if [[ ! "$NEW_PACKAGE" =~ ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$ ]]; then
    echo "error: '$NEW_PACKAGE' doesn't look like a Java package (expected e.g. com.acme.myapp)" >&2
    exit 1
fi

if [[ ! "$NEW_APP_NAME" =~ ^[A-Za-z][A-Za-z0-9]*$ ]]; then
    echo "error: '$NEW_APP_NAME' should be a single PascalCase identifier (expected e.g. AcmeApp)" >&2
    exit 1
fi

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

if [[ -n "$(git status --porcelain)" ]]; then
    echo "error: working tree isn't clean. Commit, stash, or 'git add' everything first." >&2
    exit 1
fi

OLD_PACKAGE_PATH="$(echo "$OLD_PACKAGE" | tr '.' '/')"
NEW_PACKAGE_PATH="$(echo "$NEW_PACKAGE" | tr '.' '/')"

# GNU and BSD sed differ in -i syntax.
if sed --version >/dev/null 2>&1; then
    sed_inplace() { sed -i "$@"; }
else
    sed_inplace() { sed -i '' "$@"; }
fi

echo "Renaming package: $OLD_PACKAGE -> $NEW_PACKAGE"
echo "Renaming app name: $OLD_APP_NAME -> $NEW_APP_NAME"
$DRY_RUN && echo "(dry run - no files will be changed)"
echo

replace_in_tracked_files() {
    local search="$1" replace="$2"
    local files
    files="$(git grep -lI --fixed-strings "$search" -- . || true)"
    if [[ -z "$files" ]]; then
        return
    fi
    while IFS= read -r file; do
        echo "  text:  $file"
        if ! $DRY_RUN; then
            sed_inplace "s#${search}#${replace}#g" "$file"
        fi
    done <<<"$files"
}

echo "Files to update:"
replace_in_tracked_files "$OLD_PACKAGE" "$NEW_PACKAGE"
replace_in_tracked_files "$OLD_APP_NAME" "$NEW_APP_NAME"
echo

echo "Kotlin source directories to move:"
while IFS= read -r -d '' dir; do
    target="${dir%$OLD_PACKAGE_PATH}${NEW_PACKAGE_PATH}"
    echo "  dir:   $dir -> $target"
    if ! $DRY_RUN; then
        mkdir -p "$(dirname "$target")"
        git mv "$dir" "$target"
    fi
done < <(find . -type d -regex ".*/kotlin/${OLD_PACKAGE_PATH}" -not -path '*/build/*' -print0)
echo

# Room names schema directories after the database class: <package>.database.AppDatabase.
# Migrations need the old schema files, so the directories move with the package.
echo "Room schema directories to move:"
while IFS= read -r -d '' dir; do
    name="$(basename "$dir")"
    target="$(dirname "$dir")/${NEW_PACKAGE}${name#"$OLD_PACKAGE"}"
    echo "  dir:   $dir -> $target"
    if ! $DRY_RUN; then
        git mv "$dir" "$target"
    fi
done < <(find . -type d -path '*/schemas/*' -name "${OLD_PACKAGE}.*" -not -path '*/build/*' -print0)
echo

if $DRY_RUN; then
    echo "Dry run complete - nothing was changed."
    exit 0
fi

# Remove empty directories left after git mv.
find . -type d -empty -path '*/kotlin/*' -not -path '*/build/*' -delete

echo "Reformatting with ktlint..."
./gradlew ktlintFormat --rerun-tasks --quiet

echo
echo "Done. Review the diff, update README.md's title/description by hand, then commit."
