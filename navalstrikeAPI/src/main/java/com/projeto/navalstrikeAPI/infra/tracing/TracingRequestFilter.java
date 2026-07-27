package com.projeto.navalstrikeAPI.infra.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TracingRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TracingRequestFilter.class);

    private final Tracer tracer;

    public TracingRequestFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? uri + "?" + queryString : uri;

        Span currentSpan = tracer.currentSpan();
        String traceId = currentSpan != null ? currentSpan.context().traceId() : "no-trace";
        String spanId = currentSpan != null ? currentSpan.context().spanId() : "no-span";

        log.info("[ENTRADA] {} {} | traceId={} spanId={}", method, fullPath, traceId, spanId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            log.info("[SAÍDA] {} {} | status={} duração={}ms | traceId={} spanId={}",
                    method, fullPath, status, duration, traceId, spanId);

            if (duration > 1000) {
                log.warn("[LENTA] {} {} demorou {}ms | traceId={}", method, fullPath, duration, traceId);
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }
}
