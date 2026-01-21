package com.examplo.pagoamigos.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, Model model, 
                       @RequestParam(required = false) String error, 
                       @RequestParam(required = false) String logout) {
        
        // El token CSRF es gestionado automáticamente por Spring Security
        // Solo necesitamos asegurarnos de que esté disponible en el modelo
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        
        if(error != null){
            model.addAttribute("Error", "Usuario o contraseña incorrectos");
        }
        if(logout != null){
            model.addAttribute("Logout", "Has cerrado sesión con éxito");
        }

        return "auth/login";
    }
}
