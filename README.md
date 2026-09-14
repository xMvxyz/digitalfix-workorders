# DigitalFix WorkOrders — Caso 6

Microservicio principal: órdenes de trabajo. Spring Boot 4 + JPA + H2 file.

## Máquina de estados

`CREADA → ASIGNADA → EN_DESPLAZAMIENTO → EN_EJECUCIÓN → CERRADA / CANCELADA`

- `CANCELADA` desde cualquier no-terminal. `CERRADA/CANCELADA` terminales.
- `ASIGNADA` exige `tecnico`.
- Regla clave: no se puede pasar a `EN_EJECUCION` sin `ASIGNAR` (secuencialidad estricta en `WorkOrderService.changeStatus` → `409` si se viola).
- `DELETE` solo Admin/Supervisor, nunca `CERRADA`.

## Endpoints

Base `/api/workorders` (puerto `8082`):

- `GET /api/workorders` + headers `X-User-Email/X-User-Role` (Cliente solo ve las suyas)
- `GET /api/workorders/{id}`
- `POST /api/workorders` `{"clienteEmail","servicio","descripcion?","repuestoId?"}` → `201 CREADA`
- `PATCH /api/workorders/{id}/status` `{"estado","tecnico?"}` → `200/400/403/404/409`
- `DELETE /api/workorders/{id}` → `204/403/409`

## Auth delegada

El BFF inyecta `X-User-Email/X-User-Role`. `isPrivileged = Admin|Supervisor`.

## Persistencia

H2 file `/tmp/workordersdb` + volumen Docker (sobrevive a restart, se pierde con `down -v`). Seed: `cliente@digitalfix.cl` en `CREADA`.

## Run

```
./mvnw -DskipTests package
docker compose up -d --build
```

## Colección Postman EP1

https://benjamin-1874968.postman.co/workspace/Benjamin's-Workspace~edccb9a7-d9b0-4dac-8429-645e20bcb7f8/collection/45505473-ff1f78c6-e53c-4f7b-b861-e5387a510074?action=share&creator=45505473
