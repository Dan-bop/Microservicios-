package pago_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pagos")
@Data
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "usuario_username", nullable = false)
    private String usuarioUsername;

    @Column(name = "monto_total", precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Column(name = "es_anulado")
    private Boolean esAnulado = false;

    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetallePago> detalles;

    public enum MetodoPago {
        EFECTIVO, YAPE, PLIN;

        public static boolean isValido(String valor) {
            if (valor == null) return false;
            for (MetodoPago m : values()) {
                if (m.name().equalsIgnoreCase(valor)) return true;
            }
            return false;
        }
    }
}
