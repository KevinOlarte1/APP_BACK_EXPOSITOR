package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.out.LineaPedidoResponseDto;
import com.gestorventas.deposito.models.*;
import com.gestorventas.deposito.models.producto.Producto;
import com.gestorventas.deposito.repositories.*;
import com.gestorventas.deposito.specifications.LineaPedidoSpecifications;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Servicio encargado de gestionar la lógica del negocio relacionado con las líneas de pedido.
 * <p>
 *     Permite registrar, consultar, actualizar y eliminar líneas de pedido.
 * </p>
 * @author Kevin
 */
@Service
@AllArgsConstructor
public class LineaPedidoService {

    private final LineaPedidoRepository lineaPedidoRepository;
    private final VendedorRepository vendedorRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    /**
     * Guardar una nueva línea de pedido en el sistema.
     */
    public LineaPedidoResponseDto add(long idVendedor, long idCliente, long idPedido,
                                      long idProducto, int cantidad, Double precio) {
        System.out.println("Entradno al crear service ");
        // Validar existencia de vendedor
        Vendedor vendedor = vendedorRepository.findById(idVendedor);
        if (vendedor == null)
            throw new RuntimeException("Vendedor inexistente");

        // Validar cliente
        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null)
            throw new RuntimeException("Cliente inexistente");
        if (cliente.getVendedor() == null || cliente.getVendedor().getId() != idVendedor)
            throw new RuntimeException("El cliente no pertenece al vendedor indicado");

        // Obtener pedido directo desde repositorio
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null)
            throw new RuntimeException("Pedido inexistente");
        if (pedido.isFinalizado())
            throw new RuntimeException("El pedido ya está finalizado");

        // Validar que el pedido pertenece al cliente correcto
        if (pedido.getCliente() == null || pedido.getCliente().getId() != idCliente)
            throw new RuntimeException("El pedido no pertenece al cliente indicado");

        // Validar producto
        Producto producto = productoRepository.findById(idProducto);
        if (producto == null)
            throw new RuntimeException("Producto inexistente");

        // Validar cantidad y precio
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        if (precio == null)
            precio = producto.getPrecio();
        else if (precio < 0)
            throw new IllegalArgumentException("El precio debe ser mayor o igual a 0");

        // Crear y guardar la línea
        LineaPedido linea = new LineaPedido();
        linea.setPedido(pedido);
        linea.setProducto(producto);
        linea.setCantidad(cantidad);
        linea.setPrecio(precio);

        BigDecimal bdCantidad = BigDecimal.valueOf(cantidad);
        BigDecimal bdPrecio = BigDecimal.valueOf(precio);
        BigDecimal subtotal = bdCantidad.multiply(bdPrecio);

        // Guardar forzando sincronización del contexto
        LineaPedido saved = lineaPedidoRepository.saveAndFlush(linea);

        pedido.setBrutoTotal(
                pedido.getBrutoTotal().add(subtotal)
        );

        pedidoRepository.save(pedido);

        return new LineaPedidoResponseDto(saved);
    }


    /**
     * Obtener una línea de pedido por su id.
     */
    public LineaPedidoResponseDto get(long id) {
        LineaPedido linea = lineaPedidoRepository.findById(id);
        if (linea == null)
            return null;
        return new LineaPedidoResponseDto(linea);
    }

    /**
     * Obtener listado de todas las líneas de pedido filtradas.
     */
    public List<LineaPedidoResponseDto> get(Long idLinea, Long idPedido, Long idVendedor, Long idCliente) {
        return lineaPedidoRepository.findAll(
                        LineaPedidoSpecifications.filter(idLinea, idPedido, idVendedor, idCliente)
                ).stream()
                .map(LineaPedidoResponseDto::new)
                .toList();
    }

    /**
     * Obtener listado de todas las líneas de pedido filtradas.
     */
    public List<LineaPedidoResponseDto> get(Long idLinea, Long idPedido, Long idCliente) {
        return lineaPedidoRepository.findAll(
                        LineaPedidoSpecifications.filter(idLinea, idPedido, null, idCliente)
                ).stream()
                .map(LineaPedidoResponseDto::new)
                .toList();
    }

    /**
     * Actualizar una línea de pedido existente.
     */
    public LineaPedidoResponseDto update(long id, Integer cantidad, Double precio, Long idVendedor) {
        LineaPedido linea = lineaPedidoRepository.findById(id);
        if (linea == null)
            throw new RuntimeException("Línea no encontrada");

        Pedido pedido = pedidoRepository.findById((long) linea.getPedido().getId());
        if (pedido.isFinalizado())
            throw new RuntimeException("Pedido finalizado");
        if(idVendedor != null)
            if (!Objects.equals(pedido.getCliente().getVendedor().getId(), idVendedor))
                throw new RuntimeException("No tiene permisos para editar este pedido");

        BigDecimal oldQty = BigDecimal.valueOf(linea.getCantidad());
        BigDecimal oldPrice = BigDecimal.valueOf(linea.getPrecio());
        BigDecimal oldSubtotal = oldQty.multiply(oldPrice);

        if (cantidad != null && cantidad > 0)
            linea.setCantidad(cantidad);

        if (precio != null && precio > 0)
            linea.setPrecio(precio);

        linea = lineaPedidoRepository.save(linea);


        BigDecimal newQty = BigDecimal.valueOf(linea.getCantidad());
        BigDecimal newPrice = BigDecimal.valueOf(linea.getPrecio());
        BigDecimal newSubtotal = newQty.multiply(newPrice);

        pedido.setBrutoTotal(
                pedido.getBrutoTotal()
                        .subtract(oldSubtotal)
                        .add(newSubtotal)
        );

        pedidoRepository.save(pedido);

        return new LineaPedidoResponseDto(linea);
    }

    /**
     * Eliminar una línea de pedido.
     */
    public void delete(long idCliente, long idPedido, long idLinea) {
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null || pedido.isFinalizado())
            throw new RuntimeException("Pedido finalizado");

        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null || cliente.getVendedor() == null)
            throw new RuntimeException("No tiene permisos para editar este pedido");


        LineaPedido linea = lineaPedidoRepository.findById(idLinea);
        if (linea == null || linea.getPedido() == null || linea.getPedido().getId() != idPedido)
            throw new RuntimeException("Linea no encontrada");

        BigDecimal qty = BigDecimal.valueOf(linea.getCantidad());
        BigDecimal price = BigDecimal.valueOf(linea.getPrecio());
        BigDecimal subtotal = qty.multiply(price);

        pedido.setBrutoTotal(
                pedido.getBrutoTotal().subtract(subtotal)
        );

        pedidoRepository.save(pedido);

        lineaPedidoRepository.deleteById(idLinea);
    }

    /**
     * Eliminar una línea de pedido.
     */
    public void delete(long idVendedor, long idCliente, long idPedido, long idLinea) {
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null || pedido.isFinalizado())
            throw new RuntimeException("Pedido finalizado");

        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null || cliente.getVendedor() == null || cliente.getVendedor().getId() != idVendedor)
            throw new RuntimeException("No tiene permisos para editar este pedido");


        LineaPedido linea = lineaPedidoRepository.findById(idLinea);
        if (linea == null || linea.getPedido() == null || linea.getPedido().getId() != idPedido)
            throw new RuntimeException("Linea no encontrada");

        BigDecimal qty = BigDecimal.valueOf(linea.getCantidad());
        BigDecimal price = BigDecimal.valueOf(linea.getPrecio());
        BigDecimal subtotal = qty.multiply(price);

        pedido.setBrutoTotal(
                pedido.getBrutoTotal().subtract(subtotal)
        );

        pedidoRepository.save(pedido);

        lineaPedidoRepository.deleteById(idLinea);
    }
}
