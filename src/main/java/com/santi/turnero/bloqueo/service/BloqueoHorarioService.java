package com.santi.turnero.bloqueo.service;

import com.santi.turnero.bloqueo.domain.BloqueoHorario;
import com.santi.turnero.bloqueo.dto.BloqueoHorarioResponse;
import com.santi.turnero.bloqueo.dto.CreateBloqueoHorarioRequest;
import com.santi.turnero.bloqueo.repository.BloqueoHorarioRepository;
import com.santi.turnero.peluqueria.domain.Peluqueria;
import com.santi.turnero.peluqueria.repository.PeluqueriaRepository;
import com.santi.turnero.peluquero.domain.Peluquero;
import com.santi.turnero.peluquero.repository.PeluqueroRepository;
import com.santi.turnero.shared.exception.BusinessException;
import com.santi.turnero.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BloqueoHorarioService {

    private final BloqueoHorarioRepository bloqueoHorarioRepository;
    private final PeluqueriaRepository peluqueriaRepository;
    private final PeluqueroRepository peluqueroRepository;

    @Transactional
    public BloqueoHorarioResponse crear(CreateBloqueoHorarioRequest request) {
        if (!request.fechaHoraFin().isAfter(request.fechaHoraInicio())) {
            throw new BusinessException("La fecha de fin del bloqueo debe ser posterior al inicio.");
        }

        Peluqueria peluqueria = peluqueriaRepository.findById(request.peluqueriaId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la peluqueria solicitada."));

        Peluquero peluquero = null;
        if (request.peluqueroId() != null) {
            peluquero = peluqueroRepository.findById(request.peluqueroId())
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontro el peluquero solicitado."));

            if (!peluquero.getPeluqueria().getId().equals(peluqueria.getId())) {
                throw new BusinessException("El peluquero no pertenece a la peluqueria indicada.");
            }
        }

        BloqueoHorario bloqueo = BloqueoHorario.builder()
                .peluqueria(peluqueria)
                .peluquero(peluquero)
                .fechaHoraInicio(request.fechaHoraInicio())
                .fechaHoraFin(request.fechaHoraFin())
                .motivo(request.motivo().trim())
                .build();

        return toResponse(bloqueoHorarioRepository.save(bloqueo));
    }

    private BloqueoHorarioResponse toResponse(BloqueoHorario bloqueoHorario) {
        return new BloqueoHorarioResponse(
                bloqueoHorario.getId(),
                bloqueoHorario.getPeluqueria().getId(),
                bloqueoHorario.getPeluquero() != null ? bloqueoHorario.getPeluquero().getId() : null,
                bloqueoHorario.getFechaHoraInicio(),
                bloqueoHorario.getFechaHoraFin(),
                bloqueoHorario.getMotivo()
        );
    }
}
