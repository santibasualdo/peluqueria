package com.santi.turnero.turno.mapper;

import com.santi.turnero.turno.domain.Turno;
import com.santi.turnero.turno.dto.TurnoInternoResponse;
import com.santi.turnero.turno.dto.TurnoResponse;
import org.springframework.stereotype.Component;

@Component
public class TurnoMapper {

    public TurnoResponse toResponse(Turno turno) {
        return new TurnoResponse(
                turno.getId(),
                turno.getPeluqueria().getId(),
                turno.getPeluqueria().getNombre(),
                turno.getPeluquero().getId(),
                turno.getPeluquero().getNombre(),
                turno.getCliente().getId(),
                turno.getCliente().getNombre(),
                maskTelefono(turno.getCliente().getTelefono()),
                turno.getServicio().getId(),
                turno.getServicio().getNombre(),
                turno.getServicio().getDuracionMinutos(),
                turno.getFechaHoraInicio(),
                turno.getFechaHoraFin(),
                turno.getEstado(),
                turno.getObservaciones()
        );
    }

    public TurnoInternoResponse toInternalResponse(Turno turno) {
        return new TurnoInternoResponse(
                turno.getId(),
                turno.getPeluqueria().getId(),
                turno.getPeluqueria().getNombre(),
                turno.getPeluquero().getId(),
                turno.getPeluquero().getNombre(),
                turno.getCliente().getId(),
                turno.getCliente().getNombre(),
                turno.getCliente().getTelefono(),
                turno.getServicio().getId(),
                turno.getServicio().getNombre(),
                turno.getServicio().getDuracionMinutos(),
                turno.getFechaHoraInicio(),
                turno.getFechaHoraFin(),
                turno.getEstado(),
                turno.getObservaciones()
        );
    }

    private String maskTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return null;
        }

        String digits = telefono.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "****";
        }

        String lastFour = digits.substring(digits.length() - 4);
        return "****" + lastFour;
    }
}
