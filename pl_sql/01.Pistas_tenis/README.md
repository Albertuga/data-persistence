# 🎾 Sistema Transaccional de Reserva de Pistas en PL/SQL

Módulo de lógica de base de datos implementado en **Oracle PL/SQL** para la gestión de reservas y cancelaciones de pistas deportivas. 

El proyecto aborda el control transaccional explícito (`COMMIT` / `ROLLBACK`), el uso de **cursores explícitos** con verificación de estados (`%NOTFOUND`), el manejo de atributos del sistema (`SQL%ROWCOUNT`) y la normalización temporal mediante funciones de truncado de fecha (`TRUNC`).
___
## Funciones Implementadas

1. reservarPista(p_socio, p_fecha, p_hora) RETURN INTEGER
Busca y asigna la primera pista libre para una fecha y franja horaria determinadas

2. anularReserva(p_socio, p_fecha, p_hora, p_pista) RETURN INTEGER
Cancela una reserva existente asegurando la identidad del solicitante.

