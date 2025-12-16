package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.dto.out.ConfigDtoResponse;
import com.gestorventas.deposito.repositories.ParametrosGlobalesRepository;
import com.gestorventas.deposito.services.ParametrosGlobalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ParametrosGlobalesController {
    private final ParametrosGlobalesService globalesService;

    @GetMapping
    public ResponseEntity<ConfigDtoResponse> getGlobales(){
            return ResponseEntity.ok(globalesService.getAll());
    }
    @GetMapping("/iva")
    public Integer iva() {
        return globalesService.getIva();
    }

    @GetMapping("/descuento")
    public Integer descuento() {
        return globalesService.getDescuento();
    }

     @GetMapping("/grupoMax")
    public Integer grupo() {
        return globalesService.getGrupoMax();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> set(@RequestBody HashMap<String, Integer> map) {

        globalesService.set(map.get("iva"), map.get("descuento"), map.get("grupoMax"));
        return ResponseEntity.ok().build();
    }

}
