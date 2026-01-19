package com.examplo.pagoamigos.dto;
import jakarta.validation.constraints.NotBlank;

public class UsuarioDTO {
    @NotBlank
    private String nombre;
    
    @NotBlank
    private String correo;
    
    @NotBlank
    private String contraseña;

    @NotBlank
    private String apellidoMaterno;
    
    @NotBlank
    private String apellidoPaterno;

    @NotBlank
    private String telefono;
}
