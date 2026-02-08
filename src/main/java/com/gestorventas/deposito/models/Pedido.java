package com.gestorventas.deposito.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Fecha y hora del pedido.
     */
    @Column(nullable = false)
    private LocalDate fecha;

    /**
     * Cliente que realizó el pedido.
     */
    @ManyToOne
    @JoinColumn(name = "fk_cliente", nullable = false)
    private Cliente cliente;

    private boolean finalizado;


    /**
     * Líneas de pedido asociadas.
     */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.LAZY)
    private Set<LineaPedido> lineas = new LinkedHashSet<>();

    /**
     * Total de la compra.
     */
    @Column(nullable = false)
    private int descuento;

    /**
     * IVA aplicado al pedido.
     */
    @Column(nullable = false)
    private int iva;

    /**
     * Total bruto del pedido.
     */
    @Column(nullable = false)
    private BigDecimal brutoTotal;


    public Pedido(int descuento, int iva){
        this.fecha = LocalDate.now();
        this.finalizado = false;
        this.descuento = descuento;
        this.iva = iva;
        this.brutoTotal = BigDecimal.ZERO;
    }

    public Pedido(){
        this.fecha = LocalDate.now();
        this.finalizado = false;
        this.descuento = 0;
        this.iva = 0;
        this.brutoTotal = BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineaPedido other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
