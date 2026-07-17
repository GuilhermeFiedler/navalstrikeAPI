package com.projeto.navalstrikeAPI.domain.ranking.repository;

import com.projeto.navalstrikeAPI.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RankingRepository extends JpaRepository<User, UUID> {

    String BASE_QUERY = """
            SELECT u.id, u.name,
                   COUNT(CASE WHEN m.winner_id = u.id THEN 1 END) AS victories,
                   COUNT(CASE WHEN m.winner_id != u.id THEN 1 END) AS defeats,
                   COUNT(*) AS total_matches
            FROM users u
            JOIN matches m ON (m.player_1 = u.id OR m.player_2 = u.id) AND m.status = 'FINISHED'
            GROUP BY u.id, u.name
            """;

    String COUNT_QUERY = """
            SELECT COUNT(*) FROM (
                SELECT u.id
                FROM users u
                JOIN matches m ON (m.player_1 = u.id OR m.player_2 = u.id) AND m.status = 'FINISHED'
                GROUP BY u.id
            ) ranked
            """;

    @Query(value = BASE_QUERY + "ORDER BY victories DESC, total_matches ASC",
            countQuery = COUNT_QUERY, nativeQuery = true)
    Page<Object[]> findRankingByVictoriesDesc(Pageable pageable);

    @Query(value = BASE_QUERY + "ORDER BY victories ASC, total_matches ASC",
            countQuery = COUNT_QUERY, nativeQuery = true)
    Page<Object[]> findRankingByVictoriesAsc(Pageable pageable);

    @Query(value = BASE_QUERY + "ORDER BY defeats DESC, total_matches ASC",
            countQuery = COUNT_QUERY, nativeQuery = true)
    Page<Object[]> findRankingByDefeatsDesc(Pageable pageable);

    @Query(value = BASE_QUERY + "ORDER BY defeats ASC, total_matches ASC",
            countQuery = COUNT_QUERY, nativeQuery = true)
    Page<Object[]> findRankingByDefeatsAsc(Pageable pageable);

    @Query(value = BASE_QUERY + "ORDER BY total_matches DESC, victories DESC",
            countQuery = COUNT_QUERY, nativeQuery = true)
    Page<Object[]> findRankingByTotalMatchesDesc(Pageable pageable);

    @Query(value = BASE_QUERY + "ORDER BY total_matches ASC, victories DESC",
            countQuery = COUNT_QUERY, nativeQuery = true)
    Page<Object[]> findRankingByTotalMatchesAsc(Pageable pageable);
}
