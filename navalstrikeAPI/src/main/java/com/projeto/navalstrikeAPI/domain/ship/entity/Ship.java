package com.projeto.navalstrikeAPI.domain.ship.entity;

import com.projeto.navalstrikeAPI.common.enums.ShipType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Entity
@Table(name ="ships")
@Getter
@Setter
@NoArgsConstructor
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    ShipType shipType;

    boolean sunk;

}
