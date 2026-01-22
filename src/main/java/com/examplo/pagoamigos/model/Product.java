package com.examplo.pagoamigos.model;

import java.util.Set;
import java.util.HashSet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
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

    // Indica si el producto tendrá cuotas (pagos mensuales)
    private Boolean hasInstallments = false;

    // Número de cuotas si aplica
    @Column(nullable = true)
    private Integer installmentsCount;

    // Frecuencia de las cuotas: 'monthly' o 'biweekly' (quincenal)
    @Column(nullable = true)
    private String installmentFrequency;

    // Usuario que creó la solicitud (solicitante)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id")
    private User creator;

    // Usuario que validará (amigo/validador)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "validator_id")
    private User validator;

    // Cuotas mensuales asociadas al producto
    @jakarta.persistence.OneToMany(mappedBy = "product", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MonthlyPayment> monthlyPayments = new HashSet<>();
}
