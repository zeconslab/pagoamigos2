# Guía de Configuración de Seguridad - Compra Amigos

## 1. Configurar Variables de Entorno (OBLIGATORIO)

### En Windows PowerShell (Sesión actual):
```powershell
# Establecer la contraseña de la base de datos
$env:DB_PASSWORD = "tu_contraseña_segura_aqui"

# Opcional: cambiar el usuario si no es 'sa'
$env:DB_USERNAME = "sa"

# Verificar que se estableció correctamente
echo $env:DB_PASSWORD
```

### En Windows PowerShell (Permanente - Usuario actual):
```powershell
# Agregar permanentemente para el usuario actual
[System.Environment]::SetEnvironmentVariable('DB_PASSWORD', 'tu_contraseña_segura_aqui', 'User')
[System.Environment]::SetEnvironmentVariable('DB_USERNAME', 'sa', 'User')

# Reiniciar la terminal para que tome efecto
```

### En Windows CMD:
```cmd
set DB_PASSWORD=tu_contraseña_segura_aqui
set DB_USERNAME=sa
```

## 2. Verificar SQL Server

Asegúrate de que SQL Server esté corriendo y la base de datos existe:

```powershell
# Verificar si SQL Server está corriendo
Get-Service -Name "MSSQL*"

# Si necesitas iniciarlo
Start-Service -Name "MSSQLSERVER"
```

## 3. Ejecutar la Aplicación

```powershell
# Compilar y ejecutar
mvn clean spring-boot:run
```

## 4. Credenciales de Prueba

Después de que la aplicación inicie, usa estas credenciales:

### Usuario Validador:
- **Email**: validator@pagoamigos.com
- **Contraseña**: Validator123!

### Usuario Solicitante:
- **Email**: solicitante@pagoamigos.com
- **Contraseña**: Solicitante123!

## 5. Configuraciones de Seguridad Implementadas

### ✅ Protecciones Activas:

1. **CSRF Protection**: Tokens en cookies para prevenir ataques CSRF
2. **Password Encryption**: BCrypt con 12 rounds
3. **Session Security**: 
   - Timeout: 30 minutos
   - Cookies HttpOnly, Secure, SameSite=Strict
   - Nombre personalizado: PAGOAMIGOS_SESSION

4. **Headers de Seguridad**:
   - Content Security Policy (CSP)
   - HSTS (HTTP Strict Transport Security) - 1 año
   - X-Frame-Options: DENY
   - Referrer-Policy
   - Permissions-Policy

5. **Validaciones de Entrada**:
   - Email: formato válido, único, máx 100 caracteres
   - Nombres: solo letras, 2-50 caracteres
   - Teléfono: exactamente 10 dígitos
   - Previene SQL Injection y XSS

6. **Control de Acceso**:
   - Autenticación basada en roles (VALIDATOR, SOLICITANTE)
   - Sesiones limitadas a 1 por usuario
   - Rutas protegidas por rol

## 6. Antes de Ir a Producción

### Cambiar en `application.properties`:

```properties
# PRODUCCIÓN - Cambiar estas configuraciones:

# Deshabilitar DevTools
spring.devtools.restart.enabled=false
spring.devtools.livereload.enabled=false

# Habilitar cache
spring.thymeleaf.cache=true
spring.resources.cache.period=31536000

# SQL Server con certificado válido
spring.datasource.url=jdbc:sqlserver://tu-servidor:1433;databaseName=pagoamigo;encrypt=true;trustServerCertificate=false

# Cambiar a 'validate' o 'none' (NUNCA 'update' en producción)
spring.jpa.hibernate.ddl-auto=validate

# Deshabilitar SQL logging
spring.jpa.show-sql=false

# Logging mínimo
logging.level.root=WARN
logging.level.com.examplo.pagoamigos=INFO
logging.level.org.springframework.security=WARN
```

### Variables de Entorno Requeridas en Producción:

```bash
DB_USERNAME=usuario_produccion
DB_PASSWORD=contraseña_super_segura_larga_y_compleja
```

## 7. Troubleshooting

### Error: "Cannot create a session after the response has been committed"
✅ Ya corregido con CookieCsrfTokenRepository

### Error: "Forbidden (403)"
- Verifica que el usuario tenga el rol correcto (VALIDATOR o SOLICITANTE)
- Limpia las cookies del navegador

### Error: "Connection refused to SQL Server"
```powershell
# Verificar servicio SQL Server
Get-Service -Name "MSSQL*"

# Verificar conexión
Test-NetConnection -ComputerName localhost -Port 1433
```

### Error: "DB_PASSWORD is not set"
```powershell
# Establecer temporalmente
$env:DB_PASSWORD = "tu_contraseña"

# Luego ejecutar
mvn spring-boot:run
```

## 8. Mejoras Futuras Recomendadas

1. **Rate Limiting**: Implementar Bucket4j para limitar intentos de login
2. **Auditoría**: Registrar todos los accesos y cambios
3. **2FA**: Autenticación de dos factores para usuarios
4. **Captcha**: En el formulario de login después de varios intentos fallidos
5. **Password Policy**: Forzar cambio periódico de contraseñas
6. **Backup**: Automatizar backups de la base de datos

## 9. Contacto y Soporte

Para dudas sobre seguridad o configuración, consulta la documentación de Spring Security:
https://docs.spring.io/spring-security/reference/index.html
