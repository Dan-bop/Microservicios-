package auth_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    private Rol rol = Rol.OPERADOR;

    public enum Rol {
        ADMIN, OPERADOR
    }
}