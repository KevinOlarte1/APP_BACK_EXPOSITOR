package com.gestorventas.deposito.repositories;

import com.gestorventas.deposito.interfaces.GastosCliente;
import com.gestorventas.deposito.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder y gestionar entidades {@link Cliente}
 * <p>
 *     Permite realizar operaciones CRUD
 * </p>
 * @author Kevin William Olarte Braun.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente,Long>, JpaSpecificationExecutor<Cliente> {

    public Cliente findById(long id);

    boolean existsByCif(String cif);


    @Query("""
        SELECT
            c.id AS clienteId,
            c.nombre AS clienteNombre,
            ROUND(SUM(lp.cantidad * lp.producto.precio), 2) AS total
        FROM Cliente c
        JOIN c.pedidos p
        JOIN p.lineas lp
        WHERE c.vendedor.id = :idVendedor
        GROUP BY c.id, c.nombre
      
        
""")
    List<GastosCliente> getGastosClientesByVendedor(long idVendedor);
}
