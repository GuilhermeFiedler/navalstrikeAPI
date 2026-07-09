package com.projeto.navalstrikeAPI.domain.match.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatchHistoryResponse(
        UUID id,
        String opponentName,
        String result,
        LocalDateTime finishedAt,
        boolean forfeit
) {}
