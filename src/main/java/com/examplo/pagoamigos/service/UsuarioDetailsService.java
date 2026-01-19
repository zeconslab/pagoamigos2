package com.examplo.pagoamigos.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.examplo.pagoamigos.model.User;
import com.examplo.pagoamigos.repository.UserRepository;
import com.examplo.pagoamigos.security.UsuarioDetails;

public class UsuarioDetailsService implements UserDetailsService{
    private final UserRepository userRepository;

    public UsuarioDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(username)
            .orElseThrow(() ->
                new UsernameNotFoundException("Usuario no encontrado"));

        return new UsuarioDetails(user);
    }
}
