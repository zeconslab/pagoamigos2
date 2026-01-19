**PagoAmigos — Proyecto de pagos entre amigos**

Descripción
---------

PagoAmigos es una aplicación simple para gestionar pagos entre amigos: un amigo compra algo y los demás le pagan en un calendario (cuotas, recordatorios y registro de pagos).

Objetivo
--------

Permitir a grupos de amigos registrar gastos compartidos, crear calendarios de pagos, y marcar pagos realizados de forma sencilla.

Tecnologías
----------

- **Lenguaje:** Java 21 (se recomienda instalar JDK 21)
- **Framework:** Spring Boot
- **Build:** Maven (se incluye `mvnw` / `mvnw.cmd`)
- **Plantillas / Frontend:** Thymeleaf (templates en `src/main/resources/templates`)
- **Recursos estáticos:** `src/main/resources/static`

Requisitos previos
------------------

- JDK 21 instalado y configurado en `JAVA_HOME`.
- Maven incluido o usar el wrapper `mvnw` / `mvnw.cmd` provisto.

Cómo ejecutar (Windows)
-----------------------

- Usar el wrapper para compilar y ejecutar en modo desarrollo:

```
.\\mvnw.cmd clean package
.\\mvnw.cmd spring-boot:run
```

- Para generar el JAR y ejecutarlo:

```
.\\mvnw.cmd clean package
java -jar target/*.jar
```

Cómo ejecutar (Unix / macOS)
--------------------------

```
./mvnw clean package
./mvnw spring-boot:run
```

Pruebas
------

Ejecutar tests unitarios con:

```
.\\mvnw.cmd test
```

Configuración
-------------

Parámetros de configuración se encuentran en `src/main/resources/application.properties`.

Notas sobre la actualización a Java 21
------------------------------------

- Se ha generado un plan de actualización a Java 21 para este proyecto (análisis detectó Java 17 + Maven). El plan se guardó en `.github/java-upgrade/20260119204722/plan.md`.
- Pasos recomendados: revisar `plan.md`, confirmar el plan y luego ejecutar las acciones automatizadas (instalar JDK 21 en el entorno de desarrollo, actualizar `maven.compiler.source`/`target` si aplica, y ejecutar la compilación y tests).

Contacto
-------

