package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.dto.in.ProductoDto;
import com.gestorventas.deposito.dto.out.ProductoResponseDto;
import com.gestorventas.deposito.services.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Controlador REST para gestionar Productos del sistema
 *
 * Ruta base: /api/productos
 *
 * @author Kevin William Olarte Braun
 */
@RestController
@RequestMapping("/api/producto")
@AllArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Crear un producto
     * @param productoDto datos de entrada para crear los productos.
     * @return Dto Response con la info de ese producto creado.
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo producto", description = "Crea un nuevo producto en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interlo", content = @Content) //TODO: CAMBIAR ESTO, FASE PRUIEBA
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponseDto> add(
            @RequestBody ProductoDto productoDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.add(productoDto.getDescripcion(), productoDto.getPrecio(), productoDto.getIdCategoria()));
    }

    /**
     * Listar todos los productos.
     *
     * @return lista de DTOs con todos los productos
     */
    @GetMapping
    @Operation(summary = "Listar todos los productos", description = "Listar todos los productos")
    @ApiResponse(responseCode = "200", description = "Lista de productos encontrados")
    public ResponseEntity<List<ProductoResponseDto>> getAll(
            @RequestParam(required = false)Long categoriaId
            ) {
        return ResponseEntity.ok(productoService.getAll(categoriaId));
    }

    /**
     * Obtener un producto por su ID.
     * @param id identificador numerico que se usara para buscar
     * @return DTO con los datos guardados visibles.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por su ID", description = "Obtener un producto por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    public ResponseEntity<ProductoResponseDto> get(
            @PathVariable Long id) {
        ProductoResponseDto responseDto = productoService.get(id);
        return responseDto != null ? ResponseEntity.ok(responseDto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/update")
    @Operation(summary = "Actualizar los datos de un producto", description = "Actualiza los datos de un producto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponseDto> update(
            @PathVariable Long id,
            @RequestBody ProductoDto productoDto){
        ProductoResponseDto producto = productoService.update(id,productoDto);
        return producto != null ? ResponseEntity.ok(producto) : ResponseEntity.notFound().build();

    }


    /**
     * Eliminar un producto por su ID.
     * @param id identificador numerico que se usara para buscar
     * @return codigo 204 si se eliminó correctamente
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto por su ID", description = "Eliminar un producto por su ID")
    @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productoService.remove(id);
        return ResponseEntity.noContent().build();
    }

}
