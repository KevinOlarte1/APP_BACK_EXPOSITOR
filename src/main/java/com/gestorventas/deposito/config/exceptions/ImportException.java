package com.gestorventas.deposito.config.exceptions;

import java.io.IOException;
import java.util.List;

public class ImportException extends IOException {
    private final List<Long> idErrors;
    public ImportException(List<Long> idErrors) {
        this.idErrors = idErrors;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Los siguientes productos no se pudieron importar: \n");
        for(Long x: idErrors){
            sb.append(x).append("; ");
        }
        return sb.toString();
    }
}
