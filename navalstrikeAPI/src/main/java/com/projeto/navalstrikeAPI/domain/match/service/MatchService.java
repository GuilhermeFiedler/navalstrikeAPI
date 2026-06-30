package com.projeto.navalstrikeAPI.domain.match.service;

import com.projeto.navalstrikeAPI.domain.board.service.BoardService;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackRequest;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackResponse;
import com.projeto.navalstrikeAPI.domain.match.repository.MatchRepository;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final BoardService boardService;

    public Match createMatch(){

    }

    public MatchJoinMatch(UUID matchId){

    }

    public AttackResponse attack(UUID matchId, AttackRequest request){

    }

    public Match findById(UUID id){

    }

}
