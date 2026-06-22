package pago_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pago_service.dto.ClienteResponseDTO;

import java.util.List;

@FeignClient(name = "cliente-service")
public interface ClienteClient {

    @GetMapping("/api/clientes/{id}")
    ClienteResponseDTO obtenerCliente(
            @PathVariable("id") Long id
    );

    @PutMapping("/api/clientes/{id}/quitar-deuda")
    void quitarDeuda(
            @PathVariable Long id,
            @RequestBody List<String> meses
    );

    // 👈 AGREGA ESTE MÉTODO PARA RESTAURAR LA DEUDA AL ANULAR
    @PutMapping("/api/clientes/{id}/agregar-deuda")
    void agregarDeuda(
            @PathVariable Long id,
            @RequestBody List<String> meses
    );
}