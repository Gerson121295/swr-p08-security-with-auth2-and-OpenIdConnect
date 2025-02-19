package com.gep.securityservice.oauth2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;


//Vinculamos propiedades de configuracion con campos de la clase RsaKeysConfig
//La clase RsaKeysConfig se encarga de obtener la clave pública y privada definidas en application.properties y almacenarlas en las variables publicKey y privateKey.
//Luego, estas claves pueden ser accedidas desde otras clases que necesiten usarlas, por ejemplo, para firmar o verificar tokens JWT, realizar cifrado y descifrado de datos, entre otros usos.

@ConfigurationProperties(prefix = "rsa") //esta clase obtendrá valores de configuración cuyo prefijo sea "rsa" definido en application.properties
//record define una clase inmutable con dos campos (publicKey y privateKey) que representan las claves RSA, que serán inyectadas desde la configuración.
public record RsaKeysConfig(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
}
