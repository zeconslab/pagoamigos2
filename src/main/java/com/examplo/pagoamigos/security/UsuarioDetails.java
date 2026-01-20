package com.examplo.pagoamigos.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.examplo.pagoamigos.model.User;

public class UsuarioDetails implements UserDetails {
    private final User user;

    public UsuarioDetails(User user) {
        this.user = user;
    }

    /**
     * Devuelve el nombre completo del usuario para mostrar en las vistas.
     */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (user.getName() != null) sb.append(user.getName());
        if (user.getPaternalLastName() != null) sb.append(" ").append(user.getPaternalLastName());
        if (user.getMaternalLastName() != null) sb.append(" ").append(user.getMaternalLastName());
        String full = sb.toString().trim();
        return full.isEmpty() ? user.getEmail() : full;
    }

    /** Exponer el objeto User subyacente si se necesita en templates */
    public User getUser() {
        return this.user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return  user.getRoles().stream().map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre())).toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.isActive(); }
}
