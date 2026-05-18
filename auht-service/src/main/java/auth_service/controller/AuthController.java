package auth_service.controller;

import auth_service.dto.AuthRequest;
import auth_service.dto.AuthResponse;
import auth_service.dto.UsuarioRequestDTO;
import auth_service.dto.UsuarioResponseDTO;
import auth_service.model.Usuario;
import auth_service.repository.UsuarioRepository;
import auth_service.security.JwtUtil;
import auth_service.service.UsuarioService; // <-- Se agregó la importación de tu servicio
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtUtil.generarToken(usuario.getUsername(), usuario.getRol().name());
        return ResponseEntity.ok(new AuthResponse(token, usuario.getUsername(), usuario.getRol().name()));
    }

    // Endpoint temporal para generar hash BCrypt
    @GetMapping("/hash")
    public String hash(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }

    @PostMapping("/register") // <-- Ahora está correctamente estructurado dentro de la clase
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.ok(usuarioService.crearUsuario(request));
    }

    @GetMapping("/listar")
        public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
            return ResponseEntity.ok(usuarioService.listar());
    }
}
