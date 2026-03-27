package com.santi.turnero.turno.controller;

import com.santi.turnero.turno.dto.CambiarEstadoTurnoRequest;
import com.santi.turnero.turno.dto.CancelTurnoRequest;
import com.santi.turnero.turno.dto.CreateTurnoRequest;
import com.santi.turnero.turno.dto.ReprogramarTurnoRequest;
import com.santi.turnero.turno.dto.TurnoResponse;
import com.santi.turnero.turno.dto.UpdateTurnoRequest;
import com.santi.turnero.turno.service.TurnoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @PostMapping
    public TurnoResponse crear(@Valid @RequestBody CreateTurnoRequest request) {
        return turnoService.crear(request);
    }

    @GetMapping
    public List<TurnoResponse> listarPorFecha(
            @RequestParam @NotNull Long peluqueriaId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return turnoService.listarPorFecha(peluqueriaId, fecha);
    }

    @GetMapping("/peluquero/{peluqueroId}")
    public List<TurnoResponse> listarPorPeluquero(
            @PathVariable Long peluqueroId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return turnoService.listarPorPeluquero(peluqueroId, fecha);
    }

    @PutMapping("/{turnoId}")
    public TurnoResponse editar(@PathVariable Long turnoId, @Valid @RequestBody UpdateTurnoRequest request) {
        return turnoService.editar(turnoId, request);
    }

    @PatchMapping("/{turnoId}/cancelar")
    public TurnoResponse cancelar(@PathVariable Long turnoId, @Valid @RequestBody(required = false) CancelTurnoRequest request) {
        return turnoService.cancelar(turnoId, request);
    }

    @PatchMapping("/{turnoId}/reactivar")
    public TurnoResponse reactivar(@PathVariable Long turnoId) {
        return turnoService.reactivar(turnoId);
    }

    @PatchMapping("/{turnoId}/reprogramar")
    public TurnoResponse reprogramar(@PathVariable Long turnoId, @Valid @RequestBody ReprogramarTurnoRequest request) {
        return turnoService.reprogramar(turnoId, request);
    }

    @PatchMapping("/{turnoId}/estado")
    public TurnoResponse cambiarEstado(@PathVariable Long turnoId, @Valid @RequestBody CambiarEstadoTurnoRequest request) {
        return turnoService.cambiarEstado(turnoId, request);
    }
}
