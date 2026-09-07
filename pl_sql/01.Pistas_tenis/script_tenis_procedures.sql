/* PRACTICA PL/SQL pasos 5 y 6 
Autor: Muñoz Moreno Alberto Rafael.

PASO 5:
PREGUNTA 1: CREAMOS LAS FUNCIONES ANULAR RESERVA Y RESERVAR PISTA COMO PROCEDIMIENTOS*/
CREATE OR REPLACE PROCEDURE pAnularReserva(
    p_socio varchar,
	p_fecha date,
	p_hora number, 
	p_pista number
) AS
BEGIN
--limpieza de datos
DELETE FROM RESERVAS
    WHERE trunc(fecha) = trunc(p_fecha) AND
            pista = p_pista AND
            hora = p_hora AND
            socio = p_socio;
-- si no se borra ninguna fila lanzamos la excepcion PREGUNTA 3
	if sql%rowcount = 0 then
		raise_application_error(-20000, 'Reserva inexistente');
	end if;
    commit;
end;
/
-- Procedimiento pReservarPista
create or replace PROCEDURE pReservaPista(
        p_socio VARCHAR,
        p_fecha DATE,
        p_hora INTEGER
    )  AS

    CURSOR vPistasLibres IS
        SELECT nro
        FROM pistas 
        WHERE nro NOT IN (
            SELECT pista
            FROM reservas
            WHERE 
                trunc(fecha) = trunc(p_fecha) AND
                hora = p_hora)
        order by nro FOR UPDATE of nro; --ver actualizacion PASO 6
            
    vPista INTEGER;

BEGIN
    OPEN vPistasLibres;
    FETCH vPistasLibres INTO vPista;

    IF vPistasLibres%NOTFOUND
    THEN
        CLOSE vPistasLibres;
        --PREGUNTA 4
        raise_application_error(-20001, 'No quedan pistas libres en esa fecha y hora');
    END IF;

    INSERT INTO reservas VALUES (vPista, p_fecha, p_hora, p_socio);
    CLOSE vPistasLibres;
    COMMIT;
END;
/

-- Procedimiento de los teses (TEST_PROCEDURES_TENNIS)
CREATE OR REPLACE PROCEDURE TEST_PROCEDURES_TENIS AS 
BEGIN 
--limpieza de datos inicial
DELETE FROM RESERVAS
WHERE trunc(FECHA) = trunc(CURRENT_DATE);
COMMIT;

dbms_output.put_line('--- Inicio del Test de Procedimientos ---');
--Test1
BEGIN 
    pReservaPista('Socio 1', CURRENT_DATE,12);
    dbms_output.put_line('Reserva 1 ok'); --indico salida por pantalla
EXCEPTION WHEN OTHERS THEN
    dbms_output.put_line(SQLERRM); -- Imprimo error
END;
BEGIN 
    pReservaPista('Socio 2', CURRENT_DATE,12);
    dbms_output.put_line('Reserva 2 ok'); --indico salida por pantalla
EXCEPTION WHEN OTHERS THEN
    dbms_output.put_line(SQLERRM); -- Imprimo error
END;
BEGIN 
    pReservaPista('Socio 3', CURRENT_DATE,12);
    dbms_output.put_line('Reserva 3 ok'); --indico salida por pantalla
EXCEPTION WHEN OTHERS THEN
    dbms_output.put_line(SQLERRM); -- Imprimo error
END;
-- socio 4 deberia fallar
BEGIN 
    pReservaPista('Socio 4', CURRENT_DATE,12);
    dbms_output.put_line('Reserva 4 ok'); --indico salida por pantalla
EXCEPTION WHEN OTHERS THEN
    dbms_output.put_line(SQLERRM); -- Imprimo error
END;
-- TEST DE ANULACIONES
BEGIN 
    pAnularReserva('Socio 1', CURRENT_DATE,12,1);
    dbms_output.put_line('Anulación 1 ok'); --indico salida por pantalla
EXCEPTION WHEN OTHERS THEN
    dbms_output.put_line('Anulación 1, error ' || SQLERRM); -- Imprimo error
END;
--TEST DE ANULACION INEXISTENTE
BEGIN 
    pAnularReserva('Socio 4', DATE'1920-1-1',12,1);
    dbms_output.put_line('Anulación 4 ok'); --indico salida por pantalla
EXCEPTION WHEN OTHERS THEN
    dbms_output.put_line('Anulación 4, error esperado ' || SQLERRM); -- Imprimo error
END;

END;
/

/*
PASO 6:

En caso de que dos transacciones intenten reservar la ultima pista libre, lo mas probable es que ocurra una condicion de carrera
donde ambas detectan la pista libre antes de que alguna de las dos cometa esa reserva.

La solucion que se puede implementar es empleando la clausula FOR UPDATE OF nro en el cursor de pistas libres. Esto aplica el bloqueo pesimista
sobre las filas que seleccione el cursor, impidiendo asi qeu otras transacciones lean o modifiquen la disponibilidad de las pistas hasta 
que la transaccion actual termine
*/
