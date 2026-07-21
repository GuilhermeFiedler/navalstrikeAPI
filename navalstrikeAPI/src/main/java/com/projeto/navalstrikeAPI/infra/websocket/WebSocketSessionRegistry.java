package com.projeto.navalstrikeAPI.infra.websocket;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private record SessionInfo(UUID matchId, UUID playerId) {}

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public void register(String sessionId, UUID matchId, UUID playerId) {
        sessions.put(sessionId, new SessionInfo(matchId, playerId));
    }

    public void unregister(String sessionId) {
        sessions.remove(sessionId);
    }

    public Optional<UUID> getMatchId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId)).map(SessionInfo::matchId);
    }

    public Optional<UUID> getPlayerId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId)).map(SessionInfo::playerId);
    }

    public boolean isRegistered(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public Optional<String> findSessionByPlayerAndMatch(UUID playerId, UUID matchId) {
        return sessions.entrySet().stream()
                .filter(entry -> entry.getValue().playerId().equals(playerId)
                        && entry.getValue().matchId().equals(matchId))
                .map(Map.Entry::getKey)
                .findFirst();
    }

}
