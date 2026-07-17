package com.projeto.navalstrikeAPI.domain.ranking.service;

import com.projeto.navalstrikeAPI.common.dto.PageResponse;
import com.projeto.navalstrikeAPI.domain.ranking.dto.RankingResponse;
import com.projeto.navalstrikeAPI.domain.ranking.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    @Transactional(readOnly = true)
    public PageResponse<RankingResponse> getRanking(int page, int size, String sort, String direction) {
        Pageable pageable = PageRequest.of(page, size);
        boolean desc = "desc".equalsIgnoreCase(direction);

        Page<Object[]> result = switch (sort) {
            case "defeats" -> desc
                    ? rankingRepository.findRankingByDefeatsDesc(pageable)
                    : rankingRepository.findRankingByDefeatsAsc(pageable);
            case "totalMatches" -> desc
                    ? rankingRepository.findRankingByTotalMatchesDesc(pageable)
                    : rankingRepository.findRankingByTotalMatchesAsc(pageable);
            default -> desc
                    ? rankingRepository.findRankingByVictoriesDesc(pageable)
                    : rankingRepository.findRankingByVictoriesAsc(pageable);
        };

        List<RankingResponse> content = result.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.of(content, page, size, result.getTotalElements());
    }

    private RankingResponse toResponse(Object[] row) {
        return new RankingResponse(
                (UUID) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue()
        );
    }
}
