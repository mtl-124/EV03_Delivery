param(
    [string]$Gateway = 'http://localhost:8080'
)

$ErrorActionPreference = 'Stop'
$script:ok = 0
$script:fallos = 0

function Invoke-Api {
    param([string]$Metodo, [string]$Ruta, [string]$Cuerpo)
    $parametros = @{ Uri = "$Gateway$Ruta"; Method = $Metodo; UseBasicParsing = $true; TimeoutSec = 20 }
    if ($Cuerpo) {
        $parametros.Body = $Cuerpo
        $parametros.ContentType = 'application/json'
    }
    try {
        $respuesta = Invoke-WebRequest @parametros
        return @{ Estado = [int]$respuesta.StatusCode; Texto = $respuesta.Content }
    } catch {
        $respuesta = $_.Exception.Response
        if (-not $respuesta) {
            return @{ Estado = 0; Texto = $_.Exception.Message }
        }
        $texto = ''
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            $texto = $_.ErrorDetails.Message
        } else {
            try {
                $lector = New-Object System.IO.StreamReader($respuesta.GetResponseStream())
                $texto = $lector.ReadToEnd()
            } catch {
                $texto = ''
            }
        }
        return @{ Estado = [int]$respuesta.StatusCode; Texto = $texto }
    }
}

function Test-Caso {
    param([string]$Nombre, [string]$Metodo, [string]$Ruta, [string]$Cuerpo, [int]$Esperado)
    $resultado = Invoke-Api -Metodo $Metodo -Ruta $Ruta -Cuerpo $Cuerpo
    if ($resultado.Estado -eq $Esperado) {
        $script:ok++
        Write-Host ("OK     {0} -> {1}" -f $Nombre, $resultado.Estado)
    } else {
        $script:fallos++
        Write-Host ("FALLO  {0} -> esperado {1}, recibido {2}" -f $Nombre, $Esperado, $resultado.Estado)
        Write-Host ("       {0}" -f $resultado.Texto)
    }
    if ($resultado.Texto) {
        try {
            return ($resultado.Texto | ConvertFrom-Json)
        } catch {
            return $null
        }
    }
    return $null
}

$sufijo = Get-Date -Format 'yyyyMMddHHmmss'
Write-Host "Smoke test contra $Gateway ($sufijo)"
Write-Host ''

$cuerpoCliente = '{"nombre":"Prueba","apellido":"Smoke","email":"smoke' + $sufijo + '@correo.com","telefono":"987654321","direccion":"Av. Lima 123"}'
$cliente = Test-Caso 'Registrar cliente' 'POST' '/api/clientes' $cuerpoCliente 201
Test-Caso 'Cliente con email duplicado' 'POST' '/api/clientes' $cuerpoCliente 409 | Out-Null
Test-Caso 'Cliente con datos invalidos' 'POST' '/api/clientes' '{"nombre":"","apellido":"X","email":"no","telefono":"1","direccion":""}' 400 | Out-Null
Test-Caso 'Buscar cliente por id' 'GET' ("/api/clientes/" + $cliente.id) $null 200 | Out-Null

$repartidor = Test-Caso 'Registrar repartidor' 'POST' '/api/repartidores' '{"nombre":"Carlos","apellido":"Smoke","telefono":"999111222","vehiculo":"MOTO"}' 201
Test-Caso 'Repartidor con vehiculo invalido' 'POST' '/api/repartidores' '{"nombre":"Carlos","apellido":"Smoke","telefono":"999111222","vehiculo":"AVION"}' 400 | Out-Null
$inactivo = Test-Caso 'Registrar repartidor inactivo' 'POST' '/api/repartidores' '{"nombre":"Inactivo","apellido":"Smoke","telefono":"999111222","vehiculo":"AUTO","activo":false}' 201

$cuerpoEntrega = '{"idCliente":' + $cliente.id + ',"idRepartidor":' + $repartidor.id + ',"direccion":"Av. Arequipa 456","costo":15.50}'
$entrega = Test-Caso 'Registrar entrega' 'POST' '/api/entregas' $cuerpoEntrega 201
if ($entrega.estado -eq 'PENDIENTE') {
    $script:ok++
    Write-Host 'OK     Estado inicial PENDIENTE'
} else {
    $script:fallos++
    Write-Host ("FALLO  Estado inicial: {0}" -f $entrega.estado)
}

Test-Caso 'Buscar entrega por id' 'GET' ("/api/entregas/" + $entrega.id) $null 200 | Out-Null
Test-Caso 'Listar entregas' 'GET' '/api/entregas' $null 200 | Out-Null
Test-Caso 'Listar entregas por estado' 'GET' '/api/entregas?estado=PENDIENTE' $null 200 | Out-Null
Test-Caso 'Listar con estado invalido' 'GET' '/api/entregas?estado=XX' $null 400 | Out-Null

$rutaEstado = '/api/entregas/' + $entrega.id + '/estado'
Test-Caso 'PENDIENTE a ENTREGADA (invalida)' 'PATCH' $rutaEstado '{"estado":"ENTREGADA"}' 409 | Out-Null
Test-Caso 'PENDIENTE a EN_CAMINO' 'PATCH' $rutaEstado '{"estado":"EN_CAMINO"}' 200 | Out-Null
Test-Caso 'EN_CAMINO a ENTREGADA' 'PATCH' $rutaEstado '{"estado":"ENTREGADA"}' 200 | Out-Null
Test-Caso 'ENTREGADA a PENDIENTE (invalida)' 'PATCH' $rutaEstado '{"estado":"PENDIENTE"}' 409 | Out-Null
Test-Caso 'Estado con valor invalido' 'PATCH' $rutaEstado '{"estado":"VOLANDO"}' 400 | Out-Null

$base = '"idCliente":' + $cliente.id + ',"idRepartidor":' + $repartidor.id
Test-Caso 'Entrega con costo negativo' 'POST' '/api/entregas' ('{' + $base + ',"direccion":"Av. Lima 123","costo":-5}') 400 | Out-Null
Test-Caso 'Entrega con direccion vacia' 'POST' '/api/entregas' ('{' + $base + ',"direccion":"","costo":10}') 400 | Out-Null
Test-Caso 'Entrega inexistente' 'GET' '/api/entregas/999999' $null 404 | Out-Null
Test-Caso 'Entrega con cliente inexistente' 'POST' '/api/entregas' ('{"idCliente":999999,"idRepartidor":' + $repartidor.id + ',"direccion":"Av. Lima 123","costo":10}') 404 | Out-Null
Test-Caso 'Entrega con repartidor inexistente' 'POST' '/api/entregas' ('{"idCliente":' + $cliente.id + ',"idRepartidor":999999,"direccion":"Av. Lima 123","costo":10}') 404 | Out-Null
Test-Caso 'Entrega con repartidor inactivo' 'POST' '/api/entregas' ('{"idCliente":' + $cliente.id + ',"idRepartidor":' + $inactivo.id + ',"direccion":"Av. Lima 123","costo":10}') 409 | Out-Null

Test-Caso 'Desactivar repartidor' 'PATCH' ("/api/repartidores/" + $repartidor.id + "/activo") '{"activo":false}' 200 | Out-Null
Test-Caso 'Eliminar repartidor de prueba' 'DELETE' ("/api/repartidores/" + $repartidor.id) $null 204 | Out-Null
Test-Caso 'Eliminar repartidor inactivo de prueba' 'DELETE' ("/api/repartidores/" + $inactivo.id) $null 204 | Out-Null
Test-Caso 'Eliminar cliente de prueba' 'DELETE' ("/api/clientes/" + $cliente.id) $null 204 | Out-Null
Test-Caso 'Cliente eliminado ya no existe' 'GET' ("/api/clientes/" + $cliente.id) $null 404 | Out-Null

Write-Host ''
Write-Host ("Resultado: {0} OK, {1} FALLO" -f $script:ok, $script:fallos)
if ($script:fallos -gt 0) {
    exit 1
}
exit 0
