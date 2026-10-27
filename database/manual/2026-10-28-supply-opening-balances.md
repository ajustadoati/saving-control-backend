# Saldos iniciales de suministros y multipagos

Estado: implementacion en `feature/supplies-opening-balances`; carga completa realizada y conciliada solo en la base local el 2026-10-26. Sin commit, merge ni despliegue.

## Reglas acordadas

- Fuente: LUBRICANTES PARA TRABAJAR EL 28 10 26.xlsx, Hoja1, filas 5 a 65.
- Cedula: C; saldo pendiente: M; cuota semanal acordada: K.
- 34 deudores, total individual original 7925.00. Acuerdo actualizado: sumar 90.00 a Socio con ajuste, cedula CEDULA_AJUSTE; su saldo pasa de 1000.00 a 1090.00, conservando cuota 301.00. Total ajustado: 8015.00.
- El ajuste se declara como parametro y se muestra en la simulacion; no se modifica el Excel.
- Primer cobro 2026-10-28; conservar cuotas, sin recalcular a cuatro semanas.
- Cuota final limitada al saldo pendiente.
- Producto ficticio existente: confirmar su ID en cada entorno. En local: 34, SU-0001, Suministro anterior.

## Importacion

Las cedulas de los ejemplos son marcadores: sustituirlos por los identificadores aprobados, sin guardar datos personales en Git. Las notas con datos reales permanecen fuera del repositorio.

El script `scripts/import_supply_opening_balances.py` requiere Python con openpyxl y el cliente mysql.
Crear fuera del repositorio un archivo privado (chmod 600) con seccion [client], user y password.
Nunca guardar contrasenas en el script, comandos, CSV, Git o reportes.

Simulacion (solo consulta):

```sh
python3 scripts/import_supply_opening_balances.py /ruta/fuente.xlsx \
  --defaults-file /ruta/privada/mysql.cnf \
  --product-id 34 --batch-id opening-20261028 \
  --first-payment-date 2026-10-28 \
  --expected-source-total 7925 --balance-adjustment CEDULA_AJUSTE=90 --expected-total 8015
```

Aplicar solo tras revisar la simulacion y guardar un respaldo, agregando
`--apply --expect-server-uuid UUID_VERIFICADO`. Por seguridad solo permite conexion loopback.
La identidad del servidor y su pertenencia al entorno de pruebas deben comprobarse: localhost puede ser un tunel.
`--only-number-id CEDULA` permite una prueba de una sola fila, manteniendo la verificacion del total del archivo completo.

- Crea Supply + LubricantOrder + un item del producto ficticio, dentro de una transaccion.
- No llama al flujo de ventas ni descuenta stock; no inserta movimientos, pagos ni modifica fondos.
- Registra el origen como SALDO INICIAL en supply_name y product_name del item.
- La cuota se conserva; las semanas restantes se estiman con ceil(saldo/cuota), no con las semanas historicas del Excel.
- La fecha del pedido representa el primer cobro a partir del corte, no una nueva entrega fisica.
- El marcador por lote y cedula evita duplicar una importacion repetida. No cambiar el batch-id para el mismo corte.
- Rechaza usuarios faltantes, cedulas duplicadas, diferencias de total y deudas ya existentes sin conciliar.
- Un bloqueo de importacion y comprobaciones dentro de la transaccion protegen la reejecucion concurrente.
- No migra pagos historicos ni modifica pagos por defecto.

## Coexistencia en multipagos

- Las cuotas nuevas se calculan al consultar pedidos, no se crean mas tipos de pago ni default_payment permanentes.
- Concepto visible actualizado: Suministro #PEDIDO (nombre corto, no usa supply_name). SUPPLIES + referenceId=supplyId en el payload.
- Se omiten pedidos pagados, sin saldo o posteriores a la fecha del pago.
- Si existe Suministro por defecto, se conserva sin cambios. Las cuotas nuevas requieren marcar explicitamente la opcion adicional: no duplicar cobros de la misma deuda.
- Un pago antiguo sin referencia no se asigna implicitamente a pedidos financiados. Si tiene una deuda antigua se aplica alli; si no, se conserva como ingreso sin crear deuda.
- Cada abono explicito se valida contra las deudas del socio. Actualiza Supply y el pedido vinculado dentro de la transaccion.
- Abonos invalidos no crean ingresos; el frontend revisa los estados individuales incluso cuando la respuesta HTTP es 200.
- La reversion existente repone los saldos y el estado del pedido.

## Resultados locales (2026-10-26)

- MySQL local Rancher Desktop, UUID verificado antes de escribir; respaldo privado antes de probar.
- Simulacion completa: 34 cedulas encontradas, 32 listas por 7865.00; dos conflictos (no se importo el lote).
- Cedula CEDULA_CONFLICTO_1: Excel 20.00; deuda existente 1.00.
- Cedula CEDULA_CONFLICTO_2: Excel 40.00; cinco deudas existentes por 50.00.
- Prueba de importacion de una fila, lote local-test-20261028, saldo 195.00, cuota 75.00.
- Solo para poder probar la reversion en una fecha no futura, el pedido de prueba uso 2026-10-21; la carga real conserva 2026-10-28.
- Reejecucion del script: cero inserciones, deuda identificada como ya importada.
- API multipagos: 195 -> abono 75 -> 120 -> reversion -> 195.
- Coexistencia: pago antiguo 1 + cuota 75 = total 76; solo 75 descontados de la deuda vinculada. Reversion correcta.
- Cancelacion: 195 -> 0, estado PAID -> reversion -> 195, estado PENDING.
- Sobrepago 196 rechazado como FAILURE, total confirmado 0, sin ingreso ni descuento.
- Producto ficticio: stock 500 antes/despues; movimientos inventario 36 antes/despues; pagos suministros 24 antes/despues tras reversiones.
- El pedido y auditoria creados exclusivamente por las pruebas se retiraron; no se borraron datos previos.
- 5 pruebas unitarias backend y 3 de construccion de cuotas frontend aprobadas. Compilacion backend y produccion frontend correctas.
- Pendiente resolver los dos conflictos, probar interaccion visual y revisar antes de merge/deploy.

### Ajuste posterior autorizado

- El resultado de 7865.00 para las 32 filas sin conflictos corresponde a la simulacion anterior al ajuste.
- Con los 90.00 asignados al socio con ajuste, esas 32 filas suman 7955.00. Las otras dos filas del Excel suman 60.00 y siguen pendientes de conciliacion con las deudas existentes.
- La carga completa sigue sin ejecutarse; no se modifican automaticamente deudas de socios con conflictos.

## Carga local completada (2026-10-26)

- El usuario autorizo reemplazar las deudas antiguas de CEDULA_CONFLICTO_1 y CEDULA_CONFLICTO_2 por los saldos del Excel, conservando sus abonos.
- Se agrego `--reconcile-existing CEDULA` (repetible). Solo permite conciliar deudas antiguas sin pedido vinculado; no habilitarlo para otros socios sin revision y autorizacion.
- Se bloquean y verifican los saldos revisados antes de cerrar. Conciliacion + insercion del lote se confirman dentro de una sola transaccion.
- Se cerraron supply 13 a 18 con saldo cero, sin eliminarlas ni borrar sus 11 abonos. Se conserva supply_amount y se anota el cierre en supply_name.
- Se agregaron seis registros de balance_history con descripcion CONCILIACION NO PAGO y monto negativo del saldo cerrado. No son cobros ni se escriben en pago.
- Fecha efectiva de conciliacion y saldos iniciales: 2026-10-28.
- Lote `opening-20261028`: 34 Supply + 34 LubricantOrder + 34 items historicos del producto 34; saldo total 8015.00, primera cuota 2026-10-28.
- Socio con ajuste (CEDULA_AJUSTE): saldo 1090.00, cuota 301.00. Los socios conciliados: CEDULA_CONFLICTO_1 saldo 20.00/cuota 11.00 y CEDULA_CONFLICTO_2 saldo 40.00/cuota 71.00 (sugerencia limitada a 40.00).
- Se conservaron los pedidos 1 y 3; pedido nuevo del usuario: total 120.00, saldo 90.00, cuota 30.00.
- Se compararon CHECKSUM antes/despues: funds, lubricant_product, lubricant_stock_movement, default_payment, pago, supply_payment, loan y saving sin cambios.
- Respaldo privado antes de la carga: /private/tmp/supplies-local-test/before-full-import-20261026T214508.sql. No contiene cambios de pruebas posteriores y no debe subirse a Git.
- Verificacion API: las 34 filas importadas coinciden en saldo, cuota y abono sugerido con el plan ajustado; suma 8015.00.
- Reejecucion en simulacion: 34 ya importados, cero pendientes, sin conflictos ni duplicados.
- Para reproducir la conciliacion revisada se agregaron al comando de carga `--reconcile-existing CEDULA_CONFLICTO_1 --reconcile-existing CEDULA_CONFLICTO_2`. No trasladar esa decision a produccion sin revisar los datos reales y generar su propio respaldo.
- Pendiente commit/revision, prueba visual del lote y decision de despliegue. Produccion no modificada.

## Abonos manuales antes de la primera cuota (2026-10-26)

- Se reviso el pago local id 31517: SUMINISTROS 100.00, socio CEDULA_AJUSTE, fecha 2026-10-26. Se registro como concepto generico sin supply_payment; la deuda supply 24 / pedido 6 seguia en 1090.00.
- La cuota automatica del lote comienza el 2026-10-28. Seleccionar el concepto generico antes del corte no identifica un pedido.
- Se acorto el concepto a Suministro #PEDIDO y se ofrecen los pedidos pendientes en el selector incluso antes del primer vencimiento, para abonos anticipados explicitos.
- Al seleccionarlo manualmente se asignan supplyId/supplyOrderId desde los datos del pedido; no se busca por supply_name ni se interpreta una cedula o nombre libre.
- Se conserva el monto digitado (por ejemplo 100.00). Cambiar a otro tipo limpia la referencia anterior.
- Nuevas lineas genericas con pedidos pendientes requieren seleccionar el pedido antes de guardar; se conservan los pagos genericos por defecto anteriores como excepcion explicita de compatibilidad.
- Se valida que no existan dos lineas para la misma deuda y las cuotas automaticas no duplican una seleccion manual.
- El nombre interno largo del saldo inicial se conserva para auditoria y proteccion contra duplicados de la importacion; no se usa como texto del recibo.
- El abono ya registrado requiere una vinculacion controlada, sin nuevo ingreso, bajo confirmacion del usuario.
