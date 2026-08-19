# Flower Details Backend

Backend para el catalogo, carrito y gestion de pedidos de Flower Details.

## Stack

- Java 21 y Spring Boot 4
- PostgreSQL y Flyway
- Redis para rate limiting distribuido
- JWT en cookie `HttpOnly` con CSRF
- DDD por feature, JPA/Hibernate y soft delete
- OpenAPI/Swagger UI y Actuator

## Perfiles

| Perfil | Uso | Configuracion |
| --- | --- | --- |
| `dev` | Desarrollo local | PostgreSQL local y bootstrap de admin local. Debe activarse explicitamente. |
| `testing` | QA o staging | Variables de entorno obligatorias, Redis requerido y seguridad equivalente a produccion. |
| `prod` | Produccion | Variables de entorno obligatorias, Redis requerido, Swagger deshabilitado y logs JSON. |
| `test` | Suite automatizada | Solo JUnit: H2 y Testcontainers. No es un ambiente desplegable. |

## Desarrollo local

Requisitos: JDK 21, Maven 3.9+ y PostgreSQL local en `localhost:5432` con base `flower_details` y credenciales `postgres/postgres`.

```powershell
$env:SPRING_PROFILES_ACTIVE='dev'
mvn spring-boot:run
```

La aplicacion queda en `http://localhost:8080`.

## Documentacion API

En desarrollo y `testing`:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

Swagger UI se deshabilita automaticamente con el perfil `prod`. Para probar endpoints protegidos desde Swagger se puede usar el esquema `bearerAuth` con un JWT enviado por encabezado `Authorization: Bearer <token>`.

## Ejecucion con Docker Compose

Docker Compose levanta la API con perfil `prod`, PostgreSQL 16 y Redis 7 con autenticacion. Configura un archivo local `.env` con las mismas claves de [`.env.example`](.env.example); `.env` no se versiona.

```powershell
docker compose up --build
```

La API se expone en `http://localhost:8080` por defecto. Los datos de PostgreSQL y Redis usan volumenes nombrados; las imagenes de producto se conservan en `uploads/products`.

Para detener los servicios:

```powershell
docker compose down
```

Para eliminar tambien los datos persistentes:

```powershell
docker compose down -v
```

## Variables de `testing` y `prod`

Estas variables no tienen valores por defecto en los perfiles seguros:

| Variable | Descripcion |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexion PostgreSQL. |
| `JWT_SECRET` | Secreto de al menos 32 bytes. |
| `CORS_ALLOWED_ORIGINS` | Origenes HTTPS exactos del frontend, separados por coma. |
| `TRUSTED_PROXY_ADDRESSES` | IPs o rangos CIDR de los proxies que agregan `X-Forwarded-For`; vacio si la API no esta detras de un proxy. |
| `PRODUCT_IMAGES_ROOT_PATH` | Directorio persistente para imagenes. |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` | Conexion Redis para rate limiting. |

Opcionales: `SERVER_PORT`, `JWT_ISSUER`, `JWT_AUDIENCE`, `AUTH_COOKIE_SAME_SITE`, `REDIS_SSL_ENABLED`, limites Hikari y limites de carga de imagenes.

El arranque fuera de `dev` y `test` falla si Redis no responde, si CORS no usa HTTPS, si se activa el bootstrap de administrador o si la cookie no es `Secure`.

Las contrasenas nuevas de clientes y operadores requieren al menos 10 caracteres, una mayuscula, una minuscula y un numero. BCrypt se configura con costo 12.

## Pruebas

```powershell
mvn test
```

La suite incluye pruebas HTTP con MockMvc, PostgreSQL y Redis mediante Testcontainers, CSRF, autorizacion por rol, carga de imagenes, carrito, pedidos, auditoria y concurrencia.

## Endpoints principales

| Recurso | Endpoints |
| --- | --- |
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/csrf` |
| Catalogo | `GET /api/categories`, `GET /api/products`, `GET /api/products/{id}` |
| Carrito | `GET /api/cart`, `POST /api/cart/items`, `PUT /api/cart/items/{id}`, `DELETE /api/cart/items/{id}` |
| Pedidos | `POST /api/orders`, `GET /api/orders/my`, `GET /api/orders/{id}`, `GET /api/orders/{id}/audit` |
| Gestion de pedidos | `GET /api/orders`, `PATCH /api/orders/{id}/assign`, `PATCH /api/orders/{id}/status`, `PATCH /api/orders/{id}/cancel` |

Los permisos y esquemas completos se generan en OpenAPI.
