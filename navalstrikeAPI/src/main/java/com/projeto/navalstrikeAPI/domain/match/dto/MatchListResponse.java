package com.projeto.navalstrikeAPI.domain.match.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatchListResponse(UUID id, String hostName, LocalDateTime createdAt) {}
