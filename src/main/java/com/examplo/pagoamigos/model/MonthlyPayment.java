package com.examplo.pagoamigos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "monthly_payment")
@Getter
@Setter
public class MonthlyPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private Double amount;

    // Indica si la cuota ya fue pagada
    @Column(nullable = false)
    private Boolean paid = false;

    // Fecha en que se registró el pago (nullable)
    private LocalDate paymentDate;

}
