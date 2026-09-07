# Technology Project Management System — JDBC Transactional Layer

Implementación de la capa de acceso a datos y gestión transaccional en **Java** mediante la API estándar de **JDBC**. 

El módulo resuelve operaciones empresariales críticas para la asignación de perfiles técnicos, liquidación de costes de proyectos, borrados dependientes en cascada manual y consultas complejas con múltiples uniones (`JOIN`), todo ello bajo un control atómico estricto con `commit()` y `rollback()`.

> **Nota sobre el alcance del repositorio:**  
> Con el fin de respetar los derechos de propiedad intelectual del entorno docente y mantener la síntesis del repositorio, se omiten intencionadamente los esquemas de bases de datos completos, las utilidades del *pool* de conexiones (`PoolDeConexiones.java`) y los esqueletos de clases. En este repositorio se expone exclusivamente la clase central con la **lógica transaccional, control de recursos y batería de pruebas** desarrollados por mi (`GestionProyectosTecnologia.java`).

## Estructura del proyecto
```text
├── sql/
│   ├── gestion_proyectos_tecnologia.sql  # Script DDL/DML (creación de tablas y datos semilla)
│   └── lanza_sqlplus.sh                  # Script bash de apoyo para ejecución vía SQL*Plus
│
├── src/
│   └── lsi/
│       ├── enunciado/                    # Definición de excepciones de negocio (GestionProyectosTecnologiaException)
│       │
│       ├── solucion/                     # Implementación central desarrollada por el autor
│       │   ├── GestionProyectosTecnologia.java  # Lógica transaccional JDBC y suite de pruebas unitarias
│       │   └── Misc.java                        # Clases y métodos auxiliares de soporte
│       │
│       └── util/                         # Infraestructura y utilidades de base de datos
│           ├── exceptions/oracle/        # Mapeo y tratamiento de errores específicos de Oracle
│           ├── SGBDError.java            # Constantes y códigos de error del gestor
│           ├── SGBDErrorUtil.java        # Métodos de utilidad para análisis de SQLExceptions
│           ├── EscribeBindings.java      # Utilidad de formateo y depuración de parámetros SQL (?)
│           ├── ExecuteScript.java        # Ejecutor automático de scripts SQL desde Java
│           └── PoolDeConexiones.java     # Gestión del pool de conexiones y caché de sentencias
│
├── log4j.properties                      # Configuración de trazas y nivel de logger (SLF4J / Log4j)
├── .classpath                            # Configuración de dependencias y User Library de Eclipse
└── .project                              # Descriptor de proyecto de Eclipse IDE