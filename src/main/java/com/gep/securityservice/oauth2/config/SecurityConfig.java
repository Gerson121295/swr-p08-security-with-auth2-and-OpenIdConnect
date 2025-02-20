package com.gep.securityservice.oauth2.config;


import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration //indica que una clase establece metodos de configuracion
@EnableWebSecurity //habilitar la seguridad web
@EnableMethodSecurity(prePostEnabled = true) //habilitad los metodos de seguridad para definir roles/permisos en los metodos de los controladores
public class SecurityConfig {

    @Autowired
    private RsaKeysConfig rsaKeysConfig; //inyecta la clase RsaKeysConfig que se encarga de obtener la clave pública y privada definidas en application.properties

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Este método proporciona una instancia de AuthenticationManager, que es el componente central en Spring Security para manejar la autenticación de usuarios.
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager(); //getAuthenticationManager() devuelve la instancia configurada de AuthenticationManager. Esto permite que Spring Security maneje la autenticación con los métodos configurados (como JWT o login con usuario/contraseña).
    }

    //UserDetailsService permite cargar la información sobre los usuarios
    //DaoAuthenticationProvider es un proveedor de autenticación que verifica usuarios y claves
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService){
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder); //Cómo se codifican las claves
        authProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(authProvider);
    }


    @Bean
    public UserDetailsService inMemoryUserDetailsManager(){
        return new InMemoryUserDetailsManager(
                User.withUsername("user1").password(passwordEncoder.encode("1234")).authorities("USER").build(),
                User.withUsername("user2").password(passwordEncoder.encode("1234")).authorities("USER").build(),
                User.withUsername("user3").password(passwordEncoder.encode("1234")).authorities("USER","ADMIN").build()
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)//se deshabilita csrf porque spring ya trae config para esta vulnerabilidad
                .authorizeHttpRequests( //configuracion para peticiones HTTP
                        auth -> auth.requestMatchers("/token/**").permitAll()) //Permite el acceso sin autenticación a cualquier endpoint que comience con /token/ (por ejemplo, /token/generate)
                .authorizeHttpRequests( //Configuramos la autorización para las peticiones HTTP
                        (auth) -> auth.anyRequest().authenticated() //cualquier otra ruta esta obligada a que todas las demás peticiones requieran autenticación.
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //Configura el servidor de recursos para usar JWT como mecanismo de autenticación
                //.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))  //Configura Spring Security para que el servidor actúe como un OAuth2 Resource Server y use JWT como mecanismo de autenticación.
                //.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt) //deprecated

                .httpBasic(withDefaults())
                .build();
    }


    //Decodificar y validar tokens JWT entrantes (Usa la clave pública para verificar la firma del token recibido, Si la firma es válida, el token es aceptado)
    @Bean
    JwtDecoder jwtDecoder(){
        //crear un decodificador de tokens JWT (JwtDecoder) que usa la clave pública para verificar la firma de los tokens entrantes.
        return NimbusJwtDecoder.withPublicKey(rsaKeysConfig.publicKey()).build();
    }

    //Genera y firma los tokens JWT salientes (Clave privada y pública)
    @Bean
    JwtEncoder jwtEncoder(){
        //Usa la clave privada para firmar el token antes de enviarlo. Usa la clave pública para construir el objeto JWK, asegurando que el sistema pueda verificar la firma.
        JWK jwk = new RSAKey.Builder(rsaKeysConfig.publicKey()) //Crea un constructor de una clave RSA JWK, iniciando con la clave pública.
                .privateKey(rsaKeysConfig.privateKey())  //Asocia la clave privada con la clave JWK. Se necesita la clave privada para firmar los tokens JWT.
                .build(); //Construye el objeto JWK, que representa el par de claves RSA.

        //Convierte la clave JWK en una fuente inmutable (ImmutableJWKSet) para ser usada por el codificador JWT.
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));

        //Crea un NimbusJwtEncoder que usa la clave privada RSA para firmar tokens JWT antes de enviarlos.
        return new NimbusJwtEncoder(jwkSource);
    }

}

