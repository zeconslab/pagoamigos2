package com.examplo.pagoamigos.repository;

import java.util.Optional;

import com.examplo.pagoamigos.model.Rol;

public interface RolRepository {
    Optional<Rol> findByNombre(String nombre);
}
