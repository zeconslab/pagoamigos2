package com.examplo.pagoamigos.repository;

import com.examplo.pagoamigos.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
	Page<Product> findByIdStartingWith(String prefix, Pageable pageable);
}
