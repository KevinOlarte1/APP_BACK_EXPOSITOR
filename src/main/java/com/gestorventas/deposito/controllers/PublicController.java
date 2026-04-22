package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.repositories.PedidoRepository;
import com.gestorventas.deposito.services.PedidoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedido")
public class PublicController {

    private PedidoService pedidoService;


    @GetMapping("/download")
    public ResponseEntity<byte[]> descargarPdfPublico(@RequestParam("token") String token) {
        byte[] pdfBytes;
        pdfBytes = pedidoService.getPDF(token);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pedido.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
