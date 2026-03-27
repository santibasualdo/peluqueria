package com.santi.turnero.peluquero.service;

import com.santi.turnero.peluquero.dto.PeluqueroResponse;
import com.santi.turnero.peluquero.repository.PeluqueroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PeluqueroService {

    private final PeluqueroRepository peluqueroRepository;

    @Transactional(readOnly = true)
    public List<PeluqueroResponse> listarPorPeluqueria(Long peluqueriaId) {
        return peluqueroRepository.findByPeluqueriaIdOrderByNombreAsc(peluqueriaId)
                .stream()
                .map(peluquero -> new PeluqueroResponse(
                        peluquero.getId(),
                        peluquero.getNombre(),
                        peluquero.getTelefono(),
                        peluquero.getPeluqueria().getId()
                ))
                .toList();
    }
}
