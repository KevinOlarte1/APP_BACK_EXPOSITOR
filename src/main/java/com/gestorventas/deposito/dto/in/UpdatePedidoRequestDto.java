package com.gestorventas.deposito.dto.in;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class UpdatePedidoRequestDto {
    private LocalDate fecha;
    private Integer descuento;
    private Integer iva;
}
