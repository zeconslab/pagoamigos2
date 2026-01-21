package com.examplo.pagoamigos.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras")
    @Column(nullable = false, length = 50)
    private String name;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido materno debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras")
    @Column(nullable = false, length = 50)
    private String maternalLastName;
    
    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido paterno debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras")
    @Column(nullable = false, length = 50)
    private String paternalLastName;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    @Column(nullable = false, length = 10)
    private String phone;
    
    @NotNull
    @Column(nullable = false)
    private boolean active;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles;

    // Amigos (usuarios relacionados entre sí)
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_friends",joinColumns = @JoinColumn(name = "user_id"),inverseJoinColumns = @JoinColumn(name = "friend_id"))
    private Set<User> friends = new HashSet<>();

    // Productos que creó este usuario
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private Set<Product> createdProducts;

    // Productos que este usuario valida
    @OneToMany(mappedBy = "validator", fetch = FetchType.LAZY)
    private Set<Product> validatedProducts;

    public void addFriend(User friend) {
        if (friend == null || friend.equals(this)) return;
        if (this.friends == null) this.friends = new HashSet<>();
        if (friend.getFriends() == null) friend.setFriends(new HashSet<>());
        if (this.friends.add(friend)) {
            friend.getFriends().add(this);
        }
    }

    public void removeFriend(User friend) {
        if (friend == null || this.friends == null) return;
        if (this.friends.remove(friend)) {
            if (friend.getFriends() != null) friend.getFriends().remove(this);
        }
    }
}
