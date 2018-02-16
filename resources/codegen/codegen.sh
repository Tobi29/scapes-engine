#!/bin/bash

findHome() {
    local source="${BASH_SOURCE[0]}"
    while [ -h "$source" ] ; do
        local linked="$(readlink "$source")"
        local dir="$(cd -P $(dirname "$source") && cd -P $(dirname "$linked") && pwd)"
        source="$dir/$(basename "$linked")"
    done
    (cd -P "$(dirname "$source")" && pwd)
}

CODEGEN_HOME="$(findHome)"
ENGINE_HOME="$(realpath "$CODEGEN_HOME/../..")"

echo "-- Generating arrays"
"$CODEGEN_HOME/GenArrays.kts" > "$ENGINE_HOME/Arrays/src/main/kotlin/org/tobi29/arrays/Arrays.kt"
for arrayType in Boolean Byte Short Int Long Float Double Char; do
    "$CODEGEN_HOME/GenArrays.kts" "$arrayType" > "$ENGINE_HOME/Arrays/src/main/kotlin/org/tobi29/arrays/${arrayType}Arrays.kt"
done

echo "-- Generating number conversions"
"$CODEGEN_HOME/GenNumberConversions.kts" > "$ENGINE_HOME/STDEx/src/main/kotlin/org/tobi29/stdex/NumberConversions.kt"
"$CODEGEN_HOME/GenNumberConversions128.kts" > "$ENGINE_HOME/Utils/src/main/kotlin/org/tobi29/utils/NumberConversions128.kt"

echo "-- Generating timezone data"
zcat "$CODEGEN_HOME/tzdump.gz" \
    | "$CODEGEN_HOME/GenTzData.kts" > "$ENGINE_HOME/ChronoUtils/src/main/kotlin/org/tobi29/chrono/TzData.kt"
