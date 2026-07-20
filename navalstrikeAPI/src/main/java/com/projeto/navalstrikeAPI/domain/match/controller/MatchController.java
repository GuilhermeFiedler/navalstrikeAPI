package com.projeto.navalstrikeAPI.domain.match.controller;

import com.projeto.navalstrikeAPI.domain.match.dto.*;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.match.service.MatchQueryService;
import com.projeto.navalstrikeAPI.domain.match.service.MatchService;
import com.projeto.navalstrikeAPI.domain.ship.dto.PlaceShipRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService service;
    private final MatchQueryService queryService;

    @PostMapping
    public CreateMatchResponse create() {
        UUID userId = getUserId();
        Match match = service.createMatch(userId);
        return new CreateMatchResponse(match.getId(), match.getCode());
    }

    @PostMapping("/{id}/join")
    public void join(@PathVariable UUID id) {
        service.joinMatch(id, getUserId());
    }

    @PostMapping("/join-by-code")
    public Map<String, UUID> joinByCode(@RequestBody @Valid JoinByCodeRequest request) {
        Match match = service.joinMatchByCode(request.code(), getUserId());
        return Map.of("matchId", match.getId());
    }

    @PostMapping("/{id}/attack")
    public AttackResponse attack(@PathVariable UUID id, @RequestBody AttackRequest request) {
        return service.attack(id, request, getUserId());
    }

    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable UUID id) {
        return queryService.getMatchView(id, getUserId());
    }

    @PostMapping("/{id}/place")
    public void placeShip(@PathVariable UUID id, @RequestBody PlaceShipRequest request) {
        service.placeShip(id, getUserId(), request);
    }

    @GetMapping
    public List<MatchListResponse> listAvailable() {
        return queryService.listAvailableMatches();
    }

    @GetMapping("/history")
    public MatchHistoryPageResponse history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return queryService.getMatchHistory(getUserId(), page, size);
    }

    @PostMapping("/{id}/forfeit")
    public void forfeit(@PathVariable UUID id) {
        service.forfeit(id, getUserId());
    }

    private UUID getUserId() {
        return (UUID) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getPrincipal();
    }
}
