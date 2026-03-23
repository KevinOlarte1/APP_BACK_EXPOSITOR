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

    public void enviarPrueba() throws MessagingException {
        String txt = "Hola mundo!";
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo("kevinolarte.ko@gmail.com");
        helper.setSubject("Prueba de mensaje");
        helper.setText(txt, false);
        helper.setFrom("tucorreo@empresa.com");

        mailSender.send(mensaje);


    }

    public void enviarCorreoPedido(String[] destinatario, Pedido pedido) throws MessagingException {
        String html = generarHtmlPedido(pedido);
        System.out.println("html generado");

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(destinatario);
        helper.setSubject("Confirmación de pedido #" + pedido.getId());
        helper.setText(html, true); // true para interpretar HTML
        helper.setFrom("tucorreo@empresa.com");

        mailSender.send(mensaje);
        System.out.println("Envado!!---");
    }

    private String generarHtmlPedido(Pedido pedido) {
        PedidoResponseDto pedidoMuestreo = new PedidoResponseDto(pedido);
        Cliente cliente = pedido.getCliente();
        Vendedor vendedor = cliente.getVendedor();


        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

        // Si tu pedido NO tiene fecha, deja esto como "" y quita la parte del pill.
        String fechaStr = "";
        try {
            // Cambia getFecha() por el getter real si es distinto (getCreatedAt, getFechaPedido, etc.)
            // Si es LocalDateTime, ajusta el formatter.
            LocalDate fechaObj = pedido.getFecha(); // <-- si NO existe, borra estas 8 líneas y usa fechaStr=""
            if (fechaObj != null) {
                fechaStr = fechaObj.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        } catch (Exception ignored) {
            fechaStr = "";
        }

        System.out.println("Empezando html");

        StringBuilder sb = new StringBuilder();

        sb.append("""
        <!doctype html>
        <html lang="es">
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width,initial-scale=1" />
          <title>Confirmación de pedido</title>
          <style>
            :root{
              --bg:#f7f7f8;
              --card:#ffffff;
              --text:#0f172a;
              --muted:#64748b;
              --line:#e5e7eb;
              --accent:#111827;
            }
            *{ box-sizing:border-box; }
            body{
              margin:0;
              background:var(--bg);
              font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial, "Noto Sans", "Helvetica Neue", sans-serif;
              color:var(--text);
              padding: 28px 14px;
            }
            .wrap{ max-width: 720px; margin: 0 auto; }
            .card{
              background:var(--card);
              border:1px solid var(--line);
              border-radius: 16px;
              overflow:hidden;
              box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
            }
            .topbar{ padding: 22px 22px 14px; }
            .title{
              display:flex;
              align-items: baseline;
              justify-content: space-between;
              gap: 12px;
              margin:0;
            }
            .title h1{
              margin:0;
              font-size:18px;
              letter-spacing: -0.01em;
              font-weight: 700;
            }
            .pill{
              display:inline-block;
              border:1px solid var(--line);
              border-radius: 999px;
              padding: 6px 10px;
              font-size:12px;
              color: var(--muted);
              background:#fff;
              white-space: nowrap;
            }
            .subtitle{
              margin:10px 0 0;
              color: var(--muted);
              font-size: 13px;
              line-height: 1.5;
            }
            .divider{ height:1px; background:var(--line); margin: 0; }
            .content{ padding: 18px 22px 22px; }
            .grid{
              display:grid;
              grid-template-columns: 1fr 1fr;
              gap: 10px 14px;
              margin-bottom: 16px;
            }
            .field{
              padding: 10px 12px;
              border:1px solid var(--line);
              border-radius: 12px;
              background:#fff;
            }
            .label{
              display:block;
              font-size: 11px;
              color: var(--muted);
              margin-bottom: 6px;
              letter-spacing: .02em;
              text-transform: uppercase;
            }
            .value{
              font-size: 14px;
              font-weight: 600;
              color: var(--text);
            }
            table{
              width:100%;
              border-collapse: collapse;
              margin-top: 8px;
              border:1px solid var(--line);
              border-radius: 12px;
              overflow:hidden;
            }
            thead th{
              text-align:left;
              font-size: 12px;
              color: var(--muted);
              font-weight: 700;
              padding: 12px 12px;
              border-bottom: 1px solid var(--line);
              background:#fafafa;
              letter-spacing: .02em;
              text-transform: uppercase;
            }
            tbody td{
              padding: 12px 12px;
              border-bottom: 1px solid var(--line);
              font-size: 13px;
              vertical-align: top;
            }
            tbody tr:last-child td{ border-bottom:none; }
            .num{ text-align:right; white-space: nowrap; }
            .prod{ font-weight: 700; margin:0; font-size: 13px; }
            .sku{ margin: 3px 0 0; color: var(--muted); font-size: 12px; }
            .totals{
              margin-top: 14px;
              display:flex;
              justify-content:flex-end;
            }
            .totalsBox{
              width: 320px;
              border:1px solid var(--line);
              border-radius: 12px;
              overflow:hidden;
              background:#fff;
            }
            .row{
              display:flex;
              justify-content: space-between;
              padding: 10px 12px;
              font-size: 13px;
              border-bottom:1px solid var(--line);
              color: var(--text);
            }
            .row span:first-child{ color: var(--muted); }
            .row:last-child{
              border-bottom:none;
              font-weight: 800;
              font-size: 14px;
              background: #0f172a;
              color: #fff;
            }
            .row:last-child span:first-child{ color: #e2e8f0; }
            .note{
              margin: 16px 0 0;
              color: var(--muted);
              font-size: 12px;
              line-height: 1.6;
            }
            .footer{
              margin-top: 12px;
              padding: 14px 18px;
              color: var(--muted);
              font-size: 11px;
              text-align:center;
            }
            @media (max-width: 520px){
              .grid{ grid-template-columns: 1fr; }
              .totalsBox{ width:100%; }
              .title{ align-items:flex-start; flex-direction:column; }
            }
          </style>
        </head>
        <body>
          <div class="wrap">
            <div class="card">
              <div class="topbar">
                <div class="title">
                  <h1>Confirmación de pedido</h1>
        """);

        // Pill: Pedido #id · fecha (si no hay fecha, solo pedido)
        sb.append("<span class=\"pill\">Pedido #")
                .append(pedido.getId());

        if (!fechaStr.isBlank()) {
            sb.append(" · ").append(fechaStr);
        }
        sb.append("</span>");

        sb.append("""
                </div>
                <p class="subtitle">
                  Aquí tienes el resumen del pedido.
                </p>
              </div>

              <div class="divider"></div>

              <div class="content">
                <div class="grid">
        """);

        // Cliente (ajusta si tu Cliente tiene getNombre/getApellido)
        sb.append("<div class=\"field\"><span class=\"label\">Cliente</span><span class=\"value\">")
                .append(escapeHtml(cliente.getNombre()))
                .append("</span></div>");

        sb.append("<div class=\"field\"><span class=\"label\">Email cliente</span><span class=\"value\">")
                .append(escapeHtml(cliente.getEmail())) // si no existe, cambia/elimina
                .append("</span></div>");

        sb.append("<div class=\"field\"><span class=\"label\">Vendedor</span><span class=\"value\">")
                .append(escapeHtml(vendedor.getNombre() + " " + vendedor.getApellido()))
                .append("</span></div>");

        sb.append("<div class=\"field\"><span class=\"label\">Email vendedor</span><span class=\"value\">")
                .append(escapeHtml(vendedor.getEmail()))
                .append("</span></div>");

        sb.append("""
                </div>

                <table>
                  <thead>
                    <tr>
                      <th>Producto</th>
                      <th class="num">Cantidad</th>
                      <th class="num">Precio</th>
                      <th class="num">Total</th>
                    </tr>
                  </thead>
                  <tbody>
        """);

        // ====== LINEAS ======
        // Ajusta aquí los getters según tu DTO de líneas
        for (var linea : pedido.getLineas()) { // <-- si tu getter se llama distinto, cámbialo
            String producto = String.valueOf(linea.getProducto()); // puede ser String o un objeto
            int cantidad = linea.getCantidad();
            BigDecimal precio = linea.getPrecio();
            BigDecimal subtotal = precio
                    .multiply(BigDecimal.valueOf(cantidad))
                    .setScale(2, RoundingMode.HALF_UP);

            sb.append("<tr>");

            sb.append("<td>")
                    .append("<p class=\"prod\">").append(escapeHtml(producto)).append("</p>");
            // Si no tienes SKU, quita esta línea:
            // sb.append("<p class=\"sku\">").append("SKU: ").append(escapeHtml(linea.getSku())).append("</p>");
            sb.append("</td>");

            sb.append("<td class=\"num\">").append(cantidad).append("</td>");
            sb.append("<td class=\"num\">").append(nf.format(precio)).append("</td>");
            sb.append("<td class=\"num\">").append(nf.format(subtotal)).append("</td>");

            sb.append("</tr>");
        }

        sb.append("""
                  </tbody>
                </table>
        """);

        // ====== TOTALES (SIN CALCULAR) ======
        // Ajusta estos getters a los reales de PedidoResponseDto
        sb.append("<div class=\"totals\"><div class=\"totalsBox\">");

        sb.append("<div class=\"row\"><span>Bruto</span><span>")
                .append(pedidoMuestreo.getBrutoTotal())
                .append("</span></div>");

        sb.append("<div class=\"row\"><span>Base imponible</span><span>")
                .append(pedidoMuestreo.getBaseImponible())
                .append("</span></div>");

        sb.append("<div class=\"row\"><span>IVA</span><span>")
                .append(pedidoMuestreo.getPrecioIva())
                .append("</span></div>");

        sb.append("<div class=\"row\"><span>Total</span><span>")
                .append(pedidoMuestreo.getTotal())
                .append("</span></div>");

        sb.append("</div></div>");

        sb.append("""
                <p class="note">
                  Gracias por confiar en nuestra empresa. Si no has realizado este pedido,
                  responde a este correo y lo revisamos.
                </p>
              </div>
            </div>

            <div class="footer">
              Depósito GestorVentas · No respondas a este mensaje si es automático
            </div>
          </div>
        </body>
        </html>
        """);

        return sb.toString();
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

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void enviarCorreosPedido(List<Vendedor> vendedorEnviar, Pedido pedido) throws MessagingException {
        if (vendedorEnviar == null || vendedorEnviar.isEmpty()) return;
        if (pedido == null) return;

        System.out.println("Entra bien!! asdasdasdasdasd");

        String[] emails = vendedorEnviar.stream()
                        .map(Vendedor::getEmail)
                        . filter(Objects::nonNull)
                        .toArray(String[]::new);

        enviarCorreoPedido(emails, pedido); // HTML
    }
}
