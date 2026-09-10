#!/usr/bin/env bash
set -euo pipefail

# Inspect the optimized APK itself: debug tests cannot detect removed constructors.
apk="${1:?Usage: verify-release-widget.sh path/to/optimized.apk}"
apkanalyzer_command="${APKANALYZER:-apkanalyzer}"
merger_code="$("$apkanalyzer_command" dex code --class androidx.work.OverwritingInputMerger "$apk")"
if ! grep -Eq '^\.method public constructor <init>\(\)V$' <<< "$merger_code"; then
    echo 'FAIL: Glance requires the public OverwritingInputMerger() constructor in the optimized APK.' >&2
    exit 1
fi
echo 'PASS: the optimized APK retains the reflective constructor required by Glance.'
