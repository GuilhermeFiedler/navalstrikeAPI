package com.projeto.navalstrikeAPI.domain.ranking.dto;

import java.util.UUID;

public record RankingResponse(
        UUID id,
        String name,
        long victories,
        long defeats,
        long totalMatches
) {
}
