package pago_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pago_service.dto.ClienteResponseDTO;

@FeignClient(name = "cliente-service", url = "http://localhost:8082")
public interface ClienteClient {

    @GetMapping("/api/clientes/{id}")
    ClienteResponseDTO obtenerCliente(@PathVariable Long id);
}