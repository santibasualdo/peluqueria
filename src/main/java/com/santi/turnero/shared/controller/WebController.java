package com.santi.turnero.shared.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping({"/", "/turnos"})
    public String turnos() {
        return "forward:/turnos/index.html";
    }
}
