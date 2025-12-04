package com.gestorventas.deposito.dto.out;

import com.gestorventas.deposito.models.producto.Producto;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de salida para representar los datos públicos de un producto del sistema.
 * <p>
 * Esta clase se utiliza para devolver información básica del pedido en las respuestas
 * de la API, sin incluir detalles.
 * </p>
 *
 * @author Kevin William Olarte Braun
 */
@Getter
@Setter
public class ProductoResponseDto {
    private Long id;
    private String descripcion;
    private Double precio;
    private Long idCategoria;
    private String categoria;

    public ProductoResponseDto(Producto producto) {
        this.id = producto.getId();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.idCategoria = producto.getCategoria().getId();
        this.categoria = producto.getCategoria().getNombre();
    }
}
