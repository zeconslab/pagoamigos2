# Pago Amigos - Sistema de Gestión de Pagos

Sistema seguro de gestión de pagos entre amigos con autenticación basada en roles.

## 🔒 Seguridad Implementada

### Medidas de Protección
- ✅ **Autenticación robusta** con Spring Security 6
- ✅ **BCrypt con 12 rounds** para hash de contraseñas
- ✅ **CSRF Protection** habilitado por defecto
- ✅ **Content Security Policy (CSP)** configurado
- ✅ **Protección contra Clickjacking** (X-Frame-Options: DENY)
- ✅ **Session Management** seguro con cookies HttpOnly, Secure y SameSite
- ✅ **Límite de sesiones concurrentes** por usuario
- ✅ **Validación de entrada** con Bean Validation
- ✅ **Logging seguro** sin exponer información sensible
- ✅ **Manejo de excepciones** centralizado
- ✅ **Roles y permisos** granulares

### Validaciones de Contraseña
Las contraseñas deben cumplir:
- Mínimo 8 caracteres
- Al menos una mayúscula
- Al menos una minúscula
- Al menos un número
- Al menos un carácter especial (@#$%^&+=)

## 📋 Requisitos Previos

- Java 17 o superior
- MySQL 8.0 o superior
- Maven 3.6+

## 🚀 Configuración e Instalación

### 1. Configurar Base de Datos

Crear la base de datos en MySQL:

```sql
CREATE DATABASE pagoamigos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configurar Variables de Entorno

Para producción, configura las siguientes variables de entorno:

```bash
# Windows PowerShell
$env:DB_USERNAME="tu_usuario_db"
$env:DB_PASSWORD="tu_password_db"

# Linux/Mac
export DB_USERNAME="tu_usuario_db"
export DB_PASSWORD="tu_password_db"
```

### 3. Compilar y Ejecutar

```bash
# Compilar el proyecto
.\mvnw.cmd clean package -DskipTests

# Ejecutar la aplicación
.\mvnw.cmd spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

## 👥 Usuarios de Prueba

El sistema crea automáticamente dos usuarios de prueba:

### Usuario Validador
- **Email:** `validator@pagoamigos.com`
- **Contraseña:** `Validator123!`
- **Rol:** VALIDATOR
- **Permisos:** Revisar y aprobar solicitudes

### Usuario Solicitante
- **Email:** `solicitante@pagoamigos.com`
- **Contraseña:** `Solicitante123!`
- **Rol:** SOLICITANTE
- **Permisos:** Crear y consultar solicitudes

## 🛡️ Buenas Prácticas de Seguridad

### Para Desarrollo
1. Nunca commitear credenciales en el código
2. Usar variables de entorno para información sensible
3. Mantener dependencias actualizadas
4. Revisar logs regularmente

### Para Producción
1. Cambiar contraseñas de prueba
2. Habilitar SSL/TLS (HTTPS)
3. Configurar `server.servlet.session.cookie.secure=true`
4. Usar base de datos con SSL
5. Implementar rate limiting
6. Configurar firewall y WAF
7. Realizar auditorías de seguridad periódicas
8. Implementar monitoreo y alertas

## 📁 Estructura del Proyecto

```
src/main/java/com/examplo/pagoamigos/
├── config/              # Configuración (Seguridad, Datos iniciales)
├── controller/          # Controladores MVC
├── dto/                 # Data Transfer Objects
├── exception/           # Manejo global de excepciones
├── model/               # Entidades JPA
├── repository/          # Repositorios Spring Data
├── security/            # Clases de seguridad
└── service/             # Lógica de negocio
```

## 🔧 Configuración de application.properties

El archivo `application.properties` incluye:
- Configuración de base de datos con SSL
- Configuración JPA/Hibernate
- Seguridad de sesiones
- Configuración de cookies seguras
- Logging apropiado

## 🧪 Testing

```bash
# Ejecutar tests
.\mvnw.cmd test
```

## 📝 Endpoints Principales

- `GET /login` - Página de inicio de sesión
- `POST /login` - Autenticación
- `GET /dashboard` - Panel principal (requiere autenticación)
- `POST /logout` - Cerrar sesión

## ⚠️ Notas Importantes

1. **Base de Datos:** Asegúrate de que MySQL esté corriendo antes de iniciar la aplicación
2. **Primer Inicio:** Los datos de prueba se cargan automáticamente
3. **Producción:** Desactiva `spring.jpa.hibernate.ddl-auto=update` y usa Flyway/Liquibase
4. **HTTPS:** En producción, configura certificados SSL válidos

## 🐛 Troubleshooting

### Error: "No compiler is provided"
- Solución: Instalar JDK 17+ y configurar JAVA_HOME

### Error de conexión a MySQL
- Verificar que MySQL esté corriendo
- Comprobar usuario y contraseña en variables de entorno
- Verificar que la base de datos exista

### Error 403 (Forbidden)
- Verificar que el token CSRF esté presente en formularios
- Confirmar permisos de rol para el endpoint

## 📚 Tecnologías Utilizadas

- Spring Boot 4.0.1
- Spring Security 6
- Spring Data JPA
- Thymeleaf
- MySQL
- Lombok
- Bean Validation

## 📞 Soporte

Para reportar problemas o solicitar ayuda, contacta al equipo de desarrollo.

---

**Versión:** 0.0.1-SNAPSHOT  
**Última actualización:** Enero 2026

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

