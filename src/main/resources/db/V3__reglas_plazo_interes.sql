-- Migración para bases ya creadas con una versión anterior del esquema.
-- Deriva la tasa a partir del plazo y permite frecuencia de pago diaria.

ALTER TABLE creditos
    ADD COLUMN IF NOT EXISTS tasa_interes decimal(5,2);

CREATE OR REPLACE FUNCTION obtener_tasa_interes_credito(p_inicio date, p_limite date)
RETURNS decimal(5,2)
LANGUAGE plpgsql
IMMUTABLE
STRICT
AS $$
DECLARE
  v_meses integer;
BEGIN
  IF p_limite = p_inicio + 7 THEN
    RETURN 5.00;
  ELSIF p_limite = p_inicio + 15 THEN
    RETURN 10.00;
  END IF;

  v_meses := (EXTRACT(YEAR FROM p_limite) - EXTRACT(YEAR FROM p_inicio)) * 12
           + EXTRACT(MONTH FROM p_limite) - EXTRACT(MONTH FROM p_inicio);
  IF v_meses >= 1
     AND (p_inicio + make_interval(months => v_meses))::date = p_limite THEN
    RETURN 20.00;
  END IF;

  RAISE EXCEPTION
    'Plazo no permitido en crédito existente: inicio %, límite %.',
    p_inicio, p_limite;
END;
$$;

CREATE OR REPLACE FUNCTION asignar_tasa_interes_credito()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  NEW.tasa_interes := obtener_tasa_interes_credito(NEW.fecha_inicio, NEW.fecha_limite);
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_asignar_tasa_interes_credito ON creditos;
CREATE TRIGGER trg_asignar_tasa_interes_credito
BEFORE INSERT OR UPDATE ON creditos
FOR EACH ROW
EXECUTE FUNCTION asignar_tasa_interes_credito();

UPDATE creditos SET tasa_interes = obtener_tasa_interes_credito(fecha_inicio, fecha_limite);
ALTER TABLE creditos ALTER COLUMN tasa_interes SET NOT NULL;
ALTER TABLE creditos DROP CONSTRAINT IF EXISTS chk_tasa_interes;
ALTER TABLE creditos
    ADD CONSTRAINT chk_tasa_interes CHECK (tasa_interes IN (5.00, 10.00, 20.00));
ALTER TABLE creditos DROP CONSTRAINT IF EXISTS chk_frecuencia_pago;
ALTER TABLE creditos
    ADD CONSTRAINT chk_frecuencia_pago
    CHECK (frecuencia_pago IN ('DIARIA', 'SEMANAL', 'QUINCENAL', 'MENSUAL'));
