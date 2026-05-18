package notificacion_service.controller;

import lombok.RequiredArgsConstructor;
import notificacion_service.dto.NotificacionRequest;
import notificacion_service.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final EmailService emailService;

    @PostMapping("/enviar")
    public ResponseEntity<String> enviar(@RequestBody NotificacionRequest request) {
        emailService.procesar(request);
        return ResponseEntity.ok("Notificación enviada");
    }
}