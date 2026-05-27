package pago_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ClienteResponseDTO {
    private Long id;
    private String nombre;
    private String correo;
    private BigDecimal montoMensual;
    private Integer diaPago;
    private String estado;
}
