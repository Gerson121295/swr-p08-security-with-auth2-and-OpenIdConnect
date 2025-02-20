

# Proyecto 08: Seguridad con OAuth2 y OpenID Connect

Este proyecto tiene como objetivo desarrollar una API con seguridad basada en OAuth2 y OpenID Connect. Para ello, se implementará un servidor de autorización OAuth2 con Spring Security.

<div align="center">
   <h1>Challenge | Java | Back End - SpringBoot | Spring Security Oauth2</h1>
</div>


<p align="center">
  <img src="https://img.shields.io/badge/Status-finalizado-blue"><br>
  <img src="https://img.shields.io/badge/Java-17-red">
<img src="https://img.shields.io/badge/Spring-Security-Oauth2-Orange">
  <img src="https://img.shields.io/badge/Versión-1.0-green">
</p>

👨🏻‍💻 <strong>Gerson Escobedo Pérez </strong></br>
<a href="https://www.linkedin.com/in/gerson-escobedo/" target="_blank">
<img src="https://img.shields.io/badge/-LinkedIn-%230077B5?style=for-the-badge&logo=linkedin&logoColor=white" target="_blank"></a>


### Imagen de Requerimientos
![Requerimientos](src/main/resources/img/Dependencias-security-oauth2.jpg)

### Dependencias del Proyecto
![Dependencias](src/main/resources/img/Dependencias-security-bcpkix.jpg)


## 🖥️ Tecnologías utilizadas
- ☕ Java 17
- JPA Hibernate
- [Intellij](https://www.jetbrains.com/idea/)
- [MySql](https://www.mysql.com/)
- [Java](https://www.java.com/en/)

## ⚠️ Importante! ⚠️
☕ Usar Java versión 8 o superior para compatibilidad. </br></br>
📝 Recomiendo usar el editor de Intellij</br></br>

## Instalación

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Gerson121295/SWR-P04-BancaDigital.git
   ```
2. Navegar al directorio del proyecto:
   ```bash
   cd <SWR-P03-Encuestas>
   ```
3. Construir el proyecto con Maven:
   ```bash
   mvn clean install
   ```
4. Ejecutar la aplicación:
   ```bash
   mvn spring-boot:run
   ```

## Endpoints y la forma de acceder
Este proyecto implementa autenticación con JWT para proteger recursos. A continuación, se detallan los pasos para realizar pruebas en Postman, obtener un token de acceso y acceder a recursos protegidos.

## Prueba en Postman
Levantar el proyecto `security-oauth-jwt` ejecutar la clase main.
1. En la pestaña **Authorization**, seleccionar **Basic Auth** e ingresar:
    - **Username:** `user1`
    - **Password:** `1234`
2. Realizar una petición `GET` a:
   ```
   http://localhost:8080
   ```
3. Enviar la petición y se mostrará el mensaje **"Data Test"**.


## Obtener el Token de Acceso

### Método 1: Petición GET

1. En la pestaña **Authorization**, seleccionar **Basic Auth** e ingresar:
    - **Username:** `user1`
    - **Password:** `1234`
2. Realizar una petición `GET` a:
   ```
   http://localhost:8080/token
   ```
3. Enviar la petición y se generará el token de acceso.

### Método 2: Petición POST

1. Realizar una petición `POST` a:
   ```
   http://localhost:8080/token
   ```
2. En la pestaña **Authorization**, seleccionar **Basic Auth** e ingresar:
    - **Username:** `user1`
    - **Password:** `1234`
3. En la pestaña **Body**, seleccionar **x-www-form-urlencoded** o **form-data** y agregar los siguientes parámetros:

   | Key               | Value       |
      |-------------------|------------|
   | grantType        | password o refreshToken |
   | username        | user1       |
   | password        | 1234        |
   | withRefreshToken | true o false |

4. Si en `grantType` se usa `refreshToken`, se debe agregar un campo adicional:

   | Key          | Value                    |
      |-------------|--------------------------|
   | refreshToken | (pegar el refreshToken generado) |

5. Enviar la petición generará un **token de acceso** y un **token de actualización**.

## Acceder a un Recurso Protegido

### Acceso con Rol `user`

1. Realizar una petición `GET` a:
   ```
   http://localhost:8080/dataTest
   ```
2. En la pestaña **Authorization**, seleccionar **Bearer Token** e ingresar el token de acceso generado.
3. Enviar la petición y se mostrará el mensaje **"Data Test"**, junto con el nombre de usuario y los roles/permisos del usuario actual.

### Acceso con Rol `admin` (Guardar Datos)

1. Realizar una petición `POST` a:
   ```
   http://localhost:8080/saveData
   ```
2. En la pestaña **Authorization**, seleccionar **Bearer Token** e ingresar el token de acceso generado.
3. En la pestaña **Body**, seleccionar **x-www-form-urlencoded** o **form-data** y agregar los siguientes parámetros:

   | Key  | Value         |
      |------|--------------|
   | data | "Data to save" |

4. Enviar la petición y se mostrará el mensaje **"Data Saved"** si el usuario tiene el rol/permiso de administrador.


## Notas
- Asegúrate de que el servidor esté activo en `http://localhost:8080` para realizar las peticiones.
- Utiliza herramientas como Postman o cURL para probar los endpoints.
- Los IDs utilizados en los ejemplos son ficticios; reemplázalos por los correspondientes a tu base de datos.

# 💙 Personas Contribuyentes
## Autores
[<img src="https://avatars.githubusercontent.com/u/79103450?v=4" width=115><br><sub>Gerson Escobedo</sub>](https://github.com/gerson121295)

# Licencia
![GitHub](https://img.shields.io/github/license/dropbox/dropbox-sdk-java)

License: [MIT](License.txt)

