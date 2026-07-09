package com.projeto.navalstrikeAPI.domain.board.dto;

import com.projeto.navalstrikeAPI.common.enums.ShipType;

public record AttackResult(boolean hit, boolean sunk, ShipType shipType) {}
