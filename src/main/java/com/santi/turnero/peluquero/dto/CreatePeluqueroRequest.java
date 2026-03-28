package com.santi.turnero.peluquero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePeluqueroRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 30) String telefono,
        @NotBlank @Size(min = 6, max = 72) String password,
        @NotNull Long peluqueriaId
) {
}
