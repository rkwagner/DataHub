$services = @(
    @{ Name = "MinIO API"; Port = 9000 },
    @{ Name = "MinIO Console"; Port = 9001 },
    @{ Name = "Postgres"; Port = 5432 },
    @{ Name = "Hive Metastore"; Port = 9083 },
    @{ Name = "Trino"; Port = 8080 }
)

Write-Host "Verifying Local Environment Services..." -ForegroundColor Cyan

foreach ($service in $services) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $connect = $tcp.BeginConnect("localhost", $service.Port, $null, $null)
        $wait = $connect.AsyncWaitHandle.WaitOne(1000, $false)
        
        if ($tcp.Connected) {
            Write-Host "[$([char]0x2713)] $($service.Name) is listening on port $($service.Port)" -ForegroundColor Green
            $tcp.Close()
        } else {
            Write-Host "[X] $($service.Name) is NOT listening on port $($service.Port)" -ForegroundColor Red
        }
    } catch {
        Write-Host "[X] $($service.Name) is NOT listening on port $($service.Port)" -ForegroundColor Red
    }
}
