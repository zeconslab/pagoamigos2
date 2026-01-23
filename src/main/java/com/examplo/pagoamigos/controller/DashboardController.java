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

        // Exponer un objeto vacío para el formulario de creación
        model.addAttribute("product", new Product());

        // Conteo: usar el tamaño de la lista filtrada
        model.addAttribute("pendingCount", products == null ? 0 : products.size());

        return "dashboard";
    }

    @PostMapping("/product/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(name = "validatorId", required = false) Long validatorId,
                              @RequestParam(name = "hasInstallments", required = false) Boolean hasInstallments,
                              @RequestParam(name = "installmentsCount", required = false) Integer installmentsCount,
                              @RequestParam(name = "installmentFrequency", required = false) String installmentFrequency,
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
        // Setear flag de cuotas según parámetro (checkbox)
        product.setHasInstallments(Boolean.TRUE.equals(hasInstallments));
        // Guardar configuración de cuotas en el producto
        product.setInstallmentsCount(installmentsCount);
        product.setInstallmentFrequency(installmentFrequency);

        Product saved = productRepository.save(product);

        // No crear cuotas aquí: las cuotas se generan cuando un validador aprueba la solicitud.
        redirectAttributes.addFlashAttribute("successMessage", "Producto guardado correctamente");
        return "redirect:/dashboard";
    }

    @PostMapping("/product/{id}/approve")
    public String approveProduct(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        // Verificar que el usuario tenga rol de validador
        boolean isValidator = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VALIDATOR"));
        if (!isValidator) {
            redirectAttributes.addFlashAttribute("errorMessage", "No autorizado para aprobar solicitudes");
            return "redirect:/dashboard";
        }

        productRepository.findById(id).ifPresent(product -> {
            product.setStatus(Estatus_Products.APROBADO.getCode());
            productRepository.save(product);

            // Si corresponde, crear las cuotas al momento de la aprobación
            if (Boolean.TRUE.equals(product.getHasInstallments())) {
                // evitar duplicados
                if (monthlyPaymentRepository.findByProduct_Id(product.getId()).isEmpty()) {
                    int count = (product.getInstallmentsCount() == null || product.getInstallmentsCount() <= 0)
                            ? 1 : product.getInstallmentsCount();
                    String freq = (product.getInstallmentFrequency() == null) ? "monthly" : product.getInstallmentFrequency();
                    java.util.List<MonthlyPayment> quotas = new java.util.ArrayList<>();
                    LocalDate start = LocalDate.now();
                    for (int i = 0; i < count; i++) {
                        MonthlyPayment mp = new MonthlyPayment();
                        mp.setProduct(product);
                        if ("biweekly".equalsIgnoreCase(freq) || "quincenal".equalsIgnoreCase(freq)) {
                            mp.setDueDate(start.plusDays(15L * (i + 1)));
                        } else if ("weekly".equalsIgnoreCase(freq) || "semanal".equalsIgnoreCase(freq)) {
                            mp.setDueDate(start.plusWeeks(i + 1));
                        } else {
                            mp.setDueDate(start.plusMonths(i + 1));
                        }
                        double price = (product.getPrice() == null) ? 0.0 : product.getPrice();
                        mp.setAmount(Math.round((price / count) * 100.0) / 100.0);
                        mp.setPaid(false);
                        quotas.add(mp);
                    }
                    monthlyPaymentRepository.saveAll(quotas);
                }
            }
        });

        redirectAttributes.addFlashAttribute("successMessage", "Solicitud aprobada correctamente");
        return "redirect:/dashboard";
    }
}
