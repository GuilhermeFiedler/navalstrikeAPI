package com.projeto.navalstrikeAPI.domain.ranking.controller;

import com.projeto.navalstrikeAPI.common.dto.PageResponse;
import com.projeto.navalstrikeAPI.domain.ranking.dto.RankingResponse;
import com.projeto.navalstrikeAPI.domain.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public PageResponse<RankingResponse> getRanking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "victories") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return rankingService.getRanking(page, size, sort, direction);
    }
}
