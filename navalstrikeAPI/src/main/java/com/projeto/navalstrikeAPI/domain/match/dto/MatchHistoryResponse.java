package com.projeto.navalstrikeAPI.domain.match.dto;

import java.time.Instant;
import java.util.UUID;

public record MatchHistoryResponse(
        UUID id,
        String opponentName,
        String result,
        Instant finishedAt,
        boolean forfeit
) {}
