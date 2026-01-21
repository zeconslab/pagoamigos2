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

                for (int i = 0; i < names.length; i++) {
                    Product p = new Product();
                    p.setName(names[i]);
                    p.setPrice(prices[i]);
                    products.add(productRepository.save(p));
                }
                logger.info("Productos creados: {}", products.size());

                // Asociar algunos productos a los usuarios creados
                userRepository.findByEmail("solicitante@pagoamigos.com").ifPresent(u -> {
                    u.setProducts(new HashSet<>(products.subList(0, 3)));
                    userRepository.save(u);
                });

                userRepository.findByEmail("validator@pagoamigos.com").ifPresent(u -> {
                    u.setProducts(new HashSet<>(products.subList(3, 6)));
                    userRepository.save(u);
                });
            }

            logger.info("Carga de datos de prueba completada.");
        };
    }
}
