package com.projeto.navalstrikeAPI.domain.match.service;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.common.enums.ShipType;
import com.projeto.navalstrikeAPI.common.exception.GameAlreadyStartedException;
import com.projeto.navalstrikeAPI.common.exception.MatchNotFoundException;
import com.projeto.navalstrikeAPI.common.exception.PlayerTurnException;
import com.projeto.navalstrikeAPI.common.exception.ShipPlacementException;
import com.projeto.navalstrikeAPI.domain.board.dto.AttackResult;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.board.service.BoardService;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackRequest;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackResponse;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.match.repository.MatchRepository;
import com.projeto.navalstrikeAPI.domain.ship.dto.PlaceShipRequest;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import com.projeto.navalstrikeAPI.domain.user.repository.UserRepository;
import com.projeto.navalstrikeAPI.infra.transaction.TransactionHelper;
import com.projeto.navalstrikeAPI.infra.websocket.MatchNotificationService;
import com.projeto.navalstrikeAPI.domain.skin.service.SkinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final BoardService boardService;
    private final UserRepository userRepository;
    private final MatchNotificationService notificationService;
    private final TransactionHelper transactionHelper;
    private final SkinService skinService;
    private final MatchQueryService matchQueryService;

    @Transactional
    public Match createMatch(UUID playerId) {
        Board board1 = boardService.createBoard();
        User player1 = userRepository.findById(playerId).orElseThrow();
        Match match = new Match();
        match.setStatus(GameStatus.WAITING);
        match.setBoardPlayer1(board1);
        match.setPlayer1(player1);
        match.setCode(generateRoomCode());
        return matchRepository.save(match);
    }

    @Transactional
    public Match joinMatch(UUID matchId, UUID playerId) {
        Match match = matchQueryService.findById(matchId);
        if (match.getStatus() == GameStatus.CANCELLED) {
            throw new IllegalStateException("Partida foi cancelada");
        }
        if (match.getStatus() != GameStatus.WAITING) {
            throw new GameAlreadyStartedException("Partida já iniciada");
        }
        if (match.getPlayer1().getId().equals(playerId)) {
            throw new IllegalArgumentException("Não é possível entrar na própria partida");
        }
        User player2 = userRepository.findById(playerId).orElseThrow();
        Board board2 = boardService.createBoard();
        match.setBoardPlayer2(board2);
        match.setStatus(GameStatus.PLACING);
        match.setPlayer2(player2);
        Match saved = matchRepository.save(match);

        String playerName = player2.getName();
        afterCommit(() -> notificationService.notifyPlayerJoined(matchId, playerId, playerName));

        return saved;
    }

    @Transactional
    public Match joinMatchByCode(String code, UUID playerId) {
        Match match = matchRepository.findActiveByCode(code.toUpperCase())
                .orElseThrow(() -> new MatchNotFoundException("Partida não encontrada com o código: " + code));

        return joinMatch(match.getId(), playerId);
    }

    @Transactional
    public void placeShip(UUID matchId, UUID playerId, PlaceShipRequest request) {
        Match match = matchQueryService.findById(matchId);

        if (match.getStatus() != GameStatus.WAITING && match.getStatus() != GameStatus.PLACING) {
            throw new ShipPlacementException("Não é possível posicionar navios nesta fase");
        }

        Board board;
        if (match.getPlayer1().getId().equals(playerId)) {
            board = match.getBoardPlayer1();
        } else if (match.getPlayer2() != null && match.getPlayer2().getId().equals(playerId)) {
            board = match.getBoardPlayer2();
        } else {
            throw new ShipPlacementException("Jogador não pertence a esta partida");
        }

        boardService.placeShip(board, request);

        int requiredShips = ShipType.values().length;
        boolean playerReady = board.getShips().size() == requiredShips;

        if (playerReady && bothPlayersReady(match)) {
            match.setStatus(GameStatus.ON_GOING);
            match.setCurrentTurn(match.getPlayer1());
            matchRepository.save(match);
            afterCommit(() -> {
                notificationService.notifyShipsPlaced(matchId, playerId);
                notificationService.notifyGameStarted(matchId);
            });
        } else if (playerReady) {
            afterCommit(() -> notificationService.notifyShipsPlaced(matchId, playerId));
        }
    }

    @Transactional
    public AttackResponse attack(UUID matchId, AttackRequest request, UUID playerId) {
        Match match = matchQueryService.findById(matchId);
        if (match.getStatus() != GameStatus.ON_GOING) {
            throw new IllegalStateException("Partida não está em andamento");
        }
        if (!match.getCurrentTurn().getId().equals(playerId)) {
            throw new PlayerTurnException("Não é sua vez");
        }

        Board targetBoard;
        User nextTurn;
        UUID targetPlayerId;
        if (match.getPlayer1().getId().equals(playerId)) {
            targetBoard = match.getBoardPlayer2();
            nextTurn = match.getPlayer2();
            targetPlayerId = match.getPlayer2().getId();
        } else {
            targetBoard = match.getBoardPlayer1();
            nextTurn = match.getPlayer1();
            targetPlayerId = match.getPlayer1().getId();
        }

        Coordinate coord = new Coordinate(request.x(), request.y());
        AttackResult result = boardService.attack(targetBoard, coord);
        boolean gameOver = boardService.allShipsDestroyed(targetBoard);

        if (gameOver) {
            match.setStatus(GameStatus.FINISHED);
            match.setFinishedAt(Instant.now());
            match.setWinner(userRepository.findById(playerId).orElseThrow());
        } else if (!result.hit()) {
            match.setCurrentTurn(nextTurn);
        }

        matchRepository.save(match);

        String skinSlug = result.sunk() ? skinService.getSkinSlug(targetPlayerId) : null;

        afterCommit(() -> {
            notificationService.notifyAttackResult(matchId, playerId, request.x(), request.y(),
                    result.hit(), result.sunk(),
                    result.shipType() != null ? result.shipType().name() : null,
                    gameOver, skinSlug);

            if (gameOver) {
                notificationService.notifyGameOver(matchId, playerId);
            }
        });

        return new AttackResponse(result.hit(), result.sunk(),
                result.shipType() != null ? result.shipType().name() : null,
                gameOver);
    }

    @Transactional
    public void forfeit(UUID matchId, UUID playerId) {
        Match match = matchQueryService.findById(matchId);

        if (match.getStatus() == GameStatus.FINISHED || match.getStatus() == GameStatus.CANCELLED) {
            throw new IllegalStateException("Partida já finalizada");
        }

        UUID winnerId;
        if (match.getPlayer1().getId().equals(playerId)) {
            winnerId = match.getPlayer2() != null ? match.getPlayer2().getId() : null;
        } else if (match.getPlayer2() != null && match.getPlayer2().getId().equals(playerId)) {
            winnerId = match.getPlayer1().getId();
        } else {
            throw new IllegalArgumentException("Jogador não pertence a esta partida");
        }

        if (winnerId == null) {
            match.setStatus(GameStatus.CANCELLED);
            match.setFinishedAt(Instant.now());
            match.setForfeit(true);
            matchRepository.save(match);
            return;
        }

        match.setStatus(GameStatus.FINISHED);
        match.setFinishedAt(Instant.now());
        match.setForfeit(true);
        match.setWinner(userRepository.findById(winnerId).orElseThrow());
        matchRepository.save(match);

        UUID finalWinnerId = winnerId;
        afterCommit(() -> notificationService.notifyForfeit(matchId, playerId, finalWinnerId));
    }

    private boolean bothPlayersReady(Match match) {
        int requiredShips = ShipType.values().length;
        boolean p1Ready = match.getBoardPlayer1().getShips().size() == requiredShips;
        boolean p2Ready = match.getBoardPlayer2() != null
                && match.getBoardPlayer2().getShips().size() == requiredShips;
        return p1Ready && p2Ready;
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom random = new SecureRandom();
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (matchRepository.existsActiveByCode(code));
        return code;
    }

    private void afterCommit(Runnable action) {
        transactionHelper.afterCommit(action);
    }
}
