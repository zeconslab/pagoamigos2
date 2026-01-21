package com.examplo.pagoamigos.controller;

import com.examplo.pagoamigos.Estatus_Products;
import com.examplo.pagoamigos.model.Product;
import com.examplo.pagoamigos.repository.ProductRepository;
import com.examplo.pagoamigos.repository.UserRepository;
import com.examplo.pagoamigos.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.ArrayList;

@Controller
public class DashboardController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {

        String email = null;

        if (authentication != null) {
            email = authentication.getName();
            model.addAttribute("username", authentication.getName());
            boolean isValidator = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VALIDATOR"));
            boolean isSolicitante = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SOLICITANTE"));
            model.addAttribute("isValidator", isValidator);
            model.addAttribute("isSolicitante", isSolicitante);
        }

        // Filtrar productos según rol/usuario: VALIDATOR ve todos, SOLICITANTE solo sus productos
        List<Product> products;
        boolean isValidator = Boolean.TRUE.equals(model.getAttribute("isValidator"));
        if (authentication != null && isValidator) {
            // Validador: ver productos con estatus APROBADO (2)
            products = productRepository.findByStatus(Estatus_Products.APROBADO.getCode());
        } else {
            // Solicitante: ver solo sus productos con estatus PENDIENTE (1)
            products = new ArrayList<>();
            if (email != null) {
                userRepository.findByEmail(email).ifPresent(u -> {
                    if (u.getId() != null) {
                        products.addAll(productRepository.findByUsers_IdAndStatus(u.getId(), Estatus_Products.PENDIENTE.getCode()));
                    }
                });
            }
        }
        model.addAttribute("products", products);

        // Conteo: usar el tamaño de la lista filtrada
        model.addAttribute("pendingCount", products == null ? 0 : products.size());

        return "dashboard";
    }
}
