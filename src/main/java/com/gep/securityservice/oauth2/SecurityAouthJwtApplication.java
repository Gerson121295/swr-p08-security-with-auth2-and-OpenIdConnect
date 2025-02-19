package com.gep.securityservice.oauth2;

import com.gep.securityservice.oauth2.config.RsaKeysConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
//Habilita la carga de propiedades de claves RSA
@EnableConfigurationProperties(RsaKeysConfig.class)
public class SecurityAouthJwtApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityAouthJwtApplication.class, args);
	}

	//Bean para codificar las claves
	@Bean
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

}
