package com.projeto.navalstrikeAPI.domain.ship.service;

import com.projeto.navalstrikeAPI.common.enums.ShipType;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ShipService {

    public Ship createShip(ShipType type, Set<Coordinate> coordinates){

    }

    public boolean canPlaceShip(Board board, Ship ship){}

}
