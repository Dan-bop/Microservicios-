package cliente_service.service.serviceimpl;

import cliente_service.config.RabbitMQConfig;
import cliente_service.dto.*;
import cliente_service.mapper.ClienteMapper;
import cliente_service.model.Cliente;
import cliente_service.repository.ClienteRepository;
import cliente_service.service.ClienteService;
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
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ClienteMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll().stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> buscar(String filtro) {
        return clienteRepository
                .findByNombreContainingIgnoreCaseOrDniContaining(filtro, filtro)
                .stream().map(mapper::toDTO).toList();
    }

    @Override
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

        // Asignar los meses antiguos seleccionados desde Angular
        cliente.setMesesDeuda(request.getMesesDeuda() != null ? new ArrayList<>(request.getMesesDeuda()) : new ArrayList<>());

        cliente.setInicioServicio(
                request.getInicioServicio() != null ? request.getInicioServicio() : LocalDate.now()
        );

        Cliente guardado = clienteRepository.save(cliente);

        // Protección try-catch para que RabbitMQ no rompa el guardado (Error 500)
        if (guardado.getCorreo() != null && !guardado.getCorreo().isBlank()) {
            try {
                NotificacionRequest notif = new NotificacionRequest();
                notif.setTipo("BIENVENIDA");
                notif.setCorreoDestino(guardado.getCorreo());
                notif.setNombreCliente(guardado.getNombre());
                notif.setMontoMensual(guardado.getMontoMensual());
                notif.setDiaPago(guardado.getDiaPago());
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, notif);
            } catch (Exception e) {
                System.err.println("Advertencia RabbitMQ (Bienvenida): " + e.getMessage());
            }
        }
        return mapper.toDTO(guardado);
    }

    @Override
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
        if (request.getMesesDeuda() != null) {
            cliente.setMesesDeuda(new ArrayList<>(request.getMesesDeuda()));
        }
        return mapper.toDTO(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
        cliente.setEstado(Cliente.Estado.INACTIVO);
        clienteRepository.save(cliente);

        if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
            try {
                NotificacionRequest notif = new NotificacionRequest();
                notif.setTipo("BAJA");
                notif.setCorreoDestino(cliente.getCorreo());
                notif.setNombreCliente(cliente.getNombre());
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, notif);
            } catch (Exception e) {
                System.err.println("Advertencia RabbitMQ (Baja): " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void activar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
        cliente.setEstado(Cliente.Estado.ACTIVO);
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        // Usa la consulta nativa para evitar excepciones con estados inactivos
        clienteRepository.borrarDefinitivamente(id);
    }

    @Override
    @Transactional
    public void quitarMesesDeuda(Long id, List<String> meses) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Cliente no encontrado"));

        if(cliente.getMesesDeuda() != null){
            cliente.getMesesDeuda().removeAll(meses);
        }

        clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public void agregarMesesDeuda(Long id, List<String> meses) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDeudaDTO obtenerDeuda(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));

        // 1. Empezamos con los meses que se registraron manualmente (Deuda antigua)
        List<String> deudaTotal = new ArrayList<>();
        if (cliente.getMesesDeuda() != null) {
            deudaTotal.addAll(cliente.getMesesDeuda());
        }

        // 2. Calculamos los meses transcurridos desde que inició el servicio
        LocalDate inicio = cliente.getInicioServicio() != null
                ? cliente.getInicioServicio()
                : cliente.getFechaRegistro().toLocalDate();

        LocalDate iterador = inicio.withDayOfMonth(1);
        LocalDate hoy = LocalDate.now().withDayOfMonth(1);

        while (!iterador.isAfter(hoy)) {
            String mesGenerado = traducirMes(iterador.getMonthValue()) + " " + iterador.getYear();
            // Solo lo agrega si no fue ingresado manualmente antes (evita duplicados)
            if (!deudaTotal.contains(mesGenerado)) {
                deudaTotal.add(mesGenerado);
            }
            iterador = iterador.plusMonths(1);
        }

        // 3. Multiplicamos la cantidad final de meses adeudados por la tarifa
        BigDecimal total = cliente.getMontoMensual().multiply(BigDecimal.valueOf(deudaTotal.size()));

        return new ClienteDeudaDTO(cliente.getId(), cliente.getNombre(), deudaTotal, total);
    }

    private String traducirMes(int mes) {
        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        return meses[mes - 1];
    }
}