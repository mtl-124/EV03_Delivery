# API

Todas las rutas se consumen por el gateway: `http://localhost:8080`. Cuerpos y respuestas en JSON.

## Formato de error

```json
{
  "timestamp": "2026-11-06T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Datos de entrada inválidos",
  "path": "/api/entregas",
  "errors": [
    { "campo": "direccion", "mensaje": "no debe estar vacío" },
    { "campo": "costo", "mensaje": "debe ser mayor o igual que 0" }
  ]
}
```

`errors` solo aparece en validaciones. JSON malformado, valores de enum invalidos e ids no numericos responden 400. Un error no previsto responde 500 con mensaje generico.

## Clientes

### POST /api/clientes

Cuerpo:

```json
{
  "nombre": "Maria",
  "apellido": "Lopez",
  "email": "maria.lopez@correo.com",
  "telefono": "987654321",
  "direccion": "Av. Lima 123"
}
```

Respuesta 201:

```json
{
  "id": 1,
  "nombre": "Maria",
  "apellido": "Lopez",
  "email": "maria.lopez@correo.com",
  "telefono": "987654321",
  "direccion": "Av. Lima 123"
}
```

Errores: 400 (campos vacios, email invalido, telefono fuera de 7 a 15 digitos), 409 (email duplicado).

### GET /api/clientes

Respuesta 200: lista de clientes.

### GET /api/clientes/{id}

Respuesta 200: un cliente. Error: 404, 400 si el id no es numerico.

### PUT /api/clientes/{id}

Cuerpo igual al de POST. Respuesta 200: el cliente actualizado. Errores: 400, 404, 409.

### DELETE /api/clientes/{id}

Respuesta 204 sin cuerpo. Error: 404.

## Repartidores

### POST /api/repartidores

Cuerpo (`activo` es opcional y por defecto es `true`; `vehiculo` admite MOTO, BICICLETA o AUTO):

```json
{
  "nombre": "Carlos",
  "apellido": "Rojas",
  "telefono": "999111222",
  "vehiculo": "MOTO",
  "activo": true
}
```

Respuesta 201:

```json
{
  "id": 1,
  "nombre": "Carlos",
  "apellido": "Rojas",
  "telefono": "999111222",
  "vehiculo": "MOTO",
  "activo": true
}
```

Errores: 400.

### GET /api/repartidores

Respuesta 200: lista de repartidores.

### GET /api/repartidores/{id}

Respuesta 200: un repartidor. Error: 404.

### PUT /api/repartidores/{id}

Cuerpo igual al de POST. Respuesta 200. Errores: 400, 404.

### PATCH /api/repartidores/{id}/activo

Cuerpo:

```json
{ "activo": false }
```

Respuesta 200: el repartidor actualizado. Errores: 400, 404.

### DELETE /api/repartidores/{id}

Respuesta 204 sin cuerpo. Error: 404.

## Entregas

### POST /api/entregas

Cuerpo (la fecha y el estado no se envian):

```json
{
  "idCliente": 1,
  "idRepartidor": 1,
  "direccion": "Av. Arequipa 456",
  "costo": 15.50
}
```

Respuesta 201:

```json
{
  "id": 1,
  "idCliente": 1,
  "idRepartidor": 1,
  "direccion": "Av. Arequipa 456",
  "fecha": "2026-11-06T10:15:30.123456",
  "costo": 15.50,
  "estado": "PENDIENTE"
}
```

Errores:

| Codigo | Causa |
| --- | --- |
| 400 | Direccion vacia, costo negativo, ids nulos o JSON malformado |
| 404 | Cliente o repartidor inexistente |
| 409 | Repartidor inactivo |
| 503 | cliente-service o repartidor-service no disponible |

### GET /api/entregas

Admite el parametro opcional `estado` (`PENDIENTE`, `EN_CAMINO`, `ENTREGADA`), por ejemplo `/api/entregas?estado=EN_CAMINO`. Respuesta 200: lista de entregas. Error: 400 si el estado no es valido.

### GET /api/entregas/{id}

Respuesta 200: una entrega. Error: 404.

### PATCH /api/entregas/{id}/estado

Cuerpo:

```json
{ "estado": "EN_CAMINO" }
```

Respuesta 200: la entrega con el nuevo estado. Transiciones permitidas: PENDIENTE a EN_CAMINO y EN_CAMINO a ENTREGADA. Errores: 400 (estado nulo o valor invalido), 404, 409 (transicion no permitida).

Ejemplo de 409:

```json
{
  "timestamp": "2026-11-06T10:20:00",
  "status": 409,
  "error": "Conflict",
  "message": "Transición no permitida de ENTREGADA a PENDIENTE",
  "path": "/api/entregas/1/estado"
}
```
