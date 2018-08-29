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
"$CODEGEN_HOME/GenArrays.kts" > "$ENGINE_HOME/arrays/src/main/kotlin/org/tobi29/arrays/Arrays.kt"
for arrayType in Boolean Byte Short Int Long Float Double Char; do
    "$CODEGEN_HOME/GenArrays.kts" "$arrayType" > "$ENGINE_HOME/arrays/src/main/kotlin/org/tobi29/arrays/${arrayType}Arrays.kt"
done

echo "-- Generating number conversions"
"$CODEGEN_HOME/GenNumberConversions.kts" > "$ENGINE_HOME/stdex/src/main/kotlin/org/tobi29/stdex/NumberConversions.kt"
"$CODEGEN_HOME/GenNumberConversions128.kts" > "$ENGINE_HOME/utils/src/main/kotlin/org/tobi29/utils/NumberConversions128.kt"

echo "-- Generating timezone data"
zoneinfo="/usr/share/zoneinfo"
regions=(
    Africa/ America/ Antarctica/ Arctic/ Asia/ Atlantic/ Australia/ Brazil/
    Canada/ CET Chile/ CST6CDT Cuba EET Egypt Eire EST EST5EDT Etc/ Europe/ GB
    GB-Eire GMT GMT0 GMT-0 GMT+0 Greenwich Hongkong HST Iceland Indian/ Iran
    Israel Jamaica Japan Kwajalein Libya MET Mexico/ MST MST7MDT Navajo NZ
    NZ-CHAT Pacific/ Poland Portugal PRC PST8PDT ROC ROK Singapore Turkey UCT
    Universal US/ UTC WET W-SU Zulu
)

"$CODEGEN_HOME/GenTzData.kts" $(for zone in "${regions[@]}"; do find "$zoneinfo/$zone" -type f -printf "$zone%P "; done) \
    > "$ENGINE_HOME/chrono/src/main/kotlin/org/tobi29/chrono/TzData.kt"

echo "-- Generating embedded iana database"
function ianaDbGen {
    curl "https://www.iana.org/assignments/media-types/$1.csv" \
        | "$CODEGEN_HOME/StripIana.kts" \
        | "$CODEGEN_HOME/GenStringDataJVM.kts" "com.j256.simplemagik" "iana$2" > "$ENGINE_HOME/simplemagik-jvm/src/main/kotlin/com/j256/simplemagik/IanaDb$2JVM.kt"
}

ianaDbGen "application" "Application"
ianaDbGen "audio" "Audio"
ianaDbGen "font" "Font"
ianaDbGen "image" "Image"
ianaDbGen "message" "Message"
ianaDbGen "model" "Model"
ianaDbGen "multipart" "Multipart"
ianaDbGen "text" "Text"
ianaDbGen "video" "Video"

echo "-- Generating embedded magic database"
SIMPLEMAGIK_CLASSPATH="$("$ENGINE_HOME/gradlew" ":simplemagik-jvm:build" ":simplemagik-jvm:printClasspath" -q)"
zcat "$CODEGEN_HOME/magic.gz" \
    | java -cp "$SIMPLEMAGIK_CLASSPATH" "com.j256.simplemagik.MagicCompiler" \
    | tee >("$CODEGEN_HOME/GenBinaryDataJVM.kts" "com.j256.simplemagik" "magic" > "$ENGINE_HOME/simplemagik-jvm/src/main/kotlin/com/j256/simplemagik/MagicDbJVM.kt") \
    | cat > "$ENGINE_HOME/simplemagik-jvm/src/main/resources/com/j256/simplemagik/magic"
