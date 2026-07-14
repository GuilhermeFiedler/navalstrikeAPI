package com.projeto.navalstrikeAPI.domain.skin.dto;

import com.projeto.navalstrikeAPI.common.enums.ShipType;

import java.util.Map;
import java.util.UUID;

public record SkinPackResponse(
        UUID id,
        String slug,
        String name,
        String description,
        Map<ShipType, String> assets
) {
}
