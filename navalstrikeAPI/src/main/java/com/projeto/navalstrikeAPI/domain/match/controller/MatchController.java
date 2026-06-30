package com.projeto.navalstrikeAPI.domain.match.controller;

import com.projeto.navalstrikeAPI.domain.match.dto.AttackResponse;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService service;

    @PostMapping
    public createMatchResponse create(){}

    @PostMapping("/{id}/join")
    public void join(@PathVariable UUID id){}

    @PostMapping("/{id}/attack")
    public AttackResponse attack(){}

    @GetMapping("/{id}")
    public Match get(){}
}
