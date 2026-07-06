#!/usr/bin/env bash
set -e
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
PCAP_FILE="$SCRIPT_DIR/payment-loadtest.pcap"
K6_SCRIPT="$SCRIPT_DIR/k6-loadtest.js"
INTERFACE="${INTERFACE:-lo}"

if ! command -v k6 >/dev/null 2>&1; then
  echo "ERROR: k6 is not installed. Install k6 and retry."
  exit 1
fi
if ! command -v tcpdump >/dev/null 2>&1; then
  echo "ERROR: tcpdump is not installed. Install tcpdump and retry."
  exit 1
fi

echo "Starting TCP capture on interface $INTERFACE to $PCAP_FILE"
sudo tcpdump -i "$INTERFACE" tcp port 8081 -w "$PCAP_FILE" &
TCPDUMP_PID=$!
trap 'echo "Stopping tcpdump"; sudo kill "$TCPDUMP_PID" >/dev/null 2>&1 || true' EXIT

echo "Running load test against http://localhost:8081/api/v1/payments"
k6 run "$K6_SCRIPT"

echo "Stopping tcpdump"
sudo kill "$TCPDUMP_PID" >/dev/null 2>&1 || true
wait "$TCPDUMP_PID" 2>/dev/null || true

echo "Load test complete. PCAP trace written to $PCAP_FILE"
