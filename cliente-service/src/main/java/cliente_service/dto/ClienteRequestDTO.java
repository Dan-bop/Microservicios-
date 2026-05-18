package cliente_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ClienteRequestDTO {

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String celular;
    private String correo;
    private String direccion;

    @NotNull(message = "El monto mensual es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal montoMensual;

    private Integer diaPago;
    private BigDecimal latitud;
    private BigDecimal longitud;
}
