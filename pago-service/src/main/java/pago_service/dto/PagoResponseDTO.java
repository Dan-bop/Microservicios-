package pago_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PagoResponseDTO {
    private Long id;
    private Long clienteId;
    private BigDecimal montoTotal;
    private String metodoPago;
    private String fechaPago;
    private Boolean esAnulado;
}
