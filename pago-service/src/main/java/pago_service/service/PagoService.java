package pago_service.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pago_service.client.ClienteClient;
import pago_service.config.RabbitMQConfig;
import pago_service.dto.*;
import pago_service.model.DetallePago;
import pago_service.model.Pago;
import pago_service.repository.DetallePagoRepository;
import pago_service.repository.PagoRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository pagoRepository;
    private final DetallePagoRepository detallePagoRepository;
    private final ClienteClient clienteClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public PagoResponseDTO registrarPago(RegistroPagoRequest request, String username) {

        if (request.getMeses() == null || request.getMeses().isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un mes");
        }

        if (!Pago.MetodoPago.isValido(request.getMetodoPago())) {
            throw new IllegalArgumentException("Método de pago inválido: " + request.getMetodoPago());
        }

        // Obtener datos del cliente desde cliente-service via Feign
        ClienteResponseDTO cliente = clienteClient.obtenerCliente(request.getClienteId());

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado: " + request.getClienteId());
        }

        if ("INACTIVO".equals(cliente.getEstado())) {
            throw new IllegalArgumentException("El cliente está inactivo");
        }

        long distintos = request.getMeses().stream()
                .map(m -> m.getMes().toUpperCase() + "-" + m.getAnio())
                .distinct().count();

        if (distintos != request.getMeses().size()) {
            throw new IllegalArgumentException("Hay meses duplicados");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<DetallePago> detalles = new ArrayList<>();

        for (MesPagoDTO mesDto : request.getMeses()) {
            String mes = mesDto.getMes().toUpperCase();

            boolean yaExiste = detallePagoRepository
                    .findPagoExistente(request.getClienteId(), mes, mesDto.getAnio())
                    .isPresent();

            if (yaExiste) {
                throw new IllegalArgumentException("Periodo ya pagado: " + mes + "-" + mesDto.getAnio());
            }

            // Usar montoMensual del cliente obtenido desde cliente-service
            total = total.add(cliente.getMontoMensual());

            DetallePago detalle = new DetallePago();
            detalle.setMes(mes);
            detalle.setAnio(mesDto.getAnio());
            detalle.setMontoMes(cliente.getMontoMensual());
            detalles.add(detalle);
        }

        Pago pago = new Pago();
        pago.setClienteId(request.getClienteId());
        pago.setUsuarioUsername(username);
        pago.setMetodoPago(Pago.MetodoPago.valueOf(request.getMetodoPago().toUpperCase()));
        pago.setMontoTotal(total);

        Pago guardado = pagoRepository.save(pago);
        detalles.forEach(d -> d.setPago(guardado));
        detallePagoRepository.saveAll(detalles);

        List<String> mesesStr = request.getMeses().stream()
                .map(m -> m.getMes() + " " + m.getAnio())
                .toList();

        if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
            NotificacionRequest notif = new NotificacionRequest();
            notif.setTipo("RECIBO_PAGO");
            notif.setCorreoDestino(cliente.getCorreo());
            notif.setNombreCliente(cliente.getNombre());
            notif.setMontoTotal(total);
            notif.setMetodoPago(request.getMetodoPago());
            notif.setMeses(mesesStr);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    notif
            );
        }

        log.info("Pago registrado: {} - Cliente: {} - Total: S/ {}",
                guardado.getId(), request.getClienteId(), total);

        return toDTO(guardado);
    }

    @Transactional
    public void anular(Long pagoId, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado: " + pagoId));

        if (Boolean.TRUE.equals(pago.getEsAnulado())) {
            throw new IllegalArgumentException("El pago ya fue anulado");
        }

        pago.setEsAnulado(true);
        pago.setMotivoAnulacion(motivo);
        pagoRepository.save(pago);
    }

    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listarPorCliente(Long clienteId) {
        return pagoRepository.findByClienteId(clienteId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PagoResponseDTO buscarPorId(Long id) {
        return pagoRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado: " + id));
    }

    private PagoResponseDTO toDTO(Pago p) {
        return new PagoResponseDTO(
                p.getId(), p.getClienteId(),
                p.getMontoTotal(), p.getMetodoPago().name(),
                p.getFechaPago().toString(), p.getEsAnulado()
        );
    }
}