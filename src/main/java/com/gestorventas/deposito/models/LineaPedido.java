package com.gestorventas.deposito.models;

import com.gestorventas.deposito.models.producto.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "lineas_pedido")
@Getter
@Setter
public class LineaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Pedido al que pertenece esta línea.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_pedido", nullable = false)
    private Pedido pedido;

    /**
     * Producto elegido en esta línea.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_producto", nullable = false)
    private Producto producto;

    /**
     * Cantidad de producto en esta línea.
     */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * Precio aplicado en esta línea (puede diferir del precio base).
     */
    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio = BigDecimal.ZERO;


    /**Grupo perteneciente */
    @Column(nullable = true)
    private Integer grupo;

    @Column(nullable = true)
    private Integer stockFinal;

    public LineaPedido() {}

}
