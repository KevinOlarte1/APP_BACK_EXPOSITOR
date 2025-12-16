package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.services.CategoriaService;
import com.gestorventas.deposito.services.ClienteService;
import com.gestorventas.deposito.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/config/import")
@RequiredArgsConstructor
public class ImportController {

    private final ProductoService productoService;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;

    @PostMapping(
            value = "/productos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> importarProductos(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo CSV está vacío");
        }

        try {
            int insertados = productoService.importarCsvProductos(file);
            return ResponseEntity.ok("Productos importados: " + insertados);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando CSV: " + e.getMessage());
        }
    }

    @PostMapping(
            value = "/clientes",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> importarClientes(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo CSV está vacío");
        }

        try {
            int insertados = clienteService.importarCsvClientes(file);
            return ResponseEntity.ok("Clientes importados: " + insertados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando CSV: " + e.getMessage());
        }
    }

    @PostMapping(
                value = "/categorias",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> importarCategorias(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo CSV está vacío");
        }
        try {
            int insertados = categoriaService.importarCsvCategorias(file);
            return ResponseEntity.ok("Clientes importados: " + insertados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando CSV: " + e.getMessage());
        }
    }


}
