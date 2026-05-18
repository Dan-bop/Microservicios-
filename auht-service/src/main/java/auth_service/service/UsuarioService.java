package auth_service.service;

import auth_service.dto.UsuarioRequestDTO;
import auth_service.dto.UsuarioResponseDTO;
import auth_service.model.Usuario;
import auth_service.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El usuario ya existe: " + request.getUsername());
        }

        Usuario.Rol rol;
        try {
            rol = Usuario.Rol.valueOf(request.getRol().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Rol inválido: " + request.getRol());
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername().trim());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rol);
        usuario.setNombreCompleto(request.getNombreCompleto());

        Usuario guardado = usuarioRepository.save(usuario);
        return new UsuarioResponseDTO(guardado.getId(), guardado.getUsername(), guardado.getRol());
    }

    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(u -> new UsuarioResponseDTO(u.getId(), u.getUsername(), u.getRol()))
                .toList();
    }
}