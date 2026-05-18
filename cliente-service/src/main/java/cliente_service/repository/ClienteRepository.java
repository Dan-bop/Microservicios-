package cliente_service.repository;

import cliente_service.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByDni(String dni);
    Optional<Cliente> findByDni(String dni);
    List<Cliente> findByNombreContainingIgnoreCaseOrDniContaining(String nombre, String dni);
    List<Cliente> findByEstado(Cliente.Estado estado);
}