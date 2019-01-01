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
"$CODEGEN_HOME/GenArrays.kts" > "$ENGINE_HOME/arrays/src/commonMain/kotlin/Arrays.kt"
for arrayType in Boolean Byte Short Int Long Float Double Char; do
    "$CODEGEN_HOME/GenArrays.kts" "$arrayType" > "$ENGINE_HOME/arrays/src/commonMain/kotlin/${arrayType}Arrays.kt"
done

echo "-- Generating number conversions"
"$CODEGEN_HOME/GenNumberConversions.kts" > "$ENGINE_HOME/stdex/src/commonMain/kotlin/NumberConversions.kt"
"$CODEGEN_HOME/GenNumberConversions128.kts" > "$ENGINE_HOME/utils/src/commonMain/kotlin/NumberConversions128.kt"

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
    > "$ENGINE_HOME/chrono/src/commonMain/kotlin/TzData.kt"

echo "-- Generating embedded iana database"
function ianaDbGen {
    curl "https://www.iana.org/assignments/media-types/$1.csv" \
        | "$CODEGEN_HOME/StripIana.kts" \
        | "$CODEGEN_HOME/GenStringData.kts" "com.j256.simplemagik" "iana$2" > "$ENGINE_HOME/simplemagik/src/jvmMain/kotlin/IanaDb$2.kt"
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
SIMPLEMAGIK_CLASSPATH="$("$ENGINE_HOME/gradlew" ":simplemagik:jvmJar" ":simplemagik:jvmPrintClasspath" -q)"
zcat "$CODEGEN_HOME/magic.gz" \
    | java -cp "$SIMPLEMAGIK_CLASSPATH" "com.j256.simplemagik.MagicCompiler" \
    | tee >("$CODEGEN_HOME/GenBinaryData.kts" "com.j256.simplemagik" "magic" > "$ENGINE_HOME/simplemagik/src/jvmMain/kotlin/MagicDb.kt") \
    | cat > "$ENGINE_HOME/simplemagik/src/jvmMain/resources/com/j256/simplemagik/magic"
