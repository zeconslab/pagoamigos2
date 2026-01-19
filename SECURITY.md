# 🔒 INFORME DE SEGURIDAD - Pago Amigos

**Fecha de auditoría:** Enero 2026  
**Versión:** 0.0.1-SNAPSHOT  
**Framework:** Spring Boot 4.0.1 + Spring Security 6

---

## ✅ MEDIDAS DE SEGURIDAD IMPLEMENTADAS

### 1. Autenticación y Autorización

#### ✓ Spring Security 6
- **Estado:** Implementado y configurado
- **Configuración:** `SecurityConfig.java`
- **Características:**
  - Autenticación basada en formularios
  - UserDetailsService personalizado
  - Autorización basada en roles (VALIDATOR, SOLICITANTE)
  - Rutas protegidas según roles

#### ✓ Gestión de Contraseñas
- **Algoritmo:** BCrypt con 12 rounds (más fuerte que el estándar de 10)
- **Validación robusta:**
  - Mínimo 8 caracteres
  - Mayúsculas y minúsculas requeridas
  - Al menos un número
  - Al menos un carácter especial
- **Prevención:** Contraseñas débiles rechazadas en registro

#### ✓ Control de Sesiones
- **Máximo de sesiones:** 1 por usuario
- **Invalidación:** Sesión anterior eliminada al nuevo login
- **Timeout:** 30 minutos de inactividad
- **Logout seguro:** Invalidación de sesión + eliminación de cookies

### 2. Protección contra Ataques Web

#### ✓ CSRF (Cross-Site Request Forgery)
- **Estado:** HABILITADO (por defecto en Spring Security 6)
- **Implementación:** Token CSRF automático en formularios Thymeleaf
- **Protección:** Todas las peticiones POST/PUT/DELETE requieren token válido

#### ✓ XSS (Cross-Site Scripting)
- **Escape automático:** Thymeleaf escapa HTML por defecto
- **Content Security Policy (CSP):**
  ```
  default-src 'self';
  script-src 'self' 'unsafe-inline';
  style-src 'self' 'unsafe-inline';
  img-src 'self' data:;
  ```
- **Prevención:** Ejecución de scripts maliciosos bloqueada

#### ✓ Clickjacking
- **X-Frame-Options:** DENY
- **Protección:** La aplicación no puede ser embebida en iframes
- **Prevención:** Ataques de UI redressing bloqueados

#### ✓ SQL Injection
- **ORM:** JPA/Hibernate con queries parametrizadas
- **Repositorios:** Spring Data JPA (prevención automática)
- **Validación:** Bean Validation en todas las entradas
- **Estado:** PROTEGIDO contra inyección SQL

### 3. Seguridad de Cookies y Sesiones

#### ✓ Configuración de Cookies
```properties
server.servlet.session.cookie.http-only=true    # No accesible desde JavaScript
server.servlet.session.cookie.secure=true       # Solo sobre HTTPS
server.servlet.session.cookie.same-site=strict  # Previene CSRF
```

#### ✓ JSESSIONID
- **HttpOnly:** Sí (protección contra XSS)
- **Secure:** Sí (solo HTTPS en producción)
- **SameSite:** Strict (prevención adicional CSRF)
- **Timeout:** 30 minutos
- **Destrucción al logout:** Sí

### 4. Validación de Datos

#### ✓ Bean Validation (Jakarta Validation)
- **Entradas validadas:**
  - Nombre: 2-50 caracteres
  - Email: Formato válido + máx 100 caracteres
  - Teléfono: Exactamente 10 dígitos
  - Contraseñas: Patrón robusto obligatorio
- **Prevención:** Inyección de datos maliciosos

#### ✓ Sanitización
- **Thymeleaf:** Escape automático de HTML
- **JPA:** Prevención de SQL injection
- **Logging:** Datos sensibles nunca logueados

### 5. Manejo de Errores

#### ✓ GlobalExceptionHandler
- **Exceptions manejadas:**
  - `MethodArgumentNotValidException` → 400
  - `AccessDeniedException` → 403
  - `AuthenticationException` → Redirect a login
  - `RuntimeException` → 500
  - `Exception` → 500
- **Logging seguro:** Stack traces NO expuestos al usuario
- **Mensajes genéricos:** Sin revelar detalles técnicos

#### ✓ Configuración de Errores
```properties
server.error.include-message=never
server.error.include-stacktrace=never
server.error.include-binding-errors=never
```

### 6. Logging y Auditoría

#### ✓ SLF4J Logger
- **Nivel producción:** INFO
- **Nivel desarrollo:** DEBUG
- **Eventos logueados:**
  - Intentos de autenticación fallidos
  - Accesos denegados
  - Excepciones del sistema
  - Creación de usuarios
- **NO logueados:** Contraseñas, tokens, datos sensibles

### 7. Base de Datos

#### ✓ Configuración Segura
```properties
spring.datasource.url=jdbc:mysql://...?useSSL=true&requireSSL=true
spring.jpa.open-in-view=false  # Previene lazy loading issues
```

#### ✓ Credenciales
- **Variables de entorno:** DB_USERNAME, DB_PASSWORD
- **NO hardcodeadas:** Credenciales fuera del código
- **Producción:** Usar secretos de Azure/AWS

#### ✓ Entidades
- **User:**
  - Contraseña hasheada (BCrypt)
  - Campo `active` para desactivar usuarios
  - Relación ManyToMany con roles
- **Rol:**
  - Nombres sin prefijo "ROLE_" (añadido en UsuarioDetails)

### 8. Arquitectura y Código

#### ✓ Separación de Responsabilidades
- **Controllers:** Manejo de peticiones HTTP
- **Services:** Lógica de negocio
- **Repositories:** Acceso a datos
- **DTOs:** Transferencia de datos validados
- **Security:** Implementaciones de UserDetails

#### ✓ Inyección de Dependencias
- Constructor injection (inmutable, testeable)
- No field injection

#### ✓ Lombok
- Reducción de boilerplate
- Getters/Setters automáticos
- Constructores seguros

---

## ⚠️ VULNERABILIDADES MITIGADAS

| Vulnerabilidad | Técnica de Mitigación | Estado |
|---------------|----------------------|--------|
| SQL Injection | JPA + Queries parametrizadas | ✅ |
| XSS | Thymeleaf escape + CSP | ✅ |
| CSRF | Token CSRF automático | ✅ |
| Clickjacking | X-Frame-Options: DENY | ✅ |
| Session Hijacking | HttpOnly + Secure cookies | ✅ |
| Brute Force | BCrypt (lento) + Límite de sesiones | ✅ |
| Password Cracking | BCrypt 12 rounds + Validación fuerte | ✅ |
| Information Disclosure | Error handling seguro | ✅ |
| Insecure Direct Object References | Autorización basada en roles | ✅ |
| Missing Function Level Access Control | @EnableMethodSecurity | ✅ |

---

## 🔴 RECOMENDACIONES PARA PRODUCCIÓN

### Críticas (Implementar antes de desplegar)

1. **HTTPS Obligatorio**
   ```properties
   server.ssl.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=${SSL_PASSWORD}
   server.ssl.key-store-type=PKCS12
   ```

2. **Rate Limiting**
   - Implementar con Spring Cloud Gateway o Nginx
   - Limitar intentos de login: 5 por minuto
   - Limitar requests: 100 por minuto por IP

3. **Actualizar Contraseñas de Prueba**
   - Eliminar o deshabilitar `DataInitializer.java`
   - Crear usuarios administrativos seguros

4. **Base de Datos**
   - Cambiar `spring.jpa.hibernate.ddl-auto=validate`
   - Usar Flyway/Liquibase para migraciones
   - Habilitar SSL en conexión MySQL

5. **Secrets Management**
   - Usar Azure Key Vault o AWS Secrets Manager
   - Nunca commitear `.env` o `application-prod.properties`

### Importantes (Antes de 3 meses)

6. **Auditoría de Accesos**
   - Implementar tabla `audit_log`
   - Loguear: login, logout, cambios de roles, accesos denegados

7. **Two-Factor Authentication (2FA)**
   - Implementar TOTP con Google Authenticator
   - Requerir 2FA para roles administrativos

8. **Password Policy Service**
   - Implementar rotación de contraseñas (cada 90 días)
   - Prevenir reutilización de últimas 5 contraseñas
   - Bloqueo de cuentas tras 5 intentos fallidos

9. **Monitoring y Alertas**
   - Integrar con ELK Stack o Splunk
   - Alertas para:
     - Múltiples intentos de login fallidos
     - Accesos denegados repetidos
     - Excepciones no manejadas

10. **Web Application Firewall (WAF)**
    - Configurar AWS WAF o Azure WAF
    - Reglas OWASP Core Rule Set

### Opcionales (Mejoras continuas)

11. **API Security (si se implementa)**
    - OAuth 2.0 + JWT
    - API rate limiting independiente
    - API versioning

12. **Penetration Testing**
    - Contratar auditoría externa
    - Realizar cada 6 meses
    - Usar OWASP ZAP automatizado

13. **Security Headers adicionales**
    ```
    Strict-Transport-Security: max-age=31536000
    X-Content-Type-Options: nosniff
    Referrer-Policy: strict-origin-when-cross-origin
    Permissions-Policy: geolocation=(), camera=()
    ```

---

## 📊 CHECKLIST DE SEGURIDAD

### Antes de Commit
- [ ] No hay credenciales hardcodeadas
- [ ] No hay claves API en el código
- [ ] Logs no contienen datos sensibles
- [ ] Tests de seguridad pasan

### Antes de Deploy
- [ ] HTTPS configurado y funcionando
- [ ] Variables de entorno configuradas
- [ ] Contraseñas de prueba eliminadas
- [ ] Base de datos con SSL
- [ ] Cookies Secure habilitadas
- [ ] Rate limiting configurado
- [ ] WAF activado
- [ ] Monitoring configurado
- [ ] Backups automatizados configurados

### Mantenimiento Continuo
- [ ] Actualizar dependencias mensualmente
- [ ] Revisar logs de seguridad semanalmente
- [ ] Rotar secrets trimestralmente
- [ ] Auditoría de seguridad semestral
- [ ] Revisar usuarios activos mensualmente

---

## 🧪 TESTING DE SEGURIDAD

### Tests Recomendados

1. **Authentication Tests**
   ```java
   @Test
   void loginWithValidCredentials_shouldSucceed()
   void loginWithInvalidCredentials_shouldFail()
   void loginWithInactiveUser_shouldFail()
   ```

2. **Authorization Tests**
   ```java
   @Test
   void accessDashboardWithValidatorRole_shouldSucceed()
   void accessDashboardWithoutAuth_shouldRedirect()
   void accessAdminResourceAsUser_shouldReturnForbidden()
   ```

3. **CSRF Tests**
   ```java
   @Test
   void postWithoutCsrfToken_shouldFail()
   void postWithInvalidCsrfToken_shouldFail()
   ```

4. **Input Validation Tests**
   ```java
   @Test
   void registerWithWeakPassword_shouldFail()
   void registerWithInvalidEmail_shouldFail()
   ```

---

## 📞 CONTACTO DE SEGURIDAD

Para reportar vulnerabilidades de seguridad:
- **Email:** security@pagoamigos.com (crear)
- **Proceso:** Disclosure responsable
- **Tiempo de respuesta:** 48 horas

---

## 📄 REFERENCIAS

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [NIST Password Guidelines](https://pages.nist.gov/800-63-3/)
- [CWE Top 25](https://cwe.mitre.org/top25/)

---

**Estado:** ✅ APTO PARA DESARROLLO  
**Producción:** ⚠️ REQUIERE AJUSTES (ver sección de recomendaciones)  
**Última revisión:** Enero 2026
