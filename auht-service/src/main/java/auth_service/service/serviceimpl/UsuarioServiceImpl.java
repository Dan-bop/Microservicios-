package auth_service.service.serviceimpl;



import auth_service.dto.UsuarioRequestDTO;
import auth_service.dto.UsuarioResponseDTO;
import auth_service.mapper.UsuarioMapper;
import auth_service.model.Usuario;
import auth_service.repository.UsuarioRepository;
import auth_service.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper mapper;

    @Override
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
        return mapper.toDTO(usuarioRepository.save(usuario));
    }

    @Override
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream().map(mapper::toDTO).toList();
    }

    @Override
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}