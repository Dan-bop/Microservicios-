package cliente_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 8)
    private String dni;

    @Column(nullable = false)
    private String nombre;

    private String celular;
    private String correo;
    private String direccion;

    @Column(name = "monto_mensual", precision = 10, scale = 2)
    private BigDecimal montoMensual;

    @Column(name = "dia_pago")
    private Integer diaPago = 15;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "inicio_servicio")
    private LocalDate inicioServicio;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitud;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.ACTIVO;

    public enum Estado {
        ACTIVO, INACTIVO
    }
}
