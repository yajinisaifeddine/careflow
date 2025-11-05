#!/bin/bash

# Usage: ./stress_test.sh http://localhost:8080/api/endpoint
# Example: ./stress_test.sh http://localhost:8080/health

URL=$1
DURATION=5
CONCURRENCY=20 # number of parallel requests

if [ -z "$URL" ]; then
  echo "Usage: $0 <url>"
  exit 1
fi

echo "Stress testing $URL for $DURATION seconds with $CONCURRENCY concurrent requests..."

end=$((SECONDS + DURATION))
declare -A codes
total=0

# Create a temporary file for parallel writes
TMP_FILE=$(mktemp)

while [ $SECONDS -lt $end ]; do
  for ((i = 0; i < CONCURRENCY; i++)); do
    (
      code=$(curl -s -o /dev/null -w "%{http_code}" "$URL")
      echo "$code" >>"$TMP_FILE"
    ) &
    ((total++))
  done
  sleep 0.1
done

wait

# Count response codes
while read -r code; do
  ((codes[$code]++))
done <"$TMP_FILE"

rm -f "$TMP_FILE"

echo
echo "=== Stress Test Results ==="
echo "Total requests sent: $total"
echo "-----------------------------"
for code in "${!codes[@]}"; do
  printf "HTTP %s: %d responses\n" "$code" "${codes[$code]}"
done
echo "-----------------------------"
echo "Test complete!"
