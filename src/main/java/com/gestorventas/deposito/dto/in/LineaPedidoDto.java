package com.gestorventas.deposito.dto.in;

import lombok.Getter;
import lombok.Setter;
/**
 * Objeto de transferencia de datos (DTO) utilizado para crear o actualizar Linea de pedidos.
 * <p>
 * Contiene los campos necesarios para registrar un nuevo Vendedor en el sistema.
 * No debe contener lógica de negocio ni anotaciones de persistencia.
 * </p>
 * @author Kevin William Olarte Braun
 */
@Getter
@Setter
public class LineaPedidoDto {
    private Long idProducto;
    private Integer cantidad;
    private Double precio;
    private Integer grupo;
}
