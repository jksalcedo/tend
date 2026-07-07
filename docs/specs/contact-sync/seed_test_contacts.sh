#!/bin/bash
# Manual, on-demand helper — never invoked by the build, CI, or the app.
# Seeds (or clears) native device contacts on a connected device/emulator:
# the three named prerequisite contacts required by
# 01_manual_contact_import.manual-tests.md ("Alex (Test)", "Sam (Test)",
# "Jordan (Test)"), plus an optional bulk batch of generic "Test Contact N"
# contacts for volume/scrolling testing. Requires ANDROID_HOME to be set,
# or edit ADB below.
#
# Usage:
#   ./seed_test_contacts.sh [count]      # default count: 50; no-op if already seeded
#   ./seed_test_contacts.sh --clear      # removes only contacts this script created
#
# All contacts this script creates are named "Test Contact N" or suffixed
# "(Test)" so --clear can safely target just them, never real contacts on
# the device.
#
# Known limitation: photo data is a BLOB column and can't be set through
# the plain `adb shell content insert` CLI — it only supports scalar bind
# types (b/s/i/l/f/d/n), and `content write` doesn't support per-row file
# access on this provider either (verified: raises FileNotFoundException).
# "Alex (Test)" is seeded without a photo; add one manually via the
# Contacts app if a test specifically needs photo data.

set -euo pipefail

ADB="${ANDROID_HOME:-$HOME/AppData/Local/Android/Sdk}/platform-tools/adb"
NAME_PREFIX="Test Contact"

seeded_ids() {
    "$ADB" shell "content query --uri content://com.android.contacts/raw_contacts --projection _id:display_name --where \"deleted=0\"" \
        | grep -E "display_name=($NAME_PREFIX |Alex \(Test\)|Sam \(Test\)|Jordan \(Test\))" \
        | sed -E 's/.*_id=([0-9]+),.*/\1/'
}

if [[ "${1:-}" == "--clear" ]]; then
    echo "Removing seeded test contacts..."
    count=0
    for id in $(seeded_ids); do
        "$ADB" shell content delete --uri content://com.android.contacts/raw_contacts --where "_id=$id" >/dev/null
        count=$((count + 1))
    done
    echo "Removed $count seeded contact(s)."
    exit 0
fi

existing_count=$(seeded_ids | grep -c . || true)
if [[ "$existing_count" -gt 0 ]]; then
    echo "$existing_count seeded contact(s) already exist — no-op."
    echo "Run './seed_test_contacts.sh --clear' first if you want to reseed."
    exit 0
fi

create_contact() {
    local name="$1" phone="${2:-}" email="${3:-}"
    "$ADB" shell "content insert --uri content://com.android.contacts/raw_contacts --bind account_type:s:null --bind account_name:s:null" >/dev/null

    local raw_id
    raw_id=$("$ADB" shell content query --uri content://com.android.contacts/raw_contacts --projection _id \
        | sed -E 's/.*_id=([0-9]+).*/\1/' | sort -n | tail -n1)

    "$ADB" shell "content insert --uri content://com.android.contacts/data --bind raw_contact_id:i:$raw_id --bind mimetype:s:vnd.android.cursor.item/name --bind data1:s:'$name'" >/dev/null

    if [[ -n "$phone" ]]; then
        "$ADB" shell "content insert --uri content://com.android.contacts/data --bind raw_contact_id:i:$raw_id --bind mimetype:s:vnd.android.cursor.item/phone_v2 --bind data1:s:'$phone' --bind data2:i:2" >/dev/null
    fi
    if [[ -n "$email" ]]; then
        "$ADB" shell "content insert --uri content://com.android.contacts/data --bind raw_contact_id:i:$raw_id --bind mimetype:s:vnd.android.cursor.item/email_v2 --bind data1:s:'$email' --bind data2:i:2" >/dev/null
    fi
}

echo "Seeding prerequisite contacts (Alex, Sam, Jordan)..."
create_contact "Alex (Test)" "555-0100" "alex@example.com"
create_contact "Sam (Test)" "555-0101" ""
create_contact "Jordan (Test)" "" "jordan@example.com"
echo "Prerequisite contacts seeded. Note: photo data can't be scripted (see"
echo "header comment) — add one to 'Alex (Test)' manually if a test needs it."

COUNT="${1:-50}"
echo "Seeding $COUNT bulk test contacts..."

for i in $(seq 1 "$COUNT"); do
    name="$NAME_PREFIX $i"
    phone=$(printf "555-%04d" "$i")
    email=""
    if (( i % 3 == 0 )); then
        email="contact$i@example.com"
    fi
    create_contact "$name" "$phone" "$email"

    if (( i % 10 == 0 )); then
        echo "  ...$i/$COUNT"
    fi
done

echo "Done. Seeded 3 prerequisite contacts and $COUNT bulk contacts named '$NAME_PREFIX 1'..'$NAME_PREFIX $COUNT'."
