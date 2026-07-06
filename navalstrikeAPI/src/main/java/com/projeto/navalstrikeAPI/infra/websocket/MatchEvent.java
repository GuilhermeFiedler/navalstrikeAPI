package com.projeto.navalstrikeAPI.infra.websocket;

import java.util.UUID;

public record MatchEvent(
        EventType type,
        UUID matchId,
        UUID playerId,
        Object payload
) {
    public enum EventType {
        PLAYER_JOINED,
        SHIPS_PLACED,
        GAME_STARTED,
        ATTACK_RESULT,
        GAME_OVER,
        PLAYER_FORFEIT
    }
}
