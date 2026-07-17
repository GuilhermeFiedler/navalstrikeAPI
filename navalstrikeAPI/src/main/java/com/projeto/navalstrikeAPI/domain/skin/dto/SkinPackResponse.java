package com.projeto.navalstrikeAPI.domain.skin.dto;

import java.util.UUID;

public record SkinPackResponse(
        UUID id,
        String slug,
        String name,
        String description
) {
}
