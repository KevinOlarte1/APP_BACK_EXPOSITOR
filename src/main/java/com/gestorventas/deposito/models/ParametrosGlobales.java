package com.gestorventas.deposito.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.service.annotation.GetExchange;

@Getter
@Setter
@Entity
@Table(name = "parametros_globales")
public class ParametrosGlobales {

    @Id
    private Long id = 1L;

    private Integer iva;
    private Integer descuento;
    private Integer grupoMax;
}
