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

@RestController
@RequestMapping("/api/config/export")
@RequiredArgsConstructor
public class ExportController {
    private final ProductoService productoService;
    private final ClienteService clienteService;
    private final PedidoService pedidoService;
    private final CategoriaService categoriaService;

    @GetMapping("/productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> exportProductosCsv() {

        byte[] data = productoService.exportProductosCsv();
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping("/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> exportClientesCsv() {

        byte[] data = clienteService.exportClientesCsv();
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clientes.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping("/pedidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> exportPedidosCsv() {

        byte[] data = pedidoService.exportPedidosCsv();
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pedidos.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping("/categorias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> exportarCategoriasCsv() {

        byte[] data = categoriaService.exportCategoriasCsv();
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=categorias.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(resource);
    }


}
