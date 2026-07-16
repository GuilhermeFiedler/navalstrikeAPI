package com.projeto.navalstrikeAPI.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email(regexp = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = "E-mail inválido") String email,
        @NotBlank @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres") String password,
        @NotBlank String passwordConfirmation
) {}

