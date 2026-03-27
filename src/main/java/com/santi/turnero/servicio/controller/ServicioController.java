package com.santi.turnero.servicio.controller;

import com.santi.turnero.servicio.dto.ServicioResponse;
import com.santi.turnero.servicio.service.ServicioService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @GetMapping
    public List<ServicioResponse> listarPorPeluqueria(@RequestParam @NotNull Long peluqueriaId) {
        return servicioService.listarPorPeluqueria(peluqueriaId);
    }
}
