package com.gestorventas.deposito.repositories;

import com.gestorventas.deposito.interfaces.CategoriaCount;
import com.gestorventas.deposito.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder y gestionar entidades {@link Producto}
 * <p>
 *     Permite realizar operaciones CRUD.
 * </p>
 * @author Kevin William Olarte Braun.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    Producto findById(long id);

    @Query("""
    SELECT lp.producto.categoria AS categoria, SUM(lp.cantidad) AS total
    FROM LineaPedido lp
    JOIN lp.pedido p
    JOIN p.cliente c
    where c.vendedor.id = :idVendedor
    group by lp.producto.categoria
""")
    List<CategoriaCount> findVentasPorCategoria(Long idVendedor);


}
