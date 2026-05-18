package cliente_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ClienteResponseDTO {
    private Long id;
    private String dni;
    private String nombre;
    private String celular;
    private String correo;
    private String direccion;
    private BigDecimal montoMensual;
    private Integer diaPago;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String estado;
}

