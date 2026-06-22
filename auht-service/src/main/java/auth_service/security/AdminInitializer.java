package auth_service.security;

import auth_service.model.Usuario;
import auth_service.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Esta variable toma el valor del .env o del application.yml
    @Value("${admin.default.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        // Solo crea el admin si la base de datos está vacía
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setRol(Usuario.Rol.ADMIN);
            admin.setPassword(passwordEncoder.encode(adminPassword));

            usuarioRepository.save(admin);
            System.out.println(">>> [INFO] Administrador inicial creado correctamente.");
        }
    }
}