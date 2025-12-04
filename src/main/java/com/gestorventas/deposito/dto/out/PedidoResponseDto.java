package com.gestorventas.deposito.dto.out;

import com.gestorventas.deposito.models.LineaPedido;
import com.gestorventas.deposito.models.Pedido;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de salida para representar los datos públicos de un pedido del sistema.
 * <p>
 * Esta clase se utiliza para devolver información básica del pedido en las respuestas
 * de la API, sin incluir detalles.
 * </p>
 *
 * @author Kevin William Olarte Braun
 */
@Getter
@Setter
public class PedidoResponseDto {
        private Long id;
        private LocalDate fecha;
        private Long idCliente;
        private List<Long> idLineaPedido;
        private boolean cerrado;
        private int descuento;
        private int iva;
        private String brutoTotal;
        private String baseImponible;
        private String precioIva;
        private String total;

    public PedidoResponseDto(Pedido pedido) {
        this.id = pedido.getId();
        this.fecha = pedido.getFecha();
        this.idCliente = pedido.getCliente().getId();
        this.idLineaPedido = pedido.getLineas()
                .stream()
                .map(LineaPedido::getId)
                .toList();
        this.cerrado = pedido.isFinalizado();
        this.descuento = pedido.getDescuento();
        this.iva = pedido.getIva();

        // -----------------------------
        // BRUTO TOTAL
        // -----------------------------
        var bruto = pedido.getBrutoTotal()
                .setScale(2, RoundingMode.HALF_UP);

        this.brutoTotal = bruto.toString();

        // -----------------------------
        // BASE IMPONIBLE (descuento)
        // base = bruto * (100 - descuento) / 100
        // -----------------------------
        var porcentajeDescuento = BigDecimal
                .valueOf(100 - pedido.getDescuento())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        var base = bruto.multiply(porcentajeDescuento)
                .setScale(2, RoundingMode.HALF_UP);

        this.baseImponible = base.toString();

        // -----------------------------
        // PRECIO IVA = base * iva/100
        // -----------------------------
        var porcentajeIva = BigDecimal
                .valueOf(pedido.getIva())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        var ivaImporte = base.multiply(porcentajeIva)
                .setScale(2, RoundingMode.HALF_UP);

        this.precioIva = ivaImporte.toString();

        // -----------------------------
        // TOTAL = base + iva
        // -----------------------------
        var totalFinal = base.add(ivaImporte)
                .setScale(2, RoundingMode.HALF_UP);

        this.total = totalFinal.toString();
    }

}
