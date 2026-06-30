package com.projeto.navalstrikeAPI.domain.board.service;

import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.board.repository.BoardRepository;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public Board createBoard(){}

    public void placeShip(UUID boardId, PlaceShipRequest request){}

    public boolean attack(Board board, Coordinate coordinate){}

    public boolean AllShipsDestroyed(Board board){}
}
