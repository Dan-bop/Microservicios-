package pago_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RegistroPagoRequest {

    @NotNull(message = "El clienteId es obligatorio")
    private Long clienteId;

    @NotEmpty(message = "Debe incluir al menos un mes")
    private List<MesPagoDTO> meses;

    @NotNull(message = "El método de pago es obligatorio")
    private String metodoPago;
}