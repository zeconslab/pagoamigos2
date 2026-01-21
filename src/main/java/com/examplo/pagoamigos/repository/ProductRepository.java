package com.examplo.pagoamigos.repository;

import com.examplo.pagoamigos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByStatus(int status);

	// Buscar por creator/validator
	List<Product> findByCreator_IdAndStatus(Long creatorId, int status);
	List<Product> findByValidator_IdAndStatus(Long validatorId, int status);

	// Obtener todos los productos creados/validados por un usuario (independiente de estatus)
	List<Product> findByCreator_Id(Long creatorId);
	List<Product> findByValidator_Id(Long validatorId);

	// Productos entre dos usuarios (creator -> validator)
	List<Product> findByCreator_IdAndValidator_Id(Long creatorId, Long validatorId);

	// Buscar por varios estatus para creator/validator
	List<Product> findByCreator_IdAndStatusIn(Long creatorId, List<Integer> statuses);
	List<Product> findByValidator_IdAndStatusIn(Long validatorId, List<Integer> statuses);
}
