package com.projeto.navalstrikeAPI.domain.coordinate.board.entity;

import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name ="Board")
@Getter
@Setter
@NoArgsConstructor
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    List<Ship> ships;


}
