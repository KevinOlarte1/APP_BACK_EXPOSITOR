package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.out.LineaPedidoResponseDto;
import com.gestorventas.deposito.models.*;
import com.gestorventas.deposito.models.producto.Producto;
import com.gestorventas.deposito.repositories.*;
import com.gestorventas.deposito.specifications.LineaPedidoSpecifications;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final ParametrosGlobalesService paramService;

    /**
     * Guardar una nueva línea de pedido en el sistema.
     */
    @Transactional
    public LineaPedidoResponseDto add(long idVendedor, long idCliente, long idPedido,
                                      long idProducto, int cantidad, BigDecimal precio, Integer grupo) {
        // Validar existencia de vendedor

        Vendedor vendedor = vendedorRepository.findById(idVendedor);
        if (vendedor == null)
            throw new RuntimeException("Vendedor inexistente");

        // Validar cliente
        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null)
            throw new RuntimeException("Cliente inexistente");
        if (!cliente.getVendedor().getId().equals(idVendedor))
            throw new RuntimeException("El cliente no pertenece al vendedor indicado");

        // Obtener pedido directo desde repositorio
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null)
            throw new RuntimeException("Pedido inexistente");
        if (pedido.isFinalizado())
            throw new RuntimeException("El pedido ya está finalizado");

        // Validar que el pedido pertenece al cliente correcto
        if (pedido.getCliente() == null || !pedido.getCliente().getId().equals(idCliente))
            throw new RuntimeException("El pedido no pertenece al cliente indicado");

        // Validar producto
        Producto producto = productoRepository.findById(idProducto);
        if (producto == null)
            throw new RuntimeException("Producto inexistente");


        if (!producto.isActivo()){
            throw new IllegalArgumentException("Este producto esta desactivado");
        }

        // Validar cantidad y precio
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        if (precio == null)
            precio = producto.getPrecio();
        else if (precio.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El precio debe ser mayor o igual a 0");
        //Validar grupo dentro del rango
        Integer grupoMax = paramService.getGrupoMax();
        if (grupo != null)
            if (grupoMax != null &&  grupo > grupoMax)
                throw new RuntimeException("Grupo invalido");

        // Crear y guardar la línea
        LineaPedido linea = new LineaPedido();
        linea.setPedido(pedido);
        linea.setProducto(producto);
        linea.setCantidad(cantidad);
        linea.setPrecio(precio);
        if (grupo != null)
            linea.setGrupo(grupo);

        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        // Guardar forzando sincronización del contexto
        LineaPedido saved = lineaPedidoRepository.saveAndFlush(linea);

        pedido.setBrutoTotal(
                pedido.getBrutoTotal().add(subtotal).setScale(2, RoundingMode.HALF_UP)
        );

        pedidoRepository.save(pedido);

        return new LineaPedidoResponseDto(saved);
    }

    @Transactional
    public LineaPedidoResponseDto add(long idCliente, long idPedido, long idProducto,
                                      int cantidad, BigDecimal precio, Integer grupo) {
        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null)
            throw new RuntimeException("Cliente inexistente");
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null)
            throw new RuntimeException("Pedido inexistente");
        else if (!pedido.getCliente().getId().equals(idCliente))
            throw new RuntimeException("El pedido no pertenece al cliente indicado");

        if (pedido.isFinalizado())
            throw new RuntimeException("El pedido ya está finalizado");


        Producto producto = productoRepository.findById(idProducto);
        if (producto == null)
            throw new RuntimeException("Producto inexistente");

        if (!producto.isActivo()){
            throw new IllegalArgumentException("Este producto esta desactivado");
        }

        // Validar cantidad y precio
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        if (precio == null)
            precio = producto.getPrecio();
        else if (precio.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El precio debe ser mayor o igual a 0");

        //Validar grupo dentro del rango
        Integer grupoMax = paramService.getGrupoMax();
        if (grupo != null)
            if (grupoMax != null &&  grupo > grupoMax)
                throw new RuntimeException("Grupo invalido");

        LineaPedido linea = new LineaPedido();

        linea.setPedido(pedido);
        linea.setProducto(producto);
        linea.setCantidad(cantidad);
        linea.setPrecio(precio);
        if (grupo != null)
            linea.setGrupo(grupo);

        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        // Guardar forzando sincronización del contexto
        LineaPedido saved = lineaPedidoRepository.saveAndFlush(linea);

        pedido.setBrutoTotal(
                pedido.getBrutoTotal().add(subtotal).setScale(2, RoundingMode.HALF_UP)
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
    @Transactional
    public LineaPedidoResponseDto update(long id, Integer cantidad, BigDecimal precio, Long idVendedor) {
        LineaPedido linea = lineaPedidoRepository.findById(id);
        if (linea == null)
            throw new RuntimeException("Línea no encontrada");

        Pedido pedido = pedidoRepository.findById((long) linea.getPedido().getId());
        if (pedido.isFinalizado())
            throw new RuntimeException("Pedido finalizado");

        if (idVendedor != null)
            if (!Objects.equals(pedido.getCliente().getVendedor().getId(), idVendedor))
                throw new RuntimeException("No tiene permisos para editar este pedido");

        BigDecimal oldSubtotal = linea.getPrecio()
                .multiply(BigDecimal.valueOf(linea.getCantidad()))
                .setScale(2, RoundingMode.HALF_UP);

        if (cantidad != null) {
            if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            linea.setCantidad(cantidad);
        }

        if (precio != null) {
            if (precio.compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalArgumentException("El precio debe ser mayor o igual a 0");
            linea.setPrecio(precio);
        }

        linea = lineaPedidoRepository.save(linea);

        BigDecimal newSubtotal = linea.getPrecio()
                .multiply(BigDecimal.valueOf(linea.getCantidad()))
                .setScale(2, RoundingMode.HALF_UP);

        pedido.setBrutoTotal(
                pedido.getBrutoTotal()
                        .subtract(oldSubtotal)
                        .add(newSubtotal)
                        .setScale(2, RoundingMode.HALF_UP)
        );

        pedidoRepository.save(pedido);

        return new LineaPedidoResponseDto(linea);
    }
    /**
     * Eliminar una línea de pedido.
     */
    @Transactional
    public void delete(long idCliente, long idPedido, long idLinea) {
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null)
            throw new RuntimeException("Pedido no encontrado");

        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null || cliente.getVendedor() == null)
            throw new RuntimeException("No tiene permisos para editar este pedido");


        LineaPedido linea = lineaPedidoRepository.findById(idLinea);
        if (linea == null || linea.getPedido() == null || !linea.getPedido().getId().equals(idPedido))
            throw new RuntimeException("Linea no encontrada");

        BigDecimal qty = BigDecimal.valueOf(linea.getCantidad());
        BigDecimal price = linea.getPrecio();
        BigDecimal subtotal = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);


        pedido.setBrutoTotal(
                pedido.getBrutoTotal().subtract(subtotal)
                        .setScale(2, RoundingMode.HALF_UP)
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
        if (cliente == null || cliente.getVendedor() == null || !cliente.getVendedor().getId().equals(idVendedor))
            throw new RuntimeException("No tiene permisos para editar este pedido");


        LineaPedido linea = lineaPedidoRepository.findById(idLinea);
        if (linea == null || linea.getPedido() == null || !linea.getPedido().getId().equals(idPedido))
            throw new RuntimeException("Linea no encontrada");

        BigDecimal qty = BigDecimal.valueOf(linea.getCantidad());
        BigDecimal price = linea.getPrecio();
        BigDecimal subtotal = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);

        pedido.setBrutoTotal(
                pedido.getBrutoTotal().subtract(subtotal)
                        .setScale(2, RoundingMode.HALF_UP)
        );

        pedidoRepository.save(pedido);

        lineaPedidoRepository.deleteById(idLinea);
    }

    public LineaPedidoResponseDto putStockFinal(Long idLinea, Integer stockFinal, Long idVendedor) {
        if (idLinea == null || stockFinal == null){
            throw new RuntimeException("Linea no encontrada o stock invalido");
        }

        LineaPedido linea = lineaPedidoRepository.findById(idLinea).orElse(null);
        if (linea == null){
            throw new RuntimeException("Linea no encontrada");
        }

        if (idVendedor != null){
            if (!linea.getPedido().getCliente().getVendedor().getId().equals(idVendedor)){
                throw new RuntimeException("Vendedor no encontrado");
            }
        }
        Pedido pedido = pedidoRepository.findById(linea.getPedido().getId()).orElse(null);
        if (pedido == null){
            throw new RuntimeException("Pedido no encontrado");
        }
        if (pedido.isFinalizado()){
            throw new RuntimeException("Pedido finalizado");
        }
        if (stockFinal < 0 || stockFinal > linea.getCantidad()){
            throw new RuntimeException("Stock Final invalido");
        }

        linea.setStockFinal(stockFinal);
        lineaPedidoRepository.save(linea);
        return new LineaPedidoResponseDto(linea);

    }
}
