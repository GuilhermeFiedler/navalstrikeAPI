package com.projeto.navalstrikeAPI.domain.match.dto;

public record AttackResponse(boolean hit, boolean sunk, String shipType, boolean gameOver) {
}
