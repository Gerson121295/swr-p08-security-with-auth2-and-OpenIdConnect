package com.gep.securityservice.oauth2;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

//Forma 1 de genera un par de claves RSA (pública y privada) por medio de consola
/*
#Crear clave mediante consola (Admin) o powerShell tener instalado: choco install openssl -y   y ubicarse en la ruta del proyecto y ejecutar:
 openssl genrsa -out keypair.pem 2048                    //keypair es el archivo generado que contiene la clave privada
 openssl rsa -in keypair.pem -pubout -out public.pem     //con pubout se extrae la clave publica de keypair.pem -genera public.pem
 openssl pkcs8 -topk8 -inform PEM -nocrypt -in keypair.pem -out private.pem  //convierte la clave privada a formato PKCS8 y guarda en private.pem archivo generado
*/

//Luego de generar las Claves RSA (publica y privada) se crea una carpeta en resources llamada certs en la cual se guarda lor archivos generados(pri.pem y pub.pem) y en application.properties se agrega la ruta de estos archivos

//Forma 2 de generar las claves RSA (pública y privada) usando la clase KeyPairGenerator y las guarda en archivos en formato PEM.
public class GenerateKeyPair { //Ejecutar esta clase dentro tiene main. Permitirá generar las claves RSA (pública y privada)

    //Método principal que puede lanzar excepciones relacionadas con algoritmos no soportados y errores de entrada/salida.
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA"); //Crea un generador de pares de claves para el algoritmo RSA.
        var keyPair = keyPairGenerator.generateKeyPair(); //Genera el par de claves (pública y privada).

        //Obtiene las claves en su forma codificada como un arreglo de bytes.
        byte[] pub = keyPair.getPublic().getEncoded();
        byte[] pri = keyPair.getPrivate().getEncoded();

        /* Creacion de clave Publica */
        //Crea un PemWriter para escribir en un archivo llamado pub.pem.
        PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream("pub.pem")));
        //Crea un objeto PEM con la clave pública.
        PemObject pemObject = new PemObject("PUBLIC KEY",pub);
        //Escribe la clave pública en el archivo pub.pem.
        pemWriter.writeObject(pemObject);
        //Cierra el escritor para liberar recursos.
        pemWriter.close();

        /* Creacion de clave Privada */
        //Crea un PemWriter para escribir en un archivo llamado pri.pem.
        PemWriter pemWriter2 = new PemWriter(new OutputStreamWriter(new FileOutputStream("pri.pem")));
        //Crea un objeto PEM con la clave privada.
        PemObject pemObject2 = new PemObject("PRIVATE KEY",pri);
        //Escribe la clave privada en el archivo.
        pemWriter2.writeObject(pemObject2);
        //Cierra el escritor para liberar recursos.
        pemWriter2.close();
    }
}


/*  //Para esta implementación se requiere agregar la dependencia de Bouncy Castle en el archivo pom.xml
<!-- https://mvnrepository.com/artifact/org.bouncycastle/bcpkix-jdk18on -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcpkix-jdk18on</artifactId>
    <version>1.80</version>
</dependency>
 */