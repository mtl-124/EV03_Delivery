param(
    [string]$JavaHome = 'C:\Program Files\Java\jdk-17',
    [int]$TimeoutSeconds = 240
)

$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot

$servicios = @(
    @{ Nombre = 'eureka-server';       Puerto = 8761 },
    @{ Nombre = 'cliente-service';     Puerto = 8081 },
    @{ Nombre = 'repartidor-service';  Puerto = 8082 },
    @{ Nombre = 'entrega-service';     Puerto = 8083 },
    @{ Nombre = 'api-gateway';         Puerto = 8080 }
)

function Test-Puerto {
    param([int]$Puerto)
    $cliente = New-Object System.Net.Sockets.TcpClient
    try {
        $cliente.Connect('127.0.0.1', $Puerto)
        return $true
    } catch {
        return $false
    } finally {
        $cliente.Close()
    }
}

function Test-Url {
    param([string]$Url)
    try {
        $respuesta = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
        return ($respuesta.StatusCode -eq 200)
    } catch {
        return $false
    }
}

function Wait-Condicion {
    param([scriptblock]$Condicion, [string]$Descripcion)
    $limite = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $limite) {
        if (& $Condicion) {
            Write-Host "  listo: $Descripcion"
            return
        }
        Start-Sleep -Seconds 3
    }
    throw "Tiempo agotado esperando: $Descripcion"
}

if (-not (Test-Path (Join-Path $JavaHome 'bin\java.exe'))) {
    throw "No se encontro java.exe en $JavaHome"
}

if (-not (Test-Puerto 3306)) {
    throw 'MySQL no responde en 127.0.0.1:3306. Inicia MySQL desde el panel de XAMPP.'
}

foreach ($servicio in $servicios) {
    $nombre = $servicio.Nombre
    $puerto = $servicio.Puerto
    if (Test-Puerto $puerto) {
        throw "El puerto $puerto ya esta ocupado. Ejecuta scripts\stop-all.ps1 o libera el puerto antes de continuar."
    }
}

foreach ($servicio in $servicios) {
    $nombre = $servicio.Nombre
    $puerto = $servicio.Puerto
    $carpeta = Join-Path $raiz $nombre
    Write-Host "Arrancando $nombre en el puerto $puerto"
    $comando = "`$host.UI.RawUI.WindowTitle = '$nombre'; `$env:JAVA_HOME = '$JavaHome'; Set-Location '$carpeta'; .\mvnw.cmd spring-boot:run"
    Start-Process -FilePath 'powershell.exe' -ArgumentList @('-NoExit', '-NoProfile', '-Command', $comando) | Out-Null
    Wait-Condicion -Condicion { Test-Puerto $puerto } -Descripcion "$nombre escuchando en $puerto"
}

Write-Host 'Esperando que el gateway enrute hacia los tres servicios'
Wait-Condicion -Condicion {
    (Test-Url 'http://localhost:8080/api/clientes') -and
    (Test-Url 'http://localhost:8080/api/repartidores') -and
    (Test-Url 'http://localhost:8080/api/entregas')
} -Descripcion 'gateway responde 200 en clientes, repartidores y entregas'

Write-Host ''
Write-Host 'Sistema listo'
Write-Host '  UI:     http://localhost:8080/'
Write-Host '  Eureka: http://localhost:8761'
