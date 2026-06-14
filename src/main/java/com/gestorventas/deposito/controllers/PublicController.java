package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.repositories.PedidoRepository;
import com.gestorventas.deposito.services.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador público encargado de exponer recursos accesibles
 * mediante token sin necesidad de autenticación.
 * <p>
 * Actualmente permite la descarga pública de PDFs de pedidos finalizados.
 *
 * Ruta base: /pedido
 *
 * @author Kevin William Olarte Braun
 */
@RestController
@RequestMapping("/pedido")
@AllArgsConstructor
public class PublicController {

    private PedidoService pedidoService;


    /**
     * Descarga el PDF público de un pedido a partir de su token.
     *
     * @param token token público asociado al pedido
     * @return archivo PDF del pedido solicitado
     * @throws RuntimeException si el token es inválido
     *                          o el pedido no existe
     */
    @GetMapping("/download")
    @Operation(
            summary = "Descargar PDF público de un pedido",
            description = "Permite descargar el PDF de un pedido mediante un token público"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generado correctamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno generando el PDF")
    })
    public ResponseEntity<byte[]> descargarPdfPublico(@RequestParam("token") String token) {
        System.out.println("Entra!");
        byte[] pdfBytes;
        pdfBytes = pedidoService.getPDF(token);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pedido.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
