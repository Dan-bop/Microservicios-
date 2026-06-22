package cliente_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private List<String> mesesDeuda;
    private String estado;
    private LocalDate inicioServicio;
}