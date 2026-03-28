package com.santi.turnero.peluquero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeOwnPasswordRequest(
        @NotBlank @Size(min = 6, max = 72) String nuevaPassword,
        @NotBlank @Size(min = 6, max = 72) String confirmarPassword
) {
}
