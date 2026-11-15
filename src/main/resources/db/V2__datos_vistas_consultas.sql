-- ============================================================
-- V2: DATOS DE PRUEBA, CONSULTAS, VISTAS Y TRANSACCIÓN
-- Sistema de Créditos y Cobranzas - PostgreSQL
--
-- Contenido:
--   1. Datos de prueba (15 clientes, 15 cobradores, 18 créditos,
--      36 cuotas, 20 gestiones, 17 pagos)
--   2. JOIN 1: INNER JOIN (pagos por cobrador/crédito)
--   3. JOIN 2: LEFT JOIN  (cartera completa por cobrador)
--   4. Subconsulta 1: Escalar en WHERE (créditos sobre promedio)
--   5. Subconsulta 2: Correlacionada EXISTS (clientes en mora)
--   6. Vista regular: vista_cuotas_vencidas
--   7. Vista materializada: vista_resumen_creditos
--   8. Transacción: refinanciamiento con validación de interés
-- ============================================================

BEGIN;

-- ============================================================
-- 1. CLIENTES (15 registros)
-- ============================================================
INSERT INTO clientes (id, nombre_cliente, telefono_cliente, fecha_creacion) VALUES
(1,  'Carlos Andrés Rodríguez', '3001234501', '2025-01-05 09:00:00'),
(2,  'María Fernanda Gómez',    '3001234502', '2025-01-08 10:15:00'),
(3,  'Luis Eduardo Martínez',   '3001234503', '2025-01-12 11:30:00'),
(4,  'Diana Carolina Pérez',    '3001234504', '2025-01-15 14:00:00'),
(5,  'Andrés Felipe Torres',    '3001234505', '2025-01-20 08:45:00'),
(6,  'Laura Camila Sánchez',    '3001234506', '2025-02-02 09:30:00'),
(7,  'Jorge Enrique Ramírez',   '3001234507', '2025-02-10 13:20:00'),
(8,  'Paola Andrea Castillo',   '3001234508', '2025-02-18 15:10:00'),
(9,  'Miguel Ángel Herrera',    '3001234509', '2025-03-01 10:00:00'),
(10, 'Natalia Isabel Moreno',   '3001234510', '2025-03-07 11:45:00'),
(11, 'Sebastián David Vargas',  '3001234511', '2025-03-15 08:30:00'),
(12, 'Valentina Rojas Díaz',    '3001234512', '2025-04-01 14:15:00'),
(13, 'Óscar Mauricio Castro',   '3001234513', '2025-04-12 09:20:00'),
(14, 'Andrea Milena Jiménez',   '3001234514', '2025-05-03 16:00:00'),
(15, 'Ricardo José Méndez',     '3001234515', '2025-05-15 10:40:00');

-- ============================================================
-- 2. COBRADORES (15 registros)
-- ============================================================
INSERT INTO cobradores (id, nombre_cobrador, telefono_cobrador, activo, fecha_creacion) VALUES
(1,  'Fernando Alberto Ruiz',  '3105551001', TRUE,  '2025-01-02 08:00:00'),
(2,  'Claudia Patricia León',  '3105551002', TRUE,  '2025-01-03 08:30:00'),
(3,  'Mauricio Esteban Silva', '3105551003', TRUE,  '2025-01-04 09:00:00'),
(4,  'Sandra Milena Ortiz',    '3105551004', TRUE,  '2025-01-05 09:30:00'),
(5,  'Diego Alejandro Ríos',   '3105551005', TRUE,  '2025-01-06 10:00:00'),
(6,  'Mónica Liliana Vega',    '3105551006', TRUE,  '2025-01-07 10:30:00'),
(7,  'Camilo Andrés Parra',    '3105551007', TRUE,  '2025-01-08 11:00:00'),
(8,  'Juliana Marcela Peña',   '3105551008', TRUE,  '2025-01-09 11:30:00'),
(9,  'Héctor Fabio Molina',    '3105551009', TRUE,  '2025-01-10 12:00:00'),
(10, 'Liliana María Acosta',   '3105551010', TRUE,  '2025-01-11 12:30:00'),
(11, 'Felipe Santiago Duarte', '3105551011', TRUE,  '2025-01-12 13:00:00'),
(12, 'Gloria Esperanza Niño',  '3105551012', TRUE,  '2025-01-13 13:30:00'),
(13, 'Roberto Carlos Pardo',   '3105551013', TRUE,  '2025-01-14 14:00:00'),
(14, 'Patricia Elena Suárez',  '3105551014', FALSE, '2025-01-15 14:30:00'),
(15, 'Julián David Cárdenas',  '3105551015', FALSE, '2025-01-16 15:00:00');

-- ============================================================
-- 3. CRÉDITOS (18 registros)
-- tasa_interes se calcula desde las fechas del crédito:
--   7 días=5%, 15 días=10%, uno o más meses completos=20%.
-- frecuencia_pago define únicamente cada cuánto se genera una cuota.
-- ============================================================
INSERT INTO creditos
  (id, credito_origen_id, cliente_id, cobrador_id, monto,
   fecha_inicio, fecha_limite, frecuencia_pago, estado)
VALUES
(1,  NULL, 1,  1,  1000000.00, '2026-01-01', '2026-03-01', 'MENSUAL', 'PAGADO'),
(2,  NULL, 2,  2,  1200000.00, '2026-01-05', '2026-03-05', 'MENSUAL', 'PAGADO'),
(3,  NULL, 3,  3,  1500000.00, '2025-11-01', '2026-01-01', 'MENSUAL', 'REFINANCIADO'),
(4,  NULL, 4,  4,   800000.00, '2026-01-10', '2026-03-10', 'MENSUAL', 'PAGADO'),
(5,  NULL, 5,  5,  2000000.00, '2026-01-15', '2026-03-15', 'MENSUAL', 'PAGADO'),
(6,  NULL, 6,  6,   900000.00, '2026-02-01', '2026-04-01', 'MENSUAL', 'PAGADO'),
(7,  NULL, 7,  7,  1600000.00, '2026-02-05', '2026-04-05', 'MENSUAL', 'PAGADO'),
(8,  NULL, 8,  8,  1100000.00, '2025-11-15', '2026-01-15', 'MENSUAL', 'REFINANCIADO'),
(9,  NULL, 9,  9,  2500000.00, '2026-02-10', '2026-04-10', 'MENSUAL', 'PAGADO'),
(10, NULL, 10, 10,  700000.00, '2026-02-15', '2026-04-15', 'MENSUAL', 'PAGADO'),
(11, NULL, 11, 11, 1300000.00, '2026-10-01', '2026-12-01', 'MENSUAL', 'AL_DIA'),
(12, NULL, 12, 12, 1800000.00, '2026-10-05', '2026-12-05', 'MENSUAL', 'AL_DIA'),
(13, NULL, 13, 13, 1400000.00, '2025-10-01', '2025-12-01', 'MENSUAL', 'REFINANCIADO'),
(14, NULL, 14, 14, 2200000.00, '2025-10-15', '2025-12-15', 'MENSUAL', 'REFINANCIADO'),
(15, NULL, 15, 15, 1700000.00, '2025-11-01', '2026-01-01', 'MENSUAL', 'REFINANCIADO'),
(16, 3,   3,  3,  1500000.00, '2026-10-10', '2026-12-10', 'MENSUAL', 'AL_DIA'),
(17, 8,   8,  8,  1100000.00, '2026-10-12', '2026-12-12', 'MENSUAL', 'AL_DIA'),
(18, 12,  12, 12, 1800000.00, '2026-10-15', '2026-12-15', 'MENSUAL', 'AL_DIA');

-- ============================================================
-- 4. CUOTAS (36 registros)
-- ============================================================
INSERT INTO cuotas (id, credito_id, numero_cuota, monto_cuota, fecha_vencimiento, estado) VALUES
(1,  1,  1,  500000.00, '2026-02-01', 'PAGADA'),
(2,  1,  2,  500000.00, '2026-03-01', 'PAGADA'),
(3,  2,  1,  600000.00, '2026-02-05', 'PAGADA'),
(4,  2,  2,  600000.00, '2026-03-05', 'PAGADA'),
(5,  3,  1,  750000.00, '2025-12-01', 'PENDIENTE'),
(6,  3,  2,  750000.00, '2026-01-01', 'PENDIENTE'),
(7,  4,  1,  400000.00, '2026-02-10', 'PAGADA'),
(8,  4,  2,  400000.00, '2026-03-10', 'PAGADA'),
(9,  5,  1, 1000000.00, '2026-02-15', 'PAGADA'),
(10, 5,  2, 1000000.00, '2026-03-15', 'PAGADA'),
(11, 6,  1,  450000.00, '2026-03-01', 'PAGADA'),
(12, 6,  2,  450000.00, '2026-04-01', 'PAGADA'),
(13, 7,  1,  800000.00, '2026-03-05', 'PAGADA'),
(14, 7,  2,  800000.00, '2026-04-05', 'PAGADA'),
(15, 8,  1,  550000.00, '2025-12-15', 'PENDIENTE'),
(16, 8,  2,  550000.00, '2026-01-15', 'PENDIENTE'),
(17, 9,  1, 1250000.00, '2026-03-10', 'PAGADA'),
(18, 9,  2, 1250000.00, '2026-04-10', 'PAGADA'),
(19, 10, 1,  350000.00, '2026-03-15', 'PAGADA'),
(20, 10, 2,  350000.00, '2026-04-15', 'PAGADA'),
(21, 11, 1,  650000.00, '2026-11-01', 'PENDIENTE'),
(22, 11, 2,  650000.00, '2026-12-01', 'PENDIENTE'),
(23, 12, 1,  900000.00, '2026-11-05', 'PENDIENTE'),
(24, 12, 2,  900000.00, '2026-12-05', 'PENDIENTE'),
(25, 13, 1,  700000.00, '2025-11-01', 'PENDIENTE'),
(26, 13, 2,  700000.00, '2025-12-01', 'PENDIENTE'),
(27, 14, 1, 1100000.00, '2025-11-15', 'PENDIENTE'),
(28, 14, 2, 1100000.00, '2025-12-15', 'PENDIENTE'),
(29, 15, 1,  850000.00, '2025-12-01', 'PENDIENTE'),
(30, 15, 2,  850000.00, '2026-01-01', 'PENDIENTE'),
(31, 16, 1,  750000.00, '2026-11-10', 'PENDIENTE'),
(32, 16, 2,  750000.00, '2026-12-10', 'PENDIENTE'),
(33, 17, 1,  550000.00, '2026-11-12', 'PENDIENTE'),
(34, 17, 2,  550000.00, '2026-12-12', 'PENDIENTE'),
(35, 18, 1,  900000.00, '2026-11-15', 'PENDIENTE'),
(36, 18, 2,  900000.00, '2026-12-15', 'PENDIENTE');

-- Distribuir capital + interés entre las cuotas de cada crédito.
WITH distribucion AS (
    SELECT
        cu.id,
        cr.monto + ROUND(cr.monto * cr.tasa_interes / 100, 2) AS total_credito,
        COUNT(*) OVER (PARTITION BY cu.credito_id) AS cantidad,
        ROW_NUMBER() OVER (PARTITION BY cu.credito_id ORDER BY cu.numero_cuota) AS orden
    FROM cuotas cu
    JOIN creditos cr ON cr.id = cu.credito_id
),
montos AS (
    SELECT *, ROUND(total_credito / cantidad, 2) AS monto_base
    FROM distribucion
)
UPDATE cuotas cu
SET monto_cuota = CASE
    WHEN m.orden = m.cantidad
    THEN ROUND(m.total_credito - m.monto_base * (m.cantidad - 1), 2)
    ELSE m.monto_base
END
FROM montos m
WHERE cu.id = m.id;

-- Una cuota del crédito 11 cubre el interés y permite demostrar el refinanciamiento.
UPDATE cuotas SET estado = 'PAGADA' WHERE id = 21;

-- ============================================================
-- 5. GESTIONES DE COBRO (20 registros)
-- ============================================================
INSERT INTO gestiones_cobro
  (id, credito_id, cuota_id, fecha_gestion, tipo_gestion, resultado, observaciones)
VALUES
(1,  1,  1,  '2026-02-01 09:00:00', 'LLAMADA', 'PAGO_CONFIRMADO',   'El cliente confirmó el pago de la primera cuota.'),
(2,  1,  2,  '2026-03-01 09:30:00', 'MENSAJE', 'PAGO_CONFIRMADO',   'Se recibió confirmación del pago de la segunda cuota.'),
(3,  2,  3,  '2026-02-05 10:00:00', 'LLAMADA', 'PAGO_CONFIRMADO',   'El cliente informó que realizó el pago.'),
(4,  2,  4,  '2026-03-05 10:30:00', 'VISITA',  'PAGO_CONFIRMADO',   'Se verificó el pago de la última cuota.'),
(5,  3,  5,  '2025-12-02 08:45:00', 'LLAMADA', 'SIN_RESPUESTA',     'No fue posible establecer comunicación con el cliente.'),
(6,  3,  6,  '2026-01-03 14:00:00', 'VISITA',  'REFINANCIACION',    'El cliente solicitó revisar alternativas de refinanciación.'),
(7,  4,  7,  '2026-02-10 11:00:00', 'LLAMADA', 'PAGO_CONFIRMADO',   'Se confirmó el pago de la primera cuota.'),
(8,  4,  8,  '2026-03-10 11:30:00', 'MENSAJE', 'PAGO_CONFIRMADO',   'El cliente confirmó el pago final.'),
(9,  5,  9,  '2026-02-15 08:00:00', 'LLAMADA', 'PAGO_CONFIRMADO',   'El cliente confirmó la transferencia.'),
(10, 5,  10, '2026-03-15 08:30:00', 'VISITA',  'PAGO_CONFIRMADO',   'Se verificó el cumplimiento de la obligación.'),
(11, 13, 25, '2026-10-20 09:00:00', 'LLAMADA', 'PROMESA_PAGO',      'El cliente indicó que necesita un nuevo plazo.'),
(12, 13, 26, '2026-10-21 10:00:00', 'MENSAJE', 'SIN_RESPUESTA',     'Se envió recordatorio de la obligación pendiente.'),
(13, 14, 27, '2026-10-21 11:30:00', 'VISITA',  'PROMESA_PAGO',      'El cliente manifestó intención de ponerse al día.'),
(14, 14, 28, '2026-10-22 14:00:00', 'LLAMADA', 'ACUERDO_PAGO',      'Se acordó revisar el calendario de pagos.'),
(15, 15, 29, '2026-10-22 15:30:00', 'MENSAJE', 'SIN_RESPUESTA',     'Se envió notificación de cuotas vencidas.'),
(16, 15, 30, '2026-10-23 08:45:00', 'VISITA',  'PROMESA_PAGO',      'El cliente solicitó tiempo para organizar sus finanzas.'),
(17, 16, 31, '2026-10-24 09:15:00', 'LLAMADA', 'CONTACTO_EXITOSO',  'Se explicó al cliente el calendario del crédito refinanciado.'),
(18, 16, 32, '2026-10-25 10:30:00', 'MENSAJE', 'RECORDATORIO',      'Se envió recordatorio de la próxima cuota.'),
(19, 17, 33, '2026-10-25 13:00:00', 'LLAMADA', 'CONTACTO_EXITOSO',  'El cliente confirmó conocer las nuevas condiciones.'),
(20, 18, 35, '2026-10-26 15:00:00', 'MENSAJE', 'RECORDATORIO',      'Se notificó la fecha de vencimiento de la primera cuota.');

-- ============================================================
-- 6. PAGOS (16 registros)
-- ============================================================
INSERT INTO pagos (id, cuota_id, gestion_cobro_id, monto, fecha_pago) VALUES
(1,  1,  1,   500000.00, '2026-02-01 09:15:00'),
(2,  2,  2,   500000.00, '2026-03-01 09:45:00'),
(3,  3,  3,   600000.00, '2026-02-05 10:15:00'),
(4,  4,  4,   600000.00, '2026-03-05 11:00:00'),
(5,  7,  7,   400000.00, '2026-02-10 11:15:00'),
(6,  8,  8,   400000.00, '2026-03-10 11:45:00'),
(7,  9,  9,  1000000.00, '2026-02-15 08:15:00'),
(8,  10, 10, 1000000.00, '2026-03-15 08:45:00'),
(9,  11, NULL, 450000.00, '2026-03-01 12:00:00'),
(10, 12, NULL, 450000.00, '2026-04-01 12:30:00'),
(11, 13, NULL, 800000.00, '2026-03-05 13:00:00'),
(12, 14, NULL, 800000.00, '2026-04-05 13:30:00'),
(13, 17, NULL, 1250000.00,'2026-03-10 14:00:00'),
(14, 18, NULL, 1250000.00,'2026-04-10 14:30:00'),
(15, 19, NULL, 350000.00, '2026-03-15 15:00:00'),
(16, 20, NULL, 350000.00, '2026-04-15 15:30:00'),
(17, 21, NULL, 780000.00, '2026-11-01 12:00:00');

-- El conjunto de prueba registra un pago completo por cada cuota marcada PAGADA.
UPDATE pagos p
SET monto = cu.monto_cuota
FROM cuotas cu
WHERE cu.id = p.cuota_id
  AND cu.estado = 'PAGADA';

-- Sincronizar secuencias IDENTITY
SELECT setval(pg_get_serial_sequence('clientes',      'id'), (SELECT MAX(id) FROM clientes));
SELECT setval(pg_get_serial_sequence('cobradores',    'id'), (SELECT MAX(id) FROM cobradores));
SELECT setval(pg_get_serial_sequence('creditos',      'id'), (SELECT MAX(id) FROM creditos));
SELECT setval(pg_get_serial_sequence('cuotas',        'id'), (SELECT MAX(id) FROM cuotas));
SELECT setval(pg_get_serial_sequence('gestiones_cobro','id'),(SELECT MAX(id) FROM gestiones_cobro));
SELECT setval(pg_get_serial_sequence('pagos',         'id'), (SELECT MAX(id) FROM pagos));

COMMIT;


-- ============================================================
-- JOIN 1 — INNER JOIN (tres tablas encadenadas)
-- Qué muestra: detalle de pagos registrados, agrupando
--              cobrador → crédito → cuota → pago.
--              Solo aparecen créditos que tienen pagos.
-- Por qué INNER JOIN: excluye registros sin correspondencia
--                     en ambos lados de la relación.
-- ============================================================
SELECT
    cob.nombre_cobrador,
    cl.nombre_cliente,
    cr.id                                          AS credito_id,
    cr.monto                                       AS capital_prestado,
    cr.tasa_interes                                AS tasa_pct,
    ROUND(cr.monto * cr.tasa_interes / 100, 2)    AS interes_generado,
    cu.numero_cuota,
    cu.monto_cuota,
    p.monto                                        AS monto_pagado,
    p.fecha_pago
FROM creditos cr
INNER JOIN clientes    cl  ON cl.id  = cr.cliente_id
INNER JOIN cobradores  cob ON cob.id = cr.cobrador_id
INNER JOIN cuotas      cu  ON cu.credito_id = cr.id
INNER JOIN pagos       p   ON p.cuota_id    = cu.id
ORDER BY cob.nombre_cobrador, cr.id, cu.numero_cuota;


-- ============================================================
-- JOIN 2 — LEFT JOIN
-- Qué muestra: cartera de TODOS los cobradores (activos e
--              inactivos), incluyendo los que no tienen créditos
--              asignados (aparecen con 0 en los totales).
-- Por qué LEFT JOIN: conserva todos los registros de cobradores
--                    aunque no haya créditos relacionados.
-- ============================================================
SELECT
    cob.id                                                     AS cobrador_id,
    cob.nombre_cobrador,
    cob.activo,
    COUNT(cr.id)                                               AS total_creditos,
    COALESCE(SUM(cr.monto), 0)                                 AS cartera_capital,
    COALESCE(SUM(ROUND(cr.monto * cr.tasa_interes / 100, 2)), 0) AS cartera_interes,
    COALESCE(SUM(cr.monto), 0)
      + COALESCE(SUM(ROUND(cr.monto * cr.tasa_interes / 100, 2)), 0) AS cartera_total
FROM cobradores cob
LEFT JOIN creditos cr ON cr.cobrador_id = cob.id
GROUP BY cob.id, cob.nombre_cobrador, cob.activo
ORDER BY cartera_total DESC NULLS LAST;


-- ============================================================
-- SUBCONSULTA 1 — Escalar en cláusula WHERE
-- Qué hace: retorna los créditos cuyo capital supera el
--           promedio general, mostrando cuánto excede.
-- Tipo subconsulta: escalar → devuelve un único valor (AVG)
--                   que se usa como umbral en el filtro.
-- ============================================================
SELECT
    cr.id                                          AS credito_id,
    cl.nombre_cliente,
    cr.monto,
    cr.tasa_interes                                AS tasa_pct,
    ROUND(cr.monto * cr.tasa_interes / 100, 2)    AS interes_generado,
    cr.estado,
    ROUND(
        cr.monto - (SELECT AVG(monto) FROM creditos), 2
    )                                              AS exceso_sobre_promedio
FROM creditos cr
JOIN clientes cl ON cl.id = cr.cliente_id
WHERE cr.monto > (SELECT AVG(monto) FROM creditos)   -- subconsulta escalar
ORDER BY cr.monto DESC;


-- ============================================================
-- SUBCONSULTA 2 — Correlacionada con EXISTS
-- Qué hace: obtiene los clientes que tienen al menos una
--           cuota vencida y sin pagar (aptos para cobro urgente).
-- Tipo subconsulta: correlacionada → referencia cr.cliente_id
--                   de la consulta externa en cada evaluación.
-- ============================================================
SELECT
    cl.id              AS cliente_id,
    cl.nombre_cliente,
    cl.telefono_cliente
FROM clientes cl
WHERE EXISTS (                                       -- subconsulta correlacionada
    SELECT 1
    FROM creditos cr
    JOIN cuotas cu ON cu.credito_id = cr.id
    WHERE cr.cliente_id      = cl.id               -- correlación con la fila externa
      AND cu.estado          = 'PENDIENTE'
      AND cu.fecha_vencimiento < CURRENT_DATE
)
ORDER BY cl.nombre_cliente;


-- ============================================================
-- VISTA 1 — Regular (recalculada en cada consulta)
-- Propósito: cuotas vencidas pendientes con días de mora y
--            el interés total del crédito al que pertenecen.
-- Cuándo usar: consultas frecuentes en tiempo real (dashboard
--              diario del cobrador).
-- ============================================================
CREATE OR REPLACE VIEW vista_cuotas_vencidas AS
SELECT
    cu.id                                          AS cuota_id,
    cl.nombre_cliente,
    cl.telefono_cliente,
    cr.id                                          AS credito_id,
    cob.nombre_cobrador,
    cu.numero_cuota,
    cu.monto_cuota,
    cu.fecha_vencimiento,
    (CURRENT_DATE - cu.fecha_vencimiento)          AS dias_vencidos,
    cr.tasa_interes                                AS tasa_pct,
    ROUND(cr.monto * cr.tasa_interes / 100, 2)    AS interes_credito
FROM cuotas cu
JOIN creditos   cr  ON cr.id  = cu.credito_id
JOIN clientes   cl  ON cl.id  = cr.cliente_id
JOIN cobradores cob ON cob.id = cr.cobrador_id
WHERE cu.estado           = 'PENDIENTE'
  AND cu.fecha_vencimiento < CURRENT_DATE;

-- Consultar vista regular
SELECT * FROM vista_cuotas_vencidas ORDER BY dias_vencidos DESC;


-- ============================================================
-- VISTA 2 — Materializada (snapshot pre-calculado en disco)
-- Propósito: resumen financiero por crédito: capital, interés,
--            total pagado, saldo y flag de interés cubierto
--            (prerequisito para refinanciar).
-- Diferencia vs. regular: los datos se persisten físicamente
--   y se actualizan manualmente con REFRESH MATERIALIZED VIEW.
--   Ideal para reportes gerenciales y operaciones costosas.
-- ============================================================
DROP VIEW IF EXISTS vista_resumen_creditos;   -- eliminar si existía como vista regular

CREATE MATERIALIZED VIEW vista_resumen_creditos AS
SELECT
    cr.id                                                       AS credito_id,
    cl.nombre_cliente,
    cob.nombre_cobrador,
    cr.frecuencia_pago,
    cr.monto                                                    AS capital,
    cr.tasa_interes                                             AS tasa_pct,
    ROUND(cr.monto * cr.tasa_interes / 100, 2)                 AS interes_generado,
    cr.monto
      + ROUND(cr.monto * cr.tasa_interes / 100, 2)             AS total_a_pagar,
    cr.estado                                                   AS estado_credito,
    COALESCE(SUM(cu.monto_cuota), 0)                           AS total_cuotas,
    COALESCE(SUM(pag.total_pagado), 0)                         AS total_pagado,
    COALESCE(SUM(cu.monto_cuota), 0)
      - COALESCE(SUM(pag.total_pagado), 0)                     AS saldo_pendiente,
    -- Flag: interés cubierto → habilita refinanciamiento
    CASE
        WHEN COALESCE(SUM(pag.total_pagado), 0)
             >= ROUND(cr.monto * cr.tasa_interes / 100, 2)
        THEN TRUE ELSE FALSE
    END                                                         AS interes_cubierto
FROM creditos cr
JOIN clientes   cl  ON cl.id  = cr.cliente_id
JOIN cobradores cob ON cob.id = cr.cobrador_id
LEFT JOIN cuotas cu ON cu.credito_id = cr.id
LEFT JOIN (
    SELECT cuota_id, SUM(monto) AS total_pagado
    FROM pagos GROUP BY cuota_id
) pag ON pag.cuota_id = cu.id
GROUP BY cr.id, cl.nombre_cliente, cob.nombre_cobrador,
         cr.frecuencia_pago, cr.monto, cr.tasa_interes, cr.estado
WITH DATA;

-- Índice único (habilita REFRESH CONCURRENTLY sin bloqueo)
CREATE UNIQUE INDEX idx_mv_resumen_creditos ON vista_resumen_creditos (credito_id);

-- Consultar vista materializada
SELECT * FROM vista_resumen_creditos ORDER BY saldo_pendiente DESC;

-- Para refrescar tras nuevos pagos o créditos:
-- REFRESH MATERIALIZED VIEW vista_resumen_creditos;
-- REFRESH MATERIALIZED VIEW CONCURRENTLY vista_resumen_creditos;  -- sin bloqueo de lectura


-- ============================================================
-- TRANSACCIÓN — Refinanciamiento de crédito
-- ============================================================
-- Reglas de negocio aplicadas:
--   R1. El crédito debe existir y estar en estado AL_DIA
--   R2. No debe tener ya un refinanciamiento activo (UNIQUE en BD)
--   R3. El total pagado debe cubrir el interés generado
--       (interés calculado según las fechas del crédito)
--   R4. Se marca el crédito origen como REFINANCIADO
--   R5. Se crea el nuevo crédito con mismo monto y cobrador
--   R6. Se generan cuotas con capital + interés según frecuencia_pago
--   R7. Se refresca la vista materializada
-- ============================================================
BEGIN;

DO $$
DECLARE
    p_credito_origen_id  INTEGER := 11;
    v_credito            creditos%ROWTYPE;
    v_total_pagado       DECIMAL(12,2);
    v_interes_requerido  DECIMAL(12,2);
    v_total_a_pagar      DECIMAL(12,2);
    v_nuevo_id           INTEGER;
    v_fecha_inicio       DATE := CURRENT_DATE;
    v_fecha_limite       DATE;
    v_fecha_vencimiento  DATE;
    v_intervalo          INTERVAL;
    v_plazo_meses        INTEGER := 0;
    v_cantidad_cuotas    INTEGER := 0;
    v_monto_cuota        DECIMAL(12,2);
    v_monto_actual       DECIMAL(12,2);
    v_total_asignado     DECIMAL(12,2) := 0;
    i                    INTEGER;
BEGIN
    SELECT * INTO v_credito
    FROM creditos
    WHERE id = p_credito_origen_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Crédito % no encontrado.', p_credito_origen_id;
    END IF;

    IF v_credito.estado <> 'AL_DIA' THEN
        RAISE EXCEPTION
            'El crédito % está en estado "%" y no puede refinanciarse.',
            p_credito_origen_id, v_credito.estado;
    END IF;

    IF EXISTS (SELECT 1 FROM creditos WHERE credito_origen_id = p_credito_origen_id) THEN
        RAISE EXCEPTION
            'El crédito % ya tiene un refinanciamiento.',
            p_credito_origen_id;
    END IF;

    v_interes_requerido := ROUND(v_credito.monto * v_credito.tasa_interes / 100, 2);

    SELECT COALESCE(SUM(p.monto), 0)
    INTO v_total_pagado
    FROM pagos p
    JOIN cuotas cu ON cu.id = p.cuota_id
    WHERE cu.credito_id = p_credito_origen_id;

    RAISE NOTICE '— Interés requerido  : $%', v_interes_requerido;
    RAISE NOTICE '— Total pagado       : $%', v_total_pagado;
    RAISE NOTICE '— Falta cubrir       : $%', GREATEST(0, v_interes_requerido - v_total_pagado);

    IF v_total_pagado < v_interes_requerido THEN
        RAISE EXCEPTION
            'No se puede refinanciar el crédito %. '
            'Interés requerido: $%, pagado: $%, falta: $%.',
            p_credito_origen_id,
            v_interes_requerido,
            v_total_pagado,
            (v_interes_requerido - v_total_pagado);
    END IF;

    UPDATE creditos SET estado = 'REFINANCIADO' WHERE id = p_credito_origen_id;

    IF v_credito.fecha_limite IN (
        v_credito.fecha_inicio + 7,
        v_credito.fecha_inicio + 15
    ) THEN
        v_fecha_limite := v_fecha_inicio + (v_credito.fecha_limite - v_credito.fecha_inicio);
    ELSE
        v_plazo_meses := (
            (EXTRACT(YEAR FROM v_credito.fecha_limite) - EXTRACT(YEAR FROM v_credito.fecha_inicio)) * 12
            + EXTRACT(MONTH FROM v_credito.fecha_limite)
            - EXTRACT(MONTH FROM v_credito.fecha_inicio)
        )::INTEGER;
        v_fecha_limite := (v_fecha_inicio + make_interval(months => v_plazo_meses))::date;
    END IF;

    v_intervalo := CASE v_credito.frecuencia_pago
        WHEN 'DIARIA'    THEN INTERVAL '1 day'
        WHEN 'SEMANAL'   THEN INTERVAL '7 days'
        WHEN 'QUINCENAL' THEN INTERVAL '15 days'
        WHEN 'MENSUAL'   THEN INTERVAL '1 month'
    END;

    i := 1;
    LOOP
        v_fecha_vencimiento := LEAST(
            (v_fecha_inicio + (i * v_intervalo))::date,
            v_fecha_limite
        );
        v_cantidad_cuotas := v_cantidad_cuotas + 1;
        EXIT WHEN v_fecha_vencimiento = v_fecha_limite;
        i := i + 1;
    END LOOP;

    INSERT INTO creditos (
        credito_origen_id, cliente_id, cobrador_id,
        monto, fecha_inicio, fecha_limite,
        frecuencia_pago, estado
    ) VALUES (
        p_credito_origen_id,
        v_credito.cliente_id, v_credito.cobrador_id,
        v_credito.monto,
        v_fecha_inicio, v_fecha_limite,
        v_credito.frecuencia_pago,
        'AL_DIA'
    )
    RETURNING id INTO v_nuevo_id;

    v_total_a_pagar := v_credito.monto + v_interes_requerido;
    v_monto_cuota := ROUND(v_total_a_pagar / v_cantidad_cuotas, 2);
    v_total_asignado := 0;

    FOR i IN 1..v_cantidad_cuotas LOOP
        v_fecha_vencimiento := LEAST(
            (v_fecha_inicio + (i * v_intervalo))::date,
            v_fecha_limite
        );
        IF i = v_cantidad_cuotas THEN
            v_monto_actual := v_total_a_pagar - v_total_asignado;
        ELSE
            v_monto_actual := v_monto_cuota;
            v_total_asignado := v_total_asignado + v_monto_actual;
        END IF;

        INSERT INTO cuotas (credito_id, numero_cuota, monto_cuota, fecha_vencimiento, estado)
        VALUES (
            v_nuevo_id,
            i,
            v_monto_actual,
            v_fecha_vencimiento,
            'PENDIENTE'
        );
    END LOOP;

    REFRESH MATERIALIZED VIEW vista_resumen_creditos;

    RAISE NOTICE '✔ Refinanciamiento exitoso.';
    RAISE NOTICE '  Crédito origen  : % → estado REFINANCIADO', p_credito_origen_id;
    RAISE NOTICE '  Nuevo crédito   : %', v_nuevo_id;
    RAISE NOTICE '  Capital         : $%', v_credito.monto;
    RAISE NOTICE '  Tasa aplicada   : %%%', v_credito.tasa_interes;
    RAISE NOTICE '  Interés         : $%', v_interes_requerido;
    RAISE NOTICE '  Total a pagar   : $%', v_total_a_pagar;
    RAISE NOTICE '  Cuotas creadas  : %', v_cantidad_cuotas;
    RAISE NOTICE '  Vence           : %', v_fecha_limite;

END $$;

COMMIT;


-- ============================================================
-- VERIFICACIONES FINALES
-- ============================================================

-- Cadena completa de refinanciamientos (CTE recursivo)
WITH RECURSIVE cadena AS (
    SELECT id, credito_origen_id, cliente_id, monto, tasa_interes, estado, 1 AS nivel
    FROM creditos WHERE credito_origen_id IS NULL
    UNION ALL
    SELECT cr.id, cr.credito_origen_id, cr.cliente_id, cr.monto, cr.tasa_interes, cr.estado,
           c.nivel + 1
    FROM creditos cr
    JOIN cadena c ON c.id = cr.credito_origen_id
)
SELECT nivel,
       id               AS credito_id,
       credito_origen_id,
       monto,
       tasa_interes     AS tasa_pct,
       ROUND(monto * tasa_interes / 100, 2) AS interes,
       estado
FROM cadena
ORDER BY nivel, id;

-- Conteo final por tabla
SELECT 'clientes'        AS tabla, COUNT(*) AS registros FROM clientes
UNION ALL SELECT 'cobradores',              COUNT(*) FROM cobradores
UNION ALL SELECT 'creditos',                COUNT(*) FROM creditos
UNION ALL SELECT 'cuotas',                  COUNT(*) FROM cuotas
UNION ALL SELECT 'gestiones_cobro',         COUNT(*) FROM gestiones_cobro
UNION ALL SELECT 'pagos',                   COUNT(*) FROM pagos
ORDER BY tabla;

-- Casos de verificación para la tasa por plazo; todos deben indicar correcto = TRUE.
SELECT plazo, tasa_obtenida, tasa_esperada, tasa_obtenida = tasa_esperada AS correcto
FROM (
    VALUES
      ('7 días', obtener_tasa_interes_credito(DATE '2026-01-01', DATE '2026-01-08'), 5.00::DECIMAL),
      ('15 días', obtener_tasa_interes_credito(DATE '2026-01-01', DATE '2026-01-16'), 10.00::DECIMAL),
      ('1 mes', obtener_tasa_interes_credito(DATE '2026-01-01', DATE '2026-02-01'), 20.00::DECIMAL),
      ('3 meses', obtener_tasa_interes_credito(DATE '2026-01-01', DATE '2026-04-01'), 20.00::DECIMAL)
) AS casos(plazo, tasa_obtenida, tasa_esperada);

-- Verificación: las cuotas deben sumar capital + interés por crédito (0 filas esperadas).
SELECT cr.id AS credito_id,
       SUM(cu.monto_cuota) AS total_cuotas,
       cr.monto + ROUND(cr.monto * cr.tasa_interes / 100, 2) AS total_esperado
FROM creditos cr
JOIN cuotas cu ON cu.credito_id = cr.id
GROUP BY cr.id, cr.monto, cr.tasa_interes
HAVING SUM(cu.monto_cuota) <> cr.monto + ROUND(cr.monto * cr.tasa_interes / 100, 2);

-- Verificación de integridad: gestión debe pertenecer al mismo crédito que la cuota
SELECT gc.id AS gestion_id, gc.credito_id, gc.cuota_id, cu.credito_id AS credito_real
FROM gestiones_cobro gc
JOIN cuotas cu ON cu.id = gc.cuota_id
WHERE gc.credito_id <> cu.credito_id;   -- debe devolver 0 filas
