package com.projeto.navalstrikeAPI.domain.skin.dto;

import java.util.UUID;

public record EquippedSkinResponse(
        UUID skinPackId,
        String slug,
        String name
) {
}
