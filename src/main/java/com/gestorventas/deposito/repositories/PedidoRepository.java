package com.gestorventas.deposito.repositories;

import com.gestorventas.deposito.interfaces.ProductoCount;
import com.gestorventas.deposito.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder y gestionar entidades {@link Pedido}
 * <p>
 *     Permite realizar operaciones CRUD
 * </p>
 * @author Kevin William Olarte Braun.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido,Long>, JpaSpecificationExecutor<Pedido> {

    public Pedido findById(long id);
    // 🔹 Estadística global (todos los vendedores)
    @Query("""
           SELECT EXTRACT(YEAR FROM p.fecha), SUM(lp.precio * lp.cantidad)
           FROM Pedido p JOIN p.lineas lp
           GROUP BY EXTRACT(YEAR FROM p.fecha)
           ORDER BY EXTRACT(YEAR FROM p.fecha)
           """)
    List<Object[]> getEstadisticaGlobal(); // WHERE p.finalizado = true

    // Estadística por vendedor
    @Query("""
           SELECT EXTRACT(YEAR FROM p.fecha), ROUND(SUM(lp.precio * lp.cantidad),2)
           FROM Pedido p JOIN p.lineas lp
           WHERE p.cliente.vendedor.id = :idVendedor
           GROUP BY EXTRACT(YEAR FROM p.fecha)
           ORDER BY EXTRACT(YEAR FROM p.fecha)
           """)
    List<Object[]> getEstadisticaPorVendedor(@Param("idVendedor") Long idVendedor);

    // Estadística por cliente
    @Query("""
           SELECT EXTRACT(YEAR FROM p.fecha), ROUND(SUM(lp.precio * lp.cantidad),2)
           FROM Pedido p JOIN p.lineas lp
           WHERE p.finalizado = false AND p.cliente.id = :idCliente
           GROUP BY EXTRACT(YEAR FROM p.fecha)
           ORDER BY EXTRACT(YEAR FROM p.fecha)
           """)
    List<Object[]> getEstadisticaPorCliente(@Param("idCliente") Long idCliente);

    // 🔹 Totales por cliente de un vendedor (agrupado por nombre)
    @Query("""
           SELECT EXTRACT(YEAR FROM p.fecha), SUM(lp.precio * lp.cantidad)
           FROM Pedido p JOIN p.lineas lp
           WHERE p.finalizado = true AND p.cliente.vendedor.id = :idVendedor AND p.cliente.id = :idCliente
           GROUP BY EXTRACT(YEAR FROM p.fecha)
           ORDER BY EXTRACT(YEAR FROM p.fecha)
           """)
    List<Object[]> getTotalesPorClientesDeVendedor(@Param("idVendedor") Long idVendedor, @Param("idCliente") Long idCliente);

    @Query("""
            SELECT COUNT(p)
            FROM Pedido p
            JOIN p.cliente c
            WHERE c.vendedor.id = :idVendedor
            AND p.finalizado = true
""")
    long countFinalizadosByVendedor(long idVendedor);

    @Query("""
            SELECT COUNT(p)
            FROM Pedido p
            JOIN p.cliente c
            WHERE c.vendedor.id = :idVendedor
            AND p.finalizado = false
""")
    long countNotFinalizadosByVendedor(long idVendedor);

    @Query("""
            SELECT lp.producto.id as productoId, SUM(lp.cantidad) as total
            FROM Pedido p JOIN p.lineas lp
            WHERE p.cliente.vendedor.id = :idVendedor
            GROUP BY lp.producto.id
            ORDER BY total DESC
    """)
    List<ProductoCount> countTopProductsByVendedor(Long idVendedor);
}
