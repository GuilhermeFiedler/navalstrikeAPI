package com.projeto.navalstrikeAPI.domain.match.controller;

import com.projeto.navalstrikeAPI.domain.match.dto.*;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.match.service.MatchService;
import com.projeto.navalstrikeAPI.domain.ship.dto.PlaceShipRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService service;

    @PostMapping
    public CreateMatchResponse create(){
        UUID userId = (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Match match = service.createMatch(userId);
        return new CreateMatchResponse(match.getId());
    }

    @PostMapping("/{id}/join")
    public void join(@PathVariable UUID id){
        UUID userId = (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        service.joinMatch(id,userId);
    }

    @PostMapping("/{id}/attack")
    public AttackResponse attack(@PathVariable UUID id, @RequestBody AttackRequest request){
        UUID userId = (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        return service.attack(id,request,userId);
            }

    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable UUID id){
        UUID userId = (UUID) Objects.requireNonNull(Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal());
        return service.getMatchView(id, userId);
    }
    @PostMapping("/{id}/place")
    public void placeShip(@PathVariable UUID id, @RequestBody PlaceShipRequest request) {
        UUID userId = (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        service.placeShip(id, userId, request);
    }
    @GetMapping
    public List<MatchListResponse> listAvailable() {
        return service.listAvailableMatches();
    }
}
