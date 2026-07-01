package com.projeto.navalstrikeAPI.domain.board.service;

import com.projeto.navalstrikeAPI.common.exception.ShipOverlapException;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.board.repository.BoardRepository;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.ship.dto.PlaceShipRequest;
import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;
import com.projeto.navalstrikeAPI.domain.ship.service.ShipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final ShipService shipService;

    public Board createBoard(){
        Board board = new Board();
        return boardRepository.save(board);
    }

    public void placeShip(UUID boardId, PlaceShipRequest request){
        Board board = boardRepository.findById(boardId).orElseThrow();
        Ship ship = shipService.createShip(request.type(), request.coordinates());

        if (!shipService.canPlaceShip(board, ship)){
            throw new ShipOverlapException("Navio sobrepõe outro");
        }
        board.getShips().add(ship);
        boardRepository.save(board);
    }

    public boolean attack(Board board, Coordinate coordinate){
        for (Ship ship : board.getShips()){
            if (ship.hit(coordinate)){
                boardRepository.save(board);
                return true;
            }
        }
        return false;
    }

    public boolean allShipsDestroyed(Board board){
        return board.getShips().stream().allMatch(Ship::isSunk);
    }
}
