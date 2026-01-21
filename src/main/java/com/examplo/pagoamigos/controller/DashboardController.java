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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

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

        // Filtrar productos según rol/usuario: ambos ven solo productos asociados a su usuario
        List<Product> products = new ArrayList<>();
        products = productRepository.findAll();

        model.addAttribute("products", products);

        // Conteo: usar el tamaño de la lista filtrada
        model.addAttribute("pendingCount", products == null ? 0 : products.size());

        return "dashboard";
    }
}
