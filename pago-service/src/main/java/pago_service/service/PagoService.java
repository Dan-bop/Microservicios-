package pago_service.service;

import pago_service.dto.*;
import java.util.List;

public interface PagoService {
    PagoResponseDTO registrarPago(RegistroPagoRequest request, String username);
    void anular(Long pagoId, String motivo);
    List<PagoResponseDTO> listarPorCliente(Long clienteId);
    PagoResponseDTO buscarPorId(Long id);
}