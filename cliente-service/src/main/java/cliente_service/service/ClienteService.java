package cliente_service.service;

import cliente_service.dto.*;
import java.util.List;

public interface ClienteService {
    List<ClienteResponseDTO> listarTodos();
    ClienteResponseDTO buscarPorId(Long id);
    List<ClienteResponseDTO> buscar(String filtro);
    ClienteResponseDTO registrar(ClienteRequestDTO request);
    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO request);
    void desactivar(Long id);
    void activar(Long id); // <-- NUEVO MÉTODO
    void eliminar(Long id);
    void quitarMesesDeuda(Long id, List<String> meses);
    void agregarMesesDeuda(Long id, List<String> meses);
    ClienteDeudaDTO obtenerDeuda(Long id);
}