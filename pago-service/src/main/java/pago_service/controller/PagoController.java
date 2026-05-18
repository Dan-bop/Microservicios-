package pago_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pago_service.dto.PagoResponseDTO;
import pago_service.dto.RegistroPagoRequest;
import pago_service.service.PagoService;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping("/cobrar")
    public ResponseEntity<PagoResponseDTO> cobrar(
            @Valid @RequestBody RegistroPagoRequest request,
            @RequestHeader("X-Username") String username) {
        return ResponseEntity.ok(pagoService.registrarPago(request, username));
    }

    @PutMapping("/anular/{id}")
    public ResponseEntity<String> anular(
            @PathVariable Long id,
            @RequestParam String motivo,
            @RequestHeader("X-Username") String username) {
        pagoService.anular(id, motivo, username);
        return ResponseEntity.ok("Pago anulado correctamente");
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PagoResponseDTO>> listarPorCliente(
            @PathVariable Long clienteId) {
        return ResponseEntity.ok(pagoService.listarPorCliente(clienteId));
    }
}
