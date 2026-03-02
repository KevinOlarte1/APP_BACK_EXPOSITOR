package com.gestorventas.deposito.dto.out;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportErrorResponseDto {
    private String id;
    private String nombre;

    public ImportErrorResponseDto(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}
