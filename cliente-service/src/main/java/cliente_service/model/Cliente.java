package cliente_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // Lista de meses específicos que debe (Ej: ["Enero 2026", "Febrero 2026"])
    @ElementCollection
    @CollectionTable(name = "cliente_meses_deuda", joinColumns = @JoinColumn(name = "cliente_id"))
    @Column(name = "mes")
    private List<String> mesesDeuda = new ArrayList<>();

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "inicio_servicio")
    private LocalDate inicioServicio;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.ACTIVO;

    public enum Estado {
        ACTIVO, INACTIVO
    }
}