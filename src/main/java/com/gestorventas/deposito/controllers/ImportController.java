package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.config.exceptions.ImportException;
import com.gestorventas.deposito.services.CategoriaService;
import com.gestorventas.deposito.services.ClienteService;
import com.gestorventas.deposito.services.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST encargado de importar información masiva
 * al sistema mediante archivos CSV.
 * <p>
 * Permite importar productos, clientes y categorías,
 * así como eliminar datos relacionados con las importaciones.
 *
 * Ruta base: /api/config/import
 *
 * @author Kevin William Olarte Braun
 */
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
    @Operation(summary = "Importar productos", description = "Importa productos al sistema mediante un archivo CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Productos importados correctamente"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío o error de validación del CSV"),
            @ApiResponse(responseCode = "500", description = "Error interno procesando el archivo")
    })
    public ResponseEntity<?> importarProductos(@RequestParam("file") MultipartFile file) {
        ResponseEntity<?> error = validarArchivo(file);
        if (error != null) return error;

        try {
            int insertados = productoService.importarCsvProductos(file);
            return ResponseEntity.ok("Productos importados: " + insertados);

        } catch (ImportException e){
            return ResponseEntity.badRequest().body(e.getImportErrorResponseDto());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error procesando CSV: " + e.getMessage());
        }
    }

    @PostMapping(
            value = "/clientes",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Importar clientes", description = "Importa clientes al sistema mediante un archivo CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clientes importados correctamente"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío o error de validación del CSV"),
            @ApiResponse(responseCode = "500", description = "Error interno procesando el archivo")
    })
    public ResponseEntity<?> importarClientes(@RequestParam("file") MultipartFile file) {
        ResponseEntity<?> error = validarArchivo(file);
        if (error != null) return error;

        try {
            int insertados = clienteService.importarCsvClientes(file);
            return ResponseEntity.ok("Clientes importados: " + insertados);
        } catch (ImportException e){
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getImportErrorResponseDto());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando CSV: " + e.getMessage());
        }
    }

    @PostMapping(
                value = "/categorias",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Importar categorías", description = "Importa categorías al sistema mediante un archivo CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorías importadas correctamente"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío o error de validación del CSV"),
            @ApiResponse(responseCode = "500", description = "Error interno procesando el archivo")
    })
    public ResponseEntity<?> importarCategorias(@RequestParam("file") MultipartFile file) {
        ResponseEntity<?> error = validarArchivo(file);
        if (error != null) return error;

        try {
            int insertados = categoriaService.importarCsvCategorias(file);
            return ResponseEntity.ok("Categorías importadas: " + insertados);
        } catch (ImportException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getImportErrorResponseDto());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando CSV: " + e.getMessage());
        }
    }

    /**
     * Valida que el archivo recibido exista y tenga contenido.
     *
     * @param file archivo recibido en la petición
     * @return respuesta de error si el archivo no es válido, o {@code null} si es correcto
     */
    private ResponseEntity<?> validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("El archivo CSV está vacío");
        }
        return null;
    }
}
