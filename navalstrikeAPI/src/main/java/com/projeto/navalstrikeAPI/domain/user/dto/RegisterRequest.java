package com.projeto.navalstrikeAPI.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 15, message = "O nome deve ter entre 3 e 15 caracteres") String name,
        @NotBlank @Size(max = 255) @Email(regexp = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = "E-mail inválido") String email,
        @NotBlank @Size(min = 8, max = 128, message = "A senha deve ter entre 8 e 128 caracteres") String password,
        @NotBlank String passwordConfirmation
) {}

