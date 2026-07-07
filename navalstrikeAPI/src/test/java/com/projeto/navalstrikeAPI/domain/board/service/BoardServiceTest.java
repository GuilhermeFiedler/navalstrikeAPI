package com.projeto.navalstrikeAPI.domain.board.service;

import com.projeto.navalstrikeAPI.common.enums.ShipType;
import com.projeto.navalstrikeAPI.common.exception.InvalidCoordinateException;
import com.projeto.navalstrikeAPI.common.exception.ShipOverlapException;
import com.projeto.navalstrikeAPI.common.exception.ShipPlacementException;
import com.projeto.navalstrikeAPI.domain.board.dto.AttackResult;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.board.repository.BoardRepository;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.ship.dto.PlaceShipRequest;
import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;
import com.projeto.navalstrikeAPI.domain.ship.service.ShipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ShipService shipService;

    @InjectMocks
    private BoardService boardService;

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
        board.setId(UUID.randomUUID());
    }

    @Nested
    @DisplayName("createBoard")
    class CreateBoardTests {

        @Test
        @DisplayName("deve criar um board vazio e salvar")
        void shouldCreateAndSaveBoard() {
            when(boardRepository.save(any(Board.class))).thenAnswer(inv -> {
                Board b = inv.getArgument(0);
                b.setId(UUID.randomUUID());
                return b;
            });

            Board result = boardService.createBoard();

            assertThat(result).isNotNull();
            assertThat(result.getShips()).isEmpty();
            assertThat(result.getMisses()).isEmpty();
            verify(boardRepository).save(any(Board.class));
        }
    }

    @Nested
    @DisplayName("placeShip")
    class PlaceShipTests {

        @Test
        @DisplayName("deve posicionar navio válido horizontalmente")
        void shouldPlaceShipHorizontally() {
            Set<Coordinate> coords = new LinkedHashSet<>();
            coords.add(new Coordinate(0, 0));
            coords.add(new Coordinate(1, 0));

            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);
            Ship ship = new Ship(ShipType.DESTROYER, coords);

            when(shipService.createShip(ShipType.DESTROYER, coords)).thenReturn(ship);
            when(shipService.canPlaceShip(board, ship)).thenReturn(true);
            when(boardRepository.save(board)).thenReturn(board);

            boardService.placeShip(board, request);

            assertThat(board.getShips()).hasSize(1);
            verify(boardRepository).save(board);
        }

        @Test
        @DisplayName("deve posicionar navio válido verticalmente")
        void shouldPlaceShipVertically() {
            Set<Coordinate> coords = new LinkedHashSet<>();
            coords.add(new Coordinate(0, 0));
            coords.add(new Coordinate(0, 1));

            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);
            Ship ship = new Ship(ShipType.DESTROYER, coords);

            when(shipService.createShip(ShipType.DESTROYER, coords)).thenReturn(ship);
            when(shipService.canPlaceShip(board, ship)).thenReturn(true);
            when(boardRepository.save(board)).thenReturn(board);

            boardService.placeShip(board, request);

            assertThat(board.getShips()).hasSize(1);
        }

        @Test
        @DisplayName("deve lançar exceção quando coordenada fora do tabuleiro")
        void shouldThrowWhenCoordinateOutOfBounds() {
            Set<Coordinate> coords = new LinkedHashSet<>();
            coords.add(new Coordinate(10, 0));
            coords.add(new Coordinate(11, 0));

            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);

            assertThatThrownBy(() -> boardService.placeShip(board, request))
                    .isInstanceOf(InvalidCoordinateException.class)
                    .hasMessage("Coordenada fora do tabuleiro");
        }

        @Test
        @DisplayName("deve lançar exceção quando coordenada negativa")
        void shouldThrowWhenCoordinateNegative() {
            Set<Coordinate> coords = new LinkedHashSet<>();
            coords.add(new Coordinate(-1, 0));
            coords.add(new Coordinate(0, 0));

            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);

            assertThatThrownBy(() -> boardService.placeShip(board, request))
                    .isInstanceOf(InvalidCoordinateException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando tipo de navio já existe no board")
        void shouldThrowWhenDuplicateShipType() {
            Set<Coordinate> existingCoords = Set.of(new Coordinate(0, 0), new Coordinate(1, 0));
            Ship existingShip = new Ship(ShipType.DESTROYER, existingCoords);
            board.getShips().add(existingShip);

            Set<Coordinate> newCoords = new LinkedHashSet<>();
            newCoords.add(new Coordinate(3, 0));
            newCoords.add(new Coordinate(4, 0));

            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, newCoords);

            assertThatThrownBy(() -> boardService.placeShip(board, request))
                    .isInstanceOf(ShipPlacementException.class)
                    .hasMessage("Já existe um navio deste tipo no tabuleiro");
        }

        @Test
        @DisplayName("deve lançar exceção quando coordenadas não são contíguas")
        void shouldThrowWhenCoordinatesNotContiguous() {
            Set<Coordinate> coords = new LinkedHashSet<>();
            coords.add(new Coordinate(0, 0));
            coords.add(new Coordinate(2, 0));

            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);

            assertThatThrownBy(() -> boardService.placeShip(board, request))
                    .isInstanceOf(ShipPlacementException.class)
                    .hasMessage("Coordenadas devem ser contíguas em linha reta");
        }

        @Test
        @DisplayName("deve lançar exceção quando coordenadas em diagonal")
        void shouldThrowWhenCoordinatesDiagonal() {
            Set<Coordinate> coords = new LinkedHashSet<>();
            coords.add(new Coordinate(0, 0));
            coords.add(new Coordinate(1, 1));

            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);

            assertThatThrownBy(() -> boardService.placeShip(board, request))
                    .isInstanceOf(ShipPlacementException.class)
                    .hasMessage("Coordenadas devem ser contíguas em linha reta");
        }

        @Test
        @DisplayName("deve lançar exceção quando quantidade de coordenadas não bate com tipo")
        void shouldThrowWhenWrongCoordinateCount() {
            Set<Coordinate> coords = new LinkedHashSet<>();
            coords.add(new Coordinate(0, 0));
            coords.add(new Coordinate(1, 0));
            coords.add(new Coordinate(2, 0));

            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);

            assertThatThrownBy(() -> boardService.placeShip(board, request))
                    .isInstanceOf(ShipPlacementException.class)
                    .hasMessageContaining("Quantidade de coordenadas incorreta");
        }

        @Test
        @DisplayName("deve lançar exceção quando navio sobrepõe outro")
        void shouldThrowWhenShipOverlaps() {
            Set<Coordinate> coords = new LinkedHashSet<>();
            coords.add(new Coordinate(0, 0));
            coords.add(new Coordinate(1, 0));

            PlaceShipRequest request = new PlaceShipRequest(ShipType.DESTROYER, coords);
            Ship ship = new Ship(ShipType.DESTROYER, coords);

            when(shipService.createShip(ShipType.DESTROYER, coords)).thenReturn(ship);
            when(shipService.canPlaceShip(board, ship)).thenReturn(false);

            assertThatThrownBy(() -> boardService.placeShip(board, request))
                    .isInstanceOf(ShipOverlapException.class)
                    .hasMessage("Navio sobrepõe outro");
        }
    }

    @Nested
    @DisplayName("attack")
    class AttackTests {

        @Test
        @DisplayName("deve retornar hit=true quando acerta navio")
        void shouldReturnHitWhenShipIsHit() {
            Set<Coordinate> coords = Set.of(new Coordinate(3, 5), new Coordinate(4, 5));
            Ship ship = new Ship(ShipType.DESTROYER, coords);
            board.getShips().add(ship);

            when(boardRepository.save(board)).thenReturn(board);

            AttackResult result = boardService.attack(board, new Coordinate(3, 5));

            assertThat(result.hit()).isTrue();
            assertThat(result.sunk()).isFalse();
        }

        @Test
        @DisplayName("deve retornar sunk=true quando afunda navio")
        void shouldReturnSunkWhenShipDestroyed() {
            Set<Coordinate> coords = Set.of(new Coordinate(3, 5), new Coordinate(4, 5));
            Ship ship = new Ship(ShipType.DESTROYER, coords);
            ship.hit(new Coordinate(3, 5));
            board.getShips().add(ship);

            when(boardRepository.save(board)).thenReturn(board);

            AttackResult result = boardService.attack(board, new Coordinate(4, 5));

            assertThat(result.hit()).isTrue();
            assertThat(result.sunk()).isTrue();
        }

        @Test
        @DisplayName("deve retornar miss quando erra")
        void shouldReturnMissWhenNoShipHit() {
            Set<Coordinate> coords = Set.of(new Coordinate(0, 0), new Coordinate(1, 0));
            Ship ship = new Ship(ShipType.DESTROYER, coords);
            board.getShips().add(ship);

            when(boardRepository.save(board)).thenReturn(board);

            AttackResult result = boardService.attack(board, new Coordinate(5, 5));

            assertThat(result.hit()).isFalse();
            assertThat(result.sunk()).isFalse();
            assertThat(board.getMisses()).contains(new Coordinate(5, 5));
        }

        @Test
        @DisplayName("deve lançar exceção quando coordenada já foi atacada (miss)")
        void shouldThrowWhenCoordinateAlreadyMissed() {
            board.getMisses().add(new Coordinate(5, 5));

            assertThatThrownBy(() -> boardService.attack(board, new Coordinate(5, 5)))
                    .isInstanceOf(InvalidCoordinateException.class)
                    .hasMessage("Coordenada já atacada");
        }

        @Test
        @DisplayName("deve lançar exceção quando coordenada já foi atacada (hit)")
        void shouldThrowWhenCoordinateAlreadyHit() {
            Set<Coordinate> coords = Set.of(new Coordinate(3, 5), new Coordinate(4, 5));
            Ship ship = new Ship(ShipType.DESTROYER, coords);
            ship.hit(new Coordinate(3, 5));
            board.getShips().add(ship);

            assertThatThrownBy(() -> boardService.attack(board, new Coordinate(3, 5)))
                    .isInstanceOf(InvalidCoordinateException.class)
                    .hasMessage("Coordenada já atacada");
        }

        @Test
        @DisplayName("deve lançar exceção quando coordenada fora do tabuleiro")
        void shouldThrowWhenAttackOutOfBounds() {
            assertThatThrownBy(() -> boardService.attack(board, new Coordinate(10, 5)))
                    .isInstanceOf(InvalidCoordinateException.class)
                    .hasMessage("Coordenada fora do tabuleiro");
        }
    }

    @Nested
    @DisplayName("allShipsDestroyed")
    class AllShipsDestroyedTests {

        @Test
        @DisplayName("deve retornar true quando todos os navios estão afundados")
        void shouldReturnTrueWhenAllSunk() {
            Set<Coordinate> coords = Set.of(new Coordinate(0, 0), new Coordinate(1, 0));
            Ship ship = new Ship(ShipType.DESTROYER, coords);
            ship.hit(new Coordinate(0, 0));
            ship.hit(new Coordinate(1, 0));
            board.getShips().add(ship);

            assertThat(boardService.allShipsDestroyed(board)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando há navios não afundados")
        void shouldReturnFalseWhenNotAllSunk() {
            Set<Coordinate> coords = Set.of(new Coordinate(0, 0), new Coordinate(1, 0));
            Ship ship = new Ship(ShipType.DESTROYER, coords);
            ship.hit(new Coordinate(0, 0));
            board.getShips().add(ship);

            assertThat(boardService.allShipsDestroyed(board)).isFalse();
        }

        @Test
        @DisplayName("deve retornar true quando board não tem navios")
        void shouldReturnTrueWhenNoShips() {
            assertThat(boardService.allShipsDestroyed(board)).isTrue();
        }
    }
}
