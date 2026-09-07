#️ Ecosistemas de persistencia: PL/SQL, JDBC & JPA

Repositorio que reúne implementaciones prácticas del ciclo de persistencia de datos en el entorno empresarial Java y bases de datos relacionales. Abarca desde la lógica pura en el motor relacional hasta el mapeo objeto-relacional de alto nivel.

---

## Tecnologías y Conceptos Clave

* **PL/SQL (Procedural Language/SQL):** Lenguaje procedimental propio de Oracle que extiende SQL. Permite ejecutar transacciones críticas, procedimientos y funciones directamente en el motor de base de datos, reduciendo el tráfico de red y garantizando control de concurrencia pesimista.
* **JDBC (Java Database Connectivity):** API estándar de bajo nivel en Java para la conexión con bases de datos relacionales. Permite el control directo de transacciones atómicas (`commit`/`rollback`), optimización de consultas vía `PreparedStatement` y gestión manual de recursos de conexión.
* **JPA (Jakarta Persistence / Hibernate):** Especificación estándar de mapeo objeto-relacional (ORM) en Java. Abstrae la manipulación de tablas mediante entidades fuertemente tipadas, gestión de claves compuestas, relaciones complejas y persistencia declarativa a través del `EntityManager`.

---

## Proyectos Incluidos

### 1. ⚡ [PL/SQL] — Transacciones y Concurrencia en Base de Datos
* **Caso de uso:** Gestión de matrículas de cursos formativos y control de reservas en instalaciones deportivas.
* **Aspectos técnicos:** Bloqueo a nivel de fila (`SELECT ... FOR UPDATE`), promoción automática en listas de espera, uso de cursores explícitos y transformación de errores del motor mediante `PRAGMA EXCEPTION_INIT`.

### 2. 🔌 [JDBC] — Capa de Persistencia y Transaccionalidad Manual
* **Caso de uso:** Sistema de gestión y liquidación económica de perfiles en proyectos tecnológicos.
* **Aspectos técnicos:** Control de ciclo de vida transaccional (`setAutoCommit(false)`), prevención de inyecciones SQL mediante `PreparedStatement`, cálculos monetarios precisos con `BigDecimal` y borrados relacionales en cascada orquestados manualmente.

### 3. 🍽️ [JPA / Hibernate] — Dominio y Patrón Generic DAO
* **Caso de uso:** Plataforma de gestión de catering, menús, bonos de clientes y compras.
* **Aspectos técnicos:** Claves primarias compuestas (`@EmbeddedId`), objetos de valor (`@Embeddable`), consultas tipadas en JPQL y aislamiento de persistencia mediante arquitectura multicapa y el patrón Generic DAO.