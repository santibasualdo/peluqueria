package com.santi.turnero.peluquero.service;

import com.santi.turnero.peluquero.domain.Peluquero;
import com.santi.turnero.peluquero.dto.ChangeOwnPasswordRequest;
import com.santi.turnero.peluquero.dto.CreatePeluqueroRequest;
import com.santi.turnero.peluquero.dto.PeluqueroResponse;
import com.santi.turnero.peluquero.repository.PeluqueroRepository;
import com.santi.turnero.peluqueria.domain.Peluqueria;
import com.santi.turnero.peluqueria.repository.PeluqueriaRepository;
import com.santi.turnero.shared.exception.BusinessException;
import com.santi.turnero.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PeluqueroService {

    private final PeluqueroRepository peluqueroRepository;
    private final PeluqueriaRepository peluqueriaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<PeluqueroResponse> listarPorPeluqueria(Long peluqueriaId) {
        return peluqueroRepository.findByPeluqueriaIdOrderByNombreAsc(peluqueriaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PeluqueroResponse crear(CreatePeluqueroRequest request) {
        String telefonoNormalizado = normalizeTelefono(request.telefono());
        if (telefonoNormalizado == null) {
            throw new BusinessException("El telefono del peluquero es obligatorio.");
        }

        if (peluqueroRepository.findByUsuarioIgnoreCaseAndActivoTrue(telefonoNormalizado).isPresent()) {
            throw new BusinessException("Ya existe un peluquero activo con ese telefono.");
        }

        Peluqueria peluqueria = peluqueriaRepository.findById(request.peluqueriaId())
                .orElseThrow(() -> new ResourceNotFoundException("No encontramos la peluqueria indicada."));

        Peluquero peluquero = Peluquero.builder()
                .nombre(request.nombre().trim())
                .telefono(telefonoNormalizado)
                .usuario(telefonoNormalizado)
                .passwordHash(passwordEncoder.encode(request.password().trim()))
                .activo(true)
                .requiereCambioPassword(true)
                .peluqueria(peluqueria)
                .build();

        return toResponse(peluqueroRepository.save(peluquero));
    }

    @Transactional
    public void cambiarPasswordPrimerAcceso(Long peluqueroId, ChangeOwnPasswordRequest request) {
        if (!request.nuevaPassword().equals(request.confirmarPassword())) {
            throw new BusinessException("La confirmacion de la contrasena no coincide.");
        }

        Peluquero peluquero = peluqueroRepository.findById(peluqueroId)
                .orElseThrow(() -> new ResourceNotFoundException("No encontramos el peluquero autenticado."));

        peluquero.setPasswordHash(passwordEncoder.encode(request.nuevaPassword().trim()));
        peluquero.setRequiereCambioPassword(false);
        peluqueroRepository.save(peluquero);
    }

    private PeluqueroResponse toResponse(Peluquero peluquero) {
        return new PeluqueroResponse(
                peluquero.getId(),
                peluquero.getNombre(),
                peluquero.getTelefono(),
                peluquero.getPeluqueria().getId()
        );
    }

    private String normalizeTelefono(String telefono) {
        if (telefono == null) {
            return null;
        }

        String digitsOnly = telefono.replaceAll("\\D", "");
        return digitsOnly.isBlank() ? null : digitsOnly;
    }
}
