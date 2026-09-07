# Soluciones de Base de Datos y Programación Transaccional en PL/SQL

Repositorio que reúne implementaciones en **Oracle PL/SQL** centradas en la resolución de problemas empresariales mediante lógica en base de datos. 

Los módulos abordan el control de concurrencia pesimista, la integridad relacional estricta, la gestión integral del ciclo de vida transaccional (`COMMIT` / `ROLLBACK`), el uso de cursores y el mapeo de excepciones del sistema a reglas funcionales.

---

## Módulos del Repositorio

| **`01_Pistas/`** | Motor de asignación horaria y anulación de pistas deportivas con verificación de aforo en tiempo real. | Cursores explícitos (`OPEN`, `FETCH`, `CLOSE`, `%NOTFOUND`), `SQL%ROWCOUNT`, normalización temporal con `TRUNC()`. |
| **`02_Matriculas/`** | Sistema integral de matriculación en cursos, gestión de colas de espera automáticas y liquidación de pagos. | `SELECT ... FOR UPDATE`, bloqueo pesimista, `PRAGMA EXCEPTION_INIT`, promoción FIFO, `RAISE_APPLICATION_ERROR`. |

## Resumen de Contenido

### 1. Gestión Transaccional de Matrículas y Cursos (`01_Gestion_Matriculas`)
Implementa las transacciones críticas para la administración de plazas en ediciones formativas:
* **Matriculación Concurrente:** Bloqueo a nivel de fila (`FOR UPDATE`) sobre las ediciones para garantizar que dos transacciones paralelas no generen sobrecupo (*overbooking*).
* **Gestión Atómica de Espera:** Promoción cronológica automática de alumnos en lista de espera al liberarse una plaza confirmada mediante cancelación.
* **Integridad de Pagos:** Registro de abonos con control de duplicidad mediante la captura del error `ORA-00001` (violación de clave única) con `PRAGMA EXCEPTION_INIT`, transformándolo en un código de negocio limpio (`-20009`).

### 2. Control de Reservas e Instalaciones Deportivas (`02_Reserva_Pistas`)
Gestiona la ocupación y disponibilidad de pistas por franjas horarias:
* **Asignación mediante Cursores Explícitos:** Consulta de pistas libres con subconsultas `NOT IN`, iterando y validando la disponibilidad mediante `%NOTFOUND`.
* **Anulación Segura y Control de Impacto:** Eliminación parametrizada por socio y franja horaria, validando el éxito de la transacción mediante `SQL%ROWCOUNT` antes de consolidar con `COMMIT` o deshacer con `ROLLBACK`.
* **Tratamiento de Fechas:** Uso de `TRUNC()` para omitir componentes residuales de tiempo y comparar únicamente año, mes y día de forma precisa.

---

## Validación y Pruebas

Ambos proyectos incluyen scripts de pruebas unitarias automatizadas que garantizan la consistencia:
1. Reinician las tuplas a un estado conocido antes de cada comprobación para mantener la idempotencia.
2. Simulan accesos concurrentes, desbordamiento de límites máximos y peticiones con datos inexistentes.
3. Validan los códigos de salida y el estado final de las tablas frente al resultado esperado.

---

## Tecnologías Utilizadas

* **Gestor de Base de Datos:** Oracle Database 19c / 21c (XE o Enterprise)
* **Lenguaje Procedural:** PL/SQL
* **Herramientas de Desarrollo:** Oracle SQL Developer / SQL*Plus 