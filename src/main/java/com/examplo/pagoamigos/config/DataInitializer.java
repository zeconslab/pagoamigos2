package com.examplo.pagoamigos.config;

import java.util.Set;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

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
                                   ProductRepository productRepository,
                                   JdbcTemplate jdbcTemplate) {
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

            // Obtener referencias a los usuarios creados (si existen)
            User solicitanteUser = userRepository.findByEmail("solicitante@pagoamigos.com").orElse(null);
            User validatorUser = userRepository.findByEmail("validator@pagoamigos.com").orElse(null);

            logger.info("Inicia creación de productos de ejemplo");
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
                    Product product = new Product();
                    product.setName(names[i]);
                    product.setPrice(prices[i]);
                    product.setStatus(1); // PENDIENTE
                    product.setCreator(solicitanteUser);
                    product.setValidator(validatorUser);
                    product.setCreatedAt(LocalDateTime.now());
                    products.add(product);
                    productRepository.save(product);
                    logger.info("Producto creado: {} - ${}", names[i], prices[i]);
                }
            }

            // Establecer amistad entre solicitante y validador usando inserción directa en la tabla join
            if (solicitanteUser != null && validatorUser != null) {
                logger.info("Estableciendo amistad entre {} y {}", solicitanteUser.getEmail(), validatorUser.getEmail());
                try {
                    String sql = "IF NOT EXISTS (SELECT 1 FROM user_friends WHERE user_id = ? AND friend_id = ?) INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";
                    jdbcTemplate.update(sql, solicitanteUser.getId(), validatorUser.getId(), solicitanteUser.getId(), validatorUser.getId());
                    jdbcTemplate.update(sql, validatorUser.getId(), solicitanteUser.getId(), validatorUser.getId(), solicitanteUser.getId());
                    logger.info("Amistad insertada en la tabla user_friends");
                } catch (Exception ex) {
                    logger.warn("No fue posible insertar amistad en user_friends: {}", ex.getMessage());
                }
            }

            logger.info("Carga de datos de prueba completada.");
        };
    }
}
