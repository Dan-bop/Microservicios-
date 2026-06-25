package pago_service.service.serviceimpl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pago_service.client.ClienteClient;
import pago_service.config.RabbitMQConfig;
import pago_service.dto.*;
import pago_service.mapper.PagoMapper;
import pago_service.model.*;
import pago_service.repository.*;
import pago_service.service.PagoService;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoServiceImpl.class);

    private final PagoRepository pagoRepository;
    private final DetallePagoRepository detallePagoRepository;
    private final ClienteClient clienteClient;
    private final RabbitTemplate rabbitTemplate;
    private final PagoMapper mapper;

    @Override
    @Transactional
    public PagoResponseDTO registrarPago(RegistroPagoRequest request, String username) {
        // Validar que existan meses seleccionados
        if (request.getMeses() == null || request.getMeses().isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un mes");
        }
        // Validar método de pago
        if (!Pago.MetodoPago.isValido(request.getMetodoPago())) {
            throw new IllegalArgumentException(
                    "Método de pago inválido: " + request.getMetodoPago()
            );}
        // Obtener datos actuales del cliente
        ClienteResponseDTO cliente =
                clienteClient.obtenerCliente(request.getClienteId());
        // Validar existencia del cliente
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }
        // Validar estado del cliente
        if ("INACTIVO".equals(cliente.getEstado())) {
            throw new IllegalArgumentException("El cliente está inactivo");
        }
        // Evitar meses repetidos en la misma petición
        long distintos = request.getMeses()
                .stream()
                .map(m -> m.getMes().toUpperCase()
                        + "-" + m.getAnio())
                .distinct()
                .count();
        if (distintos != request.getMeses().size()) {
            throw new IllegalArgumentException("Hay meses duplicados");
        }
        BigDecimal total = BigDecimal.ZERO;
        // Crear entidad pago
        Pago pago = new Pago();
        pago.setClienteId(request.getClienteId());
        pago.setUsuarioUsername(username);
        pago.setMetodoPago(
                Pago.MetodoPago.valueOf(
                        request.getMetodoPago().toUpperCase()
                ));
        // Crear detalle por cada mes pagado
        for (MesPagoDTO mesDto : request.getMeses()) {
            String mes = mesDto.getMes().toUpperCase();
            // Revisar si ese periodo ya fue pagado
            if(detallePagoRepository
                    .findPagoExistente(
                            request.getClienteId(),
                            mes,
                            mesDto.getAnio()
                    )
                    .isPresent()) {
                throw new IllegalArgumentException(
                        "Periodo ya pagado: "
                                + mes + "-" + mesDto.getAnio()
                );
            }
            // Sumar monto mensual
            total = total.add(cliente.getMontoMensual());
            // Crear detalle del pago
            DetallePago detalle = new DetallePago();
            detalle.setMes(mes);
            detalle.setAnio(mesDto.getAnio());
            detalle.setMontoMes(cliente.getMontoMensual());
            pago.addDetalle(detalle);
        }
        // Guardar total calculado
        pago.setMontoTotal(total);
        // Guardar pago en BD
        Pago guardado = pagoRepository.save(pago);

        // QUITAR LOS MESES PAGADOS DE LA DEUDA DEL CLIENTE
        List<String> mesesPagados =
                request.getMeses()
                        .stream()
                        .map(m ->
                                m.getMes()
                                        + " "
                                        + m.getAnio()
                        )
                        .toList();
        clienteClient.quitarDeuda(
                request.getClienteId(),
                mesesPagados
        );
        // Enviar notificación por RabbitMQ
        enviarNotificacion(cliente, total, mesesPagados);
        return mapper.toDTO(guardado);
    }
    // Enviar correo de confirmación del pago
    private void enviarNotificacion(
            ClienteResponseDTO cliente,
            BigDecimal total,
            List<String> meses
    ) {
        if(cliente.getCorreo() == null ||
                cliente.getCorreo().isBlank()) {
            return;
        }
        try {
            NotificacionRequest notif =
                    new NotificacionRequest();
            notif.setTipo("RECIBO_PAGO");
            notif.setCorreoDestino(cliente.getCorreo());
            notif.setNombreCliente(cliente.getNombre());
            notif.setMontoTotal(total);
            notif.setMetodoPago("PAGO");
            notif.setMeses(meses);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    notif
            );
            log.info(
                    "Notificación enviada para {}",
                    cliente.getNombre()
            );
        } catch(Exception e) {
            log.error(
                    "Error enviando notificación: {}",
                    e.getMessage()
            );
        }
    }
    // Anular un pago existente

    @Override
    @Transactional
    public void anular(Long pagoId, String motivo) {
        // 1. Validaciones
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));

        if(Boolean.TRUE.equals(pago.getEsAnulado())) {
            throw new IllegalArgumentException("El pago ya fue anulado");
        }

        // 2. Marcar como anulado
        pago.setEsAnulado(true);
        pago.setMotivoAnulacion(motivo);
        pagoRepository.save(pago);


        // Debes obtener los meses de los detalles del pago
        List<String> mesesAActualizar = pago.getDetalles().stream()
                .map(d -> d.getMes() + " " + d.getAnio())
                .toList();

        clienteClient.agregarDeuda(pago.getClienteId(), mesesAActualizar);
    }



    // Listar pagos de un cliente
    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listarPorCliente(Long clienteId) {
        return pagoRepository
                .findByClienteId(clienteId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }
    // Buscar pago por ID
    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO buscarPorId(Long id) {
        return pagoRepository
                .findById(id)
                .map(mapper::toDTO)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Pago no encontrado"
                        )
                );}
}