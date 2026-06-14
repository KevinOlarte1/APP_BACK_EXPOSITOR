package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.services.CategoriaService;
import com.gestorventas.deposito.services.ClienteService;
import com.gestorventas.deposito.services.PedidoService;
import com.gestorventas.deposito.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * Controlador REST encargado de exportar información del sistema
 * en formato CSV.
 * <p>
 * Permite descargar listados de productos, clientes, pedidos y categorías.
 * Todas las operaciones están restringidas a usuarios con rol ADMIN.
 *
 * Ruta base: /api/config/export
 *
 * @author Kevin William Olarte Braun
 */
@RestController
@RequestMapping("/api/config/export")
@RequiredArgsConstructor
public class ExportController {
    private final ProductoService productoService;
    private final ClienteService clienteService;
    private final PedidoService pedidoService;
    private final CategoriaService categoriaService;

    /**
     * Exporta el listado de productos registrados en formato CSV.
     *
     * @return archivo CSV descargable con los productos del sistema
     */
    @GetMapping("/productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> exportProductosCsv() {
        return buildCsvResponse(
                productoService.exportProductosCsv(),
                "productos.csv"
        );
    }

    /**
     * Exporta el listado de clientes registrados en formato CSV.
     *
     * @return archivo CSV descargable con los clientes del sistema
     */
    @GetMapping("/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> exportClientesCsv() {
        return buildCsvResponse(
                clienteService.exportClientesCsv(),
                "clientes.csv"
        );
    }

    /**
     * Exporta el listado de pedidos registrados en formato CSV.
     *
     * @return archivo CSV descargable con los pedidos del sistema
     */
    @GetMapping("/pedidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> exportPedidosCsv() {
        return buildCsvResponse(
                pedidoService.exportPedidosCsv(),
                "pedidos.csv"
        );
    }

    /**
     * Exporta el listado de categorías registradas en formato CSV.
     *
     * @return archivo CSV descargable con las categorías del sistema
     */
    @GetMapping("/categorias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> exportarCategoriasCsv() {
        return buildCsvResponse(
                categoriaService.exportCategoriasCsv(),
                "categorias.csv"
        );
    }

    /**
     * Construye una respuesta HTTP para la descarga de un archivo CSV.
     * <p>
     * Configura las cabeceras necesarias para que el navegador trate
     * la respuesta como un archivo descargable adjunto.
     *
     * @param data contenido del archivo CSV en formato byte[]
     * @param filename nombre que tendrá el archivo descargado
     * @return respuesta HTTP con el recurso CSV listo para descarga
     */
    private ResponseEntity<ByteArrayResource> buildCsvResponse(byte[] data, String filename) {
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(resource);
    }
}
