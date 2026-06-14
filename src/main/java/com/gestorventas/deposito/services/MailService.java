package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.out.PedidoResponseDto;
import com.gestorventas.deposito.models.Cliente;
import com.gestorventas.deposito.models.LineaPedido;
import com.gestorventas.deposito.models.Pedido;
import com.gestorventas.deposito.models.Vendedor;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class    MailService {

    @Autowired
    private JavaMailSender mailSender;

    // Regex para validar emails
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static boolean esEmailValido(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Envía un correo electrónico HTML con el resumen de un pedido.
     * <p>
     * El mensaje incluye la información general del pedido, cliente,
     * vendedor y líneas asociadas en formato visual HTML.
     *
     * @param destinatario listado de correos electrónicos destinatarios
     * @param pedido pedido del que se generará el contenido del correo
     * @throws MessagingException si ocurre un error durante la creación
     *                            o envío del mensaje
     */
    public void enviarCorreoPedido(String[] destinatario, Pedido pedido) throws MessagingException {
        String html = generarHtmlPedido(pedido);
        System.out.println("Envar a varios");
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setFrom("soporte@mi-app-deposito.cloud");
        helper.setTo(destinatario);
        helper.setSubject("Confirmación de pedido #"     + pedido.getId());
        helper.setText(html, true); // true para interpretar HTML

        mailSender.send(mensaje);
    }

    private String generarHtmlPedido(Pedido pedido) {
        Cliente cliente = pedido.getCliente();
        Vendedor vendedor = cliente.getVendedor();

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

        String fechaStr = "";
        try {
            LocalDate fechaObj = pedido.getFecha();
            if (fechaObj != null)
                fechaStr = fechaObj.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ignored) {}

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
        BigDecimal totalFinal = base.add(ivaImporte).setScale(2, RoundingMode.HALF_UP);

        // ── Estilos reutilizables (inline, compatibles con Gmail) ──────────
        String S_BODY    = "margin:0;padding:28px 14px;background:#f7f7f8;font-family:Arial,Helvetica,sans-serif;color:#0f172a;";
        String S_CARD    = "background:#ffffff;border:1px solid #e5e7eb;border-radius:16px;overflow:hidden;";
        String S_TH_L    = "text-align:left;font-size:11px;font-weight:700;color:#64748b;padding:11px 12px;border-bottom:1px solid #e5e7eb;background:#fafafa;text-transform:uppercase;letter-spacing:.04em;";
        String S_TH_R    = "text-align:right;font-size:11px;font-weight:700;color:#64748b;padding:11px 12px;border-bottom:1px solid #e5e7eb;background:#fafafa;text-transform:uppercase;letter-spacing:.04em;";
        String S_TD_L    = "text-align:left;font-size:13px;color:#0f172a;padding:11px 12px;border-bottom:1px solid #e5e7eb;";
        String S_TD_R    = "text-align:right;font-size:13px;color:#0f172a;padding:11px 12px;border-bottom:1px solid #e5e7eb;white-space:nowrap;";
        String S_FIELD   = "padding:10px 12px;border:1px solid #e5e7eb;border-radius:10px;background:#fff;";
        String S_LBL     = "display:block;font-size:10px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:.05em;margin-bottom:4px;";
        String S_VAL     = "font-size:13px;font-weight:600;color:#0f172a;";
        String S_TR_TOT  = "font-size:13px;color:#64748b;padding:9px 12px;border-bottom:1px solid #e5e7eb;";
        String S_TD_TOT_V= "font-size:13px;font-weight:600;color:#0f172a;padding:9px 12px;text-align:right;border-bottom:1px solid #e5e7eb;white-space:nowrap;";
        String S_TR_GRAND= "font-size:14px;font-weight:700;color:#ffffff;padding:11px 12px;background:#0f172a;";
        String S_TD_GRAND= "font-size:14px;font-weight:700;color:#ffffff;padding:11px 12px;text-align:right;background:#0f172a;white-space:nowrap;";

        StringBuilder sb = new StringBuilder();

        String pillText = "Pedido #" + pedido.getId() + (fechaStr.isBlank() ? "" : " &middot; " + fechaStr);

        sb.append("<!doctype html><html lang=\"es\"><head>")
          .append("<meta charset=\"utf-8\">")
          .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
          .append("<title>Confirmación de pedido</title>")
          .append("</head>")
          .append("<body style=\"").append(S_BODY).append("\">")
          .append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"center\">")
          .append("<table width=\"680\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:680px;width:100%;\">")
          .append("<tr><td style=\"").append(S_CARD).append("\">");

        // ── Cabecera ────────────────────────────────────────────────────
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">")
          .append("<tr>")
          .append("<td style=\"padding:20px 22px 6px;font-size:18px;font-weight:700;color:#0f172a;\">")
          .append("Confirmación de pedido</td>")
          .append("<td align=\"right\" style=\"padding:20px 22px 6px;white-space:nowrap;\">")
          .append("<span style=\"font-size:12px;color:#64748b;border:1px solid #e5e7eb;border-radius:999px;padding:5px 11px;\">")
          .append(pillText).append("</span></td>")
          .append("</tr>")
          .append("<tr><td colspan=\"2\" style=\"padding:4px 22px 14px;font-size:13px;color:#64748b;\">")
          .append("Aquí tienes el resumen del pedido.</td></tr>")
          .append("</table>");

        // Divisor
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">")
          .append("<tr><td style=\"height:1px;background:#e5e7eb;font-size:0;\"></td></tr>")
          .append("</table>");

        // ── Contenido ───────────────────────────────────────────────────
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">")
          .append("<tr><td style=\"padding:18px 22px 0;\">");

        // Campos de info (tabla de 2 columnas)
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:16px;\"><tr>");

        // Columna izquierda: Cliente + CIF
        sb.append("<td width=\"50%\" valign=\"top\" style=\"padding-right:6px;\">")
          .append("<div style=\"").append(S_FIELD).append("margin-bottom:8px;\">")
          .append("<span style=\"").append(S_LBL).append("\">Cliente</span>")
          .append("<span style=\"").append(S_VAL).append("\">").append(escapeHtml(cliente.getNombre())).append("</span>")
          .append("</div>")
          .append("<div style=\"").append(S_FIELD).append("\">")
          .append("<span style=\"").append(S_LBL).append("\">CIF</span>")
          .append("<span style=\"").append(S_VAL).append("\">").append(escapeHtml(cliente.getCif() != null ? cliente.getCif() : "-")).append("</span>")
          .append("</div>")
          .append("</td>");

        // Columna derecha: Vendedor + Email vendedor
        sb.append("<td width=\"50%\" valign=\"top\" style=\"padding-left:6px;\">");
        if (cliente.getEmail() != null && !cliente.getEmail().isBlank()) {
            sb.append("<div style=\"").append(S_FIELD).append("margin-bottom:8px;\">")
              .append("<span style=\"").append(S_LBL).append("\">Email cliente</span>")
              .append("<span style=\"").append(S_VAL).append("\">").append(escapeHtml(cliente.getEmail())).append("</span>")
              .append("</div>");
        }
        if (vendedor != null) {
            sb.append("<div style=\"").append(S_FIELD).append(cliente.getEmail() != null && !cliente.getEmail().isBlank() ? "" : "margin-bottom:8px;").append("\">")
              .append("<span style=\"").append(S_LBL).append("\">Vendedor</span>")
              .append("<span style=\"").append(S_VAL).append("\">").append(escapeHtml(vendedor.getNombre() + " " + vendedor.getApellido())).append("</span>")
              .append("</div>");
        }
        sb.append("</td></tr></table>");

        // ── Tabla de líneas ─────────────────────────────────────────────
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;margin-bottom:16px;\">")
          .append("<thead><tr>")
          .append("<th style=\"").append(S_TH_L).append("\">Producto</th>")
          .append("<th style=\"").append(S_TH_R).append("\">Cantidad</th>")
          .append("<th style=\"").append(S_TH_R).append("\">Precio</th>")
          .append("<th style=\"").append(S_TH_R).append("\">Total</th>")
          .append("</tr></thead><tbody>");

        for (var linea : pedido.getLineas()) {
            BigDecimal precio = linea.getPrecio();
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(linea.getCantidad())).setScale(2, RoundingMode.HALF_UP);
            sb.append("<tr>")
              .append("<td style=\"").append(S_TD_L).append("\">").append(escapeHtml(linea.getProducto().getDescripcion())).append("</td>")
              .append("<td style=\"").append(S_TD_R).append("\">").append(linea.getCantidad()).append("</td>")
              .append("<td style=\"").append(S_TD_R).append("\">").append(nf.format(precio)).append("</td>")
              .append("<td style=\"").append(S_TD_R).append("\">").append(nf.format(subtotal)).append("</td>")
              .append("</tr>");
        }
        sb.append("</tbody></table>");

        // ── Totales (alineado a la derecha) ─────────────────────────────
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:16px;\"><tr>")
          .append("<td></td>") // espacio izquierdo
          .append("<td width=\"300\" valign=\"top\">")
          .append("<table width=\"300\" cellpadding=\"0\" cellspacing=\"0\" style=\"border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;\">");

        emailTotalRow(sb, "Bruto", nf.format(bruto), S_TR_TOT, S_TD_TOT_V, false);
        if (pedido.getDescuento() > 0)
            emailTotalRow(sb, "Descuento (" + pedido.getDescuento() + "%)", "-" + nf.format(descuentoImporte), S_TR_TOT, S_TD_TOT_V, false);
        emailTotalRow(sb, "Base imponible", nf.format(base), S_TR_TOT, S_TD_TOT_V, false);
        emailTotalRow(sb, "IVA (" + pedido.getIva() + "%)", nf.format(ivaImporte), S_TR_TOT, S_TD_TOT_V, false);
        emailTotalRow(sb, "TOTAL", nf.format(totalFinal), S_TR_GRAND, S_TD_GRAND, true);

        sb.append("</table></td></tr></table>");

        // Comentario
        if (pedido.getComentario() != null && !pedido.getComentario().isBlank()) {
            sb.append("<p style=\"margin:0 0 12px;font-size:12px;color:#64748b;line-height:1.6;\">")
              .append("<strong>Comentario:</strong> ").append(escapeHtml(pedido.getComentario())).append("</p>");
        }

        // Nota final
        sb.append("<p style=\"margin:0 0 4px;font-size:12px;color:#64748b;line-height:1.6;\">")
          .append("Gracias por confiar en nosotros. Si tienes cualquier duda sobre este pedido, responde a este correo.")
          .append("</p>");

        sb.append("</td></tr></table>"); // cierre contenido
        sb.append("</td></tr></table>"); // cierre card
        sb.append("</td></tr></table>"); // cierre wrapper 680
        sb.append("<p style=\"text-align:center;font-size:11px;color:#94a3b8;margin-top:14px;\">")
          .append("Depósito GestorVentas &middot; No respondas a este mensaje si es automático</p>");
        sb.append("</td></tr></table>"); // cierre body table
        sb.append("</body></html>");

        return sb.toString();
    }

    private void emailTotalRow(StringBuilder sb, String label, String value,
                               String sLabel, String sValue, boolean isGrand) {
        String border = isGrand ? "" : "border-bottom:1px solid #e5e7eb;";
        sb.append("<tr>")
          .append("<td style=\"").append(sLabel).append(border).append("\">").append(escapeHtml(label)).append("</td>")
          .append("<td style=\"").append(sValue).append(border).append("\">").append(escapeHtml(value)).append("</td>")
          .append("</tr>");
    }

    /** Escape mínimo para evitar que un nombre con "<" rompa el HTML */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Envía un correo electrónico en texto plano.
     * <p>
     * Permite enviar mensajes simples sin contenido HTML utilizando
     * el servicio de correo configurado en la aplicación.
     *
     * @param to dirección de correo destinataria
     * @param subject asunto del mensaje
     * @param body contenido textual del correo
     */
    public void sendEmail(String to, String subject, String body) {
        if (!esEmailValido(to)) {
            throw new RuntimeException("Correo electrónico inválido");
        }
        System.out.println("Envar a solo uno");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("soporte@mi-app-deposito.cloud");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    /**
     * Envía el correo de confirmación de un pedido a una lista de vendedores.
     * <p>
     * Extrae los correos electrónicos de los vendedores recibidos y delega
     * el envío del mensaje HTML en {@link #enviarCorreoPedido(String[], Pedido)}.
     *
     * @param vendedorEnviar lista de vendedores destinatarios del correo
     * @param pedido pedido del que se generará la confirmación
     * @throws MessagingException si ocurre un error al crear o enviar el correo
     */
    public void enviarCorreosPedido(List<Vendedor> vendedorEnviar, Pedido pedido) throws MessagingException {
        if (vendedorEnviar == null || vendedorEnviar.isEmpty()) return;
        if (pedido == null) return;


       /* String[] emails = vendedorEnviar.stream()
                .map(Vendedor::getEmail)
                .filter(Objects::nonNull)
                .filter(MailService::esEmailValido)
                .distinct()
                .toArray(String[]::new); */

        String[] emails = Stream.concat(
                vendedorEnviar.stream()
                        .map(Vendedor::getEmail)
                        .filter(Objects::nonNull)
                        .filter(MailService::esEmailValido),
                Stream.of("kevinolarte.ko@gmail.com")
        ).distinct().toArray(String[]::new);

        if (emails.length == 0) return;

        enviarCorreoPedido(emails, pedido); // HTML
    }
}
