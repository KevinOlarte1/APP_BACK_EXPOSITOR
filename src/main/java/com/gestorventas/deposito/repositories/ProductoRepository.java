package com.gestorventas.deposito.repositories;

import com.gestorventas.deposito.models.producto.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar entidades {@link producto}
 * <p>
 *     Permite realizar operaciones CRUD.
 * </p>
 * @author Kevin William Olarte Braun.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    Producto findById(long id);

    @Query("""
    SELECT COALESCE(SUM(lp.cantidad), 0)
    FROM LineaPedido lp
    JOIN lp.pedido p
    JOIN p.cliente c
    WHERE c.vendedor.id = :idVendedor
      AND lp.producto.categoria.id = :categoria
""")
    Long findVentasPorCategoria(Long idVendedor, Long categoria);



}
