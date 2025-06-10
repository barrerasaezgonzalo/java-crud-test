# CRUD de Productos con Spring Boot y MySQL

Este proyecto es una aplicación CRUD simple (creada para aprender Spring Boot) para gestión de productos, construida con Spring Boot y algunas de estas herramientas:
 
Lombok  
JPA  
WebFlux  
Base de datos MySql  
Cache Redis  
JWT Auth  
Jacoco Coverage  
SLF4J Logs  
Arquitectura Hexagonal

### 🛠 Para instalar MySQL (usando los valores por defecto: usuario `root` y clave en blanco), puedes usar:

```bash
brew install mysql
brew services start mysql
```

### Luego, accede a MySQL con:

```bash
mysql -u root -p
```
### Dentro del cliente de MySQL, crea la base de datos ejecutando:

```sql
CREATE DATABASE productos;
```

### Para ejecutar la aplicación, usa:
```bash
./gradlew bootRun
```

## Creación de usuario para pruebas

Para probar el endpoint de creación de usuarios, primero crea un usuario en la base de datos MySQL manualmente (tabla users).

```sql
INSERT INTO users (username, password) VALUES (
  'tester',
  '$2b$12$UTZyNwmJg5h4xYR/G8WfIO4cKBVuSm6FVej5rRrLIP/P6MjpFqd/.'
);
```
Una vez creado el usuario en la base de datos , puedes autenticarte haciendo un `POST` a:
POST /users/login con el body


```json
{
  "username": "tester",
  "password": "test123"
}
```

### La documentación de la API está disponible en:  
[Documentación API Swagger](http://localhost:8080/swagger-ui/index.html)

### Tests

Ejecuta los tests con reporte:   

```bash
./gradlew clean test jacocoTestReport
```