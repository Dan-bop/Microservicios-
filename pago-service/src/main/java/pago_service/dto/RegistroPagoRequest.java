package pago_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RegistroPagoRequest {

    @NotNull
    private long clienteId;

    @NotNull
    private BigDecimal montoMensual;

    @NotNull
    private List<MesPagoDTO> meses;

    @NotNull
    private String metodoPago;
}
