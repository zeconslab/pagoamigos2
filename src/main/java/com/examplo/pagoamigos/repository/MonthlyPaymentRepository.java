package com.examplo.pagoamigos.repository;

import com.examplo.pagoamigos.model.MonthlyPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyPaymentRepository extends JpaRepository<MonthlyPayment, Long> {
    List<MonthlyPayment> findByProduct_Id(Long productId);
}
