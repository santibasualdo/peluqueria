package com.santi.turnero.cliente.service;

import com.santi.turnero.cliente.domain.Cliente;
import com.santi.turnero.cliente.repository.ClienteRepository;
import com.santi.turnero.turno.dto.TurnoClienteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente obtenerOCrear(TurnoClienteRequest request) {
        String telefonoNormalizado = request.telefono().trim();

        Cliente cliente = clienteRepository.findByTelefono(telefonoNormalizado)
                .orElseGet(() -> Cliente.builder().telefono(telefonoNormalizado).build());

        cliente.setNombre(request.nombre().trim());
        cliente.setTelefono(telefonoNormalizado);
        cliente.setObservaciones(request.observaciones());

        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Cliente> buscarPorTelefono(String telefono) {
        return clienteRepository.findByTelefono(telefono.trim());
    }

    @Transactional
    public Cliente registrarOActualizarDesdeWhatsApp(String telefono, String nombreCompleto) {
        String telefonoNormalizado = telefono.trim();

        Cliente cliente = clienteRepository.findByTelefono(telefonoNormalizado)
                .orElseGet(() -> Cliente.builder().telefono(telefonoNormalizado).build());

        cliente.setTelefono(telefonoNormalizado);
        cliente.setNombre(nombreCompleto.trim());

        return clienteRepository.save(cliente);
    }
}
