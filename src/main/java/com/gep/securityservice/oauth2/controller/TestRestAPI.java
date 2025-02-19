package com.gep.securityservice.oauth2.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestRestAPI {

    //@GetMapping
    //public Map<String, Object> dataTest(){ return Map.of("message", "Data Test");}

    @GetMapping("/dataTest")
    @PreAuthorize("hasAuthority('SCOPE_USER')") //define el rol/permiso necesario para acceder al recurso (SCOPE_USER)
    public Map<String, Object> dataTest(Authentication authentication){
        return Map.of(
                "message", "Data Test",
                "username", authentication.getName(), //usuario autenticado
                "authorities", authentication.getAuthorities()  //roles/permisos del usuario
        );
    }

    @PostMapping("/saveData")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')") //solo usuarios con roles/permisos de administrador pueden acceder a este recurso
    public Map<String,Object> saveData(String data){
        return Map.of("dataSaved",data);
    }

}
