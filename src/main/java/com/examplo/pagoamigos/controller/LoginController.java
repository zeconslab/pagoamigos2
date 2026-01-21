package com.examplo.pagoamigos.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, Model model, 
                       @RequestParam(required = false) String error, 
                       @RequestParam(required = false) String logout) {
        
        // Obtener o generar token CSRF
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        
        // Si no existe el token, crear uno nuevo
        if (csrfToken == null) {
            CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
            tokenRepository.setCookieCustomizer(cookie -> {
                cookie.secure(true);  // Requerido para HTTPS en producción
                cookie.sameSite("Lax");
                cookie.path("/");
            });
            
            csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", UUID.randomUUID().toString());
            tokenRepository.saveToken(csrfToken, request, response);
            request.setAttribute("_csrf", csrfToken);
        }
        
        model.addAttribute("_csrf", csrfToken);
        
        if(error != null){
            model.addAttribute("Error", "Usuario o contraseña incorrectos");
        }
        if(logout != null){
            model.addAttribute("Logout", "Has cerrado sesión con éxito");
        }

        return "auth/login";
    }
}
