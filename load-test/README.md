# Load Test for SwiftPaymentApp

This folder contains a k6 script and shell/PowerShell helpers to execute a load test at 250 transactions per second for a total of 1,000,000 requests.

## Files

- `k6-loadtest.js` - k6 scenario that posts to `/api/v1/payments` at 250 TPS.
- `run-loadtest.sh` - Linux/macOS helper that captures traffic with `tcpdump` and runs the k6 test.
- `run-loadtest.ps1` - PowerShell helper for Windows/WSL environments.

## Requirements

- `k6`
- `tcpdump` (or equivalent packet capture tool)
- Service running on `http://localhost:8081`

## Run the test

1. Start the `SwiftPayApp` service locally.
2. From this folder:

```bash
cd load-test
./run-loadtest.sh
```

or on PowerShell:

```powershell
cd load-test
.\run-loadtest.ps1
```

## Result

- The load test will exercise `POST http://localhost:8081/api/v1/payments`
- The pcap trace is saved to `payment-loadtest.pcap`

## Notes

- If your service is exposed on a different base URL, set `BASE_URL` before running k6.
- The script uses a constant arrival rate of 250 requests per second for 4000 seconds, which approximates 1,000,000 total transactions.
