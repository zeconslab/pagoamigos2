package com.examplo.pagoamigos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Model model, @RequestParam(required = false) String error, @RequestParam(required = false) String logout) {
        if(error != null){
            model.addAttribute("Error", "Usuario o contraseña incorrectos");
        }
        if(logout != null){
            model.addAttribute("Logout", "Has cerrado sesión con éxito");
        }

        return "auth/login";
    }
}
