package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.out.PedidoResponseDto;
import com.gestorventas.deposito.models.Cliente;
import com.gestorventas.deposito.models.LineaPedido;
import com.gestorventas.deposito.models.Vendedor;
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
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
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
    private PedidoRepository pedidoRepository;
    private VendedorRepository vendedorRepository;
    private ClienteRepository clienteRepository;

    private final DecimalFormat df = new DecimalFormat("#.00");

    /**
     * Guardar un nuevo pedido en el sistema.
     * @param idCliente identificador a quien se le va retribuir el peiddo.
     * @param idVendedor identificador del vendedor que realizo el pedido.
     * @param descuento porcentaje de descuento aplicado al pedido.
     * @param iva porcentaje de iva aplicado al pedido.
     * @return DTO con los datos guardados visibles.
     * @throws RuntimeException entidades inexistentes.
     */
    public PedidoResponseDto add( long idCliente, long idVendedor, int descuento, int iva) {
        Cliente cliente = clienteRepository.findById(idCliente);
        if(cliente==null)
            throw new RuntimeException("Cliente inexistente");
        if (cliente.getVendedor().getId()!=idVendedor)
            throw new RuntimeException("Cliente inexistente");

        Pedido pedido = new Pedido(descuento, iva);
        pedido.setCliente(cliente);

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
        return pedidoRepository.findAll(PedidoSpecifications.filter(idVendedor, idCliente)).stream()
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
    public PedidoResponseDto update(long id, long idVendedor, long idCliente, LocalDate fecha, int descuento, int iva) {
        Pedido pedido = pedidoRepository.findById(id);

        if(pedido==null)
            return null;

        if (pedido.getCliente().getId()!=idCliente)
            return null;

        if(pedido.getCliente().getVendedor().getId()!=idVendedor)
            return null;

        if (fecha != null){
            pedido.setFecha(fecha);
        }
        if (descuento > 0){
            pedido.setDescuento(descuento);
        }
        if (iva > 0){
            pedido.setIva(iva);
        }
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
        Cliente cliente = vendedor.getClientes().stream().filter(c -> c.getId()==idCliente).findFirst().orElse(null);
        if (cliente == null)
            throw new RuntimeException("Cliente inexistente");
        Pedido pedido = cliente.getPedidos().stream().filter(p -> p.getId()==idPedido).findFirst().orElse(null);
        if (pedido == null)
            throw new RuntimeException("Pedido inexistente");

        pedido.setFinalizado(true);

        pedido = pedidoRepository.save(pedido);

        try{
            mailService.enviarCorreoPedido(vendedor.getEmail(), pedido);
            //mailService.enviarCorreoPedido(cliente.getEmail(), pedido);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return new PedidoResponseDto(pedido);
    }

    /**
     * Generar un informe PDF con los datos de un pedido.
     * @param idPedido identificador pedido a generar el informe.
     * @return byte[] con el informe generado.
     */
    public byte[] generarInformePdf(long idPedido,long idCliente, long idVendedor) {
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null)
            throw new RuntimeException("Pedido inexistente");
        if (pedido.getCliente().getId()!=idCliente)
            throw new RuntimeException("Cliente inexistente");
        if (pedido.getCliente().getVendedor().getId()!=idVendedor)
            throw new RuntimeException("Vendedor inexistente");
        if (!pedido.isFinalizado())
            throw new RuntimeException("El pedido no ha sido finalizado");


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
            double totalb = 0;
            // Filas
            for (LineaPedido linea : pedido.getLineas()) {
                double subtotal = linea.getCantidad() * linea.getPrecio();
                tabla.addCell(linea.getProducto().getDescripcion());
                tabla.addCell(String.valueOf(linea.getCantidad()));
                tabla.addCell(df.format(linea.getPrecio()) + " €");
                tabla.addCell(df.format(subtotal) + " €");
                totalb += subtotal;
            }

            document.add(tabla);

            // Total
            document.add(new Paragraph(" "));
            Paragraph total = new Paragraph("Total: " + df.format(totalb) + " €",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();
            return baos.toByteArray();

        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }




}
