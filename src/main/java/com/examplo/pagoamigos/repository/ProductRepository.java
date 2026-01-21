package com.examplo.pagoamigos.repository;

import com.examplo.pagoamigos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByStatus(int status);
	List<Product> findByUsers_IdAndStatus(Long userId, int status);

	// Obtener todos los productos asociados a un usuario (independiente de estatus)
	List<Product> findByUsers_Id(Long userId);
}
