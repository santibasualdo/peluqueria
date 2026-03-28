package com.santi.turnero.peluquero.controller;

import com.santi.turnero.peluquero.dto.CreatePeluqueroRequest;
import com.santi.turnero.peluquero.dto.PeluqueroResponse;
import com.santi.turnero.peluquero.service.PeluqueroService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/peluqueros")
@RequiredArgsConstructor
public class PeluqueroController {

    private final PeluqueroService peluqueroService;

    @GetMapping
    public List<PeluqueroResponse> listarPorPeluqueria(@RequestParam @NotNull Long peluqueriaId) {
        return peluqueroService.listarPorPeluqueria(peluqueriaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PeluqueroResponse crear(@Valid @RequestBody CreatePeluqueroRequest request) {
        return peluqueroService.crear(request);
    }
}
