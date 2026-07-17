package com.projeto.navalstrikeAPI.domain.match.dto;

import java.util.List;

public record MatchHistoryPageResponse(
        List<MatchHistoryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        long totalVictories,
        long totalDefeats
) {
    public static MatchHistoryPageResponse of(
            List<MatchHistoryResponse> content,
            int page, int size, long totalElements,
            long totalVictories, long totalDefeats
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new MatchHistoryPageResponse(content, page, size, totalElements, totalPages, totalVictories, totalDefeats);
    }
}
