package cliente_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ClienteDeudaDTO {
    private Long clienteId;
    private String clienteNombre;
    private List<String> mesesDeuda;
    private BigDecimal totalDeuda;
}