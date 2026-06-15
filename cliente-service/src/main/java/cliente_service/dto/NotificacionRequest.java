package cliente_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class NotificacionRequest {
    private String tipo;
    private String correoDestino;
    private String nombreCliente;
    private BigDecimal montoMensual;
    private Integer diaPago;
    private BigDecimal montoTotal;
    private String metodoPago;
    private List<String> meses;
    private Integer mesesDeuda;
}