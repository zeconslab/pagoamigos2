package com.examplo.pagoamigos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;
    
    @NotNull
    private String email;
    
    @NotNull
    private String password;

    @NotNull
    private String maternalLastName;
    
    @NotNull
    private String paternalLastName;

    @NotNull
    private String phone;
    
    @NotNull
    private boolean active;
}
