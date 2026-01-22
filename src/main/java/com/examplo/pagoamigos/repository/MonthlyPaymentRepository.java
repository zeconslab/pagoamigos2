package com.examplo.pagoamigos.repository;

import com.examplo.pagoamigos.model.MonthlyPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface MonthlyPaymentRepository extends JpaRepository<MonthlyPayment, Long> {
    List<MonthlyPayment> findByProductId(Long productId);
    List<MonthlyPayment> findByPayerId(Long payerId);
    List<MonthlyPayment> findByPaidFalseAndDueDateBefore(LocalDate date);
}
