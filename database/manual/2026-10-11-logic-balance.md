# Saldo conjunto de Logica

Primera fase: saldo independiente con abonos de fecha y monto. Saldo inicial: 19524.00.
No modifica fondos, ahorros, prestamos, pagos habituales ni distribucion de intereses.

## Instalacion

1. Ejecutar `2026-10-11-logic-balance.sql` en la base de datos del entorno que se va a desplegar.
2. Desplegar el backend y luego el frontend.
3. Verificar con un administrador que la tarjeta muestra 19524.00 antes de registrar abonos.

El script crea dos tablas nuevas y solo inserta el saldo inicial si no existe.
Volver a ejecutarlo no restablece un saldo que ya tenga abonos.
No se ejecuta automaticamente: el proyecto usa `ddl-auto: none`.

## API

- `GET /api/logic-balance`: saldo inicial y pendiente.
- `POST /api/logic-balance/payments`: `date` (YYYY-MM-DD), `amount`, `requestId` (UUID).
- Ambos endpoints requieren rol ADMIN.
- El abono debe ser positivo, tener hasta dos decimales y no superar el saldo; la fecha no puede ser futura.
- La operacion guarda saldo y movimiento en una sola transaccion con bloqueo del saldo compartido.
- Reintentar la misma solicitud con el mismo UUID y datos no vuelve a descontar el monto.

Los movimientos guardan administrador, fecha de registro y saldo resultante para la segunda fase.
Todavia no hay endpoints ni interfaz para historial o reversion.

## Verificacion realizada

- Cinco pruebas unitarias del servicio y validacion de solicitudes.
- Compilacion de produccion de Angular y comprobacion de sus plantillas.
- No se ha ejecutado el SQL ni registrado abonos en una base de datos.
