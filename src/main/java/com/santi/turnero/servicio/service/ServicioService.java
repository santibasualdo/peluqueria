package com.santi.turnero.servicio.service;

import com.santi.turnero.servicio.dto.ServicioResponse;
import com.santi.turnero.servicio.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;

    @Transactional(readOnly = true)
    public List<ServicioResponse> listarPorPeluqueria(Long peluqueriaId) {
        return servicioRepository.findByPeluqueriaIdOrderByNombreAsc(peluqueriaId)
                .stream()
                .map(servicio -> new ServicioResponse(
                        servicio.getId(),
                        servicio.getNombre(),
                        servicio.getDuracionMinutos(),
                        servicio.getPeluqueria().getId()
                ))
                .toList();
    }
}
