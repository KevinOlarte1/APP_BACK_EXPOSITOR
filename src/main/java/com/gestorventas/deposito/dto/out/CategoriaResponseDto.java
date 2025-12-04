package com.gestorventas.deposito.dto.out;

import com.gestorventas.deposito.models.producto.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaResponseDto {
    private Long id;
    private String nombre;

    public CategoriaResponseDto(Categoria categoria) {
        this.id = categoria.getId();
        this.nombre = categoria.getNombre();
    }
}
