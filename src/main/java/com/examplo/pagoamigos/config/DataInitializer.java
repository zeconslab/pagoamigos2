package com.examplo.pagoamigos.config;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

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
                                   PasswordEncoder passwordEncoder) {
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

            logger.info("Carga de datos de prueba completada.");
        };
    }
}
