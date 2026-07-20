package com.projeto.navalstrikeAPI.domain.match.service;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.common.exception.MatchNotFoundException;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchListResponse;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchResponse;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.match.repository.MatchRepository;
import com.projeto.navalstrikeAPI.domain.skin.service.SkinService;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import com.projeto.navalstrikeAPI.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchQueryServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SkinService skinService;

    @InjectMocks
    private MatchQueryService matchQueryService;

    private User player1;
    private User player2;
    private Board board1;
    private Board board2;
    private Match match;

    @BeforeEach
    void setUp() {
        player1 = new User();
        player1.setId(UUID.randomUUID());
        player1.setName("João");
        player1.setEmail("joao@email.com");

        player2 = new User();
        player2.setId(UUID.randomUUID());
        player2.setName("Maria");
        player2.setEmail("maria@email.com");

        board1 = new Board();
        board1.setId(UUID.randomUUID());

        board2 = new Board();
        board2.setId(UUID.randomUUID());

        match = new Match();
        match.setId(UUID.randomUUID());
        match.setStatus(GameStatus.WAITING);
        match.setPlayer1(player1);
        match.setBoardPlayer1(board1);
        match.setCode("A3X9K2");
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("deve retornar partida quando encontrada")
        void shouldReturnMatchWhenFound() {
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            Match result = matchQueryService.findById(match.getId());

            assertThat(result).isEqualTo(match);
        }

        @Test
        @DisplayName("deve lançar exceção quando partida não encontrada")
        void shouldThrowWhenNotFound() {
            UUID fakeId = UUID.randomUUID();
            when(matchRepository.findById(fakeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> matchQueryService.findById(fakeId))
                    .isInstanceOf(MatchNotFoundException.class)
                    .hasMessage("Partida não encontrada");
        }
    }

    @Nested
    @DisplayName("getMatchView")
    class GetMatchViewTests {

        @Test
        @DisplayName("deve retornar visão do player1 corretamente")
        void shouldReturnPlayer1View() {
            match.setStatus(GameStatus.ON_GOING);
            match.setPlayer2(player2);
            match.setBoardPlayer2(board2);
            match.setCurrentTurn(player1);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            MatchResponse response = matchQueryService.getMatchView(match.getId(), player1.getId());

            assertThat(response.id()).isEqualTo(match.getId());
            assertThat(response.status()).isEqualTo(GameStatus.ON_GOING);
            assertThat(response.currentTurn()).isEqualTo(player1.getId());
            assertThat(response.myBoard()).isNotNull();
            assertThat(response.opponentBoard()).isNotNull();
        }

        @Test
        @DisplayName("deve retornar opponentBoard null quando player2 não entrou")
        void shouldReturnNullOpponentBoardWhenNoPlayer2() {
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            MatchResponse response = matchQueryService.getMatchView(match.getId(), player1.getId());

            assertThat(response.opponentBoard()).isNull();
        }

        @Test
        @DisplayName("deve lançar exceção quando jogador não pertence à partida")
        void shouldThrowWhenPlayerNotInMatch() {
            UUID strangerId = UUID.randomUUID();
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchQueryService.getMatchView(match.getId(), strangerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Jogador não pertence a esta partida");
        }
    }

    @Nested
    @DisplayName("listAvailableMatches")
    class ListAvailableMatchesTests {

        @Test
        @DisplayName("deve retornar lista de partidas em WAITING")
        void shouldReturnWaitingMatches() {
            when(matchRepository.findByStatus(GameStatus.WAITING)).thenReturn(List.of(match));

            List<MatchListResponse> result = matchQueryService.listAvailableMatches();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).hostId()).isEqualTo(player1.getId());
            assertThat(result.get(0).hostName()).isEqualTo("João");
            assertThat(result.get(0).code()).isEqualTo("A3X9K2");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há partidas")
        void shouldReturnEmptyList() {
            when(matchRepository.findByStatus(GameStatus.WAITING)).thenReturn(List.of());

            List<MatchListResponse> result = matchQueryService.listAvailableMatches();

            assertThat(result).isEmpty();
        }
    }
}
