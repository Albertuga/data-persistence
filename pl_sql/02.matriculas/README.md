# Sistema de Gestión de Matrículas y Cursos en PL/SQL

Módulo transaccional implementado en **Oracle PL/SQL** para la administración del ciclo de vida de matrículas, listas de espera automáticas y cobros en ediciones de cursos formativos. 

El proyecto enfatiza el control estricto de **concurrencia pesimista** (`SELECT ... FOR UPDATE`), integridad referencial mediante restricciones del motor y gestión avanzada de excepciones funcionales y del sistema (`RAISE_APPLICATION_ERROR` y `PRAGMA EXCEPTION_INIT`).

---

##️ Modelo de Datos

El esquema relacional está normalizado y se compone de 5 entidades principales
1> Curso
2> Alumno
3> Edicion
4> Matrícula
5> Pago_Matricula

Procedimientos y Lógica Transaccional

1. matricular_alumno(p_dni_alumno, p_id_edicion)
Gestiona el alta de un estudiante en una convocatoria abierta.
cancelar_matricula(p_id_matricula).

2. cancelar_matricula(p_id_matricula) 
Revoca una matrícula activa y gestiona la cola de espera de manera atómica.

3. registrar_pago(p_id_matricula, p_importe)
Formaliza la liquidación económica de una inscripción.

### Implemento algunas excepciones y teses
Estos teses validan la consistencia transaccional ejecutando un reinicio de esquema (inicializa_test) previo a cada escenario

Inserción con alumno / edición inexistente (captura -20001 y -20002).

Matriculación en edición cerrada (-20003).

Detección de doble inscripción activa para un mismo DNI (-20004).

Asignación correcta de plaza libre (CONFIRMADA) y transición a cola (ESPERA) al agotar aforo.

Control de estados en cancelaciones (inexistente -20005, duplicada -20006).

Promoción automática de lista de espera respetando orden cronológico e identificador.

Validaciones de cobro: estado de matrícula inválido (-20007), importe dispar (-20008) y pago duplicado (-20009).

Escenario integral End-to-End con verificación de consistencia en todas las tablas afectadas.

