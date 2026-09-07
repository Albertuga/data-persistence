#  Catering Management System — JPA / Hibernate Persistence Layer

Módulo empresarial de persistencia y servicios transaccionales en **Java** implementado con **JPA (Jakarta Persistence / Hibernate)** para la gestión de servicios de catering, bonos de clientes, menús y órdenes de compra compuestas.

El proyecto implementa una arquitectura multicapa desacoplada orientada a la separación de responsabilidades, abstracción de acceso a datos mediante el patrón **DAO genérico**, y orquestación de reglas de negocio en una capa de **servicios transaccionales**.

** NOTA sobre alcance del repositorio**
> Con el fin de preservar los derechos de autor y la confidencialidad de los esqueletos y utilidades base proporcionados por el equipo docente, en este repositorio se exhiben exclusivamente las clases, entidades y métodos implementados por el mi.
---

## Arquitectura del Módulo y Estructura del Repositorio

```text
src/
└── dao/
│   ├── catering/
│   │   ├── BonoClienteDAO.java      # Consultas y persistencia de bonos por cliente
│   │   ├── ClienteDAO.java          # Búsqueda y gestión del ciclo de vida de clientes
│   │   ├── CompraDAO.java           # Gestión de transacciones de compra
│   │   └── MenuDAO.java             # Catálogo de menús y disponibilidades
│   ├── DAO.java                     # Interfaz genérica base (CRUD contract) 
│   └── JpaDAO.java                  # Implementación base abstracta con EntityManager
│
├── model/catering/
│   ├── BonoCliente.java             # Entidad: bonos contratados y saldo disponible
│   ├── Cliente.java                 # Entidad: datos de contacto y fidelización
│   ├── Compra.java                  # Entidad relacional de pedidos/consumos
│   ├── CompraPK.java                # Clave primaria compuesta (@Embeddable / @EmbeddedId)
│   ├── DireccionPostal.java         # Objeto de valor embebido (@Embeddable)
│   └── Menu.java                    # Entidad: catálogo de platos, precios y descripción
│
└── service/
    ├── catering/
    │   ├── IncidentError.java       # Catálogo tipado de errores de negocio
    │   ├── IncidentException.java   # Excepción funcional de capa de servicio
    │   ├── Service.java             # Contrato de operaciones de negocio
    │   └── ServiceImpl.java         # Lógica transaccional de catering y canjes
    │
    ├── PersistenceException.java    # Excepción raíz para fallos de acceso a datos
    ├── PersistenceFactorySingleton.java # Factoría Singleton para EntityManagerFactory
    └── PersistenceService.java      # Utilidad para demarcación de contexto de persistencia
```
	
## Componentes y Patrones de diseño
### Modelo del dominio y Mapeo relacional.
Ocurre en model catering, donde implementamos claves compuestas (@Embeddable/@EmbeddedId o @ IdClass).
	
### Capa de Acceso a datos 
Todo ocurre en dao.catering donde centralizamos las operaciones basicas en una clase parametrizada que iteractua con el EntityManager. A demas es donde ocurren las consultas especificas en cada DAO especializado.
	
### Capa de servicio
service.catering ofrece una orquestacion donde valida las reglas de negocio complejas antes de alterar el estado persistente de la BD. Tambien exponemos el tratamiento de excepciones controladas y la gestion de la factoria  (EntityManagerFactory)	


	
