package com.examplo.pagoamigos.controller;

import com.examplo.pagoamigos.Estatus_Products;
import com.examplo.pagoamigos.model.Product;
import com.examplo.pagoamigos.repository.ProductRepository;
import com.examplo.pagoamigos.repository.UserRepository;
import com.examplo.pagoamigos.model.User;
import com.examplo.pagoamigos.model.MonthlyPayment;
import com.examplo.pagoamigos.repository.MonthlyPaymentRepository;
import java.time.LocalDate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.ArrayList;

@Controller
public class DashboardController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final MonthlyPaymentRepository monthlyPaymentRepository;

    public DashboardController(ProductRepository productRepository, UserRepository userRepository, MonthlyPaymentRepository monthlyPaymentRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.monthlyPaymentRepository = monthlyPaymentRepository;
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

        // Conteo: usar el tamaño de la lista filtrada
        model.addAttribute("pendingCount", products == null ? 0 : products.size());

        return "dashboard";
    }


    // Guardar producto
    @PostMapping("/product/save")
    public String saveProduct(Product product, Authentication authentication, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication != null) {
            String email = authentication.getName();
            User creator = userRepository.findByEmail(email).orElse(null);
            if (creator != null) {
                product.setCreator(creator);
                product.setStatus(Estatus_Products.PENDIENTE.getCode());
                Product saved = productRepository.save(product);

                // Si el producto tiene mensualidades configuradas, generar las cuotas
                if (Boolean.TRUE.equals(saved.getMonthlyPaymentEnabled())
                        && saved.getInstallments() != null
                        && saved.getInstallments() > 0) {
                    List<MonthlyPayment> payments = new ArrayList<>();
                    Double installmentAmount = saved.getMonthlyPaymentAmount();
                    for (int i = 1; i <= saved.getInstallments(); i++) {
                        MonthlyPayment mp = new MonthlyPayment();
                        mp.setProduct(saved);
                        mp.setPayer(creator);
                        mp.setInstallmentNumber(i);
                        mp.setAmount(installmentAmount == null && saved.getPrice() != null ? saved.getPrice() / saved.getInstallments() : installmentAmount);
                        mp.setDueDate(LocalDate.now().plusMonths(i));
                        mp.setPaid(false);
                        payments.add(mp);
                    }
                    monthlyPaymentRepository.saveAll(payments);
                }

                redirectAttributes.addFlashAttribute("successMessage", "Producto guardado exitosamente.");
                return "redirect:/dashboard";
            }
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el producto.");
        return "redirect:/dashboard";
    }
}
