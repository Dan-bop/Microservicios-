package auth_service.mapper;

import auth_service.dto.UsuarioResponseDTO;
import auth_service.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toDTO(Usuario u) {

        return new UsuarioResponseDTO(
                u.getId(),
                u.getUsername(),
                u.getNombreCompleto(),
                u.getRol()
        );
    }
}