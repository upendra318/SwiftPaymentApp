Param(
    [string]$BaseUrl = 'http://localhost:8081',
    [string]$Interface = 'Ethernet',
    [string]$PcapFile = "$PSScriptRoot\payment-loadtest.pcap"
)

$K6Script = Join-Path $PSScriptRoot 'k6-loadtest.js'

if (-not (Get-Command k6 -ErrorAction SilentlyContinue)) {
    throw 'k6 is not installed. Install k6 and rerun this script.'
}

$tcpdump = Get-Command tcpdump -ErrorAction SilentlyContinue
if (-not $tcpdump) {
    throw 'tcpdump is not installed or not available on PATH. Install tcpdump or use WSL.'
}

Write-Host "Starting TCP capture on interface $Interface to $PcapFile"
$tcpdumpProcess = Start-Process -FilePath $tcpdump.Source -ArgumentList '-i', $Interface, '-w', $PcapFile, 'tcp port 8081' -NoNewWindow -PassThru

try {
    Write-Host "Running load test against $BaseUrl/api/v1/payments"
    k6 run $K6Script
}
finally {
    Write-Host 'Stopping tcpdump'
    Stop-Process -Id $tcpdumpProcess.Id -ErrorAction SilentlyContinue
}

Write-Host "Load test complete. PCAP trace saved to $PcapFile"
