package auth_service.dto;

import auth_service.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private String nombreCompleto;
    private Usuario.Rol rol;
}