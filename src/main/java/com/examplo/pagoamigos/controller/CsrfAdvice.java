package com.examplo.pagoamigos.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CsrfAdvice {

    @ModelAttribute("_csrfParameterName")
    public String csrfParameterName(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        return token != null ? token.getParameterName() : null;
    }

    @ModelAttribute("_csrfToken")
    public String csrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        return token != null ? token.getToken() : null;
    }
}
