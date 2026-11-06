$ErrorActionPreference = 'Stop'

$puertos = @(8080, 8081, 8082, 8083, 8761)
$detenidos = 0

foreach ($puerto in $puertos) {
    $conexiones = Get-NetTCPConnection -State Listen -LocalPort $puerto -ErrorAction SilentlyContinue
    if (-not $conexiones) {
        Write-Host "Puerto ${puerto}: libre"
        continue
    }
    foreach ($idProceso in ($conexiones | Select-Object -ExpandProperty OwningProcess -Unique)) {
        $proceso = Get-Process -Id $idProceso -ErrorAction SilentlyContinue
        if (-not $proceso) {
            continue
        }
        if ($proceso.ProcessName -ne 'java') {
            Write-Host "Puerto ${puerto}: lo usa '$($proceso.ProcessName)' (PID $idProceso), no es java. No se toca."
            continue
        }
        Write-Host "Puerto ${puerto}: deteniendo java (PID $idProceso)"
        Stop-Process -Id $idProceso -Force
        $detenidos++
    }
}

Write-Host "Procesos java detenidos: $detenidos"
