package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.dto.in.CategoriaDto;
import com.gestorventas.deposito.dto.out.CategoriaResponseDto;
import com.gestorventas.deposito.dto.out.ProductoResponseDto;
import com.gestorventas.deposito.models.producto.Categoria;
import com.gestorventas.deposito.services.CategoriaService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/categoria")
@AllArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Crear una nueva categoria en el sistema.
     * @param categoria datos de entrada para crear la categoria
     * @return Dto Response con la info de ese producto creado.
     */
    @PostMapping
    @Operation(summary = "Crear una nueva categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria creada correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno")

    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponseDto> crearCategoria(
            @RequestBody CategoriaDto categoria
    ){
       return ResponseEntity.status(HttpStatus.CREATED)
               .body(categoriaService.add(categoria.getNombre()));
    }

    /**
     * Obtener todas las categorias registradas.
     * @return Listado de categorias.
     */
    @GetMapping
    @Operation(summary = "Listat todas las categortias")
    @ApiResponse(responseCode = "200", description = "Listado encontrado")
    public ResponseEntity<List<CategoriaResponseDto>> getAll(){
        return ResponseEntity.ok(categoriaService.getAll());
    }

    /**
     * Obtener una categoria por su id.
     * @param id identificador numerico que se usara para buscar
     * @return DTO con los datos guardados visibles.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoria por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria no encontrada", content = @Content)
    })
    public ResponseEntity<CategoriaResponseDto> get(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(categoriaService.get(id));
    }

    /**
     * Actualizar los datos de una categoria existente.
     * @param id identificador numerico que se usara para buscar
     * @param categoriaDto datos actualizados
     * @return DTO con los datos guardados visibles.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de una categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actualizacíon realizada"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    public ResponseEntity<CategoriaResponseDto> update(
            @PathVariable Long id,
            @RequestBody CategoriaDto categoriaDto
    ){
        return ResponseEntity.ok(categoriaService.update(id, categoriaDto.getNombre()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Borrar una categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria eliminada"),
            @ApiResponse(responseCode = "404", description = "Categoria no encontrada", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ){
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
