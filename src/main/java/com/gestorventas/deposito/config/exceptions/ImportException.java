package com.gestorventas.deposito.config.exceptions;

import com.gestorventas.deposito.dto.out.ImportErrorResponseDto;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Getter
public class ImportException extends IOException {

    private final ImportErrorResponseDto importErrorResponseDto;
    public ImportException(ImportErrorResponseDto importErrorResponseDto) {
        this.importErrorResponseDto = importErrorResponseDto;
    }


}
