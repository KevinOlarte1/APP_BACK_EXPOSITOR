package com.gestorventas.deposito.models;

import com.gestorventas.deposito.models.producto.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @ManyToOne
    @JoinColumn(name = "fk_pedido", nullable = false)
    private Pedido pedido;

    /**
     * Producto elegido en esta línea.
     */
    @ManyToOne
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
    @Column(nullable = false)
    private Double precio;

    /**Grupo perteneciente */
    @Column(nullable = true)
    private Integer grupo;

    public LineaPedido() {}

}
