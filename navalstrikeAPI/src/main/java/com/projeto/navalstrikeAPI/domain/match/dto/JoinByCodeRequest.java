package com.projeto.navalstrikeAPI.domain.match.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinByCodeRequest(
        @NotBlank @Size(min = 6, max = 6) String code
) {}
