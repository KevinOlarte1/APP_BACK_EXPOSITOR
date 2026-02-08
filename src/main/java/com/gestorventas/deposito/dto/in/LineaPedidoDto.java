package com.gestorventas.deposito.dto.in;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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
    private BigDecimal precio;
    private Integer grupo;
    private Integer stockFinal;
}
