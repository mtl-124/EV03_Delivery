# Laboratorio 12 - Sistema de Delivery

Sistema de microservicios para administrar clientes, repartidores y entregas. Spring Boot 3.5.16, Spring Cloud 2025.0.3, Eureka, Spring Cloud Gateway, OpenFeign y MySQL con una base de datos por servicio.

## Componentes

| Servicio | Puerto | Base de datos |
| --- | --- | --- |
| eureka-server | 8761 | N/A |
| api-gateway | 8080 | N/A |
| cliente-service | 8081 | bd_cliente |
| repartidor-service | 8082 | bd_repartidor |
| entrega-service | 8083 | bd_entrega |

## Prerrequisitos

- JDK 17 (ruta esperada por los scripts: `C:\Program Files\Java\jdk-17`).
- XAMPP con MySQL iniciado en `127.0.0.1:3306` (usuario `root`, sin contrasena).
- No se necesita instalar Maven: cada proyecto incluye su Maven Wrapper (`mvnw.cmd`).

## Preparacion de la base de datos

Perfil dev: no hace falta nada, las bases se crean solas (`createDatabaseIfNotExist=true`) y las tablas con `ddl-auto: update`.

Perfil prod: cargar el esquema una vez antes de arrancar los servicios.

```powershell
C:\xampp\mysql\bin\mysql.exe -h 127.0.0.1 -P 3306 -u root --skip-password < database\init.sql
```

## Orden de arranque

Eureka, cliente-service, repartidor-service, entrega-service y api-gateway.

Con el script (abre una ventana por servicio y espera a que cada uno responda):

```powershell
.\scripts\start-all.ps1
```

Para detener todo:

```powershell
.\scripts\stop-all.ps1
```

`stop-all.ps1` solo detiene procesos `java` que escuchan en 8761, 8080, 8081, 8082 y 8083. Las ventanas de PowerShell que abrio `start-all.ps1` se cierran a mano.

Manual, desde IntelliJ IDEA: abrir la carpeta `lab12-delivery`, cargar los modulos Maven y ejecutar cada clase `*Application` en el orden indicado. Tambien se puede usar, dentro de cada carpeta de servicio:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\mvnw.cmd spring-boot:run
```

## URLs

| Recurso | URL |
| --- | --- |
| UI de pruebas | http://localhost:8080/ |
| Panel de Eureka | http://localhost:8761 |
| Swagger cliente-service | http://localhost:8081/swagger-ui.html |
| Swagger repartidor-service | http://localhost:8082/swagger-ui.html |
| Swagger entrega-service | http://localhost:8083/swagger-ui.html |

Todas las peticiones de la API se hacen por el gateway: `http://localhost:8080/api/clientes`, `/api/repartidores` y `/api/entregas`.

## Perfiles

El perfil activo se toma de `SPRING_PROFILES_ACTIVE` y por defecto es `dev`.

| Valor | dev | test | prod |
| --- | --- | --- | --- |
| Base de datos | MySQL XAMPP localhost:3306 con `createDatabaseIfNotExist=true` | H2 en memoria | MySQL con `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD` (por defecto valores de XAMPP) |
| `ddl-auto` | update | create-drop | validate (esquema de `database/init.sql`) |
| `show-sql` | true | false | false |
| Logs `com.tecsup` | DEBUG | INFO | INFO (root WARN) |
| Eureka | habilitado, localhost:8761 | deshabilitado | URL desde `EUREKA_URL` |
| Detalle de errores del servidor | `include-message: always`, `include-stacktrace: always` | valores por defecto de Spring Boot | `include-message: never`, `include-stacktrace: never` |

Ejecutar un servicio con perfil prod:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
.\mvnw.cmd spring-boot:run
Remove-Item Env:SPRING_PROFILES_ACTIVE
```

## Pruebas

Pruebas automatizadas (perfil test, H2, sin MySQL ni Eureka), en cada servicio de dominio:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
cd cliente-service
.\mvnw.cmd -q test
```

Prueba de humo contra el sistema en ejecucion:

```powershell
.\scripts\smoke-test.ps1
```

Devuelve codigo de salida 0 si todos los casos pasan. La salida guardada esta en `docs/evidencias/smoke-test-output.txt`.

## Documentacion

- `docs/ARQUITECTURA.md`: diagrama y explicacion de componentes.
- `docs/API.md`: endpoints, ejemplos y codigos esperados.
