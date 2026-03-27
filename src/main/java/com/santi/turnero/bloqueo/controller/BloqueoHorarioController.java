package com.santi.turnero.bloqueo.controller;

import com.santi.turnero.bloqueo.dto.BloqueoHorarioResponse;
import com.santi.turnero.bloqueo.dto.CreateBloqueoHorarioRequest;
import com.santi.turnero.bloqueo.service.BloqueoHorarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bloqueos")
@RequiredArgsConstructor
public class BloqueoHorarioController {

    private final BloqueoHorarioService bloqueoHorarioService;

    @PostMapping
    public BloqueoHorarioResponse crear(@Valid @RequestBody CreateBloqueoHorarioRequest request) {
        return bloqueoHorarioService.crear(request);
    }
}
