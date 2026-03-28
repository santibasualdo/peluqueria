package com.santi.turnero.peluquero.controller;

import com.santi.turnero.peluquero.dto.ChangeOwnPasswordRequest;
import com.santi.turnero.peluquero.service.PeluqueroService;
import com.santi.turnero.shared.security.AuthenticatedPeluqueroUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/peluqueros/me")
@RequiredArgsConstructor
public class PeluqueroAccountController {

    private final PeluqueroService peluqueroService;

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPasswordPropia(
            @AuthenticationPrincipal AuthenticatedPeluqueroUser authenticatedUser,
            @Valid @RequestBody ChangeOwnPasswordRequest request
    ) {
        peluqueroService.cambiarPasswordPrimerAcceso(authenticatedUser.peluqueroId(), request);
    }
}
