package com.examplo.pagoamigos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_payment")
@Getter
@Setter
public class MonthlyPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Usuario que debe pagar (puede ser el creator o cualquier otro)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payer_id")
    private User payer;

    // Número de cuota (1..n)
    private Integer installmentNumber;

    // Monto de la cuota (puede calcularse del producto, pero lo guardamos para trazabilidad)
    private Double amount;

    // Fecha de vencimiento
    private LocalDate dueDate;

    // Si fue pagado
    private Boolean paid = false;

    // Fecha de pago
    private LocalDateTime paidAt;

    // Estado adicional si se requiere
    @Column(nullable = true)
    private Integer status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
