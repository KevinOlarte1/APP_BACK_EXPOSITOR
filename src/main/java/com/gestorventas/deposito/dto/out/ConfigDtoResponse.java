package com.gestorventas.deposito.dto.out;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigDtoResponse {
    private Integer iva;
    private Integer descuento;
    private Integer grupoMax;
}
