package com.projeto.navalstrikeAPI.domain.match.service;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.common.enums.ShipType;
import com.projeto.navalstrikeAPI.common.exception.GameAlreadyStartedException;
import com.projeto.navalstrikeAPI.common.exception.MatchNotFoundException;
import com.projeto.navalstrikeAPI.common.exception.PlayerTurnException;
import com.projeto.navalstrikeAPI.common.exception.ShipPlacementException;
import com.projeto.navalstrikeAPI.domain.board.dto.AttackResult;
import com.projeto.navalstrikeAPI.domain.board.dto.BoardView;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.board.service.BoardService;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackRequest;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackResponse;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchListResponse;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchResponse;
import com.projeto.navalstrikeAPI.domain.match.repository.MatchRepository;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.ship.dto.PlaceShipRequest;
import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import com.projeto.navalstrikeAPI.domain.user.repository.UserRepository;
import com.projeto.navalstrikeAPI.infra.websocket.MatchNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final BoardService boardService;
    private final UserRepository userRepository;
    private final MatchNotificationService notificationService;

    @Transactional
    public Match createMatch(UUID playerId){
        Board board1 = boardService.createBoard();
        User player1 = userRepository.findById(playerId).orElseThrow();
        Match match = new Match();
        match.setStatus(GameStatus.WAITING);
        match.setBoardPlayer1(board1);
        match.setPlayer1(player1);
        return matchRepository.save(match);
    }

    @Transactional
    public Match joinMatch(UUID matchId, UUID playerId){
        Match match = findById(matchId);
        if (match.getStatus() != GameStatus.WAITING){
            throw new GameAlreadyStartedException("Partida já iniciada");
        }
        User player2 = userRepository.findById(playerId).orElseThrow();
        Board board2 = boardService.createBoard();
        match.setBoardPlayer2(board2);
        match.setStatus(GameStatus.PLACING);
        match.setPlayer2(player2);
        Match saved = matchRepository.save(match);

        notificationService.notifyPlayerJoined(matchId, playerId, player2.getName());

        return saved;
    }

    @Transactional
    public void placeShip(UUID matchId, UUID playerId, PlaceShipRequest request) {
        Match match = findById(matchId);

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

        if (bothPlayersReady(match)) {
            match.setStatus(GameStatus.ON_GOING);
            match.setCurrentTurn(match.getPlayer1());
            matchRepository.save(match);
            notificationService.notifyGameStarted(matchId);
        }
    }
    private boolean bothPlayersReady(Match match) {
        int requiredShips = ShipType.values().length; // 5
        boolean p1Ready = match.getBoardPlayer1().getShips().size() == requiredShips;
        boolean p2Ready = match.getBoardPlayer2() != null
                && match.getBoardPlayer2().getShips().size() == requiredShips;
        return p1Ready && p2Ready;
    }
    @Transactional
    public AttackResponse attack(UUID matchId, AttackRequest request, UUID playerId) {
        Match match = findById(matchId);
        if(match.getStatus() != GameStatus.ON_GOING){
            throw new IllegalStateException("Partida não está em andamento");
        }
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

        notificationService.notifyAttackResult(matchId, playerId, request.x(), request.y(),
                result.hit(), result.sunk(), gameOver);

        if (gameOver) {
            notificationService.notifyGameOver(matchId, playerId);
        }

        return new AttackResponse(result.hit(), result.sunk(), gameOver);
    }

    @Transactional(readOnly = true)
    public Match findById(UUID id){
        return matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFoundException("Partida não encontrada"));
    }

    @Transactional(readOnly = true)
    public MatchResponse getMatchView(UUID matchId, UUID playerId) {
        Match match = findById(matchId);

        Board myBoard;
        Board opponentBoard;

        if(match.getPlayer1().getId().equals(playerId)){
            myBoard = match.getBoardPlayer1();
            opponentBoard = match.getBoardPlayer2();
        } else if (match.getPlayer2() != null && match.getPlayer2().getId().equals(playerId)){
            myBoard = match.getBoardPlayer2();
            opponentBoard = match.getBoardPlayer1();
        } else {
            throw new IllegalArgumentException("Jogador não pertence a esta partida");
        }
        Set<Coordinate> hitsOnMe = myBoard.getShips().stream()
                .flatMap(ship->ship.getHits().stream())
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

        return new MatchResponse(match.getId(), match.getStatus(), currentTurnId, myBoardView, opponentBoardView);
    }

    @Transactional(readOnly = true)
    public List<MatchListResponse> listAvailableMatches() {
        return matchRepository.findByStatus(GameStatus.WAITING).stream()
                .map(match -> new MatchListResponse(
                        match.getId(),
                        match.getPlayer1().getName(),
                        match.getCreatedAt()
                ))
                .toList();
    }
}
