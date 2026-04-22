package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.out.PedidoResponseDto;
import com.gestorventas.deposito.enums.Role;
import com.gestorventas.deposito.models.Cliente;
import com.gestorventas.deposito.models.LineaPedido;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.repositories.LineaPedidoRepository;
import com.gestorventas.deposito.specifications.PedidoSpecifications;
import com.gestorventas.deposito.models.Pedido;
import com.gestorventas.deposito.repositories.ClienteRepository;
import com.gestorventas.deposito.repositories.PedidoRepository;
import com.gestorventas.deposito.repositories.VendedorRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * Servicio encargado de gestionar la logica del negocio relacionado con los pedidos.
 * <p>
 *     Permite registrar, consultar, actualizar y eliminar pedidos.
 * </p>
 * @author Kevin William Olarte Braun
 */
@Service
@AllArgsConstructor
public class PedidoService {

    private final MailService mailService;
    private final LineaPedidoRepository lineaPedidoRepository;
    private PedidoRepository pedidoRepository;
    private VendedorRepository vendedorRepository;
    private ClienteRepository clienteRepository;
    private ParametrosGlobalesService paramService;

    private final DecimalFormat df = new DecimalFormat("#.00");

    /**
     * Guardar un nuevo pedido en el sistema.
     * @param idCliente identificador a quien se le va retribuir el peiddo.
     * @param idVendedor identificador del vendedor que realizo el pedido.
     * @return DTO con los datos guardados visibles.
     * @throws RuntimeException entidades inexistentes.
     */
    public PedidoResponseDto add( long idCliente, long idVendedor) {
        Cliente cliente = clienteRepository.findById(idCliente);
        if(cliente==null)
            throw new RuntimeException("Cliente inexistente");
        if (cliente.getVendedor().getId()!=idVendedor)
            throw new RuntimeException("Cliente inexistente");

        int iva = paramService.getIva() == null ? 0 : paramService.getIva();
        int descuento = paramService.getDescuento()  == null ? 0 : paramService.getDescuento();
        Pedido pedido = new Pedido(descuento, iva);
        pedido.setCliente(cliente);

        pedido = pedidoRepository.save(pedido);
        return new PedidoResponseDto(pedido);
    }

    /**
     * Crear un pedido administrador.
     * @param idCliente identificador del cliente
     * @return DTO con los datos del pedido creado.
     */
    public PedidoResponseDto addAdmin(Long idCliente) {
        Optional<Cliente> cliente = clienteRepository.findById(idCliente);
        if (cliente.isEmpty())
            throw new RuntimeException("Cliente inexistente");

        int iva = paramService.getIva() == null ? 0 : paramService.getIva();
        int descuento = paramService.getDescuento()  == null ? 0 : paramService.getDescuento();

        Pedido pedido = new Pedido(descuento, iva);
        pedido.setCliente(cliente.get());

        pedido = pedidoRepository.save(pedido);
        return new PedidoResponseDto(pedido);
    }

    /**
     * Obtener un pedido por su id.
     * @param id id que representa el identificador unico
     * @param idCliente identificador del cliente
     * @param idVendedor identificador del vendedor
     * @return DTO con los datos guardados visibles.
     */
    public PedidoResponseDto get(long id, long idCliente, long idVendedor) {
        Vendedor vendedor = vendedorRepository.findById(idVendedor);
        if (vendedor == null)
            return null;
        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null || cliente.getVendedor() == null || cliente.getVendedor().getId() != idVendedor)
            return null;
        Pedido pedido = pedidoRepository.findById(id);
        if (pedido == null || pedido.getCliente() == null || pedido.getCliente().getId() != idCliente)
            return null;
        return new PedidoResponseDto(pedido);
    }

    /**
     * Obtener un pedido por su id.
     * @param id id que representa el identificador unico
     * @param idCliente identificador del cliente
     * @return DTO con los datos guardados visibles.
     */
    public PedidoResponseDto get(long id, long idCliente) {

        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null)
            return null;
        Pedido pedido = pedidoRepository.findById(id);
        if (pedido == null || pedido.getCliente() == null || pedido.getCliente().getId() != idCliente)
            return null;
        return new PedidoResponseDto(pedido);
    }


    /**
     * Obtener listado de todos los Pedidos registrados en el sistema.
     * se le puede añadir los siguentes filtrados, idVendedor, idCliente
     * @param idVendedor filtrado opcionar sacar por vendedor.
     * @param idCliente filtrado opcional sacar por cliente.
     * @return Listado DTO con todos los pedidos.
     */
    public List<PedidoResponseDto> getAll(Long idVendedor, Long idCliente) {
        Sort sort = Sort.by(
                Sort.Order.asc("finalizado"),   // primero los abiertos
                Sort.Order.desc("id")           // id de mayor a menor
        );

        return pedidoRepository.findAll(PedidoSpecifications.filter(idVendedor, idCliente), sort).stream()
                .map(PedidoResponseDto::new)
                .toList();
    }

    /**
     * Actualizar los datos de un pedido segun el identificador id
     * @param id id del peiddo a modificar.
     * @param idCliente cleinte que se le vendera el pedido
     * @param fecha fecha que se realizo el pedido actualizado
     * @return peiddo actualizado.
     * @throws RuntimeException referencia no existe.
     */
    public PedidoResponseDto update(
            long id,
            Long idVendedor,
            long idCliente,
            LocalDate fecha,
            Integer descuento,
            Integer iva,
            String comentario
    ) {

        Pedido pedido = pedidoRepository.findById(id);
        if (pedido == null)
            throw new RuntimeException("Pedido inexistente");

        if (pedido.getCliente().getId() != idCliente)
            throw new RuntimeException("El pedido no pertenece al cliente");
        if (idVendedor != null)
            if (!Objects.equals(pedido.getCliente().getVendedor().getId(), idVendedor))
                throw new RuntimeException("No tienes permiso para modificar este pedido");

        // Actualizar fecha si se envía
        if (fecha != null) {
            pedido.setFecha(fecha);
        }

        // Descuento puede ser 0, así que >= 0
        if (descuento != null && descuento >= 0) {
            pedido.setDescuento(descuento);
        }

        // IVA puede ser 0, así que >= 0
        if (iva != null && iva >= 0) {
            pedido.setIva(iva);
        }

        pedido.setComentario(comentario);

        pedido = pedidoRepository.save(pedido);

        return new PedidoResponseDto(pedido);
    }


    /**
     * Borrar un pedido del sistema en cascada con sus relaciones
     * @param id id del pedido a borrar.
     */
    public void delete(long id, long idVendedor, long idCliente) {
        Vendedor vendedor = vendedorRepository.findById(idVendedor);
        if (vendedor == null)
            return;
        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null || cliente.getVendedor() == null || cliente.getVendedor().getId() != idVendedor)
            return;
        Pedido pedido = pedidoRepository.findById(id);
        if (pedido == null || pedido.getCliente() == null || pedido.getCliente().getId() != idCliente)
            return;
        if (pedido.isFinalizado())
            return;
        pedidoRepository.delete(pedido);

    }

    /**
     * Borrar un pedio pero con menos restrinciones
     * @param id indentificador del pedido
     * @param idCliente identificador de asociado al pedido
     */
    public void delete(long id, long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null || cliente.getVendedor() == null)
            return;
        Pedido pedido = pedidoRepository.findById(id);
        if (pedido == null || pedido.getCliente() == null || pedido.getCliente().getId() != idCliente)
            return;
        pedidoRepository.delete(pedido);

    }


    /**
     * Cerrar un pedido ya registrado.
     * @param idVendedor identificador del vendedor que realizo el pedido.
     * @param idCliente identificador del cliente que realizo el pedido.
     * @param idPedido identificador del pedido que se va a cerrar.
     * @return DTO con los datos del pedido cerrado.
     * @throws RuntimeException entidades inexistentes.
     */
    public PedidoResponseDto cerrarPedido(long idVendedor, long idCliente, long idPedido) {
        Vendedor vendedor = vendedorRepository.findById(idVendedor);
        if (vendedor == null)
            throw new RuntimeException("Vendedor inexistente");
        Cliente cliente= clienteRepository.findById(idCliente);
        if (cliente == null || cliente.getVendedor().getId() != idVendedor)
            throw new RuntimeException("Cliente inexistente");
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null || pedido.getCliente().getId() != idCliente)
            throw new RuntimeException("Pedido inexistente");

        pedido.setFinalizado(true);

        pedido = pedidoRepository.save(pedido);

        try{
            Vendedor vadmin1 = vendedorRepository.findByEmail("gcholbi@gmail.com").orElse(null);
            Vendedor vadmin2 = vendedorRepository.findByEmail("josepfornesmarti@gmail.com").orElse(null);
            List<Vendedor> vendedores = new ArrayList<>();
            if (vadmin1 != null)
                vendedores.add(vadmin1);
            if (vadmin2 != null)
                vendedores.add(vadmin2);
            vendedores.add(vendedor);
            mailService.enviarCorreosPedido(vendedores, pedido);

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return new PedidoResponseDto(pedido);
    }



    public PedidoResponseDto cerrarPedido(long idCliente, long idPedido) {
        Cliente cliente= clienteRepository.findById(idCliente);
        if (cliente == null)
            throw new RuntimeException("Cliente inexistente");
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null || pedido.getCliente().getId() != idCliente)
            throw new RuntimeException("Pedido inexistente");
        BigDecimal oldBruto = pedido.getBrutoTotal();
        List<LineaPedido> lineasPeiddo = lineaPedidoRepository.getLineaPedidoByPedido(pedido);
        BigDecimal nuevoTotal = BigDecimal.ZERO;
        for (LineaPedido linea : lineasPeiddo) {
            if (linea.getStockFinal() == null || linea.getStockFinal() < 0){
                throw new RuntimeException("Hay lineas sin sotck_final definido");
            }
            if (linea.getStockFinal() > linea.getCantidad())
                throw new RuntimeException("Incongruencia de valores");
            //Revalorizamos unidad, con la diferencia del habia haber.
            linea.setCantidad(linea.getCantidad() - linea.getStockFinal());
            nuevoTotal = nuevoTotal.add(linea.getPrecio().multiply(
                    BigDecimal.valueOf(linea.getCantidad())
                            .setScale(2, RoundingMode.HALF_UP)));

        }
        pedido.setFinalizado(true);
        pedido.setBrutoTotal(nuevoTotal);
        pedido.setToken(generarTokenUnico());

        pedido = pedidoRepository.save(pedido);
        try{
            List<Vendedor> vendedorEnviar = new ArrayList<Vendedor>();
            Vendedor vendedorCliente = cliente.getVendedor();
            vendedorEnviar.add(vendedorCliente);
            vendedorRepository.findByEmail("gcholbi@gmail.com").ifPresent(vendedorEnviar::add);
            vendedorRepository.findByEmail("josepfornesmarti@gmail.com").ifPresent(vendedorEnviar::add);

            mailService.enviarCorreosPedido(vendedorEnviar, pedido);


        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return new PedidoResponseDto(pedido);


    }

    public byte[] exportPedidosCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID;FECHA;PRODUCTO;UNIDADES;PVP;CIF_CLIENTE\n");
        List<Pedido> pedidos = pedidoRepository.findAll();

        for (Pedido pedido : pedidos) {
            for (LineaPedido linea : pedido.getLineas()) {
                csv.append(pedido.getId()).append(";")
                        .append(pedido.getFecha()).append(";")
                        .append(linea.getProducto().getDescripcion()).append(";")
                        .append(linea.getCantidad()).append(";")
                        .append(linea.getPrecio()).append(";")
                        .append(pedido.getCliente().getCif()).append("\n");
            }
        }
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);

    }

    /**
     * Genera el PDF de un pedido a partir de su token público.
     *
     * @param token Token asociado al pedido.
     * @return PDF en formato byte[].
     * @throws RuntimeException Si el pedido no existe, no está finalizado
     *                          o ocurre un error al generar el PDF.
     */
    public byte[] getPDF(String token) {
        Pedido pedido = pedidoRepository.findByToken(token);
        if (pedido == null) {
            throw new RuntimeException("Pedido inexistente");
        }

        if (!pedido.isFinalizado())
            throw new RuntimeException("El pedido no ha sido finalizado");

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));


        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Título
            Paragraph titulo = new Paragraph("Informe de Pedido #" + pedido.getId(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph("Cliente: " + pedido.getCliente().getNombre()));
            document.add(new Paragraph(" ")); // Espacio

            // Tabla
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);

            // Encabezados
            String[] headers = {"Producto", "Cantidad", "Precio", "Total"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(cell);
            }
            BigDecimal totalb = BigDecimal.ZERO;
            // Filas
            for (LineaPedido linea : pedido.getLineas()) {
                BigDecimal subtotal = linea.getPrecio()
                        .multiply(BigDecimal.valueOf(linea.getCantidad()))
                        .setScale(2, RoundingMode.HALF_UP);
                tabla.addCell(linea.getProducto().getDescripcion());
                tabla.addCell(String.valueOf(linea.getCantidad()));
                tabla.addCell(nf.format(linea.getPrecio()));
                tabla.addCell(nf.format(subtotal));
                totalb = totalb.add(subtotal);

            }
            totalb = totalb.setScale(2, RoundingMode.HALF_UP);
            document.add(tabla);

            // Total
            document.add(new Paragraph(" "));
            Paragraph total = new Paragraph("Total: " + nf.format(totalb),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();
            return baos.toByteArray();

        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }
    private String generarTokenUnico() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (pedidoRepository.existsByToken(token));
        return token;
    }
}
