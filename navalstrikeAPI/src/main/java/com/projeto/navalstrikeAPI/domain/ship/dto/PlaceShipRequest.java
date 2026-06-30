package com.projeto.navalstrikeAPI.domain.ship.dto;

import com.projeto.navalstrikeAPI.common.enums.ShipType;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;

import java.util.Set;

public record PlaceShipRequest(ShipType type, Set<Coordinate> coordinates){
}
