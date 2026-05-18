package cliente_service.service;

import cliente_service.dto.ClienteDeudaDTO;
import cliente_service.dto.ClienteRequestDTO;
import cliente_service.dto.ClienteResponseDTO;
import cliente_service.model.Cliente;
import cliente_service.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
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

        return toDTO(clienteRepository.save(cliente));
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
    }

    @Transactional(readOnly = true)
    public ClienteDeudaDTO obtenerDeuda(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));

        LocalDate inicio = cliente.getInicioServicio() != null
                ? cliente.getInicioServicio()
                : cliente.getFechaRegistro().toLocalDate();

        LocalDate hoy = LocalDate.now();
        List<String> deuda = new ArrayList<>();
        LocalDate iterador = inicio.withDayOfMonth(1);

        while (!iterador.isAfter(hoy.withDayOfMonth(1))) {
            String periodo = traducirMes(iterador.getMonthValue()) + " " + iterador.getYear();
            deuda.add(periodo);
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
