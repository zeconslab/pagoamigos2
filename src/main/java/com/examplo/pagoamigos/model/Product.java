package com.examplo.pagoamigos.model;

import java.util.Set;
import java.time.LocalDateTime;
import java.util.HashSet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @NotNull(message = "El monto estimado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private Double price;
    @Column(nullable = true)
    private Integer status;

    // Indica si el producto tendrá cuotas (pagos mensuales)
    private Boolean hasInstallments = false;

    // Mapeo para columna existente en BD que no estaba en el modelo
    @jakarta.persistence.Column(name = "monthly_payment_enabled", nullable = false)
    private Boolean monthlyPaymentEnabled = false;

    // Número de cuotas si aplica
    @Column(nullable = true)
    @Min(value = 1, message = "La cantidad de cuotas debe ser al menos 1")
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

    // Nombre de archivo de la imagen asociada (opcional)
    @Column(name = "image_filename", nullable = true)
    private String imageFilename;

    // Fecha de creación del producto
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
