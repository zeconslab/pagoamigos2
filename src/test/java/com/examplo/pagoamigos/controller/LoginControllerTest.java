
package com.examplo.pagoamigos.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    private final LoginController controller = new LoginController();

    @Test
    void returnsAuthLoginView() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Model model = mock(Model.class);

        String view = controller.login(request, response, model, null, null);

        assertEquals("auth/login", view);
        verifyNoInteractions(model); // no attributes added when nothing present
    }

    @Test
    void addsCsrfTokenToModelWhenPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Model model = mock(Model.class);
        CsrfToken csrfToken = mock(CsrfToken.class);

        when(request.getAttribute("_csrf")).thenReturn(csrfToken);

        String view = controller.login(request, response, model, null, null);

        assertEquals("auth/login", view);
        verify(model).addAttribute("_csrf", csrfToken);
    }

    @Test
    void addsErrorAndLogoutMessagesWhenParametersPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Model model = mock(Model.class);

        String view = controller.login(request, response, model, "someError", "true");

        assertEquals("auth/login", view);
        verify(model).addAttribute("Error", "Usuario o contraseña incorrectos");
        verify(model).addAttribute("Logout", "Has cerrado sesión con éxito");
    }
}