package com.projeto.navalstrikeAPI.domain.match.service;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.common.enums.ShipType;
import com.projeto.navalstrikeAPI.common.exception.GameAlreadyStartedException;
import com.projeto.navalstrikeAPI.common.exception.MatchNotFoundException;
import com.projeto.navalstrikeAPI.common.exception.PlayerTurnException;
import com.projeto.navalstrikeAPI.common.exception.ShipPlacementException;
import com.projeto.navalstrikeAPI.domain.board.dto.AttackResult;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.board.service.BoardService;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackRequest;
import com.projeto.navalstrikeAPI.domain.match.dto.AttackResponse;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchListResponse;
import com.projeto.navalstrikeAPI.domain.match.dto.MatchResponse;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.match.repository.MatchRepository;
import com.projeto.navalstrikeAPI.domain.ship.dto.PlaceShipRequest;
import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import com.projeto.navalstrikeAPI.domain.user.repository.UserRepository;
import com.projeto.navalstrikeAPI.domain.skin.service.SkinService;
import com.projeto.navalstrikeAPI.infra.transaction.TransactionHelper;
import com.projeto.navalstrikeAPI.infra.websocket.MatchNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private BoardService boardService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchNotificationService notificationService;

    @Mock
    private TransactionHelper transactionHelper;

    @Mock
    private SkinService skinService;

    @InjectMocks
    private MatchService matchService;

    private User player1;
    private User player2;
    private Board board1;
    private Board board2;
    private Match match;

    @BeforeEach
    void setUp() {
        lenient().doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(transactionHelper).afterCommit(any(Runnable.class));

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

    private Set<Coordinate> generateCoords(int size, int startX, int y) {
        Set<Coordinate> coords = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            coords.add(new Coordinate(startX + i, y));
        }
        return coords;
    }

    @Nested
    @DisplayName("createMatch")
    class CreateMatchTests {

        @Test
        @DisplayName("deve criar partida com status WAITING e player1 definido")
        void shouldCreateMatchWithWaitingStatus() {
            when(boardService.createBoard()).thenReturn(board1);
            when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));
            when(matchRepository.save(any(Match.class))).thenAnswer(inv -> {
                Match m = inv.getArgument(0);
                m.setId(UUID.randomUUID());
                return m;
            });
            when(matchRepository.existsActiveByCode(anyString())).thenReturn(false);

            Match result = matchService.createMatch(player1.getId());

            assertThat(result.getStatus()).isEqualTo(GameStatus.WAITING);
            assertThat(result.getPlayer1()).isEqualTo(player1);
            assertThat(result.getBoardPlayer1()).isEqualTo(board1);
            assertThat(result.getCode()).isNotNull().hasSize(6);
            verify(matchRepository).save(any(Match.class));
        }
    }

    @Nested
    @DisplayName("joinMatch")
    class JoinMatchTests {

        @Test
        @DisplayName("deve permitir player2 entrar em partida WAITING")
        void shouldAllowPlayer2ToJoin() {
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
            when(userRepository.findById(player2.getId())).thenReturn(Optional.of(player2));
            when(boardService.createBoard()).thenReturn(board2);
            when(matchRepository.save(match)).thenReturn(match);

            Match result = matchService.joinMatch(match.getId(), player2.getId());

            assertThat(result.getStatus()).isEqualTo(GameStatus.PLACING);
            assertThat(result.getPlayer2()).isEqualTo(player2);
            assertThat(result.getBoardPlayer2()).isEqualTo(board2);
            verify(notificationService).notifyPlayerJoined(match.getId(), player2.getId(), "Maria");
        }

        @Test
        @DisplayName("deve lançar exceção quando partida não está em WAITING")
        void shouldThrowWhenMatchNotWaiting() {
            match.setStatus(GameStatus.ON_GOING);
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.joinMatch(match.getId(), player2.getId()))
                    .isInstanceOf(GameAlreadyStartedException.class)
                    .hasMessage("Partida já iniciada");
        }

        @Test
        @DisplayName("deve lançar exceção quando partida não encontrada")
        void shouldThrowWhenMatchNotFound() {
            UUID fakeId = UUID.randomUUID();
            when(matchRepository.findById(fakeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> matchService.joinMatch(fakeId, player2.getId()))
                    .isInstanceOf(MatchNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("joinMatchByCode")
    class JoinByCodeTests {

        @Test
        @DisplayName("deve entrar na partida usando código válido")
        void shouldJoinByValidCode() {
            when(matchRepository.findActiveByCode("A3X9K2")).thenReturn(Optional.of(match));
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
            when(userRepository.findById(player2.getId())).thenReturn(Optional.of(player2));
            when(boardService.createBoard()).thenReturn(board2);
            when(matchRepository.save(match)).thenReturn(match);

            Match result = matchService.joinMatchByCode("A3X9K2", player2.getId());

            assertThat(result.getPlayer2()).isEqualTo(player2);
            assertThat(result.getStatus()).isEqualTo(GameStatus.PLACING);
        }

        @Test
        @DisplayName("deve lançar exceção quando código não encontrado")
        void shouldThrowWhenCodeNotFound() {
            when(matchRepository.findActiveByCode("XXXXXX")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> matchService.joinMatchByCode("XXXXXX", player2.getId()))
                    .isInstanceOf(MatchNotFoundException.class)
                    .hasMessageContaining("Partida não encontrada com o código");
        }

        @Test
        @DisplayName("deve aceitar código em lowercase e converter para uppercase")
        void shouldAcceptLowercaseCode() {
            when(matchRepository.findActiveByCode("A3X9K2")).thenReturn(Optional.of(match));
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
            when(userRepository.findById(player2.getId())).thenReturn(Optional.of(player2));
            when(boardService.createBoard()).thenReturn(board2);
            when(matchRepository.save(match)).thenReturn(match);

            Match result = matchService.joinMatchByCode("a3x9k2", player2.getId());

            assertThat(result.getPlayer2()).isEqualTo(player2);
        }
    }

    @Nested
    @DisplayName("placeShip")
    class PlaceShipTests {

        @Test
        @DisplayName("deve posicionar navio para player1 em status WAITING")
        void shouldPlaceShipForPlayer1() {
            Set<Coordinate> coords = Set.of(new Coordinate(0, 0), new Coordinate(1, 0));
            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            matchService.placeShip(match.getId(), player1.getId(), request);

            verify(boardService).placeShip(board1, request);
        }

        @Test
        @DisplayName("deve posicionar navio para player2 em status PLACING")
        void shouldPlaceShipForPlayer2() {
            match.setStatus(GameStatus.PLACING);
            match.setPlayer2(player2);
            match.setBoardPlayer2(board2);

            Set<Coordinate> coords = Set.of(new Coordinate(0, 0), new Coordinate(1, 0));
            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            matchService.placeShip(match.getId(), player2.getId(), request);

            verify(boardService).placeShip(board2, request);
        }

        @Test
        @DisplayName("deve mudar status para ON_GOING quando ambos posicionaram 5 navios")
        void shouldStartGameWhenBothPlayersReady() {
            match.setStatus(GameStatus.PLACING);
            match.setPlayer2(player2);
            match.setBoardPlayer2(board2);


            for (int i = 0; i < 5; i++) {
                board1.getShips().add(new Ship(ShipType.values()[i],
                        generateCoords(ShipType.values()[i].getSize(), i * 2, 0)));
            }
            for (int i = 0; i < 5; i++) {
                board2.getShips().add(new Ship(ShipType.values()[i],
                        generateCoords(ShipType.values()[i].getSize(), i * 2, 5)));
            }

            Set<Coordinate> coords = Set.of(new Coordinate(0, 9), new Coordinate(1, 9));
            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
            when(matchRepository.save(match)).thenReturn(match);

            matchService.placeShip(match.getId(), player1.getId(), request);

            assertThat(match.getStatus()).isEqualTo(GameStatus.ON_GOING);
            assertThat(match.getCurrentTurn()).isEqualTo(player1);
            verify(notificationService).notifyGameStarted(match.getId());
        }

        @Test
        @DisplayName("deve lançar exceção quando status é ON_GOING")
        void shouldThrowWhenGameAlreadyOngoing() {
            match.setStatus(GameStatus.ON_GOING);
            Set<Coordinate> coords = Set.of(new Coordinate(0, 0), new Coordinate(1, 0));
            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.placeShip(match.getId(), player1.getId(), request))
                    .isInstanceOf(ShipPlacementException.class)
                    .hasMessage("Não é possível posicionar navios nesta fase");
        }

        @Test
        @DisplayName("deve lançar exceção quando jogador não pertence à partida")
        void shouldThrowWhenPlayerNotInMatch() {
            UUID strangerId = UUID.randomUUID();
            Set<Coordinate> coords = Set.of(new Coordinate(0, 0), new Coordinate(1, 0));
            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.placeShip(match.getId(), strangerId, request))
                    .isInstanceOf(ShipPlacementException.class)
                    .hasMessage("Jogador não pertence a esta partida");
        }
    }

    @Nested
    @DisplayName("attack")
    class AttackTests {

        @BeforeEach
        void setUpOngoingMatch() {
            match.setStatus(GameStatus.ON_GOING);
            match.setPlayer2(player2);
            match.setBoardPlayer2(board2);
            match.setCurrentTurn(player1);
        }

        @Test
        @DisplayName("deve retornar hit e manter turno do atacante")
        void shouldAttackAndHit() {
            AttackRequest request = new AttackRequest(3, 5);
            Coordinate coord = new Coordinate(3, 5);
            AttackResult attackResult = new AttackResult(true, false, null);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
            when(boardService.attack(board2, coord)).thenReturn(attackResult);
            when(boardService.allShipsDestroyed(board2)).thenReturn(false);
            when(matchRepository.save(match)).thenReturn(match);

            AttackResponse response = matchService.attack(match.getId(), request, player1.getId());

            assertThat(response.hit()).isTrue();
            assertThat(response.sunk()).isFalse();
            assertThat(response.gameOver()).isFalse();
            assertThat(match.getCurrentTurn()).isEqualTo(player1);
            verify(notificationService).notifyAttackResult(match.getId(), player1.getId(), 3, 5, true, false, null, false, null);
        }

        @Test
        @DisplayName("deve retornar miss e alternar turno")
        void shouldAttackAndMiss() {
            AttackRequest request = new AttackRequest(7, 8);
            Coordinate coord = new Coordinate(7, 8);
            AttackResult attackResult = new AttackResult(false, false, null);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
            when(boardService.attack(board2, coord)).thenReturn(attackResult);
            when(boardService.allShipsDestroyed(board2)).thenReturn(false);
            when(matchRepository.save(match)).thenReturn(match);

            AttackResponse response = matchService.attack(match.getId(), request, player1.getId());

            assertThat(response.hit()).isFalse();
            assertThat(match.getCurrentTurn()).isEqualTo(player2);
        }

        @Test
        @DisplayName("deve retornar gameOver quando todos os navios afundam")
        void shouldReturnGameOverWhenAllShipsDestroyed() {
            AttackRequest request = new AttackRequest(0, 0);
            Coordinate coord = new Coordinate(0, 0);
            AttackResult attackResult = new AttackResult(true, true, ShipType.DESTROYER);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
            when(boardService.attack(board2, coord)).thenReturn(attackResult);
            when(boardService.allShipsDestroyed(board2)).thenReturn(true);
            when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));
            when(matchRepository.save(match)).thenReturn(match);

            AttackResponse response = matchService.attack(match.getId(), request, player1.getId());

            assertThat(response.gameOver()).isTrue();
            assertThat(match.getStatus()).isEqualTo(GameStatus.FINISHED);
            verify(notificationService).notifyGameOver(match.getId(), player1.getId());
        }

        @Test
        @DisplayName("deve lançar exceção quando não é o turno do jogador")
        void shouldThrowWhenNotPlayerTurn() {
            AttackRequest request = new AttackRequest(3, 5);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.attack(match.getId(), request, player2.getId()))
                    .isInstanceOf(PlayerTurnException.class)
                    .hasMessage("Não é sua vez");
        }

        @Test
        @DisplayName("deve lançar exceção quando partida não está em andamento")
        void shouldThrowWhenMatchNotOngoing() {
            match.setStatus(GameStatus.FINISHED);
            AttackRequest request = new AttackRequest(3, 5);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.attack(match.getId(), request, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Partida não está em andamento");
        }

        @Test
        @DisplayName("player2 deve atacar o board do player1")
        void shouldPlayer2AttackPlayer1Board() {
            match.setCurrentTurn(player2);
            AttackRequest request = new AttackRequest(2, 3);
            Coordinate coord = new Coordinate(2, 3);
            AttackResult attackResult = new AttackResult(true, false, null);

            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
            when(boardService.attack(board1, coord)).thenReturn(attackResult);
            when(boardService.allShipsDestroyed(board1)).thenReturn(false);
            when(matchRepository.save(match)).thenReturn(match);

            AttackResponse response = matchService.attack(match.getId(), request, player2.getId());

            assertThat(response.hit()).isTrue();
            verify(boardService).attack(board1, coord);
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

            MatchResponse response = matchService.getMatchView(match.getId(), player1.getId());

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

            MatchResponse response = matchService.getMatchView(match.getId(), player1.getId());

            assertThat(response.opponentBoard()).isNull();
        }

        @Test
        @DisplayName("deve lançar exceção quando jogador não pertence à partida")
        void shouldThrowWhenPlayerNotInMatch() {
            UUID strangerId = UUID.randomUUID();
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.getMatchView(match.getId(), strangerId))
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

            List<MatchListResponse> result = matchService.listAvailableMatches();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).hostId()).isEqualTo(player1.getId());
            assertThat(result.get(0).hostName()).isEqualTo("João");
            assertThat(result.get(0).code()).isEqualTo("A3X9K2");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há partidas")
        void shouldReturnEmptyList() {
            when(matchRepository.findByStatus(GameStatus.WAITING)).thenReturn(List.of());

            List<MatchListResponse> result = matchService.listAvailableMatches();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("forfeit")
    class ForfeitTests {

        @BeforeEach
        void setUpOngoingMatch() {
            match.setStatus(GameStatus.ON_GOING);
            match.setPlayer2(player2);
            match.setBoardPlayer2(board2);
        }

        @Test
        @DisplayName("deve finalizar partida quando player1 desiste")
        void shouldForfeitAsPlayer1() {
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
            when(userRepository.findById(player2.getId())).thenReturn(Optional.of(player2));
            when(matchRepository.save(match)).thenReturn(match);

            matchService.forfeit(match.getId(), player1.getId());

            assertThat(match.getStatus()).isEqualTo(GameStatus.FINISHED);
            verify(notificationService).notifyForfeit(match.getId(), player1.getId(), player2.getId());
        }

        @Test
        @DisplayName("deve finalizar partida quando player2 desiste")
        void shouldForfeitAsPlayer2() {
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
            when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));
            when(matchRepository.save(match)).thenReturn(match);

            matchService.forfeit(match.getId(), player2.getId());

            assertThat(match.getStatus()).isEqualTo(GameStatus.FINISHED);
            verify(notificationService).notifyForfeit(match.getId(), player2.getId(), player1.getId());
        }

        @Test
        @DisplayName("deve lançar exceção quando partida já finalizada")
        void shouldThrowWhenMatchAlreadyFinished() {
            match.setStatus(GameStatus.FINISHED);
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.forfeit(match.getId(), player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Partida já finalizada");
        }

        @Test
        @DisplayName("deve lançar exceção quando jogador não pertence à partida")
        void shouldThrowWhenPlayerNotInMatch() {
            UUID strangerId = UUID.randomUUID();
            when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.forfeit(match.getId(), strangerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Jogador não pertence a esta partida");
        }
    }
}
