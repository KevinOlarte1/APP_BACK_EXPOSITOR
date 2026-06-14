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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        pedido.setToken(generarTokenUnico());

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
        pedido.setToken(generarTokenUnico());

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
     * Obtiene el listado de pedidos registrados en el sistema aplicando
     * filtros opcionales por vendedor y cliente.
     * <p>
     * Los resultados se devuelven ordenados mostrando primero los pedidos
     * no finalizados y, dentro de cada grupo, por identificador descendente.
     *
     * @param idVendedor identificador opcional del vendedor para filtrar pedidos
     * @param idCliente identificador opcional del cliente para filtrar pedidos
     * @return listado de pedidos adaptados a DTO
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
     * Actualiza los datos generales de un pedido existente.
     * <p>
     * El método valida que el pedido pertenezca al cliente indicado y,
     * si se informa un vendedor, comprueba que tenga permiso sobre el cliente.
     * Permite modificar la fecha, el descuento, el IVA y el comentario del pedido.
     *
     * @param id identificador del pedido a actualizar
     * @param idVendedor identificador opcional del vendedor que realiza la operación
     * @param idCliente identificador del cliente asociado al pedido
     * @param fecha nueva fecha del pedido, si se desea modificar
     * @param descuento nuevo descuento aplicado, si se desea modificar
     * @param iva nuevo IVA aplicado, si se desea modificar
     * @param comentario nuevo comentario del pedido
     * @return DTO con los datos actualizados del pedido
     * @throws RuntimeException si el pedido no existe, no pertenece al cliente
     *                          o el vendedor no tiene permiso para modificarlo
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

        if (pedido.isFinalizado())
            throw new RuntimeException("No se puede modificar un pedido finalizado");

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
        if (descuento != null) {
            if (descuento < 0 || descuento > 100)
                throw new RuntimeException("El descuento no puede ser negativo");
            pedido.setDescuento(descuento);
        }

        // IVA puede ser 0, así que >= 0
        if (iva != null) {
            if (iva < 0)
                throw new RuntimeException("El Iva no puede ser negativo");
            pedido.setIva(iva);
        }

        if (comentario != null) {
            pedido.setComentario(comentario);
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
        if (pedido.isFinalizado())
            return;
        pedidoRepository.delete(pedido);

    }

    /**
     * Elimina un pedido asociado a un cliente concreto.
     * @param id identificador del pedido a eliminar
     * @param idCliente identificador del cliente propietario del pedido
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
     * Cierra un pedido asociado a un cliente y vendedor concreto.
     * <p>
     * El método valida la existencia del vendedor, del cliente y del pedido,
     * comprobando además que el cliente pertenezca al vendedor indicado y que
     * el pedido esté asociado correctamente al cliente. Posteriormente delega
     * el proceso de cierre en el método interno {@code _cerrarPedido} y envía
     * una notificación por correo a los administradores y al vendedor responsable.
     *
     * @param idVendedor identificador del vendedor responsable del pedido
     * @param idCliente identificador del cliente asociado al pedido
     * @param idPedido identificador del pedido que se desea cerrar
     * @return DTO con los datos actualizados del pedido cerrado
     * @throws RuntimeException si el vendedor, cliente o pedido no existen
     *                          o no están correctamente relacionados
     */
    @Transactional
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

        pedido = _cerrarPedido(pedido);

        try{
            List<Vendedor> vendedores = vendedorRepository.findByRole(Role.ADMIN);
            vendedores.add(vendedor);

            mailService.enviarCorreosPedido(vendedores, pedido);

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return new PedidoResponseDto(pedido);
    }

    /**
     * Cierra un pedido asociado a un cliente desde el flujo de administración.
     * <p>
     * Valida que el cliente y el pedido existan, comprueba que el pedido pertenezca
     * al cliente indicado y delega el proceso de cierre en el método interno
     * {@code _cerrarPedido}. Una vez cerrado, notifica por correo a los vendedores
     * con rol administrador.
     *
     * @param idCliente identificador del cliente asociado al pedido
     * @param idPedido identificador del pedido que se desea cerrar
     * @return DTO con los datos actualizados del pedido cerrado
     * @throws RuntimeException si el cliente no existe, el pedido no existe
     *                          o no pertenece al cliente indicado
     */
    @Transactional
    public PedidoResponseDto cerrarPedido(long idCliente, long idPedido) {
        //Validacion
        Cliente cliente= clienteRepository.findById(idCliente);
        if (cliente == null)
            throw new RuntimeException("Cliente inexistente");
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null || pedido.getCliente().getId() != idCliente)
            throw new RuntimeException("Pedido inexistente");

        pedido = _cerrarPedido(pedido);

        try{
            List<Vendedor> vendedorEnviar = vendedorRepository.findByRole(Role.ADMIN);
            mailService.enviarCorreosPedido(vendedorEnviar, pedido);

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return new PedidoResponseDto(pedido);


    }

    /**
     * Genera y exporta un archivo CSV con el listado de pedidos y sus líneas asociadas.
     * <p>
     * Cada fila del CSV representa una línea de pedido e incluye información
     * del pedido, producto, unidades, precio aplicado y CIF del cliente.
     * El archivo se genera en formato UTF-8 con BOM para garantizar
     * compatibilidad con aplicaciones como Microsoft Excel.
     *
     * @return archivo CSV en formato byte[] listo para descarga
     */
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
     * Cierra un pedido aplicando el ajuste de cantidades según el stock final
     * indicado en cada línea.
     * <p>
     * Para cada línea se valida que el stock final esté informado y que no sea
     * superior a la cantidad inicial. Posteriormente se recalcula la cantidad
     * vendida, se actualiza el importe bruto del pedido, se genera un token
     * público de descarga y se marca el pedido como finalizado.
     *
     * @param pedido pedido que se desea cerrar
     * @return pedido actualizado y marcado como finalizado
     * @throws RuntimeException si el pedido ya está finalizado o alguna línea
     *                          contiene valores de stock no válidos
     */
    private Pedido _cerrarPedido(Pedido pedido) {
        if (pedido.isFinalizado())
            throw new RuntimeException("El pedido ya está finalizado");
        //Actualizacion de lineas
        List<LineaPedido> lineasPeiddo = lineaPedidoRepository.getLineaPedidoByPedido(pedido);

        BigDecimal nuevoTotal = BigDecimal.ZERO;

        for (LineaPedido linea : lineasPeiddo) {
            //Comprobar que tenga stockFinal
            if (linea.getStockFinal() == null || linea.getStockFinal() < 0){
                throw new RuntimeException("Hay lineas sin sotck_final definido");
            }

            //No puede tener valor final mayor que na inicial
            if (linea.getStockFinal() > linea.getCantidad())
                throw new RuntimeException("Incongruencia de valores");

            //Revalorizamos unidad, con la diferencia del habia haber.
            linea.setCantidad(linea.getCantidad() - linea.getStockFinal());
            BigDecimal subtotal = linea.getPrecio()
                    .multiply(BigDecimal.valueOf(linea.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);

            nuevoTotal = nuevoTotal.add(subtotal);

        }

        //Guardar lineas en BBDD
        lineaPedidoRepository.saveAll(lineasPeiddo);


        pedido.setFinalizado(true);
        pedido.setBrutoTotal(nuevoTotal);

        //Guardar pedido en BBDD
        pedido = pedidoRepository.save(pedido);
        return  pedido;
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
        if (pedido == null)
            throw new RuntimeException("Pedido inexistente");

        Cliente cliente = pedido.getCliente();
        Vendedor vendedor = cliente.getVendedor();
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        BigDecimal bruto = pedido.getBrutoTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal base = bruto
                .multiply(BigDecimal.valueOf(100 - pedido.getDescuento()))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal descuentoImporte = bruto.subtract(base).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ivaImporte = base
                .multiply(BigDecimal.valueOf(pedido.getIva()))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(ivaImporte).setScale(2, RoundingMode.HALF_UP);

        Color cDark    = new Color(15,  23,  42);
        Color cMuted   = new Color(100, 116, 139);
        Color cLine    = new Color(229, 231, 235);
        Color cBgLight = new Color(248, 250, 252);
        Color cAltRow  = new Color(249, 250, 251);
        Color cSlate   = new Color(148, 163, 184);

        Font fHeader     = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   22, Color.WHITE);
        Font fHeaderSub  = FontFactory.getFont(FontFactory.HELVETICA,          9, cSlate);
        Font fNumPedido  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   13, Color.WHITE);
        Font fFecha      = FontFactory.getFont(FontFactory.HELVETICA,          9, cSlate);
        Font fLabel      = FontFactory.getFont(FontFactory.HELVETICA_BOLD,     7, cMuted);
        Font fValBold    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    10, cDark);
        Font fValNormal  = FontFactory.getFont(FontFactory.HELVETICA,          9, cDark);
        Font fTblHead    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,     8, cMuted);
        Font fTblCell    = FontFactory.getFont(FontFactory.HELVETICA,          9, cDark);
        Font fTotLabel   = FontFactory.getFont(FontFactory.HELVETICA,          9, cMuted);
        Font fTotValue   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,     9, cDark);
        Font fGrandLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    11, Color.WHITE);
        Font fGrandVal   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    11, Color.WHITE);
        Font fFooter     = FontFactory.getFont(FontFactory.HELVETICA,          8, cMuted);
        Font fComentLbl  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,     8, cMuted);
        Font fComentVal  = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE,  9, cMuted);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // ── CABECERA ────────────────────────────────────────────────
            PdfPTable headerTbl = new PdfPTable(2);
            headerTbl.setWidthPercentage(100);
            headerTbl.setWidths(new float[]{3f, 2f});
            headerTbl.setSpacingAfter(14);

            PdfPCell cLeft = new PdfPCell();
            cLeft.setBackgroundColor(cDark);
            cLeft.setBorder(Rectangle.NO_BORDER);
            cLeft.setPadding(18);
            Paragraph pFact = new Paragraph("FACTURA", fHeader);
            Paragraph pSub  = new Paragraph("Deposito GestorVentas", fHeaderSub);
            pSub.setSpacingBefore(3);
            cLeft.addElement(pFact);
            cLeft.addElement(pSub);
            headerTbl.addCell(cLeft);

            PdfPCell cRight = new PdfPCell();
            cRight.setBackgroundColor(cDark);
            cRight.setBorder(Rectangle.NO_BORDER);
            cRight.setPadding(18);
            cRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph pNPedido = new Paragraph("Pedido #" + pedido.getId(), fNumPedido);
            pNPedido.setAlignment(Element.ALIGN_RIGHT);
            Paragraph pFechaP = new Paragraph(pedido.getFecha().format(fmt), fFecha);
            pFechaP.setAlignment(Element.ALIGN_RIGHT);
            pFechaP.setSpacingBefore(4);
            cRight.addElement(pNPedido);
            cRight.addElement(pFechaP);
            headerTbl.addCell(cRight);
            doc.add(headerTbl);

            // ── BLOQUE DE / CLIENTE ─────────────────────────────────────
            PdfPTable infoTbl = new PdfPTable(2);
            infoTbl.setWidthPercentage(100);
            infoTbl.setSpacingAfter(14);

            PdfPCell cDe = new PdfPCell();
            cDe.setBackgroundColor(cBgLight);
            cDe.setBorderColor(cLine);
            cDe.setPadding(12);
            pdfLabelValue(cDe, "DE", "Deposito GestorVentas", fLabel, fValBold);
            if (vendedor != null) {
                pdfLabelValue(cDe, "RESPONSABLE", vendedor.getNombre() + " " + vendedor.getApellido(), fLabel, fValNormal);
                pdfLabelValue(cDe, "EMAIL", vendedor.getEmail(), fLabel, fValNormal);
            }
            infoTbl.addCell(cDe);

            PdfPCell cCli = new PdfPCell();
            cCli.setBackgroundColor(cBgLight);
            cCli.setBorderColor(cLine);
            cCli.setPadding(12);
            pdfLabelValue(cCli, "CLIENTE", cliente.getNombre(), fLabel, fValBold);
            pdfLabelValue(cCli, "CIF", cliente.getCif() != null ? cliente.getCif() : "-", fLabel, fValNormal);
            if (cliente.getTelefono() != null && !cliente.getTelefono().isBlank())
                pdfLabelValue(cCli, "TELEFONO", cliente.getTelefono(), fLabel, fValNormal);
            if (cliente.getEmail() != null && !cliente.getEmail().isBlank())
                pdfLabelValue(cCli, "EMAIL", cliente.getEmail(), fLabel, fValNormal);
            infoTbl.addCell(cCli);
            doc.add(infoTbl);

            // ── TABLA DE LINEAS ─────────────────────────────────────────
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{5f, 1.2f, 2f, 2f});
            tabla.setSpacingAfter(10);

            pdfHeaderCell(tabla, "PRODUCTO",    Element.ALIGN_LEFT,   fTblHead, cBgLight, cLine);
            pdfHeaderCell(tabla, "CANT.",        Element.ALIGN_CENTER, fTblHead, cBgLight, cLine);
            pdfHeaderCell(tabla, "P. UNITARIO", Element.ALIGN_RIGHT,  fTblHead, cBgLight, cLine);
            pdfHeaderCell(tabla, "SUBTOTAL",    Element.ALIGN_RIGHT,  fTblHead, cBgLight, cLine);

            boolean alt = false;
            for (LineaPedido linea : pedido.getLineas()) {
                Color rowBg = alt ? cAltRow : Color.WHITE;
                alt = !alt;
                BigDecimal subtotal = linea.getPrecio()
                        .multiply(BigDecimal.valueOf(linea.getCantidad()))
                        .setScale(2, RoundingMode.HALF_UP);
                pdfDataCell(tabla, linea.getProducto().getDescripcion(), Element.ALIGN_LEFT,   fTblCell, rowBg, cLine);
                pdfDataCell(tabla, String.valueOf(linea.getCantidad()),   Element.ALIGN_CENTER, fTblCell, rowBg, cLine);
                pdfDataCell(tabla, nf.format(linea.getPrecio()),          Element.ALIGN_RIGHT,  fTblCell, rowBg, cLine);
                pdfDataCell(tabla, nf.format(subtotal),                   Element.ALIGN_RIGHT,  fTblCell, rowBg, cLine);
            }
            doc.add(tabla);

            // ── COMENTARIO + TOTALES ────────────────────────────────────
            PdfPTable bottomTbl = new PdfPTable(2);
            bottomTbl.setWidthPercentage(100);
            bottomTbl.setWidths(new float[]{1f, 1f});

            PdfPCell cComent = new PdfPCell();
            cComent.setBorder(Rectangle.NO_BORDER);
            cComent.setPaddingTop(4);
            if (pedido.getComentario() != null && !pedido.getComentario().isBlank()) {
                Paragraph lbl = new Paragraph("COMENTARIO", fComentLbl);
                lbl.setSpacingAfter(4);
                cComent.addElement(lbl);
                cComent.addElement(new Paragraph(pedido.getComentario(), fComentVal));
            }
            bottomTbl.addCell(cComent);

            PdfPTable totBox = new PdfPTable(2);
            totBox.setWidthPercentage(100);
            totBox.setWidths(new float[]{1.4f, 1f});

            pdfTotalRow(totBox, "Bruto",                              nf.format(bruto),          fTotLabel, fTotValue, cLine, Color.WHITE);
            if (pedido.getDescuento() > 0)
                pdfTotalRow(totBox, "Descuento (" + pedido.getDescuento() + "%)", "-" + nf.format(descuentoImporte), fTotLabel, fTotValue, cLine, Color.WHITE);
            pdfTotalRow(totBox, "Base imponible",                     nf.format(base),           fTotLabel, fTotValue, cLine, Color.WHITE);
            pdfTotalRow(totBox, "IVA (" + pedido.getIva() + "%)",    nf.format(ivaImporte),     fTotLabel, fTotValue, cLine, Color.WHITE);
            pdfTotalRow(totBox, "TOTAL",                              nf.format(total),           fGrandLabel, fGrandVal, cDark, cDark);

            PdfPCell cTotBox = new PdfPCell(totBox);
            cTotBox.setBorder(Rectangle.NO_BORDER);
            cTotBox.setPadding(0);
            bottomTbl.addCell(cTotBox);
            doc.add(bottomTbl);

            // ── FOOTER ──────────────────────────────────────────────────
            doc.add(new Paragraph(" "));
            Paragraph footerP = new Paragraph("Deposito GestorVentas · " + LocalDate.now().getYear(), fFooter);
            footerP.setAlignment(Element.ALIGN_CENTER);
            doc.add(footerP);

            doc.close();
            return baos.toByteArray();

        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    private void pdfLabelValue(PdfPCell container, String label, String value, Font fLabel, Font fValue) {
        Paragraph pLabel = new Paragraph(label, fLabel);
        pLabel.setSpacingBefore(8);
        container.addElement(pLabel);
        container.addElement(new Paragraph(value, fValue));
    }

    private void pdfHeaderCell(PdfPTable table, String text, int align, Font font, Color bg, Color border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setPadding(9);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private void pdfDataCell(PdfPTable table, String text, int align, Font font, Color bg, Color border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setPadding(9);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private void pdfTotalRow(PdfPTable table, String label, String value,
                              Font fLabel, Font fValue, Color border, Color bg) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLabel));
        cLabel.setBorderColor(border);
        cLabel.setBackgroundColor(bg);
        cLabel.setPadding(8);
        cLabel.setPaddingLeft(10);
        table.addCell(cLabel);

        PdfPCell cValue = new PdfPCell(new Phrase(value, fValue));
        cValue.setBorderColor(border);
        cValue.setBackgroundColor(bg);
        cValue.setPadding(8);
        cValue.setPaddingRight(10);
        cValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cValue);
    }

    /**
     * Genera un token unico para la obtencion del pdf mediante token
     * @return codigo hash unico
     */
    private String generarTokenUnico() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (pedidoRepository.existsByToken(token));
        return token;
    }

    @Transactional
    public PedidoResponseDto reabrir(Long idCliente, Long id) {
        Cliente cliente = clienteRepository.findById(idCliente).orElse(null);
        if (cliente == null)
            throw new RuntimeException("Cliente inexistente");
        Pedido pedido = pedidoRepository.findById(id).orElse(null);
        if (pedido == null)
            throw new RuntimeException("Pedido inexistente");
        if (!pedido.getCliente().equals(cliente))
            throw new RuntimeException("Pedido inexistente");

        List<LineaPedido> lineas = lineaPedidoRepository.getLineaPedidoByPedido(pedido);
        for (LineaPedido linea : lineas) {
            linea.setStockFinal(null);
        }
        lineaPedidoRepository.saveAll(lineas);

        pedido.setFinalizado(false);
        pedido = pedidoRepository.save(pedido);

        return new PedidoResponseDto(pedido);
    }

    @Transactional
    public PedidoResponseDto reabrir(long idVendedor, long idCliente, long id) {
        Vendedor vendedor = vendedorRepository.findById(idVendedor);
        if (vendedor == null)
            throw new RuntimeException("Vendedor inexistente");
        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null || cliente.getVendedor() == null || cliente.getVendedor().getId() != idVendedor)
            throw new RuntimeException("Cliente inexistente");
        Pedido pedido = pedidoRepository.findById(id);
        if (pedido == null || pedido.getCliente().getId() != idCliente)
            throw new RuntimeException("Pedido inexistente");

        List<LineaPedido> lineas = lineaPedidoRepository.getLineaPedidoByPedido(pedido);
        for (LineaPedido linea : lineas) {
            linea.setStockFinal(null);
        }
        lineaPedidoRepository.saveAll(lineas);

        pedido.setFinalizado(false);
        pedido = pedidoRepository.save(pedido);

        return new PedidoResponseDto(pedido);
    }
}
