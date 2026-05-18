package cliente_service.controller;

import cliente_service.dto.ClienteDeudaDTO;
import cliente_service.dto.ClienteRequestDTO;
import cliente_service.dto.ClienteResponseDTO;
import cliente_service.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponseDTO>> buscar(@RequestParam String filtro) {
        return ResponseEntity.ok(clienteService.buscar(filtro));
    }

    @GetMapping("/{id}/deuda")
    public ResponseEntity<ClienteDeudaDTO> deuda(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerDeuda(id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> registrar(
            @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.ok(clienteService.registrar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        clienteService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
