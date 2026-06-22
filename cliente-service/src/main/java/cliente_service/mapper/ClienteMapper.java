package cliente_service.mapper;

import cliente_service.dto.ClienteResponseDTO;
import cliente_service.model.Cliente;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ClienteMapper {

    public ClienteResponseDTO toDTO(Cliente c) {
        return new ClienteResponseDTO(
                c.getId(),
                c.getDni(),
                c.getNombre(),
                c.getCelular(),
                c.getCorreo(),
                c.getDireccion(),
                c.getMontoMensual(),
                c.getDiaPago(),
                c.getMesesDeuda() != null ? c.getMesesDeuda() : new ArrayList<>(), // NUEVO: Meses adeudados
                c.getEstado() != null ? c.getEstado().name() : null,
                c.getInicioServicio()
        );
    }
}