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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        // Filtrar productos: cargar productos entre el usuario y sus amigos
        List<Product> products = new ArrayList<>();
        boolean isValidator = Boolean.TRUE.equals(model.getAttribute("isValidator"));
        if (authentication != null && email != null) {
            userRepository.findByEmail(email).ifPresent(u -> {
                if (u.getId() != null) {
                    if (isValidator) {
                        // Validador: ver productos donde es validador con estatus PENDIENTE (1)
                        products.addAll(productRepository.findByValidator_IdAndStatusWithValidator(u.getId(), Estatus_Products.PENDIENTE.getCode()));
                    } else {
                        // Solicitante: ver productos donde es creador con estatus PENDIENTE (1)
                        products.addAll(productRepository.findByCreator_IdAndStatusWithCreator(u.getId(), Estatus_Products.PENDIENTE.getCode()));
                    }
                    // Agregar lista de amigos para el modal
                    model.addAttribute("friends", u.getFriends());
                }
            });
        }
        model.addAttribute("products", products);

        // Exponer un objeto vacío para el formulario de creación
        model.addAttribute("product", new Product());

        // Conteo: usar el tamaño de la lista filtrada
        model.addAttribute("pendingCount", products == null ? 0 : products.size());

        return "dashboard";
    }

    @PostMapping("/product/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(name = "validatorId", required = false) Long validatorId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        // Asociar creador si está autenticado
        if (authentication != null) {
            String email = authentication.getName();
            userRepository.findByEmail(email).ifPresent(user -> product.setCreator(user));
        }

        // Asociar validador seleccionado (opcional)
        if (validatorId != null) {
            userRepository.findById(validatorId).ifPresent(product::setValidator);
        }

        product.setStatus(Estatus_Products.PENDIENTE.getCode());
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Producto guardado correctamente");
        return "redirect:/dashboard";
    }
}
