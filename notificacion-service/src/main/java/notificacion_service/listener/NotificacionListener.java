package notificacion_service.listener;

import lombok.RequiredArgsConstructor;
import notificacion_service.config.RabbitMQConfig;
import notificacion_service.dto.NotificacionRequest;
import notificacion_service.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificacionListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionListener.class);
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.COLA_NOTIFICACIONES)
    public void procesarNotificacion(NotificacionRequest request) {
        log.info("📨 Mensaje recibido: {} para {}",
                request.getTipo(), request.getCorreoDestino());
        try {
            emailService.procesar(request);
            log.info("✅ Email enviado correctamente");
        } catch (Exception e) {
            log.error("❌ Error procesando notificación: {}", e.getMessage());
        }
    }
}
