package com.projeto.navalstrikeAPI.domain.board.entity;

import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name ="board")
@Getter
@Setter
@NoArgsConstructor
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "board_id", nullable = false)
    private List<Ship> ships = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "board_misses", joinColumns = @JoinColumn(name = "board_id"))
    private Set<Coordinate> misses = new HashSet<>();

}
