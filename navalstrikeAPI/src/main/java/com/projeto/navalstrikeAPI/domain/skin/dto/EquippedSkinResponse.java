package com.projeto.navalstrikeAPI.domain.skin.dto;

import com.projeto.navalstrikeAPI.common.enums.ShipType;

import java.util.Map;
import java.util.UUID;

public record EquippedSkinResponse(
        UUID skinPackId,
        String slug,
        String name,
        Map<ShipType, String> assets
) {
}
