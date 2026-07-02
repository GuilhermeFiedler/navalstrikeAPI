package com.projeto.navalstrikeAPI.domain.match.controller;

import com.projeto.navalstrikeAPI.domain.match.dto.AttackRequest;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackResponse;
import com.projeto.navalstrikeAPI.domain.match.dto.CreateMatchResponse;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchResponse;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
}
