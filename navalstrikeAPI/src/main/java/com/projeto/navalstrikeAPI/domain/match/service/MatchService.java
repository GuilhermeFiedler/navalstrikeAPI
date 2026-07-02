package com.projeto.navalstrikeAPI.domain.match.service;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.common.exception.GameAlreadyStartedException;
import com.projeto.navalstrikeAPI.common.exception.MatchNotFoundException;
import com.projeto.navalstrikeAPI.common.exception.PlayerTurnException;
import com.projeto.navalstrikeAPI.domain.board.dto.AttackResult;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.board.service.BoardService;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackRequest;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackResponse;
import com.projeto.navalstrikeAPI.domain.match.repository.MatchRepository;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import com.projeto.navalstrikeAPI.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final BoardService boardService;
    private final UserRepository userRepository;

    public Match createMatch(UUID playerId){
        Board board1 = boardService.createBoard();
        User player1 = userRepository.findById(playerId).orElseThrow();
        Match match = new Match();
        match.setStatus(GameStatus.WAITING);
        match.setBoardPlayer1(board1);
        match.setPlayer1(player1);
        return matchRepository.save(match);
    }

    public Match joinMatch(UUID matchId, UUID playerId){
        Match match = findById(matchId);
        if (match.getStatus() != GameStatus.WAITING){
            throw new GameAlreadyStartedException("Partida já iniciada");
        }
        User player2 = userRepository.findById(playerId).orElseThrow();
        Board board2 = boardService.createBoard();
        match.setBoardPlayer2(board2);
        match.setCurrentTurn(match.getPlayer1());
        match.setStatus(GameStatus.ON_GOING);
        match.setPlayer2(player2);
        return matchRepository.save(match);

    }

    public AttackResponse attack(UUID matchId, AttackRequest request, UUID playerId) {
        Match match = findById(matchId);
        if (!match.getCurrentTurn().getId().equals(playerId)) {
            throw new PlayerTurnException("Não é sua vez");
        }
        Board targetBoard;
        User nextTurn;
        if (match.getPlayer1().getId().equals(playerId)) {
            targetBoard = match.getBoardPlayer2();
            nextTurn = match.getPlayer2();
        } else {
            targetBoard = match.getBoardPlayer1();
            nextTurn = match.getPlayer1();
        }
        Coordinate coord = new Coordinate(request.x(), request.y());
        AttackResult result = boardService.attack(targetBoard, coord);
        boolean gameOver = boardService.allShipsDestroyed(targetBoard);

        if (gameOver) {
            match.setStatus(GameStatus.FINISHED);
        } else {
            match.setCurrentTurn(nextTurn);
        }
        matchRepository.save(match);
        return new AttackResponse(result.hit(), result.sunk(), gameOver);
    }

    public Match findById(UUID id){
        return matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFoundException("Partida não encontrada"));
    }

}
