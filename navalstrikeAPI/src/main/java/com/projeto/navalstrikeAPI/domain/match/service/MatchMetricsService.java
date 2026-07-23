package com.projeto.navalstrikeAPI.domain.match.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MatchMetricsService {

    private final Counter matchesCreated;
    private final Counter matchesFinished;

    public MatchMetricsService(MeterRegistry registry) {
        this.matchesCreated = Counter.builder("matches.created")
                .description("Total de partidas criadas")
                .register(registry);
        this.matchesFinished = Counter.builder("matches.finished")
                .description("Total de partidas finalizadas")
                .register(registry);
    }

    public void matchCreated() {
        matchesCreated.increment();
    }

    public void matchFinished() {
        matchesFinished.increment();
    }
}
