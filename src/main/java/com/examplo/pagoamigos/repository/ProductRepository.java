package com.examplo.pagoamigos.repository;

import com.examplo.pagoamigos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByStatus(int status);

	// Buscar por creator/validator (join fetch para cargar creator/validator en la misma consulta)
	@Query("select p from Product p join fetch p.creator c where c.id = :creatorId and p.status = :status")
	List<Product> findByCreator_IdAndStatusWithCreator(@Param("creatorId") Long creatorId, @Param("status") int status);

	@Query("select p from Product p join fetch p.validator v where v.id = :validatorId and p.status = :status")
	List<Product> findByValidator_IdAndStatusWithValidator(@Param("validatorId") Long validatorId, @Param("status") int status);

	// Obtener todos los productos creados/validados por un usuario (independiente de estatus)
	@Query("select p from Product p join fetch p.creator c where c.id = :creatorId")
	List<Product> findByCreator_Id(@Param("creatorId") Long creatorId);

	@Query("select p from Product p join fetch p.validator v where v.id = :validatorId")
	List<Product> findByValidator_Id(@Param("validatorId") Long validatorId);

	// Productos entre dos usuarios (creator -> validator)
	@Query("select p from Product p join fetch p.creator c join fetch p.validator v where c.id = :creatorId and v.id = :validatorId")
	List<Product> findByCreator_IdAndValidator_Id(@Param("creatorId") Long creatorId, @Param("validatorId") Long validatorId);

	// Buscar por varios estatus para creator/validator
	@Query("select p from Product p join fetch p.creator c where c.id = :creatorId and p.status in (:statuses)")
	List<Product> findByCreator_IdAndStatusIn(@Param("creatorId") Long creatorId, @Param("statuses") List<Integer> statuses);

	@Query("select p from Product p join fetch p.validator v where v.id = :validatorId and p.status in (:statuses)")
	List<Product> findByValidator_IdAndStatusIn(@Param("validatorId") Long validatorId, @Param("statuses") List<Integer> statuses);
}
