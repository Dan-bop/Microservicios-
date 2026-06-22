package auth_service.service;


import auth_service.dto.UsuarioRequestDTO;
import auth_service.dto.UsuarioResponseDTO;
import java.util.List;

public interface UsuarioService {
    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request);
    List<UsuarioResponseDTO> listar();
    void eliminar(Long id);
}