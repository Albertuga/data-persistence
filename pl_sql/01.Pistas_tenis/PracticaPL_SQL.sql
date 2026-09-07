/* PRACTICA PL/SQL 
AUTOR: MUÑOZ MORENO ALBERTO RAFAEL*/

drop table reservas;
drop table pistas;
drop sequence seq_pistas;

create table pistas (
	nro integer primary key
	);
	
create table reservas (
	pista integer references pistas(nro),
	fecha date,
	hora integer check (hora >= 0 and hora <= 23),
	socio varchar(20),
	primary key (pista, fecha, hora)
	);
	
create sequence seq_pistas;

insert into pistas values (seq_pistas.nextval);
insert into reservas 
	values (seq_pistas.currval, '20/03/2018', 14, 'Pepito');
insert into pistas values (seq_pistas.nextval);
insert into reservas 
	values (seq_pistas.currval, '24/03/2018', 18, 'Pepito');
insert into reservas 
	values (seq_pistas.currval, '21/03/2018', 14, 'Juan');
insert into pistas values (seq_pistas.nextval);
insert into reservas 
	values (seq_pistas.currval, '22/03/2018', 13, 'Lola');
insert into reservas 
	values (seq_pistas.currval, '22/03/2018', 12, 'Pepito');

commit;

create or replace function anularReserva( 
	p_socio varchar,
	p_fecha date,
	p_hora integer, 
	p_pista integer ) 
return integer is

begin
	DELETE FROM reservas 
        WHERE
            trunc(fecha) = trunc(p_fecha) AND
            pista = p_pista AND
            hora = p_hora AND
            socio = p_socio;

	if sql%rowcount = 1 then
		commit;
		return 1;
	else
		rollback;
		return 0;
	end if;
end;
/

create or replace FUNCTION reservarPista(
        p_socio VARCHAR,
        p_fecha DATE,
        p_hora INTEGER
    ) 
RETURN INTEGER IS

    CURSOR vPistasLibres IS
        SELECT nro
        FROM pistas 
        WHERE nro NOT IN (
            SELECT pista
            FROM reservas
            WHERE 
                trunc(fecha) = trunc(p_fecha) AND
                hora = p_hora)
        order by nro;
            
    vPista INTEGER;

BEGIN
    OPEN vPistasLibres;
    FETCH vPistasLibres INTO vPista;

    IF vPistasLibres%NOTFOUND
    THEN
        CLOSE vPistasLibres;
        RETURN 0;
    END IF;

    INSERT INTO reservas VALUES (vPista, p_fecha, p_hora, p_socio);
    CLOSE vPistasLibres;
    COMMIT;
    RETURN 1;
END;
/



/*
PASO 1
PREGUNTA 1
TRUNC en las fechas sirve para recortar numeros o fechas a una precision mas especifica,
En este caso sirve para que en la comparacion de fechas que ocurre en la funcion reservarPista
solamente se tomen en cuenta las fechas y deje las horas a 00:00:00. En oracle, DATE incluye
la fecha, hora minutos y segundos.
Esto se hace con la finalidad de que coincidan las fechas unicamente con su dia mes y año.


PREGUNTA 2
sql%rowcount es una variable que nos retorna el numero de filas que fueron afectadas por la ultima
instruccion, sea esta un INSERT, UPDATE O DELETE. Es util para saber si la operación tuvo efecto o no.
Si vale 1 entonces todo ha ido bien y se cierra la transaccion con commit.
Si vale 0, esto nos indica que hubo un problema.
Tiene que utilizarse dentro de la transacción y antes de hacer COMMIT/ROLLBACK. (P9 - apuntes tema 2)

PREGUNTA 3
Un cursor es una variable que recoge el resultado de una SELECT que retorna mas de una fila. Tambien
es llamado cursor explicito, adiferencia del SELECT INTO (cursor implicito) que retorna unicamente una sola fila, ya que si
retorna mas de una, lanza una excepcion al igual que si hay cero filas (P15 apuntes -Tema 2)

En la segunda funcion (reservarPistas) tenemos el cursor vPistasLibres. Este cursor encuentra una pista de tenis disponible
en una fecha y hora especificas.

Efecto de las siguientes operaciones:
OPEN: ejecuta la sentencia SELECT definida en el cursor y prepara el conjunto de resultados
FETCH: Sirve para avanzar una posicion en el cursor, y guarda el contenido de la fila leida en las variables indicadas
CLOSE: Cierra el cursor y libera la memoria asociada a este.

%NOTFOUND propiedad booleana que vale TRUE cuando al hacer un FETCH ya no quedan filas por leer
%FOUND vale TRUE cuando FETCH ha alcanzado y recuperado una fila.

PREGUNTA 4:
NO, porque el ROLLBACK es la manera mas correcta de terminar una reserva que no ha podido completarse,
si sql%rowcount arroja 0 es porque no ha encontrado ninguna fila que cumpliera los criterios de eliminacion,
por lo que no se modifican datos. Si hacemos commit estariamos validando una transaccion que en realidad no ha hecho nada.
A pesar de ser lo mas correcto, en este caso hacer COMMIT o ROLLBACK no tendria un cambio significativo.

PREGUNTA 5:
Actualmente la transaccion reservarPista queda abierta porque si ocurriera un error inesperado, el programa se detiene, pero
como no tenemos ninguna excepcion, el control sale de la funcion sin haber llegado nunca a un cierre (Commit o rollback).
*/

/*
PASO 2:
Uso La estructura previa que ya tenia el enuncniado. Mantengo comentado como se pide en el enunciado.*/
/*
SET SERVEROUTPUT ON;

declare
 resultado integer;
begin
        --comenzamos con las 3 reservas validas
     resultado := reservarPista( 'Socio 1', CURRENT_DATE, 12 );
     if resultado=1 then
        dbms_output.put_line('Reserva 1: OK');
     else
        dbms_output.put_line('Reserva 1: MAL');
     end if;
     
     resultado := reservarPista('Socio 2', CURRENT_DATE, 12);
     if resultado =1 then
        dbms_output.put_line('Reserva 2: OK');
    else
        dbms_output.put_line('Reserva 2: MAL');
    end if;
    
    resultado := reservarPista( 'Socio 3', CURRENT_DATE, 12 );
     if resultado=1 then
        dbms_output.put_line('Reserva 3: OK');
     else
        dbms_output.put_line('Reserva 3: MAL');
     end if;
     
-- Intentamos cuarta reserva no valida (sin hueco)
     
     resultado := reservarPista( 'Socio 4', CURRENT_DATE, 12 );
     if resultado=1 then
        dbms_output.put_line('Reserva 4: OK');
     else
        dbms_output.put_line('Reserva 4: MAL');
     end if;
     
     --intentamos ahora las anulaciones, una normal y la inexistente
     dbms_output.put_line('---------ANULACIONES---------');
      -- anulacion de reserva valida
    resultado := anularreserva( 'Socio 1', CURRENT_DATE, 12, 1);
     if resultado=1 then
        dbms_output.put_line('Reserva 1 anulada: OK');
     else
        dbms_output.put_line('Reserva 1 anulada: MAL');
     end if;
  --Anulacion de reserva inexistente
     resultado := anularreserva( 'Socio 4', date '1920-1-1', 12, 1);
      if resultado=1 then
        dbms_output.put_line('Reserva 4 anulada: OK');
     else
        dbms_output.put_line('Reserva 4 anulada: (era inexistente asi que esto es lo que debe salir');
     end if;
  
end;
/

-- SELECT para verificar el resultado final
SELECT * from RESERVAS order by  FECHA, HORA, PISTA;

*/

/*
PREGUNTA 3:
Primero antes que nada he tenido que dar privilegios a hr para poder ejecutar el debugger, para ello desde SYS/1234 en sqlpluss (como en minuto 14:19 del video)
he introducido los comandos  GRANT DEBUG CONNECT SESSION TO HR; Y GRANT DEBUG ANY PROCEDURE TO HR;
de esta manera al iniciar sesion con hr consigo los privilegios suficientes para ejecutar el debugger.

En sql_ developer me dirijo a funciones y elijo la funcion anular reerva, posteriormente defino un punto de ruptura en la linea 9 y hago el debug.
en los datos, tengo que ingresar un socio en este caso metemos al socio "pepito" con fecha 10/03/2018, hora 14 y pista 1, inciiamos el debug, 
en el siguiente paso salta directamente a sql%rowcout donde veremos si se ha insertado nuestro usuario en la pestaña datos, donde podemos confirmar que si lo ha hecho
vemos como posteriormente salta al commit y al return.
Al ejecutar el DELETE podemros ver que en la pestaña datos el rowcount pasa a valer 1 lo que confirma que el usuario ha sido correctamente eliminado.

tambein nos indica donde hemos puesto el punto de ruptura.
Conectando a la base de datos PracticaPL_SQL.
Ejecutando PL/SQL: ALTER SESSION SET PLSQL_DEBUG=TRUE
Ejecutando PL/SQL: CALL DBMS_DEBUG_JDWP.CONNECT_TCP( '127.0.0.1', '24731' )
El depurador ha aceptado la conexión de la base de datos en el puerto 24731.
Punto de ruptura de origen: ANULARRESERVA.pls:9

*/

/*PASO 4: Creamos el bloque con PROCEDURE, para ello empleamos la logica del paso 2 el cual permanecerá comentado*/

SET SERVEROUTPUT ON;

CREATE OR REPLACE PROCEDURE TEST_FUNCIONES_TENIS AS
 resultado integer;
begin

-- Comienzo con limpieza de datos para empezar el test desde cero siempre
DELETE FROM RESERVAS WHERE TRUNC(FECHA) = TRUNC(CURRENT_DATE);
COMMIT;

            dbms_output.put_line('Inicio del TEST_FUNCIONES_TENIS');
        --comenzamos con las 3 reservas validas
     resultado := reservarPista( 'Socio 1', CURRENT_DATE, 12 );
     if resultado =1 then
        dbms_output.put_line('Reserva 1: OK');
    else
        dbms_output.put_line('Reserva 1: MAL');
    end if;
    
     resultado := reservarPista('Socio 2', CURRENT_DATE, 12);
     if resultado =1 then
        dbms_output.put_line('Reserva 2: OK');
    else
        dbms_output.put_line('Reserva 2: MAL');
    end if;
    
    resultado := reservarPista( 'Socio 3', CURRENT_DATE, 12 );
    if resultado =1 then
        dbms_output.put_line('Reserva 3: OK');
    else
        dbms_output.put_line('Reserva 3: MAL');
    end if;
     
-- Intentamos cuarta reserva no valida (sin hueco)
     
     resultado := reservarPista( 'Socio 4', CURRENT_DATE, 12 );
     if resultado =1 then
        dbms_output.put_line('Reserva 4: OK');
    else
        dbms_output.put_line('Reserva 4: MAL');
    end if;

      -- anulacion de reserva valida
    resultado := anularreserva( 'Socio 1', CURRENT_DATE, 12, 1);
   if resultado=1 then
        dbms_output.put_line('Reserva 1 anulada: OK');
     else
        dbms_output.put_line('Reserva 1 anulada: MAL');
     end if;
    
  --Anulacion de reserva inexistente
     resultado := anularreserva( 'Socio 4', date '1920-1-1', 12, 1);
     if resultado=1 then
        dbms_output.put_line('Reserva 4 anulada: OK');
     else
        dbms_output.put_line('Reserva 4 anulada: (era inexistente asi que esto es lo que debe salir');
     end if;
    
end;
/

-- Ejecutar como un bloque anonimo
BEGIN 
TEST_FUNCIONES_TENIS;
END;
/

--Ejecutar mediante EXEC:
EXEC TEST_FUNCIONES_TENIS;

-- SELECT para verificar el resultado final
SELECT * from RESERVAS order by  FECHA, HORA, PISTA;


/*
PASO 5 EN EL SIGUIENTE SCRIPT
*/
