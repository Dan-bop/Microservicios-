package pago_service.mapper;

import org.springframework.stereotype.Component;
import pago_service.dto.PagoResponseDTO;
import pago_service.model.Pago;

@Component
public class PagoMapper {

    public PagoResponseDTO toDTO(Pago p) {
        return new PagoResponseDTO(
                p.getId(),
                p.getClienteId(),
                p.getMontoTotal(),
                (p.getMetodoPago() != null) ? p.getMetodoPago().name() : "N/A",
                (p.getFechaPago() != null) ? p.getFechaPago().toString() : "",
                p.getEsAnulado()
        );
    }
}