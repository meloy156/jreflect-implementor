#!/usr/bin/env bash
set -euo pipefail

OUT_DIR="docs"
SEP=';'   # Git Bash на Windows

CP=""
for j in lib/*.jar; do
    [ -e "$j" ] || continue
    if [ -z "$CP" ]; then CP="$j"; else CP="$CP$SEP$j"; fi
done

SRC_FILES=$(find src -name '*.java' | sort)

echo "Запускаю javadoc..."
javadoc \
    -d "$OUT_DIR" \
    -Xdoclint:all \
    -author \
    -version \
    -classpath "$CP" \
    -sourcepath src \
    $SRC_FILES
echo "Готово, смотри $OUT_DIR/index.html"