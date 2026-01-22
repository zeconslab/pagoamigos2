package com.examplo.pagoamigos.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    private Double price;

    @Column(nullable = true)
    private Integer status;

    // Configuración de pago mensual
    @Column(name = "monthly_payment_enabled", nullable = false)
    private Boolean monthlyPaymentEnabled = false;

    // Número de cuotas/meses para pago mensual. Null o 0 significa no aplicable.
    @Column(name = "installments")
    private Integer installments;

    // Usuario que creó la solicitud (solicitante)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id")
    private User creator;

    // Usuario que validará (amigo/validador)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "validator_id")
    private User validator;

    // Retorna el monto mensual calculado (precio / installments) si está habilitado.
    @Transient
    public Double getMonthlyPaymentAmount() {
        if (Boolean.TRUE.equals(this.monthlyPaymentEnabled)
                && this.installments != null
                && this.installments > 0
                && this.price != null) {
            return this.price / this.installments;
        }
        return null;
    }

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MonthlyPayment> monthlyPayments = new HashSet<>();
}
