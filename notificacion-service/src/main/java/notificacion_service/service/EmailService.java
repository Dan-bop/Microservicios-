package notificacion_service.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import notificacion_service.dto.NotificacionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${empresa.nombre}")
    private String nombreEmpresa;

    @Value("${empresa.email}")
    private String emailEmpresa;

    @Value("${empresa.datos-pago}")
    private String datosPago;

    public void procesar(NotificacionRequest req) {
        String contenido = switch (req.getTipo()) {
            case "BIENVENIDA"    -> bienvenida(req);
            case "RECIBO_PAGO"   -> reciboPago(req);
            case "RECORDATORIO"  -> recordatorio(req);
            case "ALERTA_MORA"   -> alertaMora(req);
            case "BAJA"          -> baja(req);
            default -> throw new IllegalArgumentException("Tipo inválido: " + req.getTipo());
        };

        enviar(req.getCorreoDestino(),
                asunto(req.getTipo()), contenido);
    }

    private String bienvenida(NotificacionRequest r) {
        return """
                <h2>¡Bienvenido a %s!</h2>
                <p>Hola <b>%s</b>, es un gusto tenerte con nosotros.</p>
                <ul>
                  <li><b>Monto Mensual:</b> S/ %s</li>
                  <li><b>Día de pago:</b> %d de cada mes</li>
                </ul>
                <p>Paga mediante:<br>%s</p>
                """.formatted(nombreEmpresa, r.getNombreCliente(),
                r.getMontoMensual(), r.getDiaPago(), datosPago);
    }

    private String reciboPago(NotificacionRequest r) {
        StringBuilder meses = new StringBuilder();
        r.getMeses().forEach(m -> meses.append("<li>").append(m).append("</li>"));
        return """
                <h3 style="color:#2e7d32;">✅ Pago registrado</h3>
                <p>Hola <b>%s</b>, registramos tu pago.</p>
                <ul>
                  <li><b>Monto:</b> S/ %s</li>
                  <li><b>Método:</b> %s</li>
                </ul>
                <p><b>Meses pagados:</b></p><ul>%s</ul>
                """.formatted(r.getNombreCliente(), r.getMontoTotal(),
                r.getMetodoPago(), meses);
    }

    private String recordatorio(NotificacionRequest r) {
        return """
                <h3>⏰ Recordatorio de Pago</h3>
                <p>Hola <b>%s</b>, tu pago vence el día <b>%d</b>.</p>
                <p>Monto: <b>S/ %s</b></p>
                """.formatted(r.getNombreCliente(), r.getDiaPago(), r.getMontoMensual());
    }

    private String alertaMora(NotificacionRequest r) {
        return """
                <h3 style="color:red;">⚠️ Aviso de Mora</h3>
                <p>Estimado <b>%s</b>, tienes <b>%d mes(es)</b> pendientes.</p>
                <p>Regulariza tu pago vía:<br>%s</p>
                """.formatted(r.getNombreCliente(), r.getMesesDeuda(), datosPago);
    }

    private String baja(NotificacionRequest r) {
        return """
                <h3>Baja de Servicio</h3>
                <p>Hola <b>%s</b>, tu servicio fue dado de baja.</p>
                <p>Contáctanos si crees que es un error.</p>
                """.formatted(r.getNombreCliente());
    }

    private String asunto(String tipo) {
        return switch (tipo) {
            case "BIENVENIDA"   -> "🏠 Bienvenido - " + nombreEmpresa;
            case "RECIBO_PAGO"  -> "✅ Recibo de Pago - " + nombreEmpresa;
            case "RECORDATORIO" -> "📌 Recordatorio - " + nombreEmpresa;
            case "ALERTA_MORA"  -> "🚫 Pago Pendiente - " + nombreEmpresa;
            case "BAJA"         -> "📉 Baja - " + nombreEmpresa;
            default -> nombreEmpresa;
        };
    }

    private void enviar(String destinatario, String asunto, String cuerpo) {
        if (destinatario == null || destinatario.isBlank()) return;
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(emailEmpresa, nombreEmpresa);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(plantilla(cuerpo), true);
            mailSender.send(msg);
            log.info("Email enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", destinatario, e.getMessage());
        }
    }

    private String plantilla(String cuerpo) {
        return """
                <html><body style="font-family:Arial,sans-serif;color:#333;">
                <div style="max-width:600px;margin:auto;border:1px solid #ddd;border-radius:8px;">
                  <div style="background:#1565c0;padding:16px;text-align:center;">
                    <h2 style="color:white;margin:0;">%s</h2>
                  </div>
                  <div style="padding:24px;">%s</div>
                  <div style="background:#f5f5f5;padding:12px;font-size:0.8em;color:#777;text-align:center;">
                    Correo automático · Trujillo, La Libertad
                  </div>
                </div>
                </body></html>
                """.formatted(nombreEmpresa, cuerpo);
    }
}