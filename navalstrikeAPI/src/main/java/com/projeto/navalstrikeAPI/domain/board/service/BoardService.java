package com.projeto.navalstrikeAPI.domain.board.service;

import com.projeto.navalstrikeAPI.common.exception.InvalidCoordinateException;
import com.projeto.navalstrikeAPI.common.exception.ShipOverlapException;
import com.projeto.navalstrikeAPI.common.exception.ShipPlacementException;
import com.projeto.navalstrikeAPI.domain.board.dto.AttackResult;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.board.repository.BoardRepository;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.ship.dto.PlaceShipRequest;
import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;
import com.projeto.navalstrikeAPI.domain.ship.service.ShipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {
    private static final int BOARD_SIZE = 10;

    private final BoardRepository boardRepository;
    private final ShipService shipService;

    public Board createBoard(){
        Board board = new Board();
        return boardRepository.save(board);
    }

    @Transactional
    public void placeShip(Board board, PlaceShipRequest request){
        // Validar coordenadas dentro do tabuleiro
        for (Coordinate coord : request.coordinates()) {
            validateCoordinate(coord);
        }

        // Validar que não tem mais de 1 navio do mesmo tipo
        boolean alreadyHasType = board.getShips().stream()
                .anyMatch(s -> s.getShipType() == request.type());
        if (alreadyHasType) {
            throw new ShipPlacementException("Já existe um navio deste tipo no tabuleiro");
        }

        // Validar contiguidade
        if (!areCoordinatesContiguous(request.coordinates())) {
            throw new ShipPlacementException("Coordenadas devem ser contíguas em linha reta");
        }

        Ship ship = shipService.createShip(request.type(), request.coordinates());

        if (!shipService.canPlaceShip(board, ship)){
            throw new ShipOverlapException("Navio sobrepõe outro");
        }
        board.getShips().add(ship);
        boardRepository.save(board);
    }

    @Transactional
    public AttackResult attack(Board board, Coordinate coordinate){
        validateCoordinate(coordinate);

        boolean alreadyMissed = board.getMisses().contains(coordinate);
        boolean alreadyHit = board.getShips().stream()
                .anyMatch(ship -> ship.getHits().contains(coordinate));

        if (alreadyMissed || alreadyHit){
            throw new InvalidCoordinateException("Coordenada já atacada");
        }

        for (Ship ship : board.getShips()){
            if (ship.hit(coordinate)){
                boardRepository.save(board);
                return new AttackResult(true, ship.isSunk());
            }
        }
        board.getMisses().add(coordinate);
        boardRepository.save(board);
        return new AttackResult(false, false);
    }

    public boolean allShipsDestroyed(Board board){
        return board.getShips().stream().allMatch(Ship::isSunk);
    }

    private void validateCoordinate(Coordinate coordinate) {
        if (coordinate.getX() < 0 || coordinate.getX() >= BOARD_SIZE ||
            coordinate.getY() < 0 || coordinate.getY() >= BOARD_SIZE) {
            throw new InvalidCoordinateException("Coordenada fora do tabuleiro");
        }
    }

    private boolean areCoordinatesContiguous(Set<Coordinate> coords) {
        List<Coordinate> list = new ArrayList<>(coords);

        // Todas no mesmo X (vertical) ou todas no mesmo Y (horizontal)
        boolean sameX = list.stream().map(Coordinate::getX).distinct().count() == 1;
        boolean sameY = list.stream().map(Coordinate::getY).distinct().count() == 1;

        if (!sameX && !sameY) return false;

        if (sameX) {
            List<Integer> ys = list.stream().map(Coordinate::getY).sorted().toList();
            for (int i = 1; i < ys.size(); i++) {
                if (ys.get(i) - ys.get(i - 1) != 1) return false;
            }
        } else {
            List<Integer> xs = list.stream().map(Coordinate::getX).sorted().toList();
            for (int i = 1; i < xs.size(); i++) {
                if (xs.get(i) - xs.get(i - 1) != 1) return false;
            }
        }
        return true;
    }
}
