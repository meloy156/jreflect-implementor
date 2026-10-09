#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
SCRIPTS_DIR="scripts"
COURSE_REPO="${COURSE_REPO:-../java-advanced-2026}"

# Разделитель classpath
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) SEP=';' ;;
    *)                     SEP=':' ;;
esac

CP=""
add_cp() {
    for j in "$1"/*.jar; do
        [ -e "$j" ] || continue
        if [ -z "$CP" ]; then CP="$j"; else CP="$CP$SEP$j"; fi
    done
}

[ -d "$COURSE_REPO/artifacts" ] && add_cp "$COURSE_REPO/artifacts"
[ -d "$COURSE_REPO/lib" ]       && add_cp "$COURSE_REPO/lib"
[ -d "lib" ]                    && add_cp "lib"

if [ -z "$CP" ]; then
    echo "Не нашёл jar-ников ни в $COURSE_REPO, ни в ./lib" >&2
    exit 1
fi

BUILD_DIR="build"
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

javac \
    -d "$BUILD_DIR" \
    -classpath "$CP" \
    $(find src -name '*.java')

# MANIFEST на месте (создан вручную), но если нет — создадим
if [ ! -f "$SCRIPTS_DIR/MANIFEST.MF" ]; then
    printf 'Manifest-Version: 1.0\nMain-Class: Implementor\n\n' > "$SCRIPTS_DIR/MANIFEST.MF"
fi

jar cfm "$SCRIPTS_DIR/Implementor.jar" "$SCRIPTS_DIR/MANIFEST.MF" -C "$BUILD_DIR" .

echo "Собрано: $SCRIPTS_DIR/Implementor.jar"

rm -rf "$BUILD_DIR"