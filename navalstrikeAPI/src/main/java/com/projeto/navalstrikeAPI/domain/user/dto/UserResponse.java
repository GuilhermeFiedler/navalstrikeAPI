package com.projeto.navalstrikeAPI.domain.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name
) {
}

