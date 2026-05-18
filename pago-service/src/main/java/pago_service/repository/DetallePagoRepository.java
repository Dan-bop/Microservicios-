package pago_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pago_service.model.DetallePago;

import java.util.Optional;

public interface DetallePagoRepository extends JpaRepository<DetallePago, Long> {

    @Query("SELECT d FROM DetallePago d WHERE d.pago.clienteId = :clienteId AND d.mes = :mes AND d.anio = :anio AND d.pago.esAnulado = false")
    Optional<DetallePago> findPagoExistente(
            @Param("clienteId") Long clienteId,
            @Param("mes") String mes,
            @Param("anio") Integer anio);
}