package com.examplo.pagoamigos.service.impl;

import org.springframework.stereotype.Service;

import com.examplo.pagoamigos.model.User;
import com.examplo.pagoamigos.repository.UserRepository;
import com.examplo.pagoamigos.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User buscarPorCorreo(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
