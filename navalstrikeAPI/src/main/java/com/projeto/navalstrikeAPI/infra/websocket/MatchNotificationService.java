package com.projeto.navalstrikeAPI.infra.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notify(UUID matchId, MatchEvent event) {
        messagingTemplate.convertAndSend("/topic/match/" + matchId, event);
    }

    public void notifyPlayerJoined(UUID matchId, UUID playerId, String playerName) {
        var event = new MatchEvent(
                MatchEvent.EventType.PLAYER_JOINED,
                matchId,
                playerId,
                Map.of("playerName", playerName)
        );
        notify(matchId, event);
    }

    public void notifyShipsPlaced(UUID matchId, UUID playerId) {
        var event = new MatchEvent(
                MatchEvent.EventType.SHIPS_PLACED,
                matchId,
                playerId,
                null
        );
        notify(matchId, event);
    }

    public void notifyGameStarted(UUID matchId) {
        var event = new MatchEvent(
                MatchEvent.EventType.GAME_STARTED,
                matchId,
                null,
                null
        );
        notify(matchId, event);
    }

    public void notifyAttackResult(UUID matchId, UUID attackerId, int x, int y, boolean hit, boolean sunk, String shipType, boolean gameOver, String skinSlug) {
        var payload = new java.util.HashMap<String, Object>();
        payload.put("x", x);
        payload.put("y", y);
        payload.put("hit", hit);
        payload.put("sunk", sunk);
        payload.put("shipType", shipType);
        payload.put("gameOver", gameOver);
        payload.put("skinSlug", sunk ? skinSlug : null);

        var event = new MatchEvent(
                MatchEvent.EventType.ATTACK_RESULT,
                matchId,
                attackerId,
                payload
        );
        notify(matchId, event);
    }

    public void notifyGameOver(UUID matchId, UUID winnerId) {
        var event = new MatchEvent(
                MatchEvent.EventType.GAME_OVER,
                matchId,
                winnerId,
                Map.of("winnerId", winnerId)
        );
        notify(matchId, event);
    }

    public void notifyForfeit(UUID matchId, UUID quitterId, UUID winnerId) {
        var event = new MatchEvent(
                MatchEvent.EventType.PLAYER_FORFEIT,
                matchId,
                quitterId,
                Map.of("winnerId", winnerId, "quitterId", quitterId)
        );
        notify(matchId, event);
    }

    public void notifyPlayerDisconnected(UUID matchId, UUID playerId, int timeoutSeconds) {
        var event = new MatchEvent(
                MatchEvent.EventType.PLAYER_DISCONNECTED,
                matchId,
                playerId,
                Map.of("timeoutSeconds", timeoutSeconds)
        );
        notify(matchId, event);
    }

    public void notifyPlayerReconnected(UUID matchId, UUID playerId) {
        var event = new MatchEvent(
                MatchEvent.EventType.PLAYER_RECONNECTED,
                matchId,
                playerId,
                null
        );
        notify(matchId, event);
    }
}
