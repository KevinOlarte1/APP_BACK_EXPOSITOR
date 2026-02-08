package com.gestorventas.deposito.repositories;

import com.gestorventas.deposito.models.LineaPedido;
import com.gestorventas.deposito.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder y gestionar entidades {@link LineaPedido}
 * <p>
 *     Permite realizar operaciones CRUD
 * </p>
 * @author Kevin William Olarte Braun.
 */
@Repository
public interface LineaPedidoRepository extends JpaRepository<LineaPedido,Long>, JpaSpecificationExecutor<LineaPedido>{
    LineaPedido findById(long id);

    List<LineaPedido> getLineaPedidoByPedido(Pedido pedido);
}
