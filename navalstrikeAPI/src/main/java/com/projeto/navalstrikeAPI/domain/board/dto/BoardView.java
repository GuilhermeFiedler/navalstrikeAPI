package com.projeto.navalstrikeAPI.domain.board.dto;

import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;

import java.util.List;
import java.util.Set;

public record BoardView(List<Ship> ships,
                        Set<Coordinate> hits,
                        Set<Coordinate> misses) {
}
