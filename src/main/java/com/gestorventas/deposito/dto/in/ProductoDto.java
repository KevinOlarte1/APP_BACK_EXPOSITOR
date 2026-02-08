package com.gestorventas.deposito.dto.in;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Objeto de transferencia de datos (DTO) utilizado para crear o actualizar Vendedores.
 * <p>
 * Contiene los campos necesarios para registrar un nuevo Vendedor en el sistema.
 * No debe contener lógica de negocio ni anotaciones de persistencia.
 * </p>
 * @author Kevin William Olarte Braun
 */
@Getter
@Setter
public class ProductoDto {
    private String descripcion;
    private BigDecimal precio;
    private Long idCategoria;
}
