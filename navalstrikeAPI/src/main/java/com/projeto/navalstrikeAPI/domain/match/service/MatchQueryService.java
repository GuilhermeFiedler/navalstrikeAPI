package com.projeto.navalstrikeAPI.domain.match.service;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.common.exception.MatchNotFoundException;
import com.projeto.navalstrikeAPI.domain.board.dto.BoardView;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchHistoryPageResponse;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchHistoryResponse;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchListResponse;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchResponse;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.match.repository.MatchRepository;
import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;
import com.projeto.navalstrikeAPI.domain.skin.service.SkinService;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import com.projeto.navalstrikeAPI.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchQueryService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final SkinService skinService;

    @Transactional(readOnly = true)
    public Match findById(UUID id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFoundException("Partida não encontrada"));
    }

    @Transactional(readOnly = true)
    public MatchResponse getMatchView(UUID matchId, UUID playerId) {
        Match match = findById(matchId);

        Board myBoard;
        Board opponentBoard;
        UUID opponentId = null;

        if (match.getPlayer1().getId().equals(playerId)) {
            myBoard = match.getBoardPlayer1();
            opponentBoard = match.getBoardPlayer2();
            if (match.getPlayer2() != null) {
                opponentId = match.getPlayer2().getId();
            }
        } else if (match.getPlayer2() != null && match.getPlayer2().getId().equals(playerId)) {
            myBoard = match.getBoardPlayer2();
            opponentBoard = match.getBoardPlayer1();
            opponentId = match.getPlayer1().getId();
        } else {
            throw new IllegalArgumentException("Jogador não pertence a esta partida");
        }

        String mySkinSlug = skinService.getSkinSlug(playerId);
        String opponentSkinSlug = opponentId != null ? skinService.getSkinSlug(opponentId) : null;

        Set<Coordinate> hitsOnMe = myBoard.getShips().stream()
                .flatMap(ship -> ship.getHits().stream())
                .collect(Collectors.toSet());

        BoardView myBoardView = new BoardView(myBoard.getShips(), hitsOnMe, myBoard.getMisses());

        BoardView opponentBoardView;
        if (opponentBoard != null) {
            List<Ship> sunkShips = opponentBoard.getShips().stream()
                    .filter(Ship::isSunk)
                    .toList();
            Set<Coordinate> hitsOnOpponent = opponentBoard.getShips().stream()
                    .flatMap(ship -> ship.getHits().stream())
                    .collect(Collectors.toSet());
            opponentBoardView = new BoardView(sunkShips, hitsOnOpponent, opponentBoard.getMisses());
        } else {
            opponentBoardView = null;
        }

        UUID currentTurnId = match.getCurrentTurn() != null ? match.getCurrentTurn().getId() : null;

        return new MatchResponse(match.getId(), match.getStatus(), currentTurnId, mySkinSlug, opponentSkinSlug, myBoardView, opponentBoardView);
    }

    @Transactional(readOnly = true)
    public List<MatchListResponse> listAvailableMatches() {
        return matchRepository.findByStatus(GameStatus.WAITING).stream()
                .map(match -> new MatchListResponse(
                        match.getId(),
                        match.getPlayer1().getId(),
                        match.getPlayer1().getName(),
                        match.getCode(),
                        match.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public MatchHistoryPageResponse getMatchHistory(UUID playerId, int page, int size) {
        User player = userRepository.findById(playerId).orElseThrow();
        Page<Match> matches = matchRepository.findFinishedByPlayer(player, PageRequest.of(page, size));

        List<MatchHistoryResponse> content = matches.getContent().stream().map(match -> {
            String opponentName;
            if (match.getPlayer1().getId().equals(playerId)) {
                opponentName = match.getPlayer2() != null ? match.getPlayer2().getName() : "Desconhecido";
            } else {
                opponentName = match.getPlayer1().getName();
            }

            String result = match.getWinner() != null && match.getWinner().getId().equals(playerId)
                    ? "VICTORY" : "DEFEAT";

            return new MatchHistoryResponse(
                    match.getId(),
                    opponentName,
                    result,
                    match.getFinishedAt(),
                    match.isForfeit()
            );
        }).toList();

        long totalVictories = matchRepository.countVictories(player);
        long totalDefeats = matchRepository.countDefeats(player);

        return MatchHistoryPageResponse.of(content, page, size, matches.getTotalElements(), totalVictories, totalDefeats);
    }
}
