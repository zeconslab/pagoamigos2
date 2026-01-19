package com.examplo.pagoamigos.service;

import com.examplo.pagoamigos.model.User;

public interface UserService {
    User buscarPorCorreo(String email);
}
