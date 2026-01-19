package com.examplo.pagoamigos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examplo.pagoamigos.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}
