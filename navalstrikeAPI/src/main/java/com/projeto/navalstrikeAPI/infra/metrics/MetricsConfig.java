package com.projeto.navalstrikeAPI.infra.metrics;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    public MetricsConfig(EntityManagerFactory entityManagerFactory, MeterRegistry registry) {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);

        FunctionCounter.builder("hibernate.query.execution.total", stats, Statistics::getQueryExecutionCount)
                .register(registry);

        Gauge.builder("hibernate.query.execution.max.seconds", stats,
                        s -> s.getQueryExecutionMaxTime() / 1000.0)
                .register(registry);

        FunctionCounter.builder("hibernate.entities.loads.total", stats, Statistics::getEntityLoadCount)
                .register(registry);

        FunctionCounter.builder("hibernate.entities.inserts.total", stats, Statistics::getEntityInsertCount)
                .register(registry);

        FunctionCounter.builder("hibernate.statements.total", stats,
                        s -> s.getPrepareStatementCount())
                .register(registry);

        FunctionCounter.builder("hibernate.sessions.open.total", stats, Statistics::getSessionOpenCount)
                .register(registry);

        Gauge.builder("hibernate.query.execution.max.time.ms", stats,
                        s -> (double) s.getQueryExecutionMaxTime())
                .register(registry);
    }
}
