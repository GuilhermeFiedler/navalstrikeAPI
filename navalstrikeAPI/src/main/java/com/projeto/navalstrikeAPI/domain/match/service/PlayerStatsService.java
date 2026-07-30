package com.projeto.navalstrikeAPI.domain.match.service;

import com.projeto.navalstrikeAPI.domain.match.dto.PlayerStatsDTO;
import com.projeto.navalstrikeAPI.domain.match.repository.MatchRepository;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import com.projeto.navalstrikeAPI.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerStatsService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "playerStats", key = "#playerId")
    @Transactional(readOnly = true)
    public PlayerStatsDTO getPlayerStats(UUID playerId) {
        User player = userRepository.findById(playerId).orElseThrow();
        long totalVictories = matchRepository.countVictories(player);
        long totalDefeats = matchRepository.countDefeats(player);
        return new PlayerStatsDTO(totalVictories, totalDefeats);
    }
}
