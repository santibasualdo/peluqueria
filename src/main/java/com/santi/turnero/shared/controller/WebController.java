package com.santi.turnero.shared.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "forward:/login/index.html";
    }

    @GetMapping("/primer-acceso")
    public String primerAcceso() {
        return "forward:/primer-acceso/index.html";
    }

    @GetMapping({"/", "/turnos"})
    public String turnos() {
        return "forward:/turnos/index.html";
    }
}
