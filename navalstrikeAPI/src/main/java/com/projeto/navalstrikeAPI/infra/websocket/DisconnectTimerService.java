package com.projeto.navalstrikeAPI.infra.websocket;

import com.projeto.navalstrikeAPI.domain.match.service.MatchService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
public class DisconnectTimerService {

    private static final int RECONNECT_TIMEOUT_SECONDS = 10;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();

    private final MatchService matchService;
    private final MatchNotificationService notificationService;

    public DisconnectTimerService(@Lazy MatchService matchService, @Lazy MatchNotificationService notificationService) {
        this.matchService = matchService;
        this.notificationService = notificationService;
    }

    public void startTimer(UUID matchId, UUID playerId) {
        String key = buildKey(matchId, playerId);

        cancelTimer(matchId, playerId);

        notificationService.notifyPlayerDisconnected(matchId, playerId, RECONNECT_TIMEOUT_SECONDS);

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            activeTimers.remove(key);
            try {
                matchService.forfeit(matchId, playerId);
                log.info("Forfeit por timeout: jogador {} não reconectou à partida {} em {}s",
                        playerId, matchId, RECONNECT_TIMEOUT_SECONDS);
            } catch (Exception e) {
                log.debug("Forfeit por timeout ignorado para partida {}: {}", matchId, e.getMessage());
            }
        }, RECONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        activeTimers.put(key, future);
        log.info("Timer de reconexão iniciado: jogador {} tem {}s para reconectar à partida {}",
                playerId, RECONNECT_TIMEOUT_SECONDS, matchId);
    }

    public boolean cancelTimer(UUID matchId, UUID playerId) {
        String key = buildKey(matchId, playerId);
        ScheduledFuture<?> future = activeTimers.remove(key);

        if (future != null && !future.isDone()) {
            future.cancel(false);
            notificationService.notifyPlayerReconnected(matchId, playerId);
            log.info("Timer cancelado: jogador {} reconectou à partida {}", playerId, matchId);
            return true;
        }
        return false;
    }

    public boolean hasActiveTimer(UUID matchId, UUID playerId) {
        String key = buildKey(matchId, playerId);
        ScheduledFuture<?> future = activeTimers.get(key);
        return future != null && !future.isDone();
    }

    private String buildKey(UUID matchId, UUID playerId) {
        return matchId + ":" + playerId;
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
