package cliente_service.service;

import cliente_service.config.RabbitMQConfig;
import cliente_service.dto.ClienteDeudaDTO;
import cliente_service.dto.ClienteRequestDTO;
import cliente_service.dto.ClienteResponseDTO;
import cliente_service.dto.NotificacionRequest;
import cliente_service.model.Cliente;
import cliente_service.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final RabbitTemplate rabbitTemplate;

    private ClienteResponseDTO toDTO(Cliente c) {
        return new ClienteResponseDTO(
                c.getId(), c.getDni(), c.getNombre(),
                c.getCelular(), c.getCorreo(), c.getDireccion(),
                c.getMontoMensual(), c.getDiaPago(),
                c.getLatitud(), c.getLongitud(),
                c.getEstado().name()
        );
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> buscar(String filtro) {
        return clienteRepository
                .findByNombreContainingIgnoreCaseOrDniContaining(filtro, filtro)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public ClienteResponseDTO registrar(ClienteRequestDTO request) {
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("DNI ya registrado: " + request.getDni());
        }

        Cliente cliente = new Cliente();
        cliente.setDni(request.getDni());
        cliente.setNombre(request.getNombre());
        cliente.setCelular(request.getCelular());
        cliente.setCorreo(request.getCorreo());
        cliente.setDireccion(request.getDireccion());
        cliente.setMontoMensual(request.getMontoMensual());
        cliente.setDiaPago(request.getDiaPago() != null ? request.getDiaPago() : 15);
        cliente.setLatitud(request.getLatitud());
        cliente.setLongitud(request.getLongitud());
        cliente.setInicioServicio(LocalDate.now());

        Cliente guardado = clienteRepository.save(cliente);

        // ✅ Publicar mensaje de bienvenida en RabbitMQ
        if (guardado.getCorreo() != null && !guardado.getCorreo().isBlank()) {
            NotificacionRequest notif = new NotificacionRequest();
            notif.setTipo("BIENVENIDA");
            notif.setCorreoDestino(guardado.getCorreo());
            notif.setNombreCliente(guardado.getNombre());
            notif.setMontoMensual(guardado.getMontoMensual());
            notif.setDiaPago(guardado.getDiaPago());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    notif
            );
        }

        return toDTO(guardado);
    }

    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));

        cliente.setNombre(request.getNombre());
        cliente.setCelular(request.getCelular());
        cliente.setCorreo(request.getCorreo());
        cliente.setDireccion(request.getDireccion());
        cliente.setMontoMensual(request.getMontoMensual());
        cliente.setDiaPago(request.getDiaPago() != null ? request.getDiaPago() : 15);
        cliente.setLatitud(request.getLatitud());
        cliente.setLongitud(request.getLongitud());

        return toDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public void desactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));

        cliente.setEstado(Cliente.Estado.INACTIVO);
        clienteRepository.save(cliente);

        // ✅ Publicar mensaje de baja en RabbitMQ
        if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
            NotificacionRequest notif = new NotificacionRequest();
            notif.setTipo("BAJA");
            notif.setCorreoDestino(cliente.getCorreo());
            notif.setNombreCliente(cliente.getNombre());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    notif
            );
        }
    }

    @Transactional(readOnly = true)
    public ClienteDeudaDTO obtenerDeuda(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));

        LocalDate inicio = cliente.getInicioServicio() != null
                ? cliente.getInicioServicio()
                : cliente.getFechaRegistro().toLocalDate();

        List<String> deuda = new ArrayList<>();
        LocalDate iterador = inicio.withDayOfMonth(1);
        LocalDate hoy = LocalDate.now().withDayOfMonth(1);

        while (!iterador.isAfter(hoy)) {
            deuda.add(traducirMes(iterador.getMonthValue()) + " " + iterador.getYear());
            iterador = iterador.plusMonths(1);
        }

        BigDecimal total = cliente.getMontoMensual()
                .multiply(BigDecimal.valueOf(deuda.size()));

        return new ClienteDeudaDTO(cliente.getId(), cliente.getNombre(), deuda, total);
    }

    private String traducirMes(int mes) {
        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        return meses[mes - 1];
    }
}