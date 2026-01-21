package com.examplo.pagoamigos.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {

    @Id
    @Column(name = "id", length = 50, nullable = true)
    private String id;

    private String name;
    private Double price;

    @ManyToMany(mappedBy = "products")
    private Set<User> users;
}
