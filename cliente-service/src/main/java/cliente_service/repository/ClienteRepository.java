package cliente_service.repository;

import cliente_service.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByDni(String dni);
    Optional<Cliente> findByDni(String dni);
    List<Cliente> findByNombreContainingIgnoreCaseOrDniContaining(String nombre, String dni);
    List<Cliente> findByEstado(Cliente.Estado estado);

    @Modifying
    @Query("DELETE FROM Cliente c WHERE c.id = :id")
    void borrarDefinitivamente(@Param("id") Long id);
}