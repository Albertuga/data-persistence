/*Practica PL/SQL - Ejercicio de Evaluación 2 (12.5%)

Autor - Muñoz Moreno Alberto Rafael.

Aplicaciones de Bases de Datos.

*/


drop table pago_matricula cascade constraints;
drop table matricula cascade constraints;
drop table edicion_curso cascade constraints;
drop table curso cascade constraints;
drop table alumno cascade constraints;

drop sequence seq_pago;
drop sequence seq_matricula;
drop sequence seq_edicion;
drop sequence seq_curso;

create table alumno(
    dni      varchar2(9) primary key,
    nombre   varchar2(20) not null,
    ape1     varchar2(20) not null,
    ape2     varchar2(20) not null,
    email    varchar2(60) not null unique
);

create sequence seq_curso;

create table curso(
    id_curso     number primary key,
    nombre       varchar2(60) not null,
    horas        number not null check (horas > 0),
    precio_base  number(8,2) not null check (precio_base >= 0)
);

create sequence seq_edicion;

create table edicion_curso(
    id_edicion       number primary key,
    id_curso         number not null references curso(id_curso),
    fecha_inicio     date not null,
    fecha_fin        date not null,
    plazas_maximas   number not null check (plazas_maximas > 0),
    plazas_ocupadas  number not null check (plazas_ocupadas >= 0),
    estado           varchar2(10) not null check (estado in ('ABIERTA', 'CERRADA')),
    check (fecha_fin >= fecha_inicio),
    check (plazas_ocupadas <= plazas_maximas)
);

create sequence seq_matricula;

create table matricula(
    id_matricula    number primary key,
    dni_alumno      varchar2(9) not null references alumno(dni),
    id_edicion      number not null references edicion_curso(id_edicion),
    fecha_matricula date not null,
    estado          varchar2(12) not null check (estado in ('CONFIRMADA', 'ESPERA', 'CANCELADA')),
    importe         number(8,2) not null check (importe >= 0)
);

create sequence seq_pago;

create table pago_matricula(
    id_pago        number primary key,
    id_matricula   number not null references matricula(id_matricula),
    fecha_pago     date not null,
    importe        number(8,2) not null check (importe >= 0),
    constraint uq_pago_matricula unique (id_matricula)
);
/
-------------------------------------------------------------
-- IMPLEMENTACION DE TRANSACCIONES --
-------------------------------------------------------------
create or replace procedure matricular_alumno(
    p_dni_alumno  alumno.dni%type,
    p_id_edicion  edicion_curso.id_edicion%type
) is

-- recojo variables necesarias
var_edicion  edicion_curso%rowtype; --recojo fila de edicion que me interesa
var_precio curso.precio_base%type; --para matricula
var_duplicado number; --variable que contiene cant de alumnos 

begin
--1. verificamos si existe el alumno, si no REGLANº1 p2 enunciado -20001
    begin
        select 1 into var_duplicado --tiene que haber 1
        from alumno where dni = p_dni_alumno;
    exception 
        when no_data_found then
        raise_application_error(-20001, 'Alumno inexistente.');
    end; --fin verif alumno
--2. verificamos si la edicion existe, si no REGLANº2 p2 enunciado -20002
    begin 
        select * into var_edicion from edicion_curso
        where id_edicion = p_id_edicion for update; --FOR UPDATE OBLIGATORIO
    exception 
        when no_data_found then
        raise_application_error(-20002, 'Edicion inexistente.');
    end;
--3. verificamos si la edicion admite nuevas matriculas o si esta 'CERRADA'
    if var_edicion.estado = 'CERRADA' then 
    raise_application_error(-20003, 'La edicion no admite matriculas.');
    end if;
--4. prohibir que un alumno tenga mas de una matricula en una misma edicion
--CONFIRMADA O ESPERA es activo! excepcion si esta -20004
begin
    select 1 into var_duplicado from matricula
    where dni_alumno = p_dni_alumno and
    id_edicion = p_id_edicion and
    estado in ('CONFIRMADA', 'ESPERA');
    -- ejecuto el raise en caso de que ENCUENTRE la duplicada
    raise_application_error(-20004, 'El alumno ya tiene una matricula activa en la edicion.');
    exception
    when no_data_found then
    null; --ok porque no existe.
end;
    
--7. el importe de la matricula es precio_base del curso asociado a la edicion
select curso.precio_base into var_precio
from curso where curso.id_curso = var_edicion.id_curso; --id curso de la edicion.
    
-- 5 si la edicion = plazas disponibles = inserto matricula con CONFIRMADA, si no ESPERA
-- incremento plazas_ocupadas(tabla edicion)
    update edicion_curso
    set plazas_ocupadas = plazas_ocupadas +1
    where id_edicion = p_id_edicion and
    plazas_ocupadas < plazas_maximas; --solo aumento si hay hueco en plazas
    --inserto la matricula correspondiente 
if SQL%ROWCOUNT = 1 then --para saber si hemos podido incrementar luego decido estado
    insert into matricula(id_matricula, dni_alumno, id_edicion, fecha_matricula, estado, importe)
    values (seq_matricula.nextval, p_dni_alumno, p_id_edicion, sysdate, 'CONFIRMADA', var_precio);
    
    else --6 se inserta como espera y no aumentamos contador de plazas ocupadas.
        insert into matricula(id_matricula, dni_alumno, id_edicion, fecha_matricula, estado, importe)
        values (seq_matricula.nextval, p_dni_alumno, p_id_edicion, sysdate, 'ESPERA', var_precio);
end if;
    
    commit;
    -- en caso de exception rollback
    -- Ejemplos de ilustraciones 8 y 12. Tema 2
    exception
        when others then
            rollback;
            raise;
end;
/
/*En la pregunta 2 hemos usado FOR UPDATE en la edicion por si nos encontramos con el caso de que 
dos matriculas confirmadas de la misma edicion y dos matriculas en espera leen el estado de la 
lista de espera, estas leerían el mismo dato y podrian proporcionar la misma matricula dos veces.
El for update hace que una de las dos sesiones (la que llegue despues de la otra) entre en espera 
hasta que la primera termine, por lo que todas leerán la lista de espera actualizada.
*/
-----------------------------------------------------------------
--PROCEDIMIENTO PARA CANCELAR MATRICULA EXISTENTE ...
-----------------------------------------------------------------
create or replace procedure cancelar_matricula(
    p_id_matricula  matricula.id_matricula%type
) is 
-- inserto variables con informacion matricula 
var_matricula matricula%rowtype;        --bloqueo matricula
var_edicion matricula.id_edicion%type;  --id_edicion de la matricula
var_espera matricula.id_matricula%type; --matriculas en espera

begin
-- Aseguro rollback en caso de error bloqueando matricula y verif si existe
begin 
    select * into var_matricula --recojo todos los datos de la matricula
    from matricula 
    where id_matricula=p_id_matricula
    for update; --bloqueo la fila de mi id_matricula
    -- PREGUNTA 2.1 si la matricula no existe --> -20005
exception
    when no_data_found then
    raise_application_error(-20005, 'Matricula inexistente.');
end;
--PREGUNTA 2.2  Si ya esta cancelada se lanza -20006
if var_matricula.estado = 'CANCELADA' then
    raise_application_error(-20006, 'La matricula ya estaba cancelada.');
end if;

--2.3 si confirmada --> cancelada --> decrementa plaza
-- si queda matricula de ESPERA --> CONFIRMADA LA MAS ANTIGUA y menor id_matricula...
-- bloqueo la edicion para evitar cancelaciones concurrentes!
--recojo lista de espera
select id_edicion into var_edicion
from edicion_curso
where id_edicion = var_matricula.id_edicion
for update;

-- PASO A CANCELADA tanto para confirmadas como para las espera
update matricula
set estado = 'CANCELADA'
where id_matricula = p_id_matricula;

--si CONFIRMADA --> decremento contador, y promociono espera
if var_matricula.estado = 'CONFIRMADA' then
    -- decremento plazas_ocupadas
    update edicion_curso
    set plazas_ocupadas = plazas_ocupadas -1
    where id_edicion = var_edicion; 
    
    -- PROMOCIONO espera
    begin 
        select id_matricula into var_espera --variable que contiene las esperas
        from ( --subconsulta que extrae las esperas ordenados por fecha_matricula e id si coincide
        select id_matricula from matricula
        where id_edicion = var_edicion
        and estado = 'ESPERA'
        order by fecha_matricula asc, id_matricula asc
        ) where rownum = 1; --selecciono la columna 1 del resultado de la consulta
        --esta consulta ahora pasará a ser la confirmada
        update matricula
        set estado = 'CONFIRMADA'
        where id_matricula = var_espera;
        
        --incremento el contador de plazas ocupadas confirmadas
        update edicion_curso
        set plazas_ocupadas = plazas_ocupadas +1
        where id_edicion = var_edicion;
        
        --en caso que no tengamos lista de espera no ocurre nada
        exception 
            when no_data_found then
            null;--ver si dejar asi o incluir raise.||
    end;
end if;
    commit;
    exception
        when others then
        rollback;
        raise;
end;
/
-------------------------------------------------------------
-- IMPLEMENTAMOS EL REGISTRO DEL PAGO
-------------------------------------------------------------
create or replace procedure registrar_pago(
    p_id_matricula  matricula.id_matricula%type,
    p_importe       pago_matricula.importe%type
) is

-- variables
var_matricula matricula%rowtype;

--DEFINICION DE EXCEPCION empleo Pragma para rest 104 PREGUNTA 3.4
pago_duplicado exception;
pragma exception_init(pago_duplicado, -1);

begin
-- PREGUNTA 3.1 verifico existencia
begin 
    select * into var_matricula
    from matricula 
    where id_matricula = p_id_matricula;

-- PREGUNTA 3.2: solo se pueden pagar matriculas CONFIRMADA. si no -20007
    if var_matricula.estado != 'CONFIRMADA' then
        raise_application_error(-20007, 'Solo se pueden pagar matriculas confirmadas.');
    end if;
-- PREGUNTA 3.3 el importe abonado no coincide con la matricula
--comparo el importe del alumn con el de la matricula actual
if p_importe != var_matricula.importe then
    raise_application_error(-20008, 'El importe abonado no coincide con la matricula');
end if;
--PREGUNTA 3.4; no se puede registrar mas de un pago por matrícula, en ese caso -20009
    --intento registrar el pago, dejamos que se lance error y capturamos (evito defensiva)
    insert into pago_matricula(id_pago, id_matricula, fecha_pago, importe)
    values (seq_pago.nextval, p_id_matricula, sysdate, p_importe);
    
    commit; --en caso de que todo vaya bien
    
EXCEPTION
    when no_data_found then
        rollback;
        raise_application_error(-20005, 'Matricula inexistente.');
    when pago_duplicado then
        rollback;
        raise_application_error(-20009, 'La matricula ya ha sido abonada.');
        
    when others then
        rollback;
        raise;
    end;
end;
/

create or replace procedure reset_seq(p_seq_name varchar2)
is
    l_val number;
begin
    execute immediate
        'select ' || p_seq_name || '.nextval from dual'
        into l_val;

    execute immediate
        'alter sequence ' || p_seq_name || ' increment by -' || l_val || ' minvalue 0';

    execute immediate
        'select ' || p_seq_name || '.nextval from dual'
        into l_val;

    execute immediate
        'alter sequence ' || p_seq_name || ' increment by 1 minvalue 0';
end;
/

create or replace procedure inicializa_test is
begin
    reset_seq('seq_curso');
    reset_seq('seq_edicion');
    reset_seq('seq_matricula');
    reset_seq('seq_pago');

    delete from pago_matricula;
    delete from matricula;
    delete from edicion_curso;
    delete from curso;
    delete from alumno;

    insert into alumno values ('11111111A', 'Ana',   'Lopez',    'Martin',  'ana@ubu.es');
    insert into alumno values ('22222222B', 'Bruno', 'Perez',    'Santos',  'bruno@ubu.es');
    insert into alumno values ('33333333C', 'Carla', 'Ruiz',     'Mora',    'carla@ubu.es');
    insert into alumno values ('44444444D', 'Diego', 'Alonso',   'Gil',     'diego@ubu.es');
    insert into alumno values ('55555555E', 'Elena', 'Serrano',  'Vega',    'elena@ubu.es');

    insert into curso values (seq_curso.nextval, 'Bases de Datos',   30, 120);
    insert into curso values (seq_curso.nextval, 'PL/SQL Avanzado',  40, 180);
    insert into curso values (seq_curso.nextval, 'DevOps',           25, 150);

    insert into edicion_curso values (seq_edicion.nextval, 1, date '2026-04-01', date '2026-05-15', 2, 1, 'ABIERTA');
    insert into edicion_curso values (seq_edicion.nextval, 2, date '2026-04-10', date '2026-06-10', 1, 1, 'ABIERTA');
    insert into edicion_curso values (seq_edicion.nextval, 3, date '2026-05-01', date '2026-06-01', 2, 0, 'CERRADA');
    insert into edicion_curso values (seq_edicion.nextval, 2, date '2026-06-15', date '2026-07-30', 2, 2, 'ABIERTA');

    insert into matricula values (seq_matricula.nextval, '11111111A', 1, date '2026-02-01', 'CONFIRMADA', 120);
    insert into matricula values (seq_matricula.nextval, '22222222B', 2, date '2026-02-02', 'CONFIRMADA', 180);
    insert into matricula values (seq_matricula.nextval, '33333333C', 2, date '2026-02-03', 'ESPERA',     180);
    insert into matricula values (seq_matricula.nextval, '44444444D', 3, date '2026-02-04', 'CANCELADA',  150);
    insert into matricula values (seq_matricula.nextval, '44444444D', 4, date '2026-02-05', 'CONFIRMADA', 180);
    insert into matricula values (seq_matricula.nextval, '55555555E', 4, date '2026-02-06', 'CONFIRMADA', 180);
    insert into matricula values (seq_matricula.nextval, '33333333C', 4, date '2026-02-07', 'ESPERA',     180);

    insert into pago_matricula values (seq_pago.nextval, 1, date '2026-02-10', 120);
    insert into pago_matricula values (seq_pago.nextval, 2, date '2026-02-11', 180);

    commit;
end;
/

exec inicializa_test;

--------------------------------------------------------------
--      IMPLEMENTACION DE LOS TESES         --
--------------------------------------------------------------
--se reinicia la bd antes de cada test para garantizar indep de pruebas.
--en cada uno de los teses llamo a inicializa_test;
--1. alumno inexistente

create or replace procedure ejecucion_teses is 
begin 
    dbms_output.put_line('-- Inicio Teses --');
    
/*TEST 1:Alumno inexistente. -20001
llamo a un alumno que no existe en tabla alumno*/
inicializa_test;
    begin 
        matricular_alumno('12345678F', 1);
        dbms_output.put_line('Fallo T1: alumno inexistente');
    exception  --vemos si la excepcion se lanza correctamente
        when others then
        if sqlcode = -20001 then dbms_output.put_line('OK: T1 alumno inexistente correcto test');
        else dbms_output.put_line('FALLO: T1 codigo inesperado ' || sqlcode); 
        end if;
    end;

/* TEST 2: Edicion inexistente -20002
tendremos alumno existente con edicion inexistente (ejemplo 1313)
*/
inicializa_test;
begin 
    matricular_alumno('11111111A', 1313);
    dbms_output.put_line('Fallo T2: la edicion no existe');
exception 
    when others then 
    if sqlcode = -20002 then dbms_output.put_line('OK: T2 edicion inexistente test');
    else dbms_output.put_line('FALLO: T2 codigo inesperado ' || sqlcode);
    end if;
end;
    
/* TEST 3: Edicion Cerrada: -20003
edicion 3 estará cerrada
*/
inicializa_test;
begin 
    matricular_alumno('11111111A', 3); -- edicion cerrada
    dbms_output.put_line('Fallo T3: la edicion está cerrada');
exception 
    when others then 
    if sqlcode = -20003 then dbms_output.put_line('OK: T3 edicion cerrada test');
    else dbms_output.put_line('FALLO: T3 codigo inesperado ' || sqlcode);
    end if;
end;    
/* TEST 4: duplicidad de matricula activa -20004
alumno con matricula confirmada en edicion anterior, al matricular nuevamente debe de fallar
*/

inicializa_test;
begin 
    matricular_alumno('11111111A', 1);
    dbms_output.put_line('Fallo T4: la matricula esta duplicada');
exception 
    when others then 
    if sqlcode = -20004 then dbms_output.put_line('OK: T4 Matricula duplicada test');
    else dbms_output.put_line('FALLO: T4 codigo inesperado ' || sqlcode);
    end if;
end;

/* TEST 5: Matricula correcta con plazas disponibles

*/
inicializa_test;
declare  --empleo 2 variables para el estado de las matriculas y de las plazas 
    var_est matricula.estado%type; --declaro una variable que contiene el estado actual de la matricula
    plazas edicion_curso.plazas_ocupadas%type; --plazas ocupadas
begin 
    matricular_alumno('33333333C', 1);
    
    -- comprobamos que se haya registrado correctamente y que corresponda con la edicion
    select estado into var_est
    from matricula
    where dni_alumno = '33333333C' and id_edicion = 1 and estado = 'CONFIRMADA';
    -- comprobamos las plazas disponibles de la edicion correspondiente a la matricula
    SELECT plazas_ocupadas into plazas
    from edicion_curso where id_edicion=1;
    --si esta confirmada y hay mas de 2 plazas entonces bien  
    if var_est = 'CONFIRMADA' and plazas = 2 then
        dbms_output.put_line('OK: T5 Matricula confirmada test');
    else
        dbms_output.put_line('Fallo T5: matricula sin plazas disp.');
    end if;
end;

/*TEST 6: Matricula en lista de espera
empleo la misma logica que el anterior, solo compruebo estados de espera
*/

inicializa_test;
declare  --empleo 2 variables para el estado de las matriculas y de las plazas 
    var_est matricula.estado%type; --declaro una variable que contiene el estado actual de la matricula
    plazas edicion_curso.plazas_ocupadas%type; --plazas ocupadas
begin 
    matricular_alumno('11111111A', 2);
    
    -- comprobamos que se haya registrado correctamente y que corresponda con la edicion
    select estado into var_est
    from matricula
    where dni_alumno = '11111111A' and id_edicion = 2 and estado = 'ESPERA';
    -- comprobamos las plazas disponibles de la edicion correspondiente a la matricula
    SELECT plazas_ocupadas into plazas
    from edicion_curso where id_edicion=2;
    --la cantidad de plaas no deberia de variar
    if var_est = 'ESPERA' and plazas = 1 then
        dbms_output.put_line('OK: T6 Matricula en lista de espera test');
    else
        dbms_output.put_line('Fallo T6: matricula no está en lista de espera');
    end if;
end;

/*TEST 7: Cancelacion inexistente -20005
empleo misma logica que con los primeros 4 teses
*/
inicializa_test;
begin 
    cancelar_matricula(1234);
    dbms_output.put_line('Fallo T7: la matricula a cancelar es inexistente');
exception 
    when others then 
    if sqlcode = -20005 then dbms_output.put_line('OK: T7 Matricula inexistente test');
    else dbms_output.put_line('FALLO: T7 codigo inesperado ' || sqlcode);
    end if;
end;

/*TEST 8: Matricula ya cancelada -20006
la matricula id 4 ya está cancelada en test
implementamos misma logica 
*/
inicializa_test;
begin 
    cancelar_matricula(4);
    dbms_output.put_line('Fallo T8: la matricula a cancelar ya está cancelada');
exception 
    when others then 
    if sqlcode = -20006 then dbms_output.put_line('OK: T8 Matricula cancelada test');
    else dbms_output.put_line('FALLO: T8 codigo inesperado ' || sqlcode);
    end if;
end;

/*TEST 9: Cancelación de matrícula confirmada con promoción automática desde espera.
cancelacion de bruno (id 2) el cual esta confirmado
promocion de Carla (id 3) la cual esta en espera --> a confirmada.
*/

inicializa_test;
declare
    var_est matricula.estado%type; --recojo el estado de la matricula
begin 
    cancelar_matricula(2); --cancelo la matricula 2
    -- busco el estado actual de la matricula 3 ahora que he cancelado la 2
    select estado into var_est
    from matricula
    where id_matricula = 3;
    --verifico
    if var_est = 'CONFIRMADA' then 
        dbms_output.put_line('OK: T9 Matricula cancelada y promovida test');
        else dbms_output.put_line('FALLO: T9 codigo inesperado no promoción');
    end if;
end;

/*TEST 10: Cancelación de matrícula en espera.
cancelamos a id 3 (carla) la cual esta en espera
las plazas ocupadas se mantienen.
*/
inicializa_test;
declare  --empleo 2 variables para el estado de las matriculas y de las plazas 
    var_est matricula.estado%type; --declaro una variable que contiene el estado actual de la matricula
    plazas edicion_curso.plazas_ocupadas%type; --plazas ocupadas
begin 
    cancelar_matricula(3);
    
    --recogemos sus datos
    select estado into var_est
    from matricula where id_matricula = 3;
    -- comprobamos las plazas disponibles de la edicion correspondiente a la matricula
    SELECT plazas_ocupadas into plazas
    from edicion_curso where id_edicion=2;
    --la cantidad de plaas no deberia de variar
    if var_est = 'CANCELADA' and plazas = 1 then
        dbms_output.put_line('OK: T10 Cancelar matricula en espera sin tocar plazas_ocupadas test');
    else
        dbms_output.put_line('Fallo T10: el estado o numero de plazas es incorrecto.');
    end if;
end;

/*TEST 11: Pago sobre matricula inexistente -20005
*/
inicializa_test;
begin 
    registrar_pago(1234, 120);
    dbms_output.put_line('Fallo T11: pago a matricula inexistente');
exception 
    when others then 
    if sqlcode = -20005 then dbms_output.put_line('OK: T11 Pago a matricula inexistente test');
    else dbms_output.put_line('FALLO: T11 codigo inesperado ' || sqlcode);
    end if;
end;

/*TEST 12: Pago sobre matricula no confirmada -20007
la matricula con id:3 se mantiene en espera y solo se acepta el pago de CONFIRMADAS
*/
inicializa_test;
begin 
    registrar_pago(3, 200);
    dbms_output.put_line('Fallo T12: pago a matricula no confirmada');
exception 
    when others then 
    if sqlcode = -20007 then dbms_output.put_line('OK: T12 Pago a matricula no confirmada test');
    else dbms_output.put_line('FALLO: T12 codigo inesperado ' || sqlcode);
    end if;
end;



/*TEST 13: importe con pago incorrecto -20008
en este caso el id 5 tiene la matricula confirmada pero el importe sera incorrecto
*/
inicializa_test;
begin 
    registrar_pago(5, 55); 
    dbms_output.put_line('Fallo T13: Monto incorrecto');
exception 
    when others then 
    if sqlcode = -20008 then dbms_output.put_line('OK: T13 monto incorrecto test');
    else dbms_output.put_line('FALLO: T13 codigo inesperado ' || sqlcode);
    end if;
end;


/*TEST 14: Pago duplicado -20009
el id 1 ya tiene un pago registrado en datos iniciales, tento pagar de nuevo
*/
inicializa_test;
begin 
    registrar_pago(1, 120); --importante monto exacto si no -20008
    dbms_output.put_line('Fallo T14: pago duplicado');
exception 
    when others then 
    if sqlcode = -20009 then dbms_output.put_line('OK: T14 Pago duplicado test');
    else dbms_output.put_line('FALLO: T14 codigo inesperado ' || sqlcode);
    end if;
end;

/*TEST 15: Caso completo correcto con verificación final del estado de las tablas afectadas.
1. Matriculo alumno.utilizo al alumno 2 BRUNO porque es confirmado para posteriormente pagar mat
2. cancelar matricula. cancelamos a id 1 ana para que pase a cancelada y verificamos
3. pagar matricula. de bruno por ejemplo
4, verificacion de tablas.
*/

inicializa_test;
declare --declaro variables por cada caso
    var_id_bruno     matricula.id_matricula%type; --matricula bruno
    var_estado_bruno matricula.estado%type; --matricula estado bruno
    var_estado_ana   matricula.estado%type; --matricula estado ana (CANCELADO)
    var_plazas       edicion_curso.plazas_ocupadas%type; --cant plazas ocupadas
    var_num_pagos    number;
begin
    --1. Matrciular a bruno
    matricular_alumno('22222222B', 1);
    --matriculo en edicion 1 donde tiene 2 cupos, pasa a ser ocupados = 2
    --recupero el id de bruno
    select id_matricula into var_id_bruno
    from matricula
    where dni_alumno = '22222222B' and id_edicion =1;
    
    --2 cancelamos matricula de ana
    cancelar_matricula(1); --de confirmado a cancelada
    
    --3 registramos el pago de bruno con monto exacto
    registrar_pago(var_id_bruno, 120);
    
    -----------------------------
    -- verificacion --
    -----------------------------
    -- recogemos el estado de ana. debe ser CANCELADA
    select estado into var_estado_ana
    from matricula
    where id_matricula = 1;
    
    --recogemos el estado de bruno
    select estado into var_estado_bruno
    from matricula
    where id_matricula = var_id_bruno;
    
    --recogemos el numero de plazas ocupadas de la edicion 1 donde registramos a bruno
    select plazas_ocupadas into var_plazas
    from edicion_curso
    where id_edicion = 1; --de la edicion 1
    
    --recogemos el num de pagos por registrar el pago de bruno
    select count(*) into var_num_pagos
    from pago_matricula
    where id_matricula = var_id_bruno;
    
    --verificaciones
    if var_estado_ana = 'CANCELADA'
        and var_estado_bruno = 'CONFIRMADA'
        and var_plazas = 1
        and var_num_pagos = 1 
        then
            dbms_output.put_line('OK: T15 Caso completo correcto test');
        else --muestro los posibles datos erroneos (visibilidad de datos)
        dbms_output.put_line('FALLO: t15 error en datos salida. estado_ana=' ||var_estado_ana||
        ' estado_bruno='||var_estado_bruno||' plazas=' ||var_plazas||' pagos='||var_num_pagos);
    end if;
 end;
    dbms_output.put_line('-- Fin Teses --');
end;
/
-- para que se muestren las salidas.
set serveroutput on;
exec ejecucion_teses;
