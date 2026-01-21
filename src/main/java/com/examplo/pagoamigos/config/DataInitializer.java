package com.examplo.pagoamigos.config;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.examplo.pagoamigos.model.Product;
import com.examplo.pagoamigos.repository.ProductRepository;

import com.examplo.pagoamigos.model.Rol;
import com.examplo.pagoamigos.model.User;
import com.examplo.pagoamigos.repository.RolRepository;
import com.examplo.pagoamigos.repository.UserRepository;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                   RolRepository rolRepository, 
                                   PasswordEncoder passwordEncoder,
                                   ProductRepository productRepository) {
        return args -> {
            logger.info("Iniciando carga de datos de prueba...");

            // Crear roles si no existen
            Rol rolValidator = rolRepository.findByNombre("VALIDATOR")
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre("VALIDATOR");
                    return rolRepository.save(rol);
                });

            Rol rolSolicitante = rolRepository.findByNombre("SOLICITANTE")
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre("SOLICITANTE");
                    return rolRepository.save(rol);
                });

            logger.info("Roles creados: VALIDATOR, SOLICITANTE");

            // Crear usuario validador si no existe
            if (userRepository.findByEmail("validator@pagoamigos.com").isEmpty()) {
                User validator = new User();
                validator.setName("Juan");
                validator.setPaternalLastName("Pérez");
                validator.setMaternalLastName("García");
                validator.setEmail("validator@pagoamigos.com");
                validator.setPassword(passwordEncoder.encode("Validator123!"));
                validator.setPhone("5512345678");
                validator.setActive(true);
                validator.setRoles(Set.of(rolValidator));
                userRepository.save(validator);
                logger.info("Usuario validador creado: validator@pagoamigos.com / Validator123!");
            }

            // Crear usuario solicitante si no existe
            if (userRepository.findByEmail("solicitante@pagoamigos.com").isEmpty()) {
                User solicitante = new User();
                solicitante.setName("María");
                solicitante.setPaternalLastName("López");
                solicitante.setMaternalLastName("Hernández");
                solicitante.setEmail("solicitante@pagoamigos.com");
                solicitante.setPassword(passwordEncoder.encode("Solicitante123!"));
                solicitante.setPhone("5587654321");
                solicitante.setActive(true);
                solicitante.setRoles(Set.of(rolSolicitante));
                userRepository.save(solicitante);
                logger.info("Usuario solicitante creado: solicitante@pagoamigos.com / Solicitante123!");
            }

            // Crear productos de ejemplo y asociarlos a usuarios (si no existen)
            if (productRepository.count() == 0) {
                List<Product> products = new ArrayList<>();
                String[] names = new String[] {
                        "Laptop Pro M3",
                        "Monitor 27\" 4K",
                        "Teclado Mecánico",
                        "Mouse Inalámbrico",
                        "Silla Ergonómica",
                        "Auriculares ANC",
                        "Suscripción SaaS - 1 año",
                        "Disco SSD 1TB",
                        "Cámara Web 1080p",
                        "Licencia Office 365"
                };
                Double[] prices = new Double[] {3499.00, 599.00, 129.00, 59.00, 349.00, 199.00, 499.00, 149.00, 89.00, 129.00};

                // Obtener referencias a los usuarios creados (si existen)
                User solicitanteUser = userRepository.findByEmail("solicitante@pagoamigos.com").orElse(null);
                User validatorUser = userRepository.findByEmail("validator@pagoamigos.com").orElse(null);

                for (int i = 0; i < names.length; i++) {
                    Product p = new Product();
                    p.setName(names[i]);
                    p.setPrice(prices[i]);
                    // Set status: first 3 -> PENDIENTE (1), next 3 -> APROBADO (2), rest PENDIENTE
                    if (i < 3) {
                        p.setStatus(1); // PENDIENTE
                        // creador: solicitante
                        if (solicitanteUser != null) p.setCreator(solicitanteUser);
                    } else if (i < 6) {
                        p.setStatus(2); // APROBADO
                        // creador: solicitante; validador: validator
                        if (solicitanteUser != null) p.setCreator(solicitanteUser);
                        if (validatorUser != null) p.setValidator(validatorUser);
                    } else {
                        p.setStatus(1);
                        if (solicitanteUser != null) p.setCreator(solicitanteUser);
                    }
                    products.add(productRepository.save(p));
                }
                logger.info("Productos creados: {}", products.size());

                // Establecer amistad entre solicitante y validador usando helper (gestiona ambas direcciones)
                if (solicitanteUser != null && validatorUser != null) {
                    solicitanteUser.addFriend(validatorUser);
                    // Forzar persistencia inmediata y confirmar
                    userRepository.saveAndFlush(solicitanteUser);
                    userRepository.saveAndFlush(validatorUser);

                    // Recargar y loguear counts para verificar
                    User sReload = userRepository.findById(solicitanteUser.getId()).orElse(null);
                    User vReload = userRepository.findById(validatorUser.getId()).orElse(null);
                    logger.info("Solicitante friends after save: {}",
                            sReload == null ? 0 : (sReload.getFriends() == null ? 0 : sReload.getFriends().size()));
                    logger.info("Validator friends after save: {}",
                            vReload == null ? 0 : (vReload.getFriends() == null ? 0 : vReload.getFriends().size()));
                }
            }

            logger.info("Carga de datos de prueba completada.");
        };
    }
}
