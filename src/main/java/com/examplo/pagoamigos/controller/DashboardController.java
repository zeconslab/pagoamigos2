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

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.io.IOException;

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
    public String saveProduct(@jakarta.validation.Valid @ModelAttribute Product product,
                              org.springframework.validation.BindingResult bindingResult,
                              @RequestParam(name = "validatorId", required = false) Long validatorId,
                              @RequestParam(name = "attachment", required = false) MultipartFile attachment,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes,
                              Model model) {

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

        // Validaciones adicionales dependientes (si aplica cuotas validar cantidad)
        if (Boolean.TRUE.equals(product.getHasInstallments())) {
            if (product.getInstallmentsCount() == null || product.getInstallmentsCount() < 1) {
                bindingResult.rejectValue("installmentsCount", "Invalid.installmentsCount", "Ingrese la cantidad de cuotas");
            }
            if (product.getInstallmentFrequency() == null || product.getInstallmentFrequency().isEmpty()) {
                bindingResult.rejectValue("installmentFrequency", "Invalid.installmentFrequency", "Seleccione la frecuencia de cuotas");
            }
        }

        if (bindingResult.hasErrors()) {
            // Re-popular datos necesarios para volver a mostrar el modal con errores
            if (authentication != null) {
                String email = authentication.getName();
                userRepository.findByEmail(email).ifPresent(u -> model.addAttribute("friends", u.getFriends()));
            }
            // Añadir el objeto product y devolver la vista
            model.addAttribute("product", product);
            // También exponer lista de productos pendientes para la tabla
            // Simplemente reutilizamos lógica mínima: cargar productos si hay autenticación
            List<Product> products = new ArrayList<>();
            if (authentication != null && authentication.getName() != null) {
                String email = authentication.getName();
                boolean isValidator = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VALIDATOR"));
                userRepository.findByEmail(email).ifPresent(u -> {
                    if (isValidator) products.addAll(productRepository.findByValidator_IdAndStatusWithValidator(u.getId(), Estatus_Products.PENDIENTE.getCode()));
                    else products.addAll(productRepository.findByCreator_IdAndStatusWithCreator(u.getId(), Estatus_Products.PENDIENTE.getCode()));
                });
            }
            model.addAttribute("products", products);
            model.addAttribute("pendingCount", products.size());
            return "dashboard";
        }

        // Manejo de archivo adjunto (imagen)
        if (attachment != null && !attachment.isEmpty()) {
            // Validaciones básicas: tipo y tamaño (max 5MB)
            String contentType = attachment.getContentType();
            long maxSize = 5L * 1024L * 1024L; // 5 MB
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                bindingResult.rejectValue("imageFilename", "Invalid.image", "El archivo debe ser una imagen");
            } else if (attachment.getSize() > maxSize) {
                bindingResult.rejectValue("imageFilename", "Invalid.image.size", "La imagen debe ser menor a 5MB");
            } else {
                try {
                    Path uploadDir = Paths.get("uploads");
                    if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
                    String original = attachment.getOriginalFilename();
                    String ext = "";
                    if (original != null && original.contains(".")) {
                        ext = original.substring(original.lastIndexOf('.') + 1);
                    }
                    String savedName = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : ("." + ext));
                    Path target = uploadDir.resolve(savedName);
                    Files.copy(attachment.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                    product.setImageFilename(savedName);
                } catch (IOException e) {
                    bindingResult.rejectValue("imageFilename", "Invalid.image.save", "Error al guardar la imagen");
                }
            }

            if (bindingResult.hasErrors()) {
                // Re-popular datos necesarios para volver a mostrar el modal con errores
                if (authentication != null) {
                    String email = authentication.getName();
                    userRepository.findByEmail(email).ifPresent(u -> model.addAttribute("friends", u.getFriends()));
                }
                model.addAttribute("product", product);
                List<Product> products = new ArrayList<>();
                if (authentication != null && authentication.getName() != null) {
                    String email = authentication.getName();
                    boolean isValidator2 = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VALIDATOR"));
                    userRepository.findByEmail(email).ifPresent(u -> {
                        if (isValidator2) products.addAll(productRepository.findByValidator_IdAndStatusWithValidator(u.getId(), Estatus_Products.PENDIENTE.getCode()));
                        else products.addAll(productRepository.findByCreator_IdAndStatusWithCreator(u.getId(), Estatus_Products.PENDIENTE.getCode()));
                    });
                }
                model.addAttribute("products", products);
                model.addAttribute("pendingCount", products.size());
                return "dashboard";
            }
        }

        // Sincronizar columna DB 'monthly_payment_enabled'
        product.setMonthlyPaymentEnabled(Boolean.TRUE.equals(product.getHasInstallments()));

        Product saved = productRepository.save(product);

        // No crear cuotas aquí: las cuotas se generan cuando un validador aprueba la solicitud.
        redirectAttributes.addFlashAttribute("successMessage", "Producto guardado correctamente");
        // Pasar id guardado para mostrar overlay reutilizable en la vista
        redirectAttributes.addFlashAttribute("savedProductId", saved.getId());
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
