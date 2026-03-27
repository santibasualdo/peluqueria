package com.santi.turnero.turno.controller;

import com.santi.turnero.turno.dto.DisponibilidadResponse;
import com.santi.turnero.turno.service.DisponibilidadService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/api/v1/disponibilidad")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    @GetMapping
    public DisponibilidadResponse consultar(
            @RequestParam @NotNull Long peluqueriaId,
            @RequestParam @NotNull Long peluqueroId,
            @RequestParam @NotNull Long servicioId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer intervaloMinutos
    ) {
        return disponibilidadService.consultar(peluqueriaId, peluqueroId, servicioId, fecha, intervaloMinutos);
    }
}
