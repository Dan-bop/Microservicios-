package pago_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pago_service.model.Pago;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByClienteId(Long clienteId);

    @Query("SELECT p FROM Pago p JOIN FETCH p.detalles WHERE p.id = :id")
    Optional<Pago> findByIdWithDetalles(@Param("id") Long id);
}
