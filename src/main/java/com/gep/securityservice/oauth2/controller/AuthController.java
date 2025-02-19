package com.gep.securityservice.oauth2.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AuthController {


    //Inyecta: JwtEncoder //Genera y firma los tokens JWT salientes (Clave privada y pública)
    @Autowired
    private JwtEncoder jwtEncoder;

    // Inyecta JwtDecoder para decodificar y validar tokens JWT entrantes (Usa la clave pública para verificar la firma del token recibido, Si la firma es válida, el token es aceptado)
    @Autowired
    private JwtDecoder jwtDecoder;

    //Este método proporciona una instancia de AuthenticationManager, que es el componente central en Spring Security para manejar la autenticación de usuarios.
    @Autowired
    private AuthenticationManager authenticationManager;

    //Se inyecta UserDetailsService, que es un servicio de Spring Security para cargar los detalles de un usuario desde la DB o cualquier otra fuente.
    @Autowired
    private UserDetailsService userDetailsService;


    //Método generarToken genera un JWT para autenticar usuarios en la API. Soporta dos tipos de autenticación:
    // "password" → Usa usuario y contraseña para autenticar.
    // "refreshToken" → Usa un token de actualización para obtener un nuevo token de acceso.
    //Devuelve un ResponseEntity con un accessToken y opcionalmente un refreshToken si withRefreshToken es true.
    @PostMapping("/token")
    public ResponseEntity<Map<String,String>> generarToken( //recibe los siguientes parametros
            String grantType, //grantType → Define si es autenticación por contraseña o refreshToken
            String username, String password, //username y password → Se usan si grantType es "password".
            boolean withRefreshToken, // Si es true, genera un refresh token.
            String refreshToken){ // Se usa si grantType es "refreshToken"

        String subject = null; // → Almacena el usuario autenticado
        String scope = null;  //→ Guarda los roles/permisos del usuario.

        // Si grantType es "password", autentica al usuario con username y password.
        if(grantType.equals("password")){
            //Autenticamos al usuario con sus datos
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username,password)
            );

            //Obtenemos los datos del usuario autenticado
            subject = authentication.getName(); // Obtiene el nombre del usuario
            scope = authentication.getAuthorities() // Obtiene los roles/permisos del usuario
                    .stream().map(aut -> aut.getAuthority())
                    .collect(Collectors.joining(" "));
        }

        // Autenticación con Refresh Token
        // Si grantType es "refreshToken", verifica que el refreshToken no sea null
        else if(grantType.equals("refreshToken")){
            if (refreshToken == null){
                return new ResponseEntity<>(Map.of("errorMessage","El refresh token es requerido"), HttpStatus.UNAUTHORIZED);
            }

            //crea una variable decodeJWT de tipo Jwt y la inicializa en null para guardar el token decodificado
            Jwt decodeJWT = null;

            try{
                //Extraemos información del refresh token
                decodeJWT = jwtDecoder.decode(refreshToken);

                //Si falla, devuelve un error 401 UNAUTHORIZED.
            }catch (JwtException exception){
                return new ResponseEntity<>(Map.of("errorMessage",exception.getMessage()), HttpStatus.UNAUTHORIZED);
            }

            // Extrae el usuario (subject) del refreshToken.
            subject = decodeJWT.getSubject(); //→ Obtiene el nombre de usuario al que pertenece el refreshToken, se guarda en la variable subject

            // Usa userDetailsService.loadUserByUsername(subject) para buscar al usuario en la base de datos o en el sistema de autenticación.
            //userDetailsService es un servicio de Spring Security que carga los detalles del usuario a partir de su nombre de usuario (subject).
            //userDetails contiene la información del usuario autenticado, como su nombre, contraseña y permisos (roles).
            UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
            //userDetails.getAuthorities() obtiene los permisos del usuario. y devuelve una coleccion de GrantedAuthority
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities(); //Los permisos pueden ser, por ejemplo: "ROLE_ADMIN", "ROLE_USER", etc

            // Convierte la lista de permisos (authorities) en una cadena de texto.
            scope = authorities
                    .stream().map(auth -> auth.getAuthority()) //→ Extrae solo los nombres de los permisos.
                    .collect(Collectors.joining(" ")); // Une todos los permisos en una sola cadena separada por espacios. scope almacenara: "ROLE_ADMIN ROLE_USER"
        }

        // Creación del Access Token
        Map<String,String> idToken = new HashMap<>(); //crea un mapa de clave(String) y valor(String) llamado idToken
        Instant instant = Instant.now(); //Obtiene la fecha y hora actual

        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder() //Crea un objeto JwtClaimsSet con los datos del usuario autenticado
                .subject(subject) //Establece el sujeto del token
                .issuedAt(instant) //Establece la fecha y hora de emisión del token
                .expiresAt(instant.plus(withRefreshToken?1:5,ChronoUnit.MINUTES)) //Establece la fecha y hora de expiración del token. Que es 1 minuto si 'withRefreshToken' es true o 5 minutos si es false
                .issuer("security-service") //Establece el emisor del token
                .claim("scope",scope) //Establece el alcance del token
                .build();//Construye el objeto JwtClaimsSet

        // Genera el token JWT y lo agrega al mapa idToken.
        String jwtAccessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue(); //Genera el token JWT
        //Agrega el accessToken al mapa idToken: clave es "accessToken" y valor es jwtAccessToken
        idToken.put("accessToken",jwtAccessToken);

        //Creación del Refresh Token (Opcional)
        if(withRefreshToken){ //Si withRefreshToken es true, genera un refresh token
            JwtClaimsSet jwtClaimsSetRefresh = JwtClaimsSet.builder()
                    .subject(subject)
                    .issuedAt(instant)
                    .expiresAt(instant.plus(5, ChronoUnit.MINUTES)) //El token de actualización expira en 5 minutos
                    .issuer("security-service")
                    .claim("scope",scope)
                    .build();

            //Genera el token JWT y lo agrega al mapa idToken.
            String jwtRefreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSetRefresh)).getTokenValue();
            idToken.put("refreshToken",jwtRefreshToken); //Agrega el refreshToken al mapa idToken: clave es "refreshToken" y valor es jwtRefreshToken
        }

        //Devuelve una respuesta 200 OK con los tokens generados.
        return new ResponseEntity<>(idToken, HttpStatus.OK);
    }

    //Para prueba en postman: Clic Authorization y seleccionar Basic Auth: Agregar username:user1 y password: 1234
    // luego Seleccionar la peticion GET: http://localhost:8080 y enviar - Muestra el mensaje "Data Test"

    //Para generar el token:  Clic Authorization y seleccionar Basic Auth: Agregar username:user1 y password: 1234
    // luego: GET: http://localhost:8080/token y enviar - Genera el token de acceso


    /* Para generar el token:  ->  POST: http://localhost:8080/token
       -→ EN Postman clic en Authorization -> Seleccionar Basic Auth -> Agregar username:user1 y password: 1234
       -> Clic en Body y seleccionar x-www-form-urlencoded o form-data  luego agregar los siguientes parametros:
         en la columna KEY: grantType y en VALUE: password     o  refreshToken
            en la columna KEY: username y en VALUE: user1
            en la columna KEY: password y en VALUE: 1234
            en la columna KEY: withRefreshToken y en VALUE: true    o   fase

            //NOTA: si en grantType se coloca refreshToken: se debe agregar el refreshToken generado en el campo refreshToken: agregar otra
            // columna Key: refreshToken y en VALUE: "pegar el refreshToken generado"
         -> Enviar la petición generará un token de acceso y un token de actualización.
    */

    /* Para realizar la peticion a un recurso - Acceso Role: user: GET: http://localhost:8080/dataTest
       -→ EN Postman clic en Authorization -> Seleccionar Bearer Token -> Agregar el token de acceso generado luego de autenticarse
       -> Enviar la petición y se mostrará el mensaje "Data Test" junto con el nombre de usuario y los roles/permisos del usuario actual.
    */


    /* Realizar peticion POST - acceso Role: admin.  -> http://localhost:8080/saveData
         -→ EN Postman clic en Authorization -> Seleccionar Bearer Token -> Agregar el token de acceso generado luego de autenticarse
         -> Clic en Body y seleccionar x-www-form-urlencoded o form-data  luego agregar los siguientes parametros:
            en la columna KEY: data y en VALUE: "Data to save"
         Enviar la petición y se mostrará el mensaje "Data Saved" si el usuario tiene el rol/permiso de administrador.
     */

}

